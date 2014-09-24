package one_time_batch_jobs;

import utils.DatabaseManager;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class AddIndexesForUsers {
	
	public static void main(String[] args) {
		/*
		DB db = DatabaseManager.INSTANCE.db;
		DBCursor cursor = db.getCollection("TwitterUsers").find();
		DBCollection coll = db.getCollection("TwitterMaps");
		while ( cursor.hasNext() ) {
			DBObject user = cursor.next();
			DBObject query = new BasicDBObject("users.id",user.get("id"));
			System.out.println(coll.count(query));
			DBObject update_command = new BasicDBObject("$set",new BasicDBObject("users.$.idx",user.get("idx")));
			coll.updateMulti(query,update_command);
		}
		*/
	}
	
	public static void copyTwitterMaps() {
		DB db = DatabaseManager.INSTANCE.db;
		DBCursor cursor = db.getCollection("TwitterMaps").find();
		DBCollection coll = db.getCollection("TwitterMaps1");
		while ( cursor.hasNext() )
			coll.insert(cursor.next());
	}

}
