package com.master.core.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 *  -Xms3072m -Xmx3072m
 */
public class SpeedOfVectorVsArrayList {
	
	private static final int INTEGER_NUMBERS = 100000000;
	
	public static void main(String[] args) {
		{
			Vector<Integer> vector = new Vector<>(INTEGER_NUMBERS);
			addElements(vector, INTEGER_NUMBERS);
		}
		{
			List<Integer> list = new ArrayList<>(INTEGER_NUMBERS);
			addElements(list, INTEGER_NUMBERS);
		}
	}
	
	public static void addElements(List<Integer> list, int amount) {
		long start = System.currentTimeMillis();
		if(null == list || amount <= 0)
			throw new IllegalArgumentException();
		while(amount-- > 0) {
			list.add(amount);
		}
		long end = System.currentTimeMillis();
		System.out.println("Time cost: " + (end - start));
	}
	
}