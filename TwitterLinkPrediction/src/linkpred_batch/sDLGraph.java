package linkpred_batch;

import java.util.ArrayList;
import java.util.HashMap;

import cern.colt.function.tdouble.DoubleDoubleFunction;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseCCDoubleMatrix2D;
import cern.jet.math.tdouble.DoubleFunctions;
import core.Edge;
import features.Features;
import features.TwitterFeatureGraph;

/**
 * 
 * Abstract class for graph implementation
 *
 */
public class sDLGraph {
	
	public TwitterFeatureGraph tfg;                             
	/**The starting node*/
	public int s;                                             
	/**The future links set*/
	public ArrayList<Integer> D;     
	/**The future no-link set*/
	public ArrayList<Integer> L;
	
	
	
	// useful
	
	/**the adjacency matrix*/
	public SparseCCDoubleMatrix2D A;                             
	
	/**Pagerank*/
	public DoubleMatrix1D p;                                     
	/**Pagerank gradient*/
	public DoubleMatrix1D [] dp;
	/**Sum of the each row of the adjacency matrix*/
	public double [] rowSums;                                      
	
	
	/**
	 * Constructor
	 * 
	 * @param tfg.getN(): number of nodes
	 * @param s: the starting node
	 * @param f: the number of features
	 * @param list: the graph as list of FeatureFields
	 * @param D: the future-links set
	 * @param L: the no-links set
	 */
	public sDLGraph(TwitterFeatureGraph tfg, int s, ArrayList<Integer> D, ArrayList<Integer> L) {
		this.tfg = tfg;
		this.s = s;
		this.D = D;
		this.L = L;
		this.p = new DenseDoubleMatrix1D(this.tfg.getN());
		this.dp = new DoubleMatrix1D [this.tfg.getF()];
		for (int i = 0; i < tfg.getF(); i++)
			dp[i] = new DenseDoubleMatrix1D(tfg.getN());
		this.rowSums = new double [this.tfg.getN()];       
	}
	
	
	/**
	 * Constructor
	 */
	public sDLGraph () {};


