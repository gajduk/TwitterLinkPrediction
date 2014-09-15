package core;

import java.util.List;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class UserSnapshot {
	
	TwitterUserForMap user;
	List<Long> followers;
	
	public long getUser_id() {
		return user.id;
	}
	
	public int getidx() {
		return user.idx;
	}

	public List<Long> getFollowers() {
		return followers;
	}

	public UserSnapshot(TwitterUserForMap user, List<Long> followers) {
		super();
		this.user = user;
		this.followers = followers;
	}
	
	public long getUserId() {
		return user.getId();
	}
	
	public int getUserIdx() {
		return user.getIdx();
	}
	
	public TwitterUserForMap getUser() {
		return user;
	}

	public DBObject getDBObject() {
		return new BasicDBObject("id",getUserId()).append("idx",getUserIdx()).append("followers",followers);
	}
	
	public static UserSnapshot parseFromDB(DBObject dbo) {
		return new UserSnapshot(new TwitterUserForMap((long)(dbo.get("id")),(int)(dbo.get("idx"))),(List<Long>)dbo.get("followers"));
	}

}
