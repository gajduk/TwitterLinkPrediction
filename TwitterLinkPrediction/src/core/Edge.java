package core;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class Edge {
		
	int a,b;

	public Edge(int a, int b) {
		super();
		this.a = a;
		this.b = b;
	}

	public int getR() {
		return a;
	}

	public int getC() {
		return b;
	}
	
	public DBObject getDBObject() {
		return new BasicDBObject("a",a).append("b",b);
	}
	
	public static Edge parseFromDBObject(DBObject dbo) {
		int a = (int)dbo.get("a");
		int b = (int)dbo.get("b");
		return new Edge(a,b);
	}
	
}
