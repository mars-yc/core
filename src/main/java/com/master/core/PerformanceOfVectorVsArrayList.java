package com.master.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * -Xms3072m -Xmx3072m
 * <p>
 *     Since the time cost to retrieve or release the lock is less than
 *     the time cost for Arrays.copyOf operation. Thus we can see, if you
 *     don't initialize the length for ArrayList/Vector, ArrayList will need
 *     more time as it increase half of the previous length each time. (int newCapacity = oldCapacity + (oldCapacity >> 1);)
 *     <br>
 *     But, if you specified the length to be large enough for the Vector and ArrayList
 *     then you'll see the Vector need much more time as it to retrieve and release lock
 * </p>
 */
public class PerformanceOfVectorVsArrayList {

    private static final int MAX_INTEGER_COUNT = 100000000;
//    private static final int MAX_INTEGER_COUNT = 10000000;

    public static void main(String[] args) {
        {
            List<Integer> list = new Vector<>(MAX_INTEGER_COUNT);
//            List<Integer> list = new Vector<>();
            addIntegers(list, MAX_INTEGER_COUNT);
        }
        {
            List<Integer> list = new ArrayList<>(MAX_INTEGER_COUNT);
//            List<Integer> list = new ArrayList<>();
            addIntegers(list, MAX_INTEGER_COUNT);
        }
    }

    public static void addIntegers(List<Integer> list, int count) {
        long startTime = System.currentTimeMillis();
        if(null == list || count <= 0)
            throw new IllegalArgumentException();
        while(count-- > 0) {
            list.add(count);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Time cost: " + (endTime - startTime));
    }

}