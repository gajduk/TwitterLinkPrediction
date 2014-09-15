package features;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import linkpred_batch.FeatureField;
import linkpred_batch.sDLGraph;

public class TwitterFeatureGraph {
	
	int n;
	int f;
	ArrayList<FeatureField> list;
	//sparse matrix with FeatureFields map to rows and columns for quick access
	HashMap<Integer,HashMap<Integer,FeatureField>> ffs;
	
	public TwitterFeatureGraph(int n, int f, ArrayList<FeatureField> list) {
		super();
		this.n = n;
		this.f = f;
		this.list = list;
		this.ffs = new HashMap<Integer,HashMap<Integer,FeatureField>>();
		populateffs(this.list);
	}
	
	private void populateffs(ArrayList<FeatureField> list2) {
		for ( FeatureField ff : list ) {
			HashMap<Integer,FeatureField> map = ffs.get(ff.getRow());
			if ( map == null ) map = new HashMap<>();
			map.put(ff.getColumn(), ff);
			ffs.put(ff.getRow(), map);
		}
	}
	
	public int getN() {
		return n;
	}
	public int getF() {
		return f;
	}
	public ArrayList<FeatureField> getList() {
		return list;
	}
	
	public void removeLinks(double p) {
		System.out.println(list.size());
		System.out.println(p);
		list = new ArrayList<>(list.stream().filter(ff -> Math.random() > p).collect(Collectors.toList()));
		System.out.println(list.size());
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
			Set<Integer> D = new HashSet<Integer>(ffs.getOrDefault(s,new HashMap<>()).keySet());
			if ( D.size() < 2 ) continue;
			Set<Integer> L = new HashSet<>(all);
			L.removeAll(D);
			res[i++] = new sDLGraph(this, s, new ArrayList<>(D), new ArrayList<>(L));
			if ( i >= res.length ) break;
		}
		return res;
	}

}
