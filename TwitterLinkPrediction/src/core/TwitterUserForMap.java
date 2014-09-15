package core;
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

	public int getIdx() {
		return idx;
	}
	
	public long getId()  {
		return id;
	}

}
