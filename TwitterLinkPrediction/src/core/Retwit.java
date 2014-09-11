package core;
import java.util.Date;
import java.text.ParseException;

import utils.Utils;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


public class Retwit {
	
	 long retweetedStatusID;
	 long retweetedFromUserID;
	 String retweetedFromUserScreenName;
	 long retweetedByUserId;
	 String retweetedByUserScreenName;
	 Date retweetedAt;
	 
	 public Retwit(long retweetedStatusID, long retweetedFromUserID,
			String retweetedFromUserScreenName, long retweetedByUserId,
			String retweetedByUserScreenName, Date retweetedAt) {
		super();
		this.retweetedStatusID = retweetedStatusID;
		this.retweetedFromUserID = retweetedFromUserID;
		this.retweetedFromUserScreenName = retweetedFromUserScreenName;
		this.retweetedByUserId = retweetedByUserId;
		this.retweetedByUserScreenName = retweetedByUserScreenName;
		this.retweetedAt = retweetedAt;
	 }
	
	 @Override
	 public String toString() {
		return "Retwit [retweetedStatusID=" + retweetedStatusID
				+ ", retweetedFromUserID=" + retweetedFromUserID
				+ ", retweetedFromUserScreenName="
				+ retweetedFromUserScreenName + ", retweetedByUserId="
				+ retweetedByUserId + ", retweetedByUserScreenName="
				+ retweetedByUserScreenName + ", retweetedAt=" + retweetedAt.getTime()
				+ "]";
	 }
	 
	 public static Retwit parseFromTextFile(String[] s_line) {
		 try {
			return new Retwit(Long.parseLong(s_line[3]),Long.parseLong(s_line[5]),s_line[7],Long.parseLong(s_line[9]),s_line[11],Utils.df.parse(s_line[13]));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		 return null;
	 }
	 
	 public DBObject getDBObject() {
		 return new BasicDBObject("id", retweetedStatusID).append("Oruid",retweetedFromUserID).append("Orus",retweetedFromUserScreenName).append("uid",retweetedByUserId).append("t",retweetedAt).append("us", retweetedByUserScreenName);
	 }
	 
	 public static Retwit parsefromDBObject(DBObject db) {
		 return new Retwit((long) db.get("id"),(long) db.get("Oruid"),(String) db.get("Orus"),(long) db.get("uid"),(String) db.get("us"),new Date((long) db.get("t"))); 
	 }
	 
	

}
