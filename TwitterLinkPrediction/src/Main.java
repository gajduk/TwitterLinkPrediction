import java.util.Date;

import utils.DatabaseManager;
import utils.Utils;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

public class Main {
	
	//twit
	//	retweet='false' inReplyToStatusID='441270402178744320' inReplyToUserID='273097354' id='441271047783202816' user_id='273097354' user_screenName='ElWihYT' createdAt='18:56:30 05.03.2014' text='@YouTube @HDKGamer me ayudas a conseguir dineros en gtaonline que quiero pillarme el jester y me faltan como 140000$ xD'
	
	//retwit
	//	retweet='true' retweetedStatusID='441269049713229824' retweetedFromUserID='5988062' retweetedFromUserScreenName='TheEconomist' retweetedByUserId='2346489842' retweetedByUserScreenName='TheSimonFraser' retweetedAt='18:56:36 05.03.2014' 
			 	 
	
	
	public static void main(String[] args) throws Exception {
		
		DBCollection maps_coll = DatabaseManager.INSTANCE.db.getCollection("TwitterMaps");
		long start = Utils.df.parse("00:00:00 01.01.2014").getTime();
		long end = Utils.df.parse("00:00:00 09.08.2014").getTime();
		long step = 24*60*60*1000L;
		int count = 0;
		for ( long up_to = start ; up_to < end ; up_to += step ) {
			++count;
			/*
			if ( count != 51 ) continue;
			DBCursor cursor = twit_coll.find(new BasicDBObject("t",new BasicDBObject("$lt",up_to).append("$gt",up_to-step))).sort(new BasicDBObject("t",1));
			while ( cursor.hasNext() ) {
				Twit t = Twit.parsefromDBObject(cursor.next());
				System.out.println(t);
			}
			*/
//			System.out.println(up_to);
			//System.out.println(Utils.df.format(new Date(up_to)));
			System.out.print(maps_coll.count(new BasicDBObject("t",new BasicDBObject("$lt",up_to).append("$gt",up_to-step)))+",");
		}
		System.out.println();
	}
	

}
