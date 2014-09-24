package features;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import core.Edge;
import core.TwitterMapSnapshot;
import core.UserSnapshot;
import linkpred_batch.sDLGraph;

public class TwitterFeatureGraph {
	
	int n;
	int f;
	ArrayList<Edge> edges;
	ArrayList<Features> features;
	
	//pre-compute
	HashMap<Integer,Integer> indegree;
	HashMap<Integer,HashSet<Integer>> g;
	
	public DBObject getDBObject() {
		return new BasicDBObject("n",n).append("f",f)
				.append("edges",edges.stream().map(e -> e.getDBObject()).collect(Collectors.toList()))
				.append("features",features.stream().map(f -> f.getDBObject()).collect(Collectors.toList()));
	}
	
	public static TwitterFeatureGraph parseFromDBObject(DBObject dbo) {
		int n = (int)dbo.get("n");
		int f = (int)dbo.get("f");
		List<Edge> edges = ((List<DBObject>) dbo.get("edges")).stream().map(Edge::parseFromDBObject).collect(Collectors.toList());
		List<Features> features = ((List<DBObject>) dbo.get("features")).stream().map(Features::parseFromDBObject).collect(Collectors.toList());
		return new TwitterFeatureGraph(n,f,new ArrayList<>(edges),new ArrayList<>(features));
	}
	
	public TwitterFeatureGraph(int n, int f,ArrayList<Edge> edges, ArrayList<Features> features) {
		super();
		this.n = n;
		this.f = f;
		this.features = features;
		this.edges = edges;
		populateG(edges);
	}
	
	private void populateG(ArrayList<Edge> edges2) {
		g = new HashMap<Integer,HashSet<Integer>>();
		edges.stream().forEach(e -> {
			HashSet<Integer> h = g.get(e.getR());
			if ( h == null ) h = new HashSet<>();
			h.add(e.getC());
			g.put(e.getR(),h);
		});
		indegree = new HashMap<>();
		edges.stream().forEach(e -> indegree.put(e.getC(), indegree.getOrDefault(e.getC(),1)+1));
	}
	
	public int getInDegreeForNode(int node) {
		return indegree.getOrDefault(node,0);
	}

	public ArrayList<Features> getFeatures() {
		return features;
	}

	public HashMap<Integer, HashSet<Integer>> getG() {
		return g;
	}

	public int getN() {
		return n;
	}
	public int getF() {
		return f;
	}

	public sDLGraph[] generateNetworks(int n) {
		sDLGraph res[] = new sDLGraph[n];
		Set<Integer> all = new HashSet<Integer>();
		List<Integer> ss = new ArrayList<>();
		for ( int s = 1 ; s < this.n ; ++s )
			ss.add(s);
		all.addAll(ss);
		Collections.shuffle(ss);
		int i = 0;
		for ( Integer s : ss ) {
			Set<Integer> D = g.get(s);
			if ( D == null || D.size() < 2 ) continue;
			Set<Integer> L = new HashSet<>(all);
			L.removeAll(D);
			res[i++] = new sDLGraph(this, s, new ArrayList<>(D), new ArrayList<>(L));
			if ( i >= res.length ) break;
		}
		return res;
	}
	
	public sDLGraph getsDLfors(int s,Set<Integer> all) {
		Set<Integer> D = g.get(s);
		if ( D == null || D.size() < 2 ) return null;
		Set<Integer> L = new HashSet<>(all);
		L.removeAll(D);
		return new sDLGraph(this, s, new ArrayList<>(D), new ArrayList<>(L));
	}

	public double getFeatureValue(int feature_idx,int idx1, int idx2) {
		return features.get(feature_idx).getFeature(idx1, idx2).getValue();
	}

	public ArrayList<Edge> getEdges() {
		return edges;
	}

	public TwitterFeatureGraph removeLinks(double p) {
		ArrayList<Edge> edges = new ArrayList<Edge>(this.edges.stream().filter(e -> Math.random() > p).collect(Collectors.toList()));
		return new TwitterFeatureGraph(n,f,edges,features);
	}

	
}
