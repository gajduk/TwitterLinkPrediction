package features;

import core.TwitterUserForMap;


public class Feature {
	
	TwitterUserForMap u1,u2;
	double value;
	
	public Feature(TwitterUserForMap u1,TwitterUserForMap u2, double value) {
		super();
		this.u1 = u1;
		this.u2 = u2;
		this.value = value;
	}
	
	public int getIdx1() {
		return u1.getIdx();
	}

	public int getIdx2() {
		return u2.getIdx();
	}
	
	public long getUId1() {
		return u1.getId();
	}

	public long getUId2() {
		return u2.getId();
	}

	public TwitterUserForMap getU1() {
		return u1;
	}

	public TwitterUserForMap getU2() {
		return u2;
	}

	public double getValue() {
		return value;
	}
	
}
