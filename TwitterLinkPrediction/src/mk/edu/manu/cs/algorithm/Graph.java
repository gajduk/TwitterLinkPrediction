package mk.edu.manu.cs.algorithm;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;



/*
 * This is a directed (un)weighted (un)labeled graph
 */

public class Graph {
	public int nnodes = 0;
	public int nedges = 0;

	// these structures are full when the graph is normalized
	// the neighbors lists should always be sorted
	public int[][] gOut = null;
	public int[][] gIn = null;
	public int[] ids = null; // the original (unnormalized) ids
	public String[] labels = null; // node labels
	// ////////////////////////////////////////////////

	// these structures are full when the graph is unnormalized
	private HashMap<Integer, HashSet<Integer>> matrix = null;
	private HashMap<Integer, String> labelMap = null;
	private HashMap<Integer, Integer> valueMap = null;
	// ///////////////////////////////////////////////////////
	
	private Graph(int nnodes, int nedges, int[][] gOut, int[] ids,
			String[] labels) {
		this.nnodes = nnodes;
		this.nedges = nedges;
		this.gOut = gOut;
		this.ids = ids;
		this.labels = labels;
		this.gIn = makeInverseNodeList(gOut);
	}

	public Graph() {
		matrix = new HashMap<Integer, HashSet<Integer>>();
		labelMap = new HashMap<Integer, String>();
		valueMap = new HashMap<Integer, Integer>();
	}

	public void addNode(int i, String label, int value) {
		if (!labelMap.containsKey(i)) {
			labelMap.put(i, label);
			valueMap.put(i, value);
			nnodes++;
			matrix.put(i, new HashSet<Integer>());
		}
	}

	public Graph toNormalizedGraph() {
		// if it's already normalized
		if (matrix == null) {
			return null;
		}

		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		int node = 0;
		int[] ids = new int[nnodes];
		String[] labels = new String[nnodes];
		for (Integer key : matrix.keySet()) {
			ids[node] = key;
			labels[node] = labelMap.get(key);
			map.put(key, node++);
		}
		int[][] gOut = new int[nnodes][];
		for (Integer key : matrix.keySet()) {
			HashSet<Integer> links = matrix.get(key);
			int nodeA = map.get(key);
			int index = 0;
			gOut[nodeA] = new int[links.size()];
			for (Integer link : links) {
				gOut[nodeA][index++] = map.get(link);
			}
		}
		Graph.sortNeighbourList(gOut);
		return new Graph(nnodes, nedges, gOut, ids, labels);
	}

	public void addEdge(int i, int j) {
		this.addNode(i, "none", 0);
		this.addNode(j, "none", 0);
		if (!matrix.get(i).contains(j)) {
			matrix.get(i).add(j);
			nedges++;
		}
	}

	public static void sortNeighbourList(int[][] gOut) {
		for (int i = 0; i < gOut.length; i++) {
			// Arrays.sort(gIn[i]); no need to sort gIn - it's always sorted
			Arrays.sort(gOut[i]);
		}
	}

	public boolean hasEdge(int i, int j) {
		if (i < nnodes && j < nnodes) {
			if (gOut[i].length > gIn[j].length) {
				return binary_search(i, gIn[j]) != -1;
			} else {
				return binary_search(j, gOut[i]) != -1;
			}
		} else {
			System.err.println("node id's exceed limit");
			return false;
		}
	}

