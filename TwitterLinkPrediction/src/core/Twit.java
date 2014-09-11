package core;
import java.text.ParseException;
import java.util.Date;

import utils.Utils;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


public class Twit {
	
	 long inReplyToStatusID,inReplyToUserID,id,user_id;
	 String user_screenName;
	 Date createdAt;
	 public String text;
	 
	 public Twit(long inReplyToStatusID, long inReplyToUserID, long id,
			long user_id, String user_screenName, Date createdAt, String text) {
		super();
		this.inReplyToStatusID = inReplyToStatusID;
		this.inReplyToUserID = inReplyToUserID;
		this.id = id;
		this.user_id = user_id;
		this.user_screenName = user_screenName;
		this.createdAt = createdAt;
		this.text = text;
	 }

	 @Override
	 public String toString() {
		 return "Twit [inReplyToStatusID=" + inReplyToStatusID
				+ ", inReplyToUserID=" + inReplyToUserID + ", id=" + id
				+ ", user_id=" + user_id + ", user_screenName="
				+ user_screenName + ", createdAt=" + createdAt + ", text="
				+ text + "]";
	 }
	 
	 public static Twit parseFromTextFile(String[] s_line) {
		 try {
			return new Twit(Long.parseLong(s_line[3]),Long.parseLong(s_line[5]),Long.parseLong(s_line[7]),Long.parseLong(s_line[9]),
					s_line[11],Utils.df.parse(s_line[13]),s_line[15].replaceAll(" <NOV_RED9375>","\\n").replaceAll("<NAVODNIK>","'"));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		 return null;
	 }
	 
	 public DBObject getDBObject() {
		 return new BasicDBObject("id", id).append("rsid", inReplyToStatusID).append("ruid",inReplyToUserID).append("uid",user_id).append("us",user_screenName).append("t",createdAt).append("text", text);
	 }
	 
	 public static Twit parsefromDBObject(DBObject db) {
		 return new Twit((long) db.get("rsid"),(long) db.get("ruid"),(long) db.get("id"),(long) db.get("uid"),(String) db.get("us"),new Date((long) db.get("t")),(String) db.get("text")); 
	 }
	
}
