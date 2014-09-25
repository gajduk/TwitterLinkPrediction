package mk.edu.manu.cs.algorithm;


import java.util.Arrays;

public class IntList {
	private int[] list = new int[10];
	int index = 0;

	public void add(int i) {
		list[index] = i;
		index++;
		if (index == list.length) {
			list = Arrays.copyOf(list, list.length * 2);
		}
	}

	public int[] getIntArray() {
		return Arrays.copyOf(list, index);
	}
	
	public void print() {
		for (int i = 0; i < index; i++) {
			System.out.print(list[i] + " ");
		}
		System.out.println();
	}
	
	public static void main(String[] args) {
		IntList list = new IntList();
		for (int i = 0; i < 26; i++) {
			list.add(i);
		}
		list.print();
		int[] b = list.getIntArray();
		for (int i = 0; i < b.length; i++) {
			System.out.print(b[i] + " ");
		}
		System.out.println();
	}
}
