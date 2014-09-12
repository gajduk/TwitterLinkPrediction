package features;


public class Feature {

	long user1_id,user2_id;
	double value;
	
	public Feature(long user1_id, long user2_id, double value) {
		super();
		this.user1_id = user1_id;
		this.user2_id = user2_id;
		this.value = value;
	}
	
	

	public long getUser1_id() {
		return user1_id;
	}



	public long getUser2_id() {
		return user2_id;
	}



	public double getValue() {
		return value;
	}
	
}