	public void buildAdjacencyMatrix (DoubleMatrix1D param) {
		int rowIndexes[] = new int[tfg.getEdges().size()];
		int columnIndexes[] = new int[tfg.getEdges().size()];
		double values[] = new double[tfg.getEdges().size()];
		int fi = 0;
		for ( Features f : tfg.getFeatures() ) {
			int i = 0;
			for ( Edge e : tfg.getEdges() ) {
				rowIndexes[i] = e.getA();
				columnIndexes[i] = e.getB();
				values[i++] = f.getFeature(e.getA(), e.getB()).getValue()*param.get(fi);
			}
			++fi;
		}	
		for ( int i = 0 ; i < values.length ; ++i )
			values[i] = weightingFunction(values[i]);
		A = new SparseCCDoubleMatrix2D(tfg.getN(),tfg.getN(),rowIndexes,columnIndexes,values, false,false,true); 
	}
	
	
	/**
	* Build the transition matrix for given adjacency matrix
	*
	* @param alpha: damping factor
	* @return SparseCCDoubleMatrix2D
	*/
	public SparseCCDoubleMatrix2D buildTransitionTranspose (double alpha) {
		int n = tfg.getEdges().size()+tfg.getN()-tfg.getG().get(s).size();
		
		int rowIndexes[] = new int[n];
		int columnIndexes[] = new int[n];
		double values[] = new double[n];
		
		
		// initialize row sums
		for (int i = 0; i < this.tfg.getN(); rowSums[i++] = 0);
		//caclulate them
		for ( Edge e : tfg.getEdges() ) {
			rowSums[e.getA()] += this.A.get(e.getA(), e.getB());
		}
		
		// (1-alpha) * A[i][j] / sumElements(A[i])) + 1(j == s) * alpha
		// build the transpose of Q 
		double value;
		int i = 0;
		HashMap<Integer,Edge> remaining_edges = new HashMap<Integer,Edge>();
		for ( Edge e : tfg.getEdges() ) {
			if ( e.getA() == s ) {
				remaining_edges.put(e.getB(),e);
				continue;
			}
			value = (1 - alpha)*this.A.get(e.getA(), e.getB());
			rowIndexes[i] = e.getA();
			columnIndexes[i] = e.getB();
			values[i++] = value/rowSums[e.getA()];
		}
		
		for (int k = 0; k < tfg.getN(); k++) {
			rowIndexes[i] = s;
			columnIndexes[i] = k; 
			if ( rowSums[k] == 0 )
				values[i++] = 1.0d;
			else {
				Edge e = remaining_edges.get(k);
				if ( e != null )
						values[i] = (1 - alpha)*this.A.get(e.getA(), e.getB());
				values[i++] += alpha;
			}
		}
		
		return new SparseCCDoubleMatrix2D(tfg.getN(),tfg.getN(),rowIndexes,columnIndexes,values, false,false,true);				
	}
	
	
	/**
	 * Returns matrix of partial derivatives of the transition matrix
	 *  with respect to the featureIndex-th parameter for the given graph 
     * 
	 * @param featureIndex: the index of the parameter with respect to which the derivative is being calculated 
	 * @param alpha: the damping factor
	 * @return SparseCCDoubleMatrix2D
	 */
	public SparseCCDoubleMatrix2D transitionDerivativeTranspose (int featureIndex, double alpha) {
		
		SparseCCDoubleMatrix2D dQt = new SparseCCDoubleMatrix2D(this.tfg.getN(), this.tfg.getN());
		
		// derivative row sums
		int r, c;
		double [] dRowSums = new double [this.tfg.getN()];
		for ( Edge e : tfg.getEdges() ) {
			dRowSums[e.getA()] += weightingFunctionDerivative(featureIndex,e.getA(), e.getB());	
		}
		
		double value;
		for ( Edge e : tfg.getEdges() ) {
			value = (weightingFunctionDerivative(featureIndex,e.getA(), e.getB()) * rowSums[e.getA()]) -
					(this.A.get(e.getA(), e.getB()) * dRowSums[e.getA()]);
			value *= (1 - alpha);
			value /= Math.pow(rowSums[e.getA()], 2);
			//dQ.set(r, c, value); 
			dQt.set(e.getA(), e.getB(), value);
		}
				
		return dQt;
	}
	
	/**
	 * Defines the edge-weighting function
	 * 
	 * @param x: weighting function argument
	 * @return double
	 */
	public double weightingFunction(double x) {
		return Math.exp(x);
	}
	
	
	/**
	 * Calculate partial derivative of the weight function (exponential funcion 
	 * considered) parameterized by w, with respect to the index-th parameter
	 * for the given graph
	 * 
	 * @param nodeIndex: the index of the node in the graph
	 * @param row: the row index of the adjacency matrix
	 * @param column: the column index of the adjacency matrix
	 * @param featureIndex: the index of the parameter with respect to which the derivative is being calculated 
	 * @return double
	 */
	public double weightingFunctionDerivative(int featureIndex, int row, int column) {
		return this.A.get(row, column) * tfg.getFeatureValue(featureIndex, row, column);
	}
	
	/**
	 * Returns true if a link from 'from' node to 'to' node in the graph,
	 * otherwise returns false
	 * 
	 * @param from: link start node
	 * @param to: link end node
	 * @return boolean
	 */
	public boolean hasLink(int from, int to) {
		return this.A.get(from, to) > 0 || this.A.get(from, to) < 0;
	}

	public boolean isColumnStochastic (SparseCCDoubleMatrix2D mat) {
		for (int i = 0; i < mat.columns(); i++) 
			if ( mat.viewColumn(i).zSum() < 0.999999999d || mat.viewColumn(i).zSum() > 1.00000000001d) return false;
		return true;
	}
	
