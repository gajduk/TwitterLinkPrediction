package mk.edu.manu.cs.algorithm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.PriorityQueue;


public class SSKCommunityAlgorithm implements GraphAlgorithm {

	public int nclusters;
	public ExpandingNode[] votes;
	public double localClustering;
	private Graph g;

	public SSKCommunityAlgorithm(Graph g) {
		this.g = g;
	}

	public void execute() {
		long start = System.currentTimeMillis();
		this.votes = new ExpandingNode[g.nnodes];
		for (int i = 0; i < votes.length; i++) {
			votes[i] = new ExpandingNode();
			votes[i].nodeid = i;
		}
		for (int i = 0; i < g.nnodes; i++) {
			voteForInfluentialNode(i, g, votes);
		}
		this.localClustering /= g.nnodes;
		int maxScore = 0;
		for (int i = 0; i < votes.length; i++) {
			if (votes[i].score > maxScore) {
				maxScore = votes[i].score;
			}
		}
		long end = System.currentTimeMillis() - start;
		System.out.println("1st pass:" + end + "ms");
		// second pass
		this.nclusters = colorWithPrioQueue(votes);
		/*
		 * start = System.currentTimeMillis();
		 * 
		 * @SuppressWarnings("unchecked") LinkedList<ExpandingNode>[]
		 * scoreGroups = (LinkedList<ExpandingNode>[]) new LinkedList[maxScore +
		 * 1]; for (int i = 0; i < votes.length; i++) { int score =
		 * votes[i].score; if (scoreGroups[score] == null) { scoreGroups[score]
		 * = new LinkedList<ExpandingNode>(); }
		 * scoreGroups[score].add(votes[i]); } end = System.currentTimeMillis()
		 * - start; System.out.println("2nd pass (sorting):" + end + "ms");
		 * start = System.currentTimeMillis(); int colorIndex = 0; for (int i =
		 * maxScore; i >= 0; i--) { System.out.println(i); if (scoreGroups[i] ==
		 * null) { continue; } for (int j = 0; j < scoreGroups[i].size(); j++) {
		 * ExpandingNode father = scoreGroups[i].get(j); // check if a new
		 * cluster is born if (father.color == -1) { father.color =
		 * colorIndex++; father.level = 0; father.fatherscore = father.score;
		 * father.finalFather = father; } // expand the cluster by
		 * painting/repainting the children for (ExpandingNode childNode :
		 * father.children) { if (childNode.color == -1) { childNode.color =
		 * father.color; childNode.fatherscore = father.score;
		 * childNode.finalFather = father; childNode.level = father.level + 1;
		 * // shift the child position in its group shiftList(childNode,
		 * scoreGroups[childNode.score]); } } } }
		 */
		/*
		 * Arrays.sort(votes); end = System.currentTimeMillis() - start; int
		 * colorIndex = 0; System.out.println("2nd pass (sorting):" + end +
		 * "ms"); start = System.currentTimeMillis(); for (int i = 0; i <
		 * votes.length; i++) { ExpandingNode father = votes[i]; // expand the
		 * cluster by painting/repainting the children if (father.color == -1) {
		 * father.color = colorIndex++; father.level = 0; father.fatherscore =
		 * father.score; father.finalFather = father; } // paint the children
		 * for (ExpandingNode childNode : father.children) { if (childNode.color
		 * != -1) { if (father.score >= childNode.fatherscore) { // First come -
		 * first father if (father.score > childNode.fatherscore) {
		 * childNode.color = father.color; childNode.level = father.level + 1;
		 * childNode.fatherscore = father.score; childNode.finalFather = father;
		 * } else if (father.level + 1 < childNode.level) { childNode.level =
		 * father.level + 1; childNode.color = father.color;
		 * childNode.finalFather = father; } } } else { childNode.color =
		 * father.color; childNode.fatherscore = father.score;
		 * childNode.finalFather = father; childNode.level = father.level + 1; }
		 * } }
		 */
		// end = System.currentTimeMillis() - start;
		// System.out.println("2nd pass (coloring):" + end + "ms");

		// calculating the modularity score (ONLY FOR UNDIRECTED!!!)
		/*
		 * @SuppressWarnings("unchecked") List<Integer>[] cluster =
		 * (List<Integer>[]) new LinkedList[this.nclusters]; for (int i = 0; i <
		 * votes.length; i++) { if (cluster[votes[i].color] == null) {
		 * cluster[votes[i].color] = new LinkedList<Integer>(); }
		 * cluster[votes[i].color].add(votes[i].nodeid); } double q = 0; int kk
		 * = 0; int aa = 0; for (int i = 0; i < cluster.length; i++) { for (int
		 * j = 0; j < cluster[i].size() - 1; j++) { for (int k = j + 1; k <
		 * cluster[i].size(); k++) { int nodeIdA = cluster[i].get(j); int
		 * nodeIdB = cluster[i].get(k); kk += g.gOut[nodeIdA].length *
		 * g.gOut[nodeIdB].length; } } } kk *= 2; for (int i = 0; i <
		 * votes.length; i++) { kk += g.gOut[i].length * g.gOut[i].length; for
		 * (int neighbour : g.gOut[i]) { if (votes[neighbour].color ==
		 * votes[i].color) { aa++; } } } q = (aa - (kk * 1.0) / g.nedges) /
		 * g.nedges; System.out.println("modularity q=" + q);
		 */
		// modularity version 2
		double q2 = 0;
		for (int i = 0; i < g.nnodes; i++) {
			for (int j = 0; j < g.nnodes; j++) {
				if (votes[i].color == votes[j].color) {
					q2 -= (g.gOut[i].length * g.gOut[j].length)
							/ (1.0 * g.nedges);
					if (g.hasEdge(i, j)) {
						q2 += 1;
					}
				}
			}
		}
		q2 /= g.nedges;
		System.out.println("modularity q2=" + q2);

		/*
		 * for (int i = 0; i < g.nnodes; i++) { if (g.has_edge(i, i)) {
		 * System.out.println("wtf"); } }
		 * 
		 * org.graphstream.graph.Graph g2 = new DefaultGraph("adsf");
		 * 
		 * for (int i = 0; i < g.nnodes; i++) { g2.addNode(i + ""); g2.getNode(i
		 * + "").addAttribute("color", votes[i].color); } for (int i = 0; i <
		 * g.nnodes; i++) { for (int j = 0; j < g.gOut[i].length; j++) {
		 * g2.addEdge(i + " " + g.gOut[i][j], i + "", g.gOut[i][j] + ""); } }
		 * Modularity m = new Modularity("color"); m.init(g2); m.compute();
		 * System.out.println(m.getModularity());
		 */
	}

