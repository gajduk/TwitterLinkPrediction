package core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import mk.edu.manu.cs.algorithm.Graph;
import utils.DatabaseManager;
import utils.Utils;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import features.FeatureExtractors;
import features.Features;
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
		List<TwitterUserForMap> all_users = new ArrayList<>(DatabaseManager.INSTANCE.getAllUsers());
		HashMap<Long,TwitterUserForMap> user_ids = new HashMap<>(all_users.stream().collect(Collectors.toMap(TwitterUserForMap::getId,Function.identity())));
		try ( BufferedReader jin = new BufferedReader(new FileReader(folder_name+"\\_followers_1000_ids_for_map.txt.txt"))) {
			while ( jin.ready() ) {
				String s_line[] = jin.readLine().split("\\s++");
				long user_id = Long.parseLong(s_line[0]);
				if ( user_ids.containsKey(user_id) ) {
					users.add(new UserSnapshot(user_ids.get(user_id),Arrays.asList(s_line).subList(2,s_line.length).stream().map(follower_id -> Long.parseLong(follower_id)).filter(follower_id -> user_ids.containsKey(follower_id)).collect(Collectors.toList())));
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
		HashMap<Long,TwitterUserForMap> user_ids = new HashMap<>(DatabaseManager.INSTANCE.getAllUsers().stream().collect(Collectors.toMap(TwitterUserForMap::getId,Function.identity())));
		ArrayList<Features> list = new ArrayList<>(fes.stream().map(fe -> new Features(fe,fe.extractFeatures(this))).collect(Collectors.toList()));
		/*
		HashMap<Integer,HashSet<Integer>> g = new HashMap<Integer,HashSet<Integer>>();
		for ( UserSnapshot us : getUsers() ) 
			g.put(us.getUserIdx(),new HashSet<Integer>(us.getFollowers().stream().map(id -> user_ids.get(id).getIdx()).collect(Collectors.toList())));
		*/
		ArrayList<Edge> edges = new ArrayList<Edge>();
		for ( UserSnapshot us : getUsers() ) 
			edges.addAll(us.getFollowers().stream().map(id -> new Edge(us.user.idx,user_ids.get(id).idx)).collect(Collectors.toList()));
//		edges.sort((a,b) -> (a.a==b.a)?Integer.compare(a.b, b.b):Integer.compare(a.a, b.a));
		return new TwitterFeatureGraph(users.size(), fes.size(),edges, list);
	}


	public Graph buildGraph() {
		HashMap<Long,TwitterUserForMap> user_ids = new HashMap<>(DatabaseManager.INSTANCE.getAllUsers().stream().collect(Collectors.toMap(TwitterUserForMap::getId,Function.identity())));
		
		Graph g = new Graph();
		users.forEach(u -> g.addNode(u.getidx(), u.getUser_id()+"", 0));
		for ( UserSnapshot u1 : users ) {
			for ( Long u2id : u1.followers ) {
				g.addEdge(u1.getidx(), user_ids.get(u2id).idx);
			}
		}
		return g.toNormalizedGraph();
	}
	
}
