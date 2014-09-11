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
		List<Feature> features = FeatureExtractors.CSTime.extractFeatures(tms);
		features.subList(0,10).stream().forEach(f -> System.out.println(f.getValue()));
	}

}
