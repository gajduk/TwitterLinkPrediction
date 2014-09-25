package mk.edu.manu.cs.algorithm;

import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class ExpandingNode implements Comparable<ExpandingNode> {
	public int nodeid;
	// public Set<Integer> colors = new HashSet<Integer>();

	public int color = -1;
	public int level = Integer.MAX_VALUE;
	public int score = 0;

	public int fatherscore = -1;

	public List<ExpandingNode> children = new LinkedList<ExpandingNode>();
	public List<ExpandingNode> fathers = new LinkedList<ExpandingNode>();

	//public List<ExpandingNode> finalFathers = new LinkedList<ExpandingNode>();
	public ExpandingNode finalFather = null;

	@Override
	public int compareTo(ExpandingNode o) {
		if (this.score - o.score != 0) {
			return o.score - this.score;
		} else if (this.level - o.level != 0) {
			return this.level - o.level;
		} else {
			return 0;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ExpandingNode) {
			return this.nodeid == ((ExpandingNode) obj).nodeid;
		} else
			return false;
	}

	public static void main(String[] args) {
		ExpandingNode n = new ExpandingNode();
		n.score = 3;
		n.level = 5;
		n.nodeid = 1;

		ExpandingNode n2 = new ExpandingNode();
		n2.score = 3;
		n2.level = 4;
		n2.nodeid = 2;

		PriorityQueue<ExpandingNode> q = new PriorityQueue<ExpandingNode>();
		q.add(n);
		q.add(n2);
		System.out.println(q.poll().nodeid);
	}
}
