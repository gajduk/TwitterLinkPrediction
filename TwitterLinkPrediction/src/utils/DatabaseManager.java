package utils;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import core.TwitterUserForMap;

public enum DatabaseManager {
	INSTANCE;

	public DB db;
	private static String users_col = "TwitterUsers";
	
	private DatabaseManager() {
		MongoClient mongoClient;
		try {
			mongoClient = new MongoClient();
			db = mongoClient.getDB( "TwitterStream" );
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public List<TwitterUserForMap> getAllUsers() {
		return db.getCollection(users_col).find().toArray().stream().map(TwitterUserForMap::parseFromDB).collect(Collectors.toList());
	}
	
	public TwitterUserForMap getUser(long user_id) {
		return TwitterUserForMap.parseFromDB(db.getCollection(users_col).findOne(new BasicDBObject("id",user_id)));
	}
	
	public TwitterUserForMap getUserByIdx(int idx) {
		return TwitterUserForMap.parseFromDB(db.getCollection(users_col).findOne(new BasicDBObject("idx",idx)));
	}

	public HashMap<String, Double> getWordItdf() {
		HashMap<String, Double> res = new HashMap<>();
		DBCursor cursor = db.getCollection("ItdfWords").find();
		while ( cursor.hasNext() ) {
			DBObject dbo = cursor.next();
			res.put((String)dbo.get("word"),(double)dbo.get("v"));
		}
		return res;
	}
	
	public HashMap<String, Double> getHashtagItdf() {
		HashMap<String, Double> res = new HashMap<>();
		DBCursor cursor = db.getCollection("ItdfWords").find();
		while ( cursor.hasNext() ) {
			DBObject dbo = cursor.next();
			res.put((String)dbo.get("hashtag"),(double)dbo.get("v"));
		}
		return res;
	}

}