	public static Graph readGraphGML(String filename, boolean makeSymmetric)
			throws IOException {
		BufferedReader r = new BufferedReader(new FileReader(filename));
		String line = null;
		Graph g = new Graph();

		int id = -1;
		String label = "none";
		int value = 0;
		int source = 0;
		int target = 0;
		// whereami tells in which section we are in; node=0;edge=1
		int whereami = -1;
		while ((line = r.readLine()) != null) {
			line = line.trim();
			if (line.startsWith("]")) {
				if (whereami == 0) {
					whereami = -1;
					g.addNode(id, label, value);
				} else if (whereami == 1) {
					whereami = -1;
					g.addEdge(source, target);
					if (makeSymmetric) {
						g.addEdge(target, source);
					}
				}
			} else if (line.startsWith("node")) {
				whereami = 0;
			} else if (line.startsWith("edge")) {
				whereami = 1;
			} else if (line.startsWith("label")) {
				label = line.split("[ \t]+")[1].replace("\"", "");
			} else if (line.startsWith("id")) {
				id = Integer.parseInt(line.split("[ \t]+")[1]);
			} else if (line.startsWith("source")) {
				source = Integer.parseInt(line.split("[ \t]+")[1]);
			} else if (line.startsWith("target")) {
				target = Integer.parseInt(line.split("[ \t]+")[1]);
			} else if (line.startsWith("value")) {
				value = Integer.parseInt(line.split("[ \t]+")[1]);
			}
		}
		System.out.println("nodes/edges: " + g.nnodes + "/" + g.nedges);
		return g.toNormalizedGraph();

	}

	public int[] commonLinks(int[] a, int[] b) {
		IntList result = new IntList();
		int i = 0, j = 0;
		while (i < a.length && j < b.length) {
			if (a[i] == b[j]) {
				result.add(a[i]);
				i++;
				j++;
			} else if (a[i] > b[j]) {
				j++;
			} else {
				i++;
			}
		}
		return result.getIntArray();
	}

	public int nCommonLinks(int[] a, int[] b) {
		int i = 0, j = 0;
		int count = 0;
		while (i < a.length && j < b.length) {
			if (a[i] == b[j]) {
				count++;
				i++;
				j++;
			} else if (a[i] > b[j]) {
				j++;
			} else {
				i++;
			}
		}
		return count;
	}

	private int binary_search(int key, int[] a) {
		int lo = 0;
		int hi = a.length - 1;
		while (lo <= hi) {
			int mid = lo + (hi - lo) / 2;
			if (a[mid] == key) {
				return mid;
			} else if (a[mid] < key) {
				lo = mid + 1;
			} else {
				hi = mid - 1;
			}
		}
		return -1;
		// target was not found
	}

	public static Graph readGraphFRF(String filename) throws IOException {
		if (!filename.endsWith(".frf")) {
			throw new IOException(
					"File extension doesn't match format (need .frf)");
		}
		BufferedReader r = new BufferedReader(new FileReader(filename));
		String line = r.readLine();
		// read no. of nodes
		int nNodes = Integer.parseInt(line);
		int nEdges = 0;

		int[][] gOut = new int[nNodes][];

		int[] ids = new int[nNodes];
		String[] labels = new String[nNodes];
		// read all the edges
		int a = 0;
		double oneProcLines = nNodes / 100.0;
		int prevProc = 0;
		while ((line = r.readLine()) != null) {
			int tmp = (int) Math.floor(a / oneProcLines);
			if (tmp > prevProc) {
				prevProc = tmp;
				System.out.println(prevProc + "%");
			}

			line = line.trim();
			String[] ss = line.split("[ \t]+");
			if (ss.length < 2) {
				throw new IOException("Line " + (a + 1)
						+ " has less than two components");
			}
			gOut[a] = new int[ss.length - 2];
			nEdges += (ss.length - 2);
			ids[a] = Integer.parseInt(ss[0]);
			labels[a] = ss[1];
			for (int i = 2; i < ss.length; i++) {
				gOut[a][i - 2] = Integer.parseInt(ss[i]);
			}
			a++;
		}
		System.out.println("no. of nodes:" + nNodes);
		System.out.println("no. of edges:" + nEdges);
		return new Graph(nNodes, nEdges, gOut, ids, labels);
	}

