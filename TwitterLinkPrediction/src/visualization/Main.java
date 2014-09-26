package visualization;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import mk.edu.manu.cs.algorithm.SSKCommunityAlgorithm;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSinkImages;
import org.graphstream.stream.file.FileSinkImages.LayoutPolicy;
import org.graphstream.stream.file.FileSinkImages.OutputPolicy;
import org.graphstream.stream.file.FileSinkImages.OutputType;
import org.graphstream.stream.file.FileSinkImages.Resolution;
import org.graphstream.stream.file.FileSinkImages.Resolutions;

import utils.DatabaseManager;
import core.TwitterMapSnapshot;



public class Main {
	
	public static void main(String[] args) throws IOException {
		TwitterMapSnapshot tms = TwitterMapSnapshot.parseFromDBObject(DatabaseManager.INSTANCE.db.getCollection("TwitterMaps").findOne());
		
		
		tms.exportToNetFile("twitter3.net");
		
		/*
		HashMap<Long,Integer> indegree = new HashMap<Long,Integer>();
		tms.getUsers().forEach(u -> u.getFollowers().forEach((uid) -> {
			indegree.put(uid,1+indegree.getOrDefault(uid, 0));
			indegree.put(u.getUser_id(),1+indegree.getOrDefault(u.getUser_id(), 0));
		}));
		Graph graph = new SingleGraph("Tutorial 1");
		tms.getUsers().forEach((user) -> {
			if (indegree.getOrDefault(user.getUser_id(), 0) > 0 ) 
				graph.addNode(""+user.getUser_id());
		});
		
		for ( UserSnapshot user : tms.getUsers() ) {
			for ( Long user2 : user.getFollowers() ) {
					if ( Math.random() < 10.0/Math.min(indegree.get(user.getUser_id()),indegree.get(user2)) )
						graph.addEdge(user.getUser_id()+","+user2,user.getUser_id()+"",user2+"",true);
				
			}
		}
		graph.addAttribute("ui.stylesheet", "node  { fill-color: red; } edge { arrow-shape: none; }");
		graph.display();
		*/
	}
	
	public static void angel() throws IOException {
		TwitterMapSnapshot tms = TwitterMapSnapshot.parseFromDBObject(DatabaseManager.INSTANCE.db.getCollection("TwitterMaps").findOne());
		
		mk.edu.manu.cs.algorithm.Graph g = tms.buildGraph();

		SSKCommunityAlgorithm alg = new SSKCommunityAlgorithm(g);
		alg.execute();
		System.out.println("avgClusteringCoeff: " + alg.localClustering);
		// alg.saveCommunityResultsToFile("python/drawing/" + filename +
		// ".draw");
		System.out.println("no. of clusters:" + alg.nclusters);

		int[] b = new int[alg.votes.length];
		int[] a = new int[alg.votes.length];

		for (int i = 0; i < alg.votes.length; i++) {
			b[i] = alg.votes[i].color;
			a[i] = Math.abs(alg.votes[i].color - 1);
		}
		System.out.println(Arrays.toString(a));
		
		HashMap<Integer,Color> colormap = new HashMap<>();
		Random random = new Random();
		
		for ( int i = 0 ; i < alg.nclusters ; ++i ) {
			final float hue = random.nextFloat();
			//Saturation between 0.1 and 0.3
			final float saturation = (random.nextInt(2000) + 1000) / 10000f;
			final float luminance = 0.9f;
			final Color color = Color.getHSBColor(hue, saturation, luminance);
			colormap.put(i, color);
		}
		
		Graph graph = new SingleGraph("Tutorial 1");
		for ( int i = 0 ; i < g.nnodes ; ++i ) {
			int degree = 0;
			for ( int k = 0 ; k < g.nnodes ; ++k ) {
				if ( g.hasEdge(i, k) || g.hasEdge(k,i)) 
					degree++;
			}
			if ( degree == 0 )
				continue;
			graph.addNode(i+"");
			Color c = colormap.get(b[i]);
			String rgb = c.getRed()+","+c.getGreen()+","+c.getBlue();
			graph.getNode(i+"").addAttribute("ui.style", "size: 10px; fill-color: rgb("+rgb+");");
		}
		for ( int i = 0 ; i < g.nnodes ; ++i ) {
			for ( int k = 0 ; k < g.nnodes ; ++k ) {
				if ( g.hasEdge(i,k) ) 
					graph.addEdge(i+","+k, i+"", k+"",true);
			}
		}
		
		graph.display();
		FileSinkImages pic = new FileSinkImages(OutputType.PNG, Resolutions.VGA);
		 
		pic.setLayoutPolicy(LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE);
		
		pic.writeAll(graph, "sample.png");
		
	}

}
