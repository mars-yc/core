package com.master.core.thread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ThreadGroupDemo implements Runnable {
    //List<String> list = new ArrayList<String>(1);
    List<String> list = Collections.synchronizedList(new ArrayList<String>());
    public void run() {
        try {
            Thread.sleep((int) (Math.random() * 2));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName());
        list.add(Thread.currentThread().getName());
    }

    public static void main(String[] args) throws InterruptedException {
        ThreadGroup group = new ThreadGroup("hongtenGroup");
        ThreadGroupDemo t = new ThreadGroupDemo();
        for (int i = 0; i < 10000; i++) {
            Thread th = new Thread(group, t, String.valueOf(i));
            th.start();
        }
        while (group.activeCount() > 0) {
            Thread.sleep(10);
        }
        System.out.println("result=============");
        System.out.println(t.list.size()); 
    }

}