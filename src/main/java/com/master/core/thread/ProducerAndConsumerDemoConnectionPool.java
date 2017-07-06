package com.master.core.thread;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * mock up the case of one producer and multi consumer
 *
 */
public class ProducerAndConsumerDemoConnectionPool {

	private static final int POOL_MAX_SIZE = 50;
	private static final int POOL_MIN_SIZE = 30;
	private static final int POOL_INIT_SIZE = 10;
	private static final int POOL_CONNECTION_INCREMENT = 10;
	private static final int PRODUCER_MONITOR_INTERVAL = 1500; //millseconds
	private static final int COUNT_OF_COMSUMERS = 1000;

	public static void main(String[] args) {
		
		try {
			System.setOut(new PrintStream(new FileOutputStream("ConnectionPoolDemo-logs.txt"))); //redirect the standard out to be log file
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		long startTime = System.currentTimeMillis();
		
		ConnectionPool pool = ConnectionPool.getInstance(POOL_INIT_SIZE, POOL_MIN_SIZE, POOL_MAX_SIZE, POOL_CONNECTION_INCREMENT);
		Producer producer = new Producer(pool, PRODUCER_MONITOR_INTERVAL);
		producer.start();

		ExecutorService executor = Executors.newCachedThreadPool();
		List<FutureTask<String>> futureTasks = new ArrayList<>();

		for (int i = 1; i <= COUNT_OF_COMSUMERS; i++) {
			ConsumerTask task = new ConsumerTask(pool);
			FutureTask<String> futureTask = new FutureTask<String>(task);
			futureTasks.add(futureTask);
			executor.submit(futureTask);
		}
		executor.shutdown();
		for (FutureTask<String> futureTask : futureTasks) {
			try {
				System.out.println(
						"finished process: " + futureTask.get() + " - [" + Thread.currentThread().getName() + "]");
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		producer.exit = true;

		long endTime = System.currentTimeMillis();
		System.out.println("Time cost to process the request for all consumers: " + (endTime - startTime) / 1000);

	}

}

class Producer extends Thread {

	private ConnectionPool pool;
	private long monitorInterval;
	public volatile boolean exit = false;

	public Producer(ConnectionPool pool, long monitorInterval) {
		this.monitorInterval = monitorInterval;
		this.pool = pool;
	}

	@Override
	public void run() {
		while (!exit) {
			System.out.println("Producer is checking the pool capacity");
			if (pool.getIdleConnectionNumber() == 0) {
				try {
					pool.tryCreatingConnection();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(monitorInterval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Producer die");
	}

}

class ConsumerTask implements Callable<String> {

	private ConnectionPool pool;

	public ConsumerTask(ConnectionPool pool) {
		this.pool = pool;
	}

	@Override
	public String call() throws Exception {
		Connection connection = null;
		try {
			connection = this.pool.getConnection();
			connection.process();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			pool.releaseConnection(connection);
		}
		return Thread.currentThread().getName();
	}

}

class ConnectionPool {

	private static ConnectionPool instance;
	private List<Connection> idleConnections = new ArrayList<Connection>();
	private List<Connection> activeConnections = new ArrayList<Connection>();
	private final int poolMaximumSize;
	private final int poolMinimumSize;
	private final int poolInitSize;
	private final int poolIncrement;
	private static final Lock lock = new Lock();

	static class Lock {
	}

	private ConnectionPool(int poolInitSize, int poolMinimumSize, int poolMaximumSize, int poolIncrement) {
		this.poolInitSize = poolInitSize;
		this.poolMinimumSize = poolMinimumSize;
		this.poolMaximumSize = poolMaximumSize;
		this.poolIncrement = poolIncrement;
		System.out.println("initialize connection pool");
		for (int i = 0; i < this.poolInitSize; i++) {
			idleConnections.add(new Connection());
		}
	}

	public int getIdleConnectionNumber() {
		synchronized (lock) {
			return idleConnections.size();
		}
	}

	public static ConnectionPool getInstance(int poolInitSize, int poolMinimumSize, int poolMaximumSize, int poolIncrement) {
		if(instance == null) {
			synchronized(lock) {
				if(instance == null) {
					instance = new ConnectionPool(poolInitSize, poolMinimumSize, poolMaximumSize, poolIncrement);
				}
			}
		}
		return instance;
	}

	public void releaseConnection(Connection connection) {
		synchronized (lock) {
			if (idleConnections.size() + activeConnections.size() > this.poolMinimumSize) {
				this.closeConnection(connection);
				return;
			}
			activeConnections.remove(connection);
			idleConnections.add(connection);
			System.out.println(
					"release connection to pool: " + connection + " - [" + Thread.currentThread().getName() + "]");
			lock.notifyAll();
		}
	}

	public Connection getConnection() throws InterruptedException {
		synchronized (lock) {
			while (idleConnections.isEmpty()) {
				System.out.println("pool is empty, waiting for creating connections" + " - ["
						+ Thread.currentThread().getName() + "]");
				lock.wait();
			}
			Connection connection = idleConnections.remove(0);
			activeConnections.add(connection);
			System.out.println(
					"getting connection from pool: " + connection + " - [" + Thread.currentThread().getName() + "]");
			return connection;
		}
	}

	/**
	 * try to create connection instead of creating connection each time.<br>
	 * take into consideration of the case when many threads blocked in below
	 * lock.wait and not even get notified before all the consumer threads
	 * finish
	 */
	public void tryCreatingConnection() throws InterruptedException {
		Connection connection = null;
		synchronized (lock) {
			while (activeConnections.size() >= this.poolMaximumSize && idleConnections.size() == 0) {
				System.out.println("Pool execess the maximum number of connections currently. - "
						+ (idleConnections.size() + activeConnections.size()));
				lock.wait();
			}
			if (idleConnections.size() == 0) {
				for(int i=0; i< poolIncrement && !isPoolFilledUp(); i++) {
					connection = new Connection();
					idleConnections.add(connection);
				}
				lock.notifyAll();
			}
		}
	}

	private boolean isPoolFilledUp() {
		return activeConnections.size() + idleConnections.size() >= this.poolMaximumSize;
	}

	/**
	 * No need to synchronize this method since one connection should always be hold by one thread. 
	 */
	public void closeConnection(Connection connection) {
		activeConnections.remove(connection);
		System.out.println("closed connection: " + connection + " - [" + Thread.currentThread().getName() + "]");
		lock.notifyAll();
	}

}

class Connection {

	public Connection() {
		System.out.println("created connection: " + this + " - [" + Thread.currentThread().getName() + "]");
	}

	/**
	 * mockup transactions here
	 */
	public void process() {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}