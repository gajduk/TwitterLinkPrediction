package core;

import com.mongodb.DBObject;


public class TwiterUserForMap {
	
	public long id;
	public int idx;
	
	public TwiterUserForMap(long id, int idx) {
		this.id = id;
		this.idx = idx;
	}
	
	public static TwiterUserForMap parseFromDB(DBObject dbo) {
		try {
			return new TwiterUserForMap((long) dbo.get("id"),(int) dbo.get("idx"));
		}
		catch (Exception e) {
			return null;
		}
	}

}
