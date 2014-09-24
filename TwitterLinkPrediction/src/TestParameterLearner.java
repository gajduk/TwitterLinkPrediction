import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import linkpred_batch.GradientDescent;
import linkpred_batch.LinkPredictionTrainer;
import linkpred_batch.Ranker;
import linkpred_batch.sDLGraph;

import org.apache.commons.math3.optim.PointValuePair;

import utils.DatabaseManager;

import com.mongodb.DBCursor;

import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseCCDoubleMatrix2D;
import features.TwitterFeatureGraph;


public class TestParameterLearner {

	public static void main(String[] args) throws ParseException, InterruptedException, FileNotFoundException, IOException, ClassNotFoundException {
		
		DBCursor cursor = DatabaseManager.INSTANCE.db.getCollection("TwitterGraphs").find();
		
		List<TwitterFeatureGraph> twgs = new ArrayList<>();
		
		while ( cursor.hasNext() ) {
			twgs.add(TwitterFeatureGraph.parseFromDBObject(cursor.next()));
		}
		
		System.out.println("Loaded");
		
		int f = 2;
		double alpha = 0.3;                                          // damping factor
		double b = 1e-6;                                             // WMW function parameter
		double lambda = 1;                                           // regularization parameter 
		
		sDLGraph[] test_sdls = generateNetworks(twgs,10);
		System.out.println("Generated");
		double true_params[] = { 1 , 1 };
		List<Integer> ss = new ArrayList<>();
		for ( int s = 1 ; s < twgs.get(0).getN() ; ++s )
			ss.add(s);
		for ( int i = 0 ; i < test_sdls.length ; ++i ) {
			sDLGraph rwg = test_sdls[i];
			rwg.buildAdjacencyMatrix(new DenseDoubleMatrix1D(true_params));
			SparseCCDoubleMatrix2D Q = rwg.buildTransitionTranspose(alpha);
			ArrayList<Integer> predicted_links = Ranker.predictLinks(
					rwg, new DenseDoubleMatrix1D(true_params), alpha, 20);
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
		
		LinkPredictionTrainer lp = new LinkPredictionTrainer(test_sdls, f, alpha, lambda, b, learningRate );
		double startw = -2;
		double endw = 2;
		double step = .5;
		System.out.printf("x = %.2f:%.2f:%.2f;\n",startw,step,endw);
		System.out.println("[X,Y] = meshgrid(x,x);");
		System.out.print("Z = [");
		for ( double w1 = startw ; w1 <= endw ; w1 += step ) {
			for ( double w2 = startw ; w2 <= endw ; w2 += step ) {
				double[] params = new double[]{w1,w2};
				lp.costFunctionAndGradient(new DenseDoubleMatrix1D(params));
//				System.out.println("Params:"+Arrays.toString(params));
				if ( w2 > startw ) System.out.print(",");
				System.out.print(lp.getCost(params));
//				System.out.println("Gradient:"+Arrays.toString(lp.getGradient(params)));
			}
			if ( w1 < endw ) System.out.print(";");
		}
		System.out.println("];");
		System.out.println("surf(X,Y,Z);");
		System.out.println("xlabel('w1');");
		System.out.println("ylabel('w2');");
		System.exit(0);
		
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

	private static sDLGraph[] generateNetworks(List<TwitterFeatureGraph> twg,
			int N) {
		sDLGraph[] res = new sDLGraph[N];
		int n = twg.get(0).getN();
		ArrayList<Integer> ss = new ArrayList<>();
		for ( int i = 0 ; i < n ; ++i )
			ss.add(i);
		Set<Integer> all = new HashSet<>(ss);
		Collections.shuffle(ss);
//		Integer[] t = {81,1687,653,1657,2045,2864,3218,3951,4001,4213,4816,5165,5187,5684,6214,7512,7612,7984,8000,8651,9091};
//		ss.addAll(Arrays.asList(t));
		Random rnd = new Random();
		for ( int i = 0 ; i < N ; ++ i)
			ss.add(rnd.nextInt(n*twg.size()));
		int k = 0;
		for ( int i = 0 ; k < N ; ++i ) {
			res[k] = twg.get(ss.get(i)/n).getsDLfors(ss.get(i)%n, all);
			if ( res[k] != null ) ++k;
		}
		return res;
	}
}
