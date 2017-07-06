package com.master.core.thread;

import java.util.List;
import java.util.Vector;

/**
 * 偏向锁
 *
 */
public class BiasedLockingDemo {
	
	public static void main(String[] args) {
		testBiasedLocking();
	}
	
	/**
	 * takes ~4 seconds with below VM arguments
	 * -XX:+UseBiasedLocking -XX:BiasedLockingStartupDelay=0 -client -Xms3072m -Xmx3072m
	 * 
	 * takes ~ 7 secons with below VM arguments as java will use Biased Locking by default since jdk 1.6, but it will starts
	 * a few seconds after the application startup
	 * -XX:+UseBiasedLocking -client -Xms3072m -Xmx3072m
	 * 
	 * takes ~8 seconds with below VM arguments
	 * -XX:-UseBiasedLocking -client -Xms3072m -Xmx3072m
	 * 
	 */
	public static void testBiasedLocking() {
		List<Integer> list = new Vector<Integer>();
		long start = System.currentTimeMillis();
		int count = 0;
		for(int i=0; i< 100; i++) {
			while(count++ <= 100000000) {
				list.add(count);
				//System.out.println("added " + count);
			}
		}
		long end = System.currentTimeMillis();
		System.out.println((end - start)/1000);
	}
	
}