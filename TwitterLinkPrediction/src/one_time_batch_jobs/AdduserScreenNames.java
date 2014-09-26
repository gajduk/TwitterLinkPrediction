package one_time_batch_jobs;

import java.util.List;

import utils.DatabaseManager;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import core.TwitterUserForMap;

public class AdduserScreenNames {
	
	public static void main(String[] args) {
		List<TwitterUserForMap> users = DatabaseManager.INSTANCE.getAllUsers();
		DBCollection colltwits = DatabaseManager.INSTANCE.db.getCollection("Twits");
		DBCollection collusers = DatabaseManager.INSTANCE.db.getCollection("TwitterUsers");
		for ( TwitterUserForMap user : users ) {
			String screenname = (String)colltwits.findOne(new BasicDBObject("uid",user.getId())).get("us");
			collusers.update(new BasicDBObject("id",user.getId()),new BasicDBObject("$set",new BasicDBObject("screenname",screenname)));
		}
	}
	
	public static void copyTwitterMaps() {
		DB db = DatabaseManager.INSTANCE.db;
		DBCursor cursor = db.getCollection("TwitterMaps").find();
		DBCollection coll = db.getCollection("TwitterMaps1");
		while ( cursor.hasNext() )
			coll.insert(cursor.next());
	}

}
