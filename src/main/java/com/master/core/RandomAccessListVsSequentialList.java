package com.master.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * As LinkedList doesn't implement the RandomAccess interface. It
 * will cost more time when you use list.get(i) method
 * <br><br>
 * But not yet get the reason when use iterator by sequential visiting, the
 * time cost for ArrayList and LinkedList don't have much difference
 */
public class RandomAccessListVsSequentialList {

    private static final int MAX_INTEGER_COUNT = 100000;
    private static final List<Integer> array;
    private static final List<Integer> link;

    public static void main(String[] args) {
        randomAccess(array);
        randomAccess(link);
        sequentialAccess(array);
        sequentialAccess(link);
    }

    static {
        array = new ArrayList<>();
        link = new LinkedList<>();
        addIntegers(array, MAX_INTEGER_COUNT);
        addIntegers(link, MAX_INTEGER_COUNT);
    }

    static public <T> void randomAccess(final List<T> list) {
        execute(new Action() {
            @Override
            public void doInAction() {
                int size = list.size();
                for(int i=0; i<size; i++) {
                    list.get(i);
                }
            }
        });
    }

    public static <T> void sequentialAccess(final List<T> list) {
        execute(new Action() {
            @Override
            public void doInAction() {
//                for(Iterator<T> it = list.iterator(); it.hasNext(); it.next());
                for(Iterator<T> it = list.iterator(); it.hasNext(); ) {
                    it.next();
                }
            }
        });
    }

    private static void execute(Action action) {
        long start = System.currentTimeMillis();
        action.doInAction();
        long end = System.currentTimeMillis();
        System.out.println("time cost: " + (end - start));
    }

    public static void addIntegers(List<Integer> list, int count) {
        if(null == list || count <= 0)
            throw new IllegalArgumentException();
        while(count-- > 0) {
            list.add(count);
        }
    }

    interface Action {
        void doInAction();
    }

}