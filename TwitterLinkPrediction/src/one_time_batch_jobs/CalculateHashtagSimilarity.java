package one_time_batch_jobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import utils.DatabaseManager;
import utils.Utils;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import core.Twit;
import core.TwiterUserForMap;

public class CalculateHashtagSimilarity {

	public static void main(String[] args) {
//		countHashtagsPerUsers();
//		calculateHashtagsItdf();
//		calculateHashtagsCosineSimilarity();
	}
	
	public static void calculateHashtagsItdf() {
		List<TwiterUserForMap> users = DatabaseManager.INSTANCE.getAllUsers();
		DBCollection words_per_user = DatabaseManager.INSTANCE.db.getCollection("HashtagsPerUser");
		HashMap<String,Double> count = new HashMap<>();
		for ( TwiterUserForMap user : users ) {
			HashMap<String,Double> count_user = (HashMap<String,Double>) words_per_user.findOne(new BasicDBObject("uid",user.id)).get("hashtags");
			count_user.keySet().forEach(key -> count.put(key,count.getOrDefault(key, 0.0)+count_user.get(key)));
		}
		DBCollection itdf_words = DatabaseManager.INSTANCE.db.getCollection("ItdfHashtags");
		itdf_words.insert(count.keySet().stream().filter(key -> count.get(key)>10).map(key -> new BasicDBObject("hashtag",key).append("v",Math.log(10000.0/count.get(key)))).collect(Collectors.toList()));
	}
	
	public static void countHashtagsPerUsers() {
		List<TwiterUserForMap> users = DatabaseManager.INSTANCE.getAllUsers();
		DBCollection twit_coll = DatabaseManager.INSTANCE.db.getCollection("Twits");
		DBCollection words_per_user = DatabaseManager.INSTANCE.db.getCollection("HashtagsPerUser");
		
		for ( TwiterUserForMap user : users ) {
			HashMap<String,Double> count = new HashMap<>();
			DBCursor cursor = twit_coll.find(new BasicDBObject("uid",user.id));
			while ( cursor.hasNext() ) {
				DBObject dbo = cursor.next();
				Twit twit = Twit.parsefromDBObject(dbo);
				List<String> hashtags = Utils.findAll(twit.text.toLowerCase(),Utils.HASHTAGS_PATTERN);
				hashtags.stream().filter(word -> word.length()>1).
					collect(Collectors.toList()).
					forEach(word -> count.put(word, 1.0+count.getOrDefault(word, 0.0)));
			}
			words_per_user.insert(new BasicDBObject("uid",user.id).append("hashtags",count));
		}
	}
	
		
	public static void calculateHashtagsCosineSimilarity() {
		HashMap<String,Double> itdf = DatabaseManager.INSTANCE.getWordItdf();
		List<TwiterUserForMap> users = DatabaseManager.INSTANCE.getAllUsers();
		DBCollection wpu = DatabaseManager.INSTANCE.db.getCollection("HashtagsPerUser");
		DBCollection cs = DatabaseManager.INSTANCE.db.getCollection("CSHashtags");
		List<DBObject> to_insert = new ArrayList<>();
		HashMap<Long,HashMap<String,Double>> user_itdf = new HashMap<>();
		for ( TwiterUserForMap user1 : users ) {
			HashMap<String,Double> u1words = new HashMap<>((Map<String,Double>) wpu.findOne(new BasicDBObject("uid",user1.id)).get("hashtags"));
			for ( String word : new ArrayList<>(u1words.keySet()) ) {
				if ( itdf.containsKey(word))
					u1words.put(word, u1words.get(word)*itdf.get(word));
				else
					u1words.remove(word);
			}
			user_itdf.put(user1.id,u1words);
		}
		for ( TwiterUserForMap user1 : users ) {
			for ( TwiterUserForMap user2 : users ) {
				if (user1.id == user2.id ) continue;
				
				HashMap<String,Double> u1words = user_itdf.get(user1.id);
				HashMap<String,Double> u2words = user_itdf.get(user2.id);
				
				if ( u2words.size() == 0 || u1words.size() == 0 ) {
					to_insert.add(new BasicDBObject("u1",user1.id).append("u2",user2.id).append("psi",0));
					continue;
				}
				
				double sum1 =  u1words.values().parallelStream().reduce((a,b) -> a+b).get();
				double sum2 =  u2words.values().parallelStream().reduce((a,b) -> a+b).get();
				HashMap<String,Double> uwords = u1words;
				if ( u1words.size() > u2words.size() ) uwords = u2words;
				double psi = uwords.keySet().parallelStream().map((s) -> u1words.getOrDefault(s,0.0d)*u2words.getOrDefault(s,0.0d)).reduce((a,b) -> a+b).get()/(sum1*sum2+500);
				to_insert.add(new BasicDBObject("u1",user1.id).append("u2",user2.id).append("psi",psi));
			}
		}	
		cs.insert(to_insert);
	}
	
}
