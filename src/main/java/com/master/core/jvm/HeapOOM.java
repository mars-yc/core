package com.master.core.jvm;

import java.util.ArrayList;
import java.util.List;

/**
 * -Xms2m -Xmx2m -XX:+HeapDumpOnOutOfMemoryError
 */
public class HeapOOM {

    static class OOMObject {}

    public static void main(String[] args) {
        List<OOMObject> list = new ArrayList<>();
        int i = 1;
        while(true) {
            System.out.println("creating OOMObject: " + i++);
            list.add(new OOMObject());
        }
    }

}
