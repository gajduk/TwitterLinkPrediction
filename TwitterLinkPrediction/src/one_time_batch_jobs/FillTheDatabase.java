package one_time_batch_jobs;
import java.io.File;

import com.mongodb.DBCollection;

class Pair<T,U> {
	T a;
	U b;
	
	public Pair(T a, U b) {
		this.a = a;
		this.b = b;
	}
	
}

public class FillTheDatabase {
	
	public static void main(String[] args) throws Exception {
		//fill in the maps
		/*
		DBCollection coll = DatabaseManager.INSTANCE.db.getCollection("TwitterMaps");
		String root_dir = "C:\\Users\\Andrej Gajduk\\Desktop\\Backup of twitter maps\\Java extract map_05_08";
		File root = new File(root_dir);
		for ( File f : root.listFiles() ) {
			if ( f.isDirectory() ) {
				TwitterMapSnapshot tms = TwitterMapSnapshot.readFromFolder(f.getAbsolutePath());
				if ( tms != null )
					coll.insert(tms.getDBObject());
			}
			
		}
		*/
		//fill in users
		/*
		try ( BufferedReader jin = new BufferedReader(new InputStreamReader(new FileInputStream("user_ids.txt"),"UTF-8"))) {
			int count = 0;
			DBCollection coll = DatabaseManager.INSTANCE.db.getCollection("TwitterUsers");
			ArrayList<Pair<Long,Integer>>  user_ids = new ArrayList<>();
			while( jin.ready() ) {
				user_ids.add(new Pair<Long,Integer>(Long.parseLong(jin.readLine()),count));
				++count;
			}
			coll.insert(user_ids.stream().map(user_id -> new BasicDBObject("id",user_id.a).append("idx",user_id.b)).collect(Collectors.toList()).toArray(new BasicDBObject[user_ids.size()]));
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
		*/
		//fill in twits & retwits
		/*
		DBCollection twit_coll = db.getCollection("TestTwits");
		DBCollection retwit_coll = db.getCollection("Retwits");
		int crt = 0,ct = 0;
		int count = 0;
		try ( BufferedReader jin = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Eclipse projects\\MANU\\Zwitter\\tweets_mak_users_from_01_03_2014_to_07_08_2014123.txt"),"UTF-8"))) {
			while( jin.ready() ) {
				String twit = jin.readLine();
				if (twit.startsWith(" retweet")) {
					String s_twit[]  = twit.split("'");
					boolean retwit = Boolean.valueOf(s_twit[1]);
					if ( retwit ) {
						//Retwit rt = Retwit.parseFromTextFile(s_twit);
						//++crt;
						//retwit_coll.insert(rt.getDBObject());
					}
					else {
						Twit t = Twit.parseFromTextFile(s_twit);
						//++ct;
						twit_coll.insert(t.getDBObject());
					}
				}
			}
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
		System.out.println("Twits:"+ct);

		System.out.println("ReTwits:"+crt);
		*/
	}

}
