package core;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


public class TwitterUserForMap {
	
	long id;
	int idx;
	String screenname;
	
	public TwitterUserForMap(long id, int idx,String screenname) {
		this.id = id;
		this.idx = idx;
		this.screenname = screenname;
	}
	
	public static TwitterUserForMap parseFromDB(DBObject dbo) {
		try {
			return new TwitterUserForMap((long) dbo.get("id"),(int) dbo.get("idx"),(String) dbo.get("screenname"));
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
