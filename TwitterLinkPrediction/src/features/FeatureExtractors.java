package features;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

import utils.DatabaseManager;
import core.TwitterMapSnapshot;
import core.UserSnapshot;

public enum FeatureExtractors implements FeatureExtractor{
	Retwits {
		public List<Feature> extractFeatures(TwitterMapSnapshot twitter_map) {
			
			return null;
		}
	},
	Mentions{
		public List<Feature> extractFeatures(TwitterMapSnapshot twitter_map) {
			
			return null;
		}
	},
	Replies{
		public List<Feature> extractFeatures(TwitterMapSnapshot twitter_map) {
			
			return null;
		}
	},
	CSHashtags {
		public List<Feature> extractFeatures(TwitterMapSnapshot twitter_map) {
			return getGlobalFeaturesForMap(twitter_map,"CSHashtags");
		}
	},
	CSTwits{
		public List<Feature> extractFeatures(TwitterMapSnapshot twitter_map) {
			return getGlobalFeaturesForMap(twitter_map,"CSHashtags");
		}
	},
	CSTime{
		public List<Feature> extractFeatures(TwitterMapSnapshot twitter_map) {
			return getGlobalFeaturesForMap(twitter_map,"CSTime");
		}
	};

	public List<Feature> getGlobalFeaturesForMap(TwitterMapSnapshot map,String coll_name) {
		List<Feature> res = new ArrayList<>();
		DBCollection coll = DatabaseManager.INSTANCE.db.getCollection(coll_name);
		for ( UserSnapshot us : map.getUsers() ) {
			long uid1 = us.getUser_id();
			for ( Long uid2 : us.getFollowers() ) {
				double psi = (double)coll.findOne(new BasicDBObject("u1",uid1).append("u2",uid2)).get("psi");
				res.add(new Feature(uid1, uid2, psi));
			}
		}
		return res;
	}

}
