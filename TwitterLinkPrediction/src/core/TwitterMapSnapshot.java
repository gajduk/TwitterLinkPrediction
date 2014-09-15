package core;
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import linkpred_batch.FeatureField;
import utils.DatabaseManager;
import utils.Utils;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import features.Feature;
import features.FeatureExtractors;
import features.TwitterFeatureGraph;


public class TwitterMapSnapshot {
	
	List<UserSnapshot> users;
	public List<UserSnapshot> getUsers() {
		return users;
	}

	public Date getTaken_at() {
		return taken_at;
	}

	Date taken_at;
	
	public TwitterMapSnapshot(List<UserSnapshot> users, Date taken_at) {
		super();
		this.users = users;
		this.taken_at = taken_at;
	}
	
	public static TwitterMapSnapshot readFromFolder(String folder_name) throws ParseException {
		Date taken_at = Utils.timestamp_df.parse(folder_name.substring(folder_name.lastIndexOf('\\')+1));
		List<UserSnapshot> users = new ArrayList<>();
		HashSet<Long> user_ids = new HashSet<>(DatabaseManager.INSTANCE.getAllUsers().stream().map(user -> user.id).collect(Collectors.toSet()));
		try ( BufferedReader jin = new BufferedReader(new FileReader(folder_name+"\\_followers_1000_ids_for_map.txt.txt"))) {
			while ( jin.ready() ) {
				String s_line[] = jin.readLine().split("\\s++");
				
				long user_id = Long.parseLong(s_line[0]);
				if ( user_ids.contains(user_id) ) {
					users.add(new UserSnapshot(user_id,Arrays.asList(s_line).subList(2,s_line.length).stream().map(follower_id -> Long.parseLong(follower_id)).filter(follower_id -> user_ids.contains(follower_id)).collect(Collectors.toList())));
				}
			}
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
		if ( users.size() != user_ids.size() ) return null;
		return new TwitterMapSnapshot(users, taken_at);
	}
	
	public static TwitterMapSnapshot parseFromDBObject(DBObject dbo) {
		long t = (long)dbo.get("t");
		List<DBObject> users_snaps = (List<DBObject>) dbo.get("users");
		List<UserSnapshot> users = new ArrayList<>(users_snaps.stream().map(UserSnapshot::parseFromDB).collect(Collectors.toList()));
		return new TwitterMapSnapshot(users,new Date(t));
	}
	
	public DBObject getDBObject() {
		return new BasicDBObject("t",taken_at.getTime()).append("users",users.stream().map(UserSnapshot::getDBObject).collect(Collectors.toList()));
	}

	public TwitterFeatureGraph buildTwitterFeatureGraph(List<FeatureExtractors> fes) {
		HashMap<Long,Integer> idxes = new HashMap<>();
		HashMap<Integer,HashMap<Integer,ArrayList<Double>>> edge_features =  new HashMap<>();
		int idx = 0;
		for ( UserSnapshot us : users )
			idxes.put(us.user_id, idx++);
		int n = users.size();
		int nf= fes.size();
		ArrayList<FeatureField> list = new ArrayList<>();
		for ( FeatureExtractors fe : fes ) {
			List<Feature> features = fe.extractFeatures(this);
			for ( Feature f : features ) {
				int idx1 = idxes.get(f.getUser1_id());
				int idx2 = idxes.get(f.getUser2_id());
				HashMap<Integer,ArrayList<Double>> m = edge_features.get(idx1);
				if ( m == null ) m = new HashMap<Integer,ArrayList<Double>>();
				ArrayList<Double> w = m.get(idx2);
				if ( w == null ) w = new ArrayList<Double>();
				w.add(f.getValue());
				m.put(idx2, w);
				edge_features.put(idx1,m);
			}
		}
		for ( Integer idx1 : edge_features.keySet() ) {
			HashMap<Integer,ArrayList<Double>> w = edge_features.get(idx1);
			for ( Integer idx2 : w.keySet() ) {
				ArrayList<Double> l = w.get(idx2);
				double d[] = new double[l.size()];
				for ( int i = 0 ; i < d.length ; ++i ) 
					d[i] = l.get(i);				
				list.add(new FeatureField(idx2, idx1, new DenseDoubleMatrix1D(d)));
			}
		}
		return new TwitterFeatureGraph(n, nf, list);
	}

}
