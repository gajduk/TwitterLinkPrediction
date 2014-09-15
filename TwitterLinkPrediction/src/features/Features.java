package features;

import java.util.HashMap;
import java.util.List;

public class Features {
	
	//originally used to extract these features
	FeatureExtractors fe;
	//the features mapped to user ids
	HashMap<Integer,HashMap<Integer,Feature>> features_map;
	//all features as list
	List<Feature> features_list;
	
	public Features(FeatureExtractors fe,List<Feature> feature_list) {
		this.fe = fe;
		this.features_list = feature_list;
		this.features_map = new HashMap<Integer,HashMap<Integer,Feature>>();
		for ( Feature f : features_list ) {
			int idx1 = f.getIdx1();
			int idx2 = f.getIdx2();
			HashMap<Integer,Feature> m = features_map.get(idx1);
			if ( m == null ) m = new HashMap<Integer,Feature>();
			m.put(idx2, f);
			features_map.put(idx1,m);
		}
	}
	
	public List<Feature> getAllFeatures() {
		return features_list;
	}
	
	public Feature getFeature(int idx1 , int idx2 ) {
		return features_map.get(idx1).get(idx2);
	}

}
