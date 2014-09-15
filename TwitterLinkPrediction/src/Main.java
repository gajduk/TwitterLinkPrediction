import java.io.File;
import java.io.PrintWriter;
import java.util.List;

import utils.DatabaseManager;
import core.TwitterUserForMap;

public class Main {
	
	//twit
	//	retweet='false' inReplyToStatusID='441270402178744320' inReplyToUserID='273097354' id='441271047783202816' user_id='273097354' user_screenName='ElWihYT' createdAt='18:56:30 05.03.2014' text='@YouTube @HDKGamer me ayudas a conseguir dineros en gtaonline que quiero pillarme el jester y me faltan como 140000$ xD'
	
	//retwit
	//	retweet='true' retweetedStatusID='441269049713229824' retweetedFromUserID='5988062' retweetedFromUserScreenName='TheEconomist' retweetedByUserId='2346489842' retweetedByUserScreenName='TheSimonFraser' retweetedAt='18:56:36 05.03.2014' 
			 	 
	
	
	public static void main(String[] args) throws Exception {
		try ( PrintWriter out = new PrintWriter(new File("ids.txt")) ) {
			List<TwitterUserForMap> users = DatabaseManager.INSTANCE.getAllUsers();
		}
	}
	

}
