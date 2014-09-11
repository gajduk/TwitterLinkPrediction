package utils;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.MongoClient;

import core.TwiterUserForMap;

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
	
	public List<TwiterUserForMap> getAllUsers() {
		return db.getCollection(users_col).find().toArray().stream().map(TwiterUserForMap::parseFromDB).collect(Collectors.toList());
	}
	
	public TwiterUserForMap getUser(long user_id) {
		return TwiterUserForMap.parseFromDB(db.getCollection(users_col).findOne(new BasicDBObject("id",user_id)));
	}
	
	public TwiterUserForMap getUserByIdx(int idx) {
		return TwiterUserForMap.parseFromDB(db.getCollection(users_col).findOne(new BasicDBObject("idx",idx)));
	}

	public HashMap<String, Double> getWordItdf() {
		// TODO Auto-generated method stub
		return null;
	}

}
