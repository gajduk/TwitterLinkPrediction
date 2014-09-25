import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import linkpred_batch.LinkPredictionTrainer;
import linkpred_batch.Ranker;
import linkpred_batch.sDLGraph;
import utils.DatabaseManager;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;

import com.mongodb.DBCursor;

import features.TwitterFeatureGraph;


public class TestParameterLearner {

	public static void main(String[] args) throws ParseException, InterruptedException, FileNotFoundException, IOException, ClassNotFoundException {
		System.setOut(new PrintStream(new FileOutputStream(new File("out.txt"))));
		DBCursor cursor = DatabaseManager.INSTANCE.db.getCollection("TwitterGraphs").find();
		
		List<TwitterFeatureGraph> twgs = new ArrayList<>();
		
		int g = 0;
		while ( cursor.hasNext() ) {
			g++;
			if ( g > 50 ) break;
			twgs.add(TwitterFeatureGraph.parseFromDBObject(cursor.next()));
		}
		
//		System.out.println("Loaded");
		
		int f = 2;
		double alpha = 0.3;                                          // damping factor
		double b = 1e-6;                                             // WMW function parameter
		double lambda = 1;                                           // regularization parameter 
		double true_params[] = { .7 , .7 };
		
		sDLGraph[] test_sdls = generateNetworksWithPredictedsDL(twgs,50,true_params,alpha,10);
//		System.out.println("Generated");
		
		LinkPredictionTrainer lp = new LinkPredictionTrainer(test_sdls, f, alpha, lambda, b, 0.001d );
		double startw = -2;
		double endw = 2;
		double step = .2499999999999;
		System.out.printf("x = %.2f:%.2f:%.2f;\n",startw,step,endw);
		System.out.println("[X,Y] = meshgrid(x,x);");
		StringBuilder sbc = new StringBuilder("C = [");
		StringBuilder sb1 = new StringBuilder("G1 = [");
		StringBuilder sb2 = new StringBuilder("G2 = [");
		for ( double w1 = startw ; w1 <= endw ; w1 += step ) {
			for ( double w2 = startw ; w2 <= endw ; w2 += step ) {
				double[] params = new double[]{w1,w2};
				lp.costFunctionAndGradient(new DenseDoubleMatrix1D(params));
//				System.out.println("Params:"+Arrays.toString(params));
				if ( w2 > startw ) {
					sbc.append(",");
					sb1.append(",");
					sb2.append(",");
				}
				sbc.append(lp.getCost(params));
				sb1.append(lp.getGradient(params)[0]);
				sb2.append(lp.getGradient(params)[1]);
//				System.out.println("Gradient:"+Arrays.toString(lp.getGradient(params)));
			}
			if ( w1 < endw ) {
				sbc.append(";");
				sb1.append(";");
				sb2.append(";");
			}
		}
		System.out.println(sbc.append("];").toString());
		System.out.println(sb1.append("];").toString());
		System.out.println(sb2.append("];").toString());
		System.out.close();
		
	}

	public static sDLGraph[] generateNetworksWithRealLinks(List<TwitterFeatureGraph> twg,
			int N) {
		sDLGraph[] res = new sDLGraph[N];
		int n = twg.get(0).getN();
		ArrayList<Integer> ss = new ArrayList<>();
		for ( int i = 0 ; i < n ; ++i )
			ss.add(i);
		Set<Integer> all = new HashSet<>(ss);
//		Integer[] t = {81,1687,653,1657,2045,2864,3218,3951,4001,4213,4816,5165,5187,5684,6214,7512,7612,7984,8000,8651,9091};
//		ss.addAll(Arrays.asList(t));
		Random rnd = new Random();
		int k = 0;
		while( k < N ) {
			int s = rnd.nextInt(n*twg.size());
			res[k] = twg.get(s/n).getsDLfors(s%n, all);
			if ( res[k] != null ) ++k;
		}
		return res;
	}
	
	//for testing only
	public static sDLGraph[] generateNetworksWithPredictedsDL(List<TwitterFeatureGraph> twgs,
			int nsDLs,double true_params[],double alpha,int topN) {
		sDLGraph[] res = new sDLGraph[nsDLs];
		int n = twgs.get(0).getN();
		ArrayList<Integer> ss = new ArrayList<>();
		for ( int i = 0 ; i < n ; ++i )
			ss.add(i);
		Random rnd = new Random();
		int k = 0;
		while( k < nsDLs ) {
			int temp = rnd.nextInt(n*twgs.size());
			int s = temp%n;
			int gi = s/n;
			TwitterFeatureGraph twg = twgs.get(gi);
			sDLGraph sdl = new sDLGraph(twg, s, null, null);
			ArrayList<Integer> predicted_links = Ranker.predictLinks(
					sdl, new DenseDoubleMatrix1D(true_params), alpha, topN);
			sdl.D = predicted_links;
			sdl.L = new ArrayList<>(ss);
			sdl.L.removeAll(sdl.D);
			res[k++] = sdl;
		}
		return res;
	}
	
}
