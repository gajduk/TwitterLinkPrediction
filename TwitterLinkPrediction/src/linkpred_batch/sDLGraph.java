package linkpred_batch;

import java.util.ArrayList;
import java.util.HashMap;

import cern.colt.function.tdouble.DoubleDoubleFunction;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseCCDoubleMatrix2D;
import cern.jet.math.tdouble.DoubleFunctions;
import core.Edge;
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
	
	public void buildAdjacencyMatrix (DoubleMatrix1D param) {
		
		int rowIndexes[] = new int[tfg.getEdges().size()];
		int columnIndexes[] = new int[tfg.getEdges().size()];
		double values[] = new double[tfg.getEdges().size()];
		int vi = 0;
		for ( Edge e : tfg.getEdges() ) {
			int r = e.getR();
			int c = e.getC();
			double temp = 0.0d;
			for ( int i = 0 ; i < tfg.getF() ; ++i ) {
				temp += param.get(i)*tfg.getFeatureValue(i, r, c);
			}	
			rowIndexes[vi] = r;
			columnIndexes[vi] = c;
			values[vi++] = weightingFunction(temp);
		}
		A = new SparseCCDoubleMatrix2D(tfg.getN(),tfg.getN(),rowIndexes,columnIndexes,values, false, false, true);
		
		/*
		this.A = new SparseCCDoubleMatrix2D(tfg.getN(), tfg.getN());
		double temp;
		int r, c;
		for ( Edge e : tfg.getEdges() ) {
			r = e.getR();
			c = e.getC();
			temp = 0.0d;
			for ( int i = 0 ; i < tfg.getF() ; ++i ) {
				temp += param.get(i)*tfg.getFeatureValue(i, r, c);
			}	
			temp = weightingFunction(temp);
			A.set(r, c, temp);
		}
		*/
		
	}
	
	
	/**
	* Build the transition matrix for given adjacency matrix
	*
	* @param alpha: damping factor
	* @return SparseCCDoubleMatrix2D
	*/
	public SparseCCDoubleMatrix2D buildTransitionTranspose (double alpha) {
		
		int n = tfg.getEdges().size()+tfg.getN()-tfg.getInDegreeForNode(s)+1;
		
		int rowIndexes[] = new int[n];
		int columnIndexes[] = new int[n];
		double values[] = new double[n];
		
		int r, c;
		for (int i = 0; i < this.tfg.getN(); rowSums[i++] = 0);
		for ( Edge e : tfg.getEdges() ) {
			r = e.getR();
			c = e.getC();
			rowSums[r] += this.A.get(r, c);
		}
		
		double value;
		int vi = 0;
		HashMap<Integer,Edge> remaining_edges = new HashMap<Integer,Edge>();
		for ( Edge e : tfg.getEdges() ) {
			if ( e.getC() == s ) {
				remaining_edges.put(e.getR(),e);
				continue;
			}
			value = (1 - alpha)*this.A.get(e.getR(), e.getC());
			rowIndexes[vi] = e.getC();
			columnIndexes[vi] = e.getR();
			values[vi++] = value/rowSums[e.getR()];
		}
		
			//k is row index, s is column index
		for (int k = 0; k < tfg.getN(); k++) {
			rowIndexes[vi] = s;
			columnIndexes[vi] = k; 
			if ( rowSums[k] == 0 )
				values[vi++] = 1.0d;
			else {
				Edge e = remaining_edges.get(k);
				if ( e != null )
						values[vi] = (1 - alpha)*this.A.get(k, s)/rowSums[k];
				values[vi++] += alpha;
			}
		}
		SparseCCDoubleMatrix2D Q = new SparseCCDoubleMatrix2D(tfg.getN(),tfg.getN(),rowIndexes,columnIndexes,values, false,false,true);
		
		/*
		SparseCCDoubleMatrix2D Q = new SparseCCDoubleMatrix2D(
				this.A.rows(), this.A.columns());
		
		// row sums
		int r, c;
		for (int i = 0; i < this.tfg.getN(); rowSums[i++] = 0);
		for ( Edge e : tfg.getEdges() ) {
			r = e.getR();
			c = e.getC();
			rowSums[r] += this.A.get(r, c);
		}
		
		// (1-alpha) * A[i][j] / sumElements(A[i])) + 1(j == s) * alpha
		// build the transpose of Q 
		double value;
		for ( Edge e : tfg.getEdges() ) {
			r = e.getR();
			c = e.getC();
			value = this.A.get(r, c);
			value *= (1 - alpha);
			Q.set(c, r, value/rowSums[r]);
		}
		
		for (int i = 0; i < Q.rows(); i++) {
			value = Q.get(this.s, i);
			value += alpha;
			if ( rowSums[i] == 0 )
				value = 1.0d;
			Q.set(this.s, i, value);
		}
		
		/*
		for ( r = 0 ; r < tfg.getN() ; ++r ) {
			for ( c = 0 ; c < tfg.getN() ; ++c ) {
				if ( Math.pow(tempQ.getQuick(r, c)-Q.getQuick(r, c),2) > 1e-6 )
					System.out.println(r+","+c);
			}	
		}
		*/
		return Q;	
			
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
		/*
		int n = tfg.getEdges().size();
		
		int rowIndexes[] = new int[n];
		int columnIndexes[] = new int[n];
		double values[] = new double[n];
		
		
		// derivative row sums
		double [] dRowSums = new double [this.tfg.getN()];
		for ( Edge e : tfg.getEdges() ) {
			dRowSums[e.getR()] += weightingFunctionDerivative(featureIndex,e.getR(), e.getC());	
		}
		
		int r, c;
		
		double value;
		int i = 0;
		for ( Edge e : tfg.getEdges() ) {
			r = e.getR();
			c = e.getC();
			
			value = (weightingFunctionDerivative(featureIndex, r, c) * rowSums[r]) -
					(this.A.get(r, c) * dRowSums[r]);
			value *= (1 - alpha);
			value /= Math.pow(rowSums[r], 2);
			//dQ.set(r, c, value); 
			rowIndexes[i] = c;
			columnIndexes[i] = r;
			values[i++] = value;
		}
				
		SparseCCDoubleMatrix2D tempdQt = new SparseCCDoubleMatrix2D(this.tfg.getN(), this.tfg.getN(),rowIndexes,columnIndexes,values,false,false,true);
		
*/
		SparseCCDoubleMatrix2D dQt = new SparseCCDoubleMatrix2D(this.tfg.getN(), this.tfg.getN());
		
		// derivative row sums
		double [] dRowSums = new double [this.tfg.getN()];
		for ( Edge e : tfg.getEdges() ) 
			dRowSums[e.getR()] += weightingFunctionDerivative(featureIndex,e.getR(), e.getC());	
		
		

		int r, c;
		double value;
		for ( Edge e : tfg.getEdges() ) {
			r = e.getR();
			c = e.getC();
			value = (weightingFunctionDerivative(featureIndex, r, c) * rowSums[r]) -
					(this.A.get(r, c) * dRowSums[r]);
			value *= (1 - alpha);
			value /= Math.pow(rowSums[r], 2);
			//dQ.set(r, c, value); 
			dQt.set(c, r, value);
		}
		
		/*
		for ( r = 0 ; r < tfg.getN() ; ++r ) {
			for ( c = 0 ; c < tfg.getN() ; ++c ) {
				if ( Math.pow(tempdQt.getQuick(r, c)-dQt.getQuick(r, c),2) > 1e-6 )
					System.out.println(r+","+c);
			}	
		}
		*/
		
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

	public static boolean isColumnStochastic (SparseCCDoubleMatrix2D mat) {
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
	}		
}