	/**
	 * Calculates pagerank and it's gradient, for given graph index
	 *  
	 * @param param: the parameters for building the adjacency matrix
	 * @param alpha: the damping factor
	 */
	public void pageRankAndGradient (DoubleMatrix1D param, double alpha) {
		buildAdjacencyMatrix(param);
		SparseCCDoubleMatrix2D Qt = buildTransitionTranspose(alpha);
		
		double EPSILON = 1e-6;
		DoubleMatrix1D oldP = new DenseDoubleMatrix1D(tfg.getN());        // the value of p in the previous iteration
						
		DoubleMatrix1D oldDp = new DenseDoubleMatrix1D(tfg.getN());       // the value of dp in the previous iteration
		                                                           // ...starts with all entries 0 
		
		//************************ 1 ****************************************
		
		boolean [] dpConverged = new boolean [tfg.getF()];
		boolean allDpConverged = false;
		boolean pConverged = false;
		p.assign(1.0 / tfg.getN()); 
		for (int k = 0; k < tfg.getF(); k++)                             // for every parameter
			dp[k].assign(DoubleFunctions.constant(0));    
		DoubleMatrix1D tmp = new DenseDoubleMatrix1D(tfg.getN());
		
		SparseCCDoubleMatrix2D [] tdt = new SparseCCDoubleMatrix2D [tfg.getF()];
		for (int k = 0; k < tfg.getF(); k++)
			tdt[k] = transitionDerivativeTranspose(k, alpha);
		
		while (!allDpConverged) {
			allDpConverged = true;
						
			for (int k = 0; k < tfg.getF(); k++) {
				if (dpConverged[k]) continue;
				
				oldDp.assign(dp[k]);
				tdt[k].zMult(p, tmp);
				Qt.zMult(oldDp, dp[k]);
				dp[k].assign(tmp, DoubleFunctions.plus);
				
				oldDp.assign(dp[k], new DoubleDoubleFunction() {
					
					@Override
					public double apply(double arg0, double arg1) {
						return Math.abs(arg0-arg1);
					}
				});
				
				if (oldDp.zSum() < EPSILON)
					dpConverged[k] = true;
				else
					allDpConverged = false;
			}
			
			if (!pConverged) {
				oldP.assign(p);
				Qt.zMult(oldP, p);
									
				oldP.assign(p, new DoubleDoubleFunction() {
			
					@Override
					public double apply(double arg0, double arg1) {
						return Math.abs(arg0-arg1);
					}
				});
				
				if (oldP.zSum() < EPSILON)
					pConverged = true;
			}
		}
		
		
		//**************************** 2 ********************************
		/*
		// PAGERANK GRADIENT
		DoubleMatrix1D tmp = new DenseDoubleMatrix1D(tfg.getN());;
		for (int k = 0; k < f; k++) {                              // for every parameter
			oldDp.assign(DoubleFunctions.constant(0));
			p.assign(1.0 / tfg.getN()); 
			dp[k].assign(DoubleFunctions.constant(0));             
			do {
				oldDp.assign(dp[k]);
				
				//transitionDerivative(k, alpha).getTranspose().zMult(p, tmp); 
				transitionDerivativeTranspose(k, alpha).zMult(p, tmp);
				Qt.zMult(oldDp, dp[k]);
				dp[k].assign(tmp, DoubleFunctions.plus);
				
				oldDp.assign(dp[k], new DoubleDoubleFunction() {
					
					@Override
					public double apply(double arg0, double arg1) {
						return Math.abs(arg0-arg1);
					}
				});
				
				// calculate next iteration page rank
				Qt.zMult(p.copy(), p);
			} while (oldDp.zSum() > EPSILON);		
		}
		
		// PAGERANK
		do {
			
			oldP.assign(p);
			Qt.zMult(oldP, p);
								
			oldP.assign(p, new DoubleDoubleFunction() {
		
				@Override
				public double apply(double arg0, double arg1) {
					return Math.abs(arg0-arg1);
				}
			});
		
		} while (oldP.zSum() > EPSILON);                         // convergence check
		*/
	}		
}
