package com.master.core.classloader;

import java.net.URL;

/**
 *
 * BootstrapClassloader  ExtClassLoader  AppClassLoader  
 *
 */
public class ClassLoaderDemo {
	
	public static void main(String[] args) {
		checkExtClassloader();
		checkClassPath();
		checkClassloaders();
	}
	
	@SuppressWarnings("restriction")
	public static void checkBootstrapClassloader() {
		URL[] urls = sun.misc.Launcher.getBootstrapClassPath().getURLs();    
        for (int i = 0; i < urls.length; i++) {    
            System.out.println(urls[i].toExternalForm());    
        }   
	}
	
	public static void checkExtClassloader() {
		System.out.println(System.getProperty("java.ext.dirs"));
	}
	
	public static void checkClassPath() {
		System.out.println(System.getProperty("java.class.path"));
	}
	
	/**
	 * can see ExtClassLoader doesn't have parent
	 */
	public static void checkClassloaders() {
		System.out.println(ClassLoaderDemo.class.getClassLoader()); //sun.misc.Launcher$AppClassLoader@36422510
		System.out.println(ClassLoaderDemo.class.getClassLoader().getParent()); //sun.misc.Launcher$ExtClassLoader@308f5944
		System.out.println(ClassLoaderDemo.class.getClassLoader().getParent().getParent());  //null
	}

}