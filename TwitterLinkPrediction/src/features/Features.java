package features;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import core.Edge;

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
	
	public DBObject getDBObject() {
		return new BasicDBObject("extractor",fe.name()).append("features",features_list.stream().map(f -> f.getDBObject()).collect(Collectors.toList()));
	}
	
	public static Features parseFromDBObject(DBObject dbo) {
		FeatureExtractors fe = FeatureExtractors.valueOf((String)dbo.get("extractor"));
		List<Feature> features_list = ((List<DBObject>)dbo.get("features")).stream().map(Feature::parseFromDBObject).collect(Collectors.toList());
		return new Features(fe,features_list);
	}

}
