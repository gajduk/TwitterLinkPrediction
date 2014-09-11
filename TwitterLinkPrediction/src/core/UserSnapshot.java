package core;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


public class UserSnapshot {
	
	long user_id;
	List<Long> followers;
	
	public long getUser_id() {
		return user_id;
	}

	public List<Long> getFollowers() {
		return followers;
	}

	public UserSnapshot(long user_id, List<Long> followers) {
		super();
		this.user_id = user_id;
		this.followers = followers;
	}
	
	public DBObject getDBObject() {
		return new BasicDBObject("id",user_id).append("followers",followers);
	}
	
	public static UserSnapshot parseFromDB(DBObject dbo) {
		return new UserSnapshot((long)(dbo.get("id")),(List<Long>)dbo.get("followers"));
	}

}
