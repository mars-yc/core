package com.master.core.cas;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

public class CASDemo {
	
	private volatile static int number;
	private static AtomicInteger atomic = new AtomicInteger();
	private static final int MAX_NUMBER_OF_THREADS = 1000000;
	
	public static void main(String[] args) {
		demoUnAtomicOperation();
//		demoAtomicOperation();
	}
	
	/**
	 * Even if the field number has identifier of volatile,
	 * the result won't be eqauals to MAX_NUMBER_OF_THREADS as operation '++' is not atomic
	 */
	public static void demoUnAtomicOperation() {
		ThreadGroup group = new ThreadGroup("unAtomicGroup");
		Thread t = new IncrementThread();
		for(int i=0; i<MAX_NUMBER_OF_THREADS; i++) {
			Thread th = new Thread(group, t, String.valueOf(i));
			th.start();
		}
        while (group.activeCount() > 0) {
            try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
        System.out.println(number);
	}
	
	static class IncrementThread extends Thread {
		
		@Override
		public void run() {
			number++;
		}
		
	}
	
	
	public static void demoAtomicOperation() {
		ExecutorService es = Executors.newCachedThreadPool();
		List<FutureTask<Void>> list = new ArrayList<>();
		for(int i=0; i< MAX_NUMBER_OF_THREADS; i++) {
			AtomicIncrementTask task = new AtomicIncrementTask();
			FutureTask<Void> futureTask = new FutureTask<Void>(task);
			es.submit(futureTask);
			list.add(futureTask);
		}
		es.shutdown();
		for(FutureTask<Void> futureTask : list) {
			try {
				futureTask.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		System.out.println(atomic.get());
	}
	
	static class AtomicIncrementTask implements Callable<Void> {
		
		@Override
		public Void call() {
			atomic.incrementAndGet();
			return null;
		}
		
	}

}