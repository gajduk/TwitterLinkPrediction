import java.util.List;

import utils.DatabaseManager;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import core.TwitterMapSnapshot;
import features.Feature;
import features.FeatureExtractors;


public class TestFeatureExtractors {
	
	public static void main(String[] args) {
		testCSExtractors();
	}
	
	public static void testCSExtractors() {
		long t = 1394582708000L;
		DBObject dbo = DatabaseManager.INSTANCE.db.getCollection("TwitterMaps").findOne(new BasicDBObject("t",t));
		TwitterMapSnapshot tms = TwitterMapSnapshot.parseFromDBObject(dbo);
		List<Feature> features = FeatureExtractors.CSWords.extractFeatures(tms);
		System.out.println(features.size());
		double avg = features.stream().map(Feature::getValue).reduce((a,b) -> a+b).get()/features.size();
		System.out.println(avg);
		features.stream().filter(f -> f.getValue() > avg*10 ).forEach(f -> System.out.println(f.getValue()));
		features = FeatureExtractors.removeOutliers(features);
		System.out.println("filtered");
		features.stream().filter(f -> f.getValue() > avg*10 ).forEach(f -> System.out.println(f.getValue()));
	}

}
