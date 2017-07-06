package com.master.core;

/**
 * 
 * The difference of java.util.Date and java.sql.Date
 * <br>
 * How to convert java.util.Date to java.sql.Date and vice verse
 */
public class SqlAndUtilDate {
	
	public static void main(String[] args) {
		test();
	}

	@SuppressWarnings("deprecation")
	private static void test() {
		java.util.Date utilDate = new java.util.Date();
		System.out.println(utilDate);
		java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
		System.out.println(sqlDate);
		System.out.printf("Time :  %s:%s:%s", sqlDate.getHours(), sqlDate.getMinutes(), sqlDate.getSeconds()); //Exception throws there
	}

}