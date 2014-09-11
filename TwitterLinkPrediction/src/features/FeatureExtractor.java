package features;

import java.util.List;

import core.TwitterMapSnapshot;


public interface FeatureExtractor {
	
	public List<Feature> extractFeatures(TwitterMapSnapshot twitter_map);
	
}