	public void serializeGraphInFRF(String filename) throws IOException {
		BufferedWriter w = new BufferedWriter(new FileWriter(filename));
		w.write(this.nnodes + "");
		w.newLine();
		for (int i = 0; i < this.nnodes; i++) {
			if (this.ids != null) {
				w.write(this.ids[i] + " ");
			} else {
				w.write(i + " ");
			}
			if (this.labels != null) {
				w.write(this.labels[i]);
			} else {
				w.write("none");
			}
			for (int j = 0; j < this.gOut[i].length; j++) {
				w.write(" " + this.gOut[i][j]);
			}
			w.newLine();
		}
		w.close();
	}

	public static int[][] makeInverseNodeList(int[][] g) {
		// make the inverse of g, i.e. make the list by in-links instead of
		// out-links
		int[][] gIn = new int[g.length][];
		int[] inLinks = new int[g.length];
		for (int i = 0; i < g.length; i++) {
			for (int j = 0; j < g[i].length; j++) {
				inLinks[g[i][j]]++;
			}
		}
		for (int i = 0; i < gIn.length; i++) {
			gIn[i] = new int[inLinks[i]];
		}
		int[] indexes = new int[g.length];
		for (int i = 0; i < g.length; i++) {
			for (int j = 0; j < g[i].length; j++) {
				int a = i;
				int b = g[i][j];
				gIn[b][indexes[b]++] = a;
			}
		}
		return gIn;
	}

	public static Graph readGraphEdgeList(String filename,
			boolean makeSymmetric, int normalizeShiftConstant)
			throws IOException {
		BufferedReader r = new BufferedReader(new FileReader(filename));
		String line = "";
		do {
			line = r.readLine();
			line.trim();
		} while (line.equals("") || line.startsWith("#"));

		// read no. of edges
		int nEdges = 100;
		if (line.split("[ \t]+").length == 1) {
			nEdges = Integer.parseInt(line);
			System.out.println("no. of edges:" + nEdges);
		} else {
			System.out.println("no. of edges: unknown");
		}

		if (normalizeShiftConstant == Integer.MAX_VALUE) {
			Graph g = new Graph();

			int lineno = 0;
			double oneProcEdges = nEdges / 100.0;
			int prevProc = 0;
			// read all the edges
			while ((line = r.readLine()) != null) {
				lineno++;
				int tmp = (int) Math.floor(lineno / oneProcEdges);
				if (tmp > prevProc) {
					prevProc = tmp;
					System.out.println(prevProc + "%");
				}
				line = line.trim();
				if (line.startsWith("#") || line.equals("")) {
					continue;
				}
				String[] ss = line.split("[ \t]+");
				int a = Integer.parseInt(ss[0]);
				int b = Integer.parseInt(ss[1]);
				g.addEdge(a, b);
				if (makeSymmetric) {
					g.addEdge(b, a);
				}
			}
			return g.toNormalizedGraph();
		} else {
			int nedges = 0;
			List<List<Integer>> list = new LinkedList<List<Integer>>();
			while ((line = r.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("#") || line.equals("")) {
					continue;
				}
				String[] ss = line.split("[ \t]+");
				int a = Integer.parseInt(ss[0]) - normalizeShiftConstant;
				int b = Integer.parseInt(ss[1]) - normalizeShiftConstant;
				while (a > list.size() - 1 || b > list.size() - 1) {
					list.add(new LinkedList<Integer>());
				}
				if (!list.get(a).contains(b)) {
					list.get(a).add(b);
					nedges++;
				}
				if (makeSymmetric) {
					if (!list.get(b).contains(a)) {
						list.get(b).add(a);
						nedges++;
					}
				}
			}
			System.out.println("no. of nodes after parsing:" + list.size());
			int[][] gOut = new int[list.size()][];
			for (int i = 0; i < gOut.length; i++) {
				gOut[i] = new int[list.get(i).size()];
				for (int j = 0; j < gOut[i].length; j++) {
					gOut[i][j] = list.get(i).get(j);
				}
			}
			Graph.sortNeighbourList(gOut);
			return new Graph(gOut.length, nedges, gOut, null, null);
		}
	}
}
