import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import linkpred_batch.GradientDescent;
import linkpred_batch.LinkPredictionTrainer;
import linkpred_batch.Ranker;
import linkpred_batch.sDLGraph;

import org.apache.commons.math3.optim.PointValuePair;

import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseCCDoubleMatrix2D;
import core.TwitterMapSnapshot;
import features.FeatureExtractors;
import features.TwitterFeatureGraph;


public class TestParameterLearner {

	public static void main(String[] args) throws ParseException {
		//list of features
		List<FeatureExtractors> fes = new ArrayList<>();
		fes.add(FeatureExtractors.Random);
		fes.add(FeatureExtractors.Random);
		
		int f = fes.size();
		double alpha = 0.3;                                          // damping factor
		double b = 1e-6;                                             // WMW function parameter
		double lambda = 1;                                           // regularization parameter 
		
		TwitterMapSnapshot tms = TwitterMapSnapshot.readFromFolder("time__05_00_02__date_15_03_2014");
		TwitterFeatureGraph twg = tms.buildTwitterFeatureGraph(fes);
		sDLGraph[] test_sdls = twg.generateNetworks(50);
		System.out.println("Generated");
		double true_params[] = { -1 , 1 };
		List<Integer> ss = new ArrayList<>();
		for ( int s = 1 ; s < twg.getN() ; ++s )
			ss.add(s);
		for ( int i = 0 ; i < test_sdls.length ; ++i ) {
			System.out.println(i);
			sDLGraph rwg = test_sdls[i];
			rwg.buildAdjacencyMatrix(new DenseDoubleMatrix1D(true_params));
			SparseCCDoubleMatrix2D Q = rwg.buildTransitionTranspose(alpha);
			System.out.println(sDLGraph.isColumnStochastic(Q));
			ArrayList<Integer> predicted_links = Ranker.predictLinks(
					rwg, new DenseDoubleMatrix1D(true_params), alpha, rwg.D.size()+1);
			rwg.D = predicted_links;
			rwg.L = new ArrayList<>(ss);
			rwg.L.removeAll(rwg.D);
		}
		
		
		long start = System.nanoTime();
		int maxIterations = 100;                                     // Maximum number of iterations  
		int restarts = 20;
		double gradientTreshold = 1e-3;                              // Gradient convergence threshold  
		double costThreshold = 15;                                   // Minimal cost
		double [] initialParameters = new double [f];
		for (int i = 0; i < f; i++)
			initialParameters[i] = Math.random();
		double learningRate = 0.0001;
		GradientDescent gd = new GradientDescent(
				new LinkPredictionTrainer(test_sdls, f, alpha, lambda, b, learningRate ), 
				maxIterations, 
				gradientTreshold, 
				costThreshold);
		PointValuePair optimum = null;
		try {
			optimum = gd.multiStartOptimize(restarts);
			//optimum = gd.optimize(initialParameters);                // do the optimization
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// GRADIENT DESCENT OPTIMIZATION END
		
		long end = System.nanoTime();
		//System.out.println(gd.getStopReason());
		System.out.println("Function minimum: " + optimum.getValue() + "\nParameters: " + 
		        optimum.getPoint()[0] + " " + optimum.getPoint()[1]);
		
						
		
		System.out.println("Results in in " + (end-start)/60E9 + " minutes.");
		
		
		double [] learnedParameters = optimum.getFirst();
	}
}
