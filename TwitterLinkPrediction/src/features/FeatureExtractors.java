package features;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import utils.DatabaseManager;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

import core.TwitterUserForMap;
import core.TwitterMapSnapshot;
import core.UserSnapshot;

public enum FeatureExtractors implements FeatureExtractor {
	
	
	Retwits {
		public List<Feature> extractFeatures(TwitterMapSnapshot twitter_map) {
			return getLocalFeaturesForMap(twitter_map,"Retwits","uid","Oruid");
			
		}
	}, 
	Mentions{
		public List<Feature> extractFeatures(TwitterMapSnapshot twitter_map) {
			return getLocalFeaturesForMap(twitter_map,"Mentions","u1id","u2id");
		}
	},
	Replies{ 
		public List<Feature> extractFeatures(TwitterMapSnapshot twitter_map) {
			return getLocalFeaturesForMap(twitter_map,"Twits","ruid","uid");
		}
	},
	CSHashtags {
		public List<Feature> extractFeatures(TwitterMapSnapshot twitter_map) {
			return getGlobalFeaturesForMap(twitter_map,"CSHashtags");
		}
	},
	CSWords{
		public List<Feature> extractFeatures(TwitterMapSnapshot twitter_map) {
			return getGlobalFeaturesForMap(twitter_map,"CSWords");
		}
	},
	CSTime{
		public List<Feature> extractFeatures(TwitterMapSnapshot twitter_map) {
			return getGlobalFeaturesForMap(twitter_map,"CSTime");
		}
	},
	
	Random {

		@Override
		public List<Feature> extractFeatures(TwitterMapSnapshot tms) {
			HashMap<Long,TwitterUserForMap> user_ids = new HashMap<>(DatabaseManager.INSTANCE.getAllUsers().stream().collect(Collectors.toMap(TwitterUserForMap::getId,Function.identity())));
			Random rnd = new Random();
			List<Feature> features = new ArrayList<Feature>();
			for ( UserSnapshot user : tms.getUsers() ) {
				for ( Long u2 : user.getFollowers() ) {
					features.add(new Feature(user.getUser(),user_ids.get(u2),rnd.nextGaussian()));
				}
			}
			return features;
		}
	};
	
	public BasicDBObject getTimeQuery(TwitterMapSnapshot tms) {
		long less_then = tms.getTaken_at().getTime();
		long greater_then = less_then-7L*24L*60L*60L*100L;
		return new BasicDBObject("t",new BasicDBObject("$lt",less_then).append("$gt",greater_then));
	}
	 
	public List<Feature> getLocalFeaturesForMap(TwitterMapSnapshot map,String coll_name,String u1,String u2) {
		HashMap<Long,TwitterUserForMap> user_ids = new HashMap<>(DatabaseManager.INSTANCE.getAllUsers().stream().collect(Collectors.toMap(TwitterUserForMap::getId,Function.identity())));
		DBCollection coll = DatabaseManager.INSTANCE.db.getCollection(coll_name);
		BasicDBObject query = getTimeQuery(map);
		HashMap<Long,Long> total_count = new HashMap<>();
		for ( UserSnapshot us : map.getUsers() ) {
			long uid1 = us.getUser_id();
			long c = coll.count(query.append(u1, uid1));
			total_count.put(uid1, c);			
		}
		List<Feature> features = new ArrayList<Feature>();
		for ( UserSnapshot us : map.getUsers() ) {
			long uid1 = us.getUser_id();
			for ( Long uid2 : us.getFollowers() ) {
				long c = coll.count(query.append(u1, uid1).append(u2,uid2));
				features.add(new Feature(user_ids.get(uid1),user_ids.get(uid2),(c*1.0+1.0)/(total_count.getOrDefault(uid1, 0L)+map.getUsers().size())));
			}
		}
		return features;
	}

	public List<Feature> getGlobalFeaturesForMap(TwitterMapSnapshot map,String coll_name) {
		HashMap<Long,TwitterUserForMap> user_ids = new HashMap<>(DatabaseManager.INSTANCE.getAllUsers().stream().collect(Collectors.toMap(TwitterUserForMap::getId,Function.identity())));
		List<Feature> res = new ArrayList<>();
		DBCollection coll = DatabaseManager.INSTANCE.db.getCollection(coll_name);
		for ( UserSnapshot us : map.getUsers() ) {
			long uid1 = us.getUser_id();
			for ( Long uid2 : us.getFollowers() ) {
				double psi = Double.parseDouble(""+coll.findOne(new BasicDBObject("u1",uid1).append("u2",uid2)).get("psi"));
				res.add(new Feature(user_ids.get(uid1), user_ids.get(uid2), psi));
			}
		}
		return res;
	}
	
	public static List<Feature> removeOutliers(List<Feature> features) {
		double avg = features.stream().map(Feature::getValue).reduce((a,b) -> a+b).get()/features.size();
		double threshold = avg*10;
		return features.stream().map(f -> new Feature(f.getU1(),f.getU2(),Math.max(f.getValue(),threshold))).collect(Collectors.toList());
	}
	
	public static List<Feature> normalize(List<Feature> features) {
		double mean = features.stream().map(Feature::getValue).reduce((a,b) -> a+b).get()/features.size();
		double variance	= Math.sqrt(features.stream().map(f -> Math.pow(f.getValue()-mean,2.0d)).reduce((a, b) -> a+b).get()/(features.size()-1));
		return features.stream().map(f -> new Feature(f.getU1(),f.getU2(),(f.getValue()-mean)/variance)).collect(Collectors.toList());
	}

}