	public void saveCommunityResultsToFile(String filename) throws IOException {
		BufferedWriter w = new BufferedWriter(new FileWriter(filename));
		int maxlevel = 0;
		for (int i = 0; i < votes.length; i++) {
			maxlevel = Math.max(maxlevel, votes[i].level);
		}
		w.write(g.nnodes + " " + nclusters + " " + maxlevel);
		w.newLine();
		for (ExpandingNode expNode : votes) {
			// shape map
			// regular = 0
			// root = 1
			char shape = 'o';
			if (expNode.level == 0) {
				shape = 's';
			}
			int tmpid = -1;
			if(g.ids != null) {
				tmpid = g.ids[expNode.nodeid];
			} else {
				tmpid = expNode.nodeid;
			}
			w.write(tmpid + " " + expNode.color + " "
					+ expNode.score + " " + shape + " " + expNode.level);
			w.newLine();
		}
		w.write(g.nedges + "");
		w.newLine();
		for (int i = 0; i < g.gOut.length; i++) {
			for (int j = 0; j < g.gOut[i].length; j++) {
				w.write(g.ids[i] + " " + g.ids[g.gOut[i][j]]);
				w.newLine();
			}
		}
		for (int i = 0; i < votes.length; i++) {
			if (votes[i].level == 0) {
				for (ExpandingNode child : votes[i].children) {
					if (child.level == 0) {
						w.write(g.ids[votes[i].nodeid] + " "
								+ g.ids[child.nodeid]);
						w.newLine();
					}
				}
			} else {
				w.write(g.ids[votes[i].nodeid] + " "
						+ g.ids[votes[i].finalFather.nodeid]);
				w.newLine();
			}
		}
		w.close();
	}

	private static int colorWithPrioQueue(ExpandingNode[] votes) {
		long start = System.currentTimeMillis();
		int colorIndex = 0;
		PriorityQueue<ExpandingNode> q = new PriorityQueue<ExpandingNode>();
		for (int i = 0; i < votes.length; i++) {
			q.add(votes[i]);
		}
		long end = System.currentTimeMillis() - start;
		System.out.println("2nd pass (sorting/queue filling):" + end + "ms");
		start = System.currentTimeMillis();
		while (!q.isEmpty()) {
			ExpandingNode node = q.poll();
			if (node.color == -1) {
				node.color = colorIndex++;
				node.level = 0;
				node.fatherscore = node.score;
				node.finalFather = node;
			}
			// color the children
			for (ExpandingNode childNode : node.children) {
				if (childNode.color == -1) {
					childNode.color = node.color;
					childNode.fatherscore = node.score;
					childNode.finalFather = node;
					if (node.level == 0 && childNode.score == node.score) {
						childNode.level = node.level;
						childNode.finalFather = childNode;
					} else {
						childNode.level = node.level + 1;
					}
					// re-balance the queue
					q.remove(childNode);
					q.add(childNode);
				}
			}
		}
		end = System.currentTimeMillis() - start;
		System.out.println("2nd pass (coloring):" + end + "ms");
		return colorIndex;
	}

	// each node can see only the lists of friends of his immediate friends and
	// nothing else (snapshot of local subgraph of depth 2)
	public void voteForInfluentialNode(int node, Graph g, ExpandingNode[] votes) {
		int[] intersection = new int[g.gOut[node].length];
		int maxIntersect = -1;
		double tmpClusterCoeff = 0;
		for (int i = 0; i < g.gOut[node].length; i++) {
			// the ith neighbor of the central node
			int neighbour = g.gOut[node][i];
			intersection[i] = g.nCommonLinks(g.gOut[node], g.gIn[neighbour]);
			tmpClusterCoeff += intersection[i];
			if (intersection[i] > maxIntersect) {
				maxIntersect = intersection[i];
			}
		}
		if (g.gOut[node].length >= 2) {
			tmpClusterCoeff /=(g.gOut[node].length * (g.gOut[node].length - 1));
			localClustering += tmpClusterCoeff;
		}
		for (int i = 0; i < intersection.length; i++) {
			if (intersection[i] == maxIntersect) {
				int fatherid = g.gOut[node][i];
				votes[fatherid].score++;
				votes[fatherid].children.add(votes[node]);
				votes[node].fathers.add(votes[fatherid]);
			}
		}
	}
}
