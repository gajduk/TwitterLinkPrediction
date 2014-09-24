package one_time_batch_jobs;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import utils.DatabaseManager;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

import core.TwitterMapSnapshot;
import features.FeatureExtractors;
import features.TwitterFeatureGraph;

public class ExtractGraphsWithRandomFeatures {
	
	public static void main(String[] args) throws ParseException, InterruptedException, FileNotFoundException, IOException {
		//list of features
		List<FeatureExtractors> fes = new ArrayList<>();
		fes.add(FeatureExtractors.Random);
		fes.add(FeatureExtractors.Random);
		

		List<TwitterMapSnapshot> tmss = new ArrayList<>();
		DBCursor cursor = DatabaseManager.INSTANCE.db.getCollection("TwitterMaps").find();
		DBCollection coll = DatabaseManager.INSTANCE.db.getCollection("TwitterGraphs");
		while ( cursor.hasNext() )
			tmss.add(TwitterMapSnapshot.parseFromDBObject(cursor.next()));
		System.out.println("Parsed");
		List<TwitterFeatureGraph> twgs = new ArrayList<>(tmss.parallelStream().map(tms -> tms.buildTwitterFeatureGraph(fes)).collect(Collectors.toList()));
		
		twgs.stream().map(twg -> twg.getDBObject()).forEach(dbo -> coll.insert(dbo));
	}

}
