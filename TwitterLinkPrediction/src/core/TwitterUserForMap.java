package core;
import java.io.Serializable;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


public class TwitterUserForMap {
	
	long id;
	int idx;
	
	public TwitterUserForMap(long id, int idx) {
		this.id = id;
		this.idx = idx;
	}
	
	public static TwitterUserForMap parseFromDB(DBObject dbo) {
		try {
			return new TwitterUserForMap((long) dbo.get("id"),(int) dbo.get("idx"));
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public DBObject getDBObject() {
		return new BasicDBObject("id",id).append("idx",idx);
	}

	public int getIdx() {
		return idx;
	}
	
	public long getId()  {
		return id;
	}

}
