package features;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import core.Edge;
import linkpred_batch.sDLGraph;

public class TwitterFeatureGraph {
	
	int n;
	int f;
	HashMap<Integer,HashSet<Integer>> g;
	ArrayList<Edge> edges;
	ArrayList<Features> features;
	
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
			HashSet<Integer> h = g.get(e.getA());
			if ( h == null ) h = new HashSet<>();
			h.add(e.getB());
			g.put(e.getA(),h);
		});
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
