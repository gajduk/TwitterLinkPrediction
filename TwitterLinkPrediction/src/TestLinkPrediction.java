import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import linkpred_batch.sDLGraph;
import linkpred_batch.Ranker;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import core.TwitterMapSnapshot;
import features.FeatureExtractors;
import features.TwitterFeatureGraph;


public class TestLinkPrediction {
	
	/**
	 * Main
	 *  
	 * @param args
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ParseException {
		List<FeatureExtractors> fes = new ArrayList<>();
		fes.add(FeatureExtractors.Random);
		fes.add(FeatureExtractors.Random);
		fes.add(FeatureExtractors.Random);
		TwitterMapSnapshot tms = TwitterMapSnapshot.readFromFolder("time__05_00_02__date_15_03_2014");
//		TwitterFeatureGraph twg = tms.buildTwitterFeatureGraph(fes);
//		sDLGraph[] train_rwgs = twg.generateNetworks(10);
		
		
		int f = fes.size();
		double alpha = 0.3;                                          // damping factor
		double b = 1e-6;                                             // WMW function parameter
		double lambda = 1;                                           // regularization parameter 
		
		long start = System.nanoTime();
		int maxIterations = 10;                                     // Maximum number of iterations  
		int restarts = 3;
		double gradientTreshold = 1e-3;                              // Gradient convergence threshold  
		double costThreshold = 15;                                   // Minimal cost
		double [] initialParameters = new double [f];
		for (int i = 0; i < f; i++)
			initialParameters[i] = Math.random();
		
		/*
		double learningRate = 0.0001;
		GradientDescent gd = new GradientDescent(
				new LinkPredictionTrainer(train_rwgs, f, alpha, lambda, b, learningRate ), 
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
		
		
		double [] trueParameters = optimum.getFirst();
		
		*/
		for ( double p = .0d ; p < .401d ; p += 0.0899d ) {
			TwitterFeatureGraph twg = tms.buildTwitterFeatureGraph(fes).removeLinks(p);
			sDLGraph[] test_rwgs = twg.generateNetworks(10);
			double []trueParameters = new double[f];
			for ( int i = 0 ; i < f ; ++i )
				trueParameters[i] = 0.000001d;
			double prec = 0.0;
			for ( int i = 0 ; i < test_rwgs.length ; ++i ) {
				sDLGraph rwg = test_rwgs[i];
				ArrayList<Integer> predicted_links = Ranker.predictLinks(
						rwg, new DenseDoubleMatrix1D(trueParameters), alpha, rwg.D.size()+1);
				ArrayList<Integer> true_links = rwg.D;
				HashSet<Integer> correct = new HashSet<Integer>(true_links);
				correct.retainAll(predicted_links);
				prec += (correct.size()*1.0d)/(rwg.D.size());
//				System.out.println("Predicted:"+predicted_links.stream().sorted().collect(Collectors.toList()));
//				System.out.println("True:"+true_links.stream().sorted().collect(Collectors.toList()));
//				System.out.println("Current D:"+rwg.tfg.getG().getOrDefault(rwg.s, new HashSet<>()).stream().sorted().collect(Collectors.toList()));
//				System.out.println("Correct:"+correct.stream().sorted().collect(Collectors.toList()));
//				System.out.println();
			}
			System.out.printf("Links removed: %.2f, Precision: %.3f \n",p,prec/test_rwgs.length);
			
		}
	
		// PREDICTIONS
		/*
		ArrayList<Integer> trueLinks = new ArrayList<Integer>();
		ArrayList<Integer> predictedLinks = new ArrayList<Integer>();
		RandomWalkGraph testGraph = ArtificialGraphGenerator.generate(n, f, s);
		trueLinks = Ranker.predictLinks(
					testGraph, new DenseDoubleMatrix1D(trueParameters), alpha, topN);
		predictedLinks = Ranker.predictLinks(testGraph, new DenseDoubleMatrix1D(optimum.getFirst()), alpha, topN);
				
		System.out.println("\nTrue links:");
		for (int i = 0; i < trueLinks.size(); i++)
		System.out.print(trueLinks.get(i) + " ");
		System.out.println();
		
		System.out.println("\nPredicted links:");
		for (int i = 0; i < predictedLinks.size(); i++)
			System.out.print(predictedLinks.get(i) + "(" + predictedLinks.get(i) % n + ") ");
		System.out.println();
		
		/*
		System.out.println("Graph generation start");           
		
		int g = 50;                                                  // number of graphs   
		int n = 1000;                                                // number of nodes per graph
		int f = 2;                                                   // number of features per node
		
		int s = 0;                                                   // the starting node
		double [] param = {1, -1};                                   // parameters vector
		DoubleMatrix1D parameters = new DenseDoubleMatrix1D(param);	
		int topN = 10;
		
		ArtificialGraphGenerator.initialize(f);                       // build the artificial graph
		RandomWalkGraph [] graphs = new Network [g];
		for (int i = 0; i < g; i++) {
			graphs[i] = ArtificialGraphGenerator.generate(n, f, s);
			ArtificialGraphGenerator.buildDandL(graphs[i], topN, parameters, alpha);  
		}
		
		System.out.println("Graph generation end");				 
		
		/*
		LinkpredProblem problem = new LinkpredProblem(graphs, f, alpha, lambda, b);
		problem.optimize();
		PointValuePair optimum = problem.getOptimum();
		//
				
		// GRADIENT DESCENT OPTIMIZATION START
		*/
	}
}
	
	
