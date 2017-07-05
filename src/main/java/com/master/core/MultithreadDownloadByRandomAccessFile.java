package com.master.core;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Mock up the case of downloading file with multi thread
 */
public class MultithreadDownloadByRandomAccessFile {

    private static final String FILE_TO_READ = "G:/test.mkv";
    private static final String FILE_TO_WRITE = "H:/copy_of_test.mkv";
//    private static final String FILE_TO_READ = "G:/test2.rmvb";
//    private static final String FILE_TO_WRITE = "H:/copy_of_test2.rmvb";

    public static void main(String[] args) {
//        singleThreadDownloading(FILE_TO_READ, FILE_TO_WRITE); //~70 seconds
        multiThreadDownloading(FILE_TO_READ, FILE_TO_WRITE, 5, 1024*1024*10);
    }

    public static void singleThreadDownloading(final String from, final String to) {
        execute(new Action() {
            @Override
            public void doInAction() {
                InputStream is = null;
                OutputStream os = null;
                try {
                    File target = new File(to);
                    if(target.exists()) {
                        target.delete();
                    }
                    is = new BufferedInputStream(new FileInputStream(from));
                    os = new BufferedOutputStream(new FileOutputStream(target));
                    byte[] buffer = new byte[1024];
                    while(is.read(buffer) > 0) {
                        os.write(buffer);
                    }
                    System.out.println("Finished copying file FILE_TO_READ " + from + " FILE_TO_WRITE " + to);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if(null != is)
                            is.close();
                        if(null != os)
                            os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void multiThreadDownloading(String from, String to, int numberOfThreads, int bufferSize) {
        long start = System.currentTimeMillis();
        List<Part> partList = downLoading(from, to, numberOfThreads, bufferSize);
        long end = System.currentTimeMillis();
        System.out.println("Total time cost for downloading without merging: " + (end - start));
        for(Part part : partList) {
            mergeToTarget(to, part, bufferSize);
            deleteFile(part.getPath());
        }
    }

    private static List<Part> downLoading(String from, String to, int numberOfThreads, int bufferSize) {

        List<Part> partList = new ArrayList<>();

        File fromFile = new File(from);
        if(!fromFile.exists()) {
            throw new IllegalArgumentException("File not found: " + from);
        }

        File toFile = new File(to);
        if(toFile.exists()) {
            toFile.delete();
        }

        List<FutureTask<Part>> taskList = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        File file = new File(from);
        long length = file.length();
        int lengthPerThread = (int) Math.ceil((double) length/numberOfThreads);
        long startPointer = 0;
        int threadNo = 1;
        while(length - startPointer > 0) {
            int lengthToRead = (int) (startPointer + lengthPerThread > length ? length - startPointer : lengthPerThread);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Part part = new Part(to + ".part" + threadNo++, startPointer, lengthToRead);
            FutureTask<Part> futureTask = new FutureTask<>(new RandomReadAndWriteTask(from, bufferSize, part));
            taskList.add(futureTask);
            executorService.submit(futureTask);
            startPointer += lengthPerThread;
        }

        for(FutureTask<Part> futureTask : taskList) {
            try {
                Part part = futureTask.get();
                partList.add(part);
                System.out.println("Finished part: " + part.getPath());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return partList;
    }

    private static void deleteFile(String path) {
        File file = new File(path);
        if(!file.exists())
            throw new IllegalArgumentException("File not exists: " + path);
        file.delete();
        System.out.println(path + " deleted.");
    }

    private static void mergeToTarget(String target, Part part, int bufferSize) {
        RandomAccessFile readRaf = null;
        RandomAccessFile writeRaf = null;
        try {
            readRaf = new RandomAccessFile(part.getPath(), "r");
            writeRaf = new RandomAccessFile(target, "rw");

            byte[] buffer = new byte[bufferSize];
            long startPointer = part.getStartPointer();
            int lengthToRead;
            while((lengthToRead = readRaf.read(buffer)) > 0) {
                writeRaf.seek(startPointer);
                writeRaf.write(buffer, 0, lengthToRead);
                startPointer += lengthToRead;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(null != readRaf)
                    readRaf.close();
                if(null != writeRaf)
                    writeRaf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void execute(Action action) {
        long start = System.currentTimeMillis();
        action.doInAction();
        long end = System.currentTimeMillis();
        System.out.println("Time cost: " + (end - start));
    }

    static class Part {

        private String path;
        private long startPointer;
        private int length;

        public Part(String path, long startPointer, int length) {
            this.path = path;
            this.startPointer = startPointer;
            this.length = length;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public long getStartPointer() {
            return startPointer;
        }

        public void setStartPointer(long startPointer) {
            this.startPointer = startPointer;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }
    }

    static class RandomReadAndWriteTask implements Callable<Part> {

        private String from;
        private int bufferSize;
        private Part part;

        public RandomReadAndWriteTask(String from, int bufferSize, Part part) {
            this.from = from;
            this.bufferSize = bufferSize;
            this.part = part;
        }

        @Override
        public Part call() {
            randomReadAndWrite(from, bufferSize, part);
            return part;
        }

        public String randomReadAndWrite(String fromPath, int bufferSize, Part part) {
            RandomAccessFile readRaf = null;
            RandomAccessFile writeRaf = null;
            try {
                readRaf = new RandomAccessFile(fromPath, "r");
                writeRaf = new RandomAccessFile(part.getPath(), "rw");
                readRaf.seek(part.getStartPointer());
                writeRaf.seek(0);
                byte[] buffer = new byte[bufferSize];

                int totalLengthToRead = part.getLength();
                long startPointer = part.getStartPointer();
                int lengthToRead;
                int lengthReadAlready = 0;

                while(totalLengthToRead > 0 && (lengthToRead = totalLengthToRead < bufferSize ? totalLengthToRead : bufferSize) > 0) {
                    readRaf.read(buffer);
                    writeRaf.write(buffer, 0, lengthToRead);
                    totalLengthToRead -= lengthToRead;
                    startPointer += lengthToRead;
                    lengthReadAlready += lengthToRead;
                    readRaf.seek(startPointer);
                    writeRaf.seek(lengthReadAlready);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if(readRaf != null)
                        readRaf.close();
                    if(writeRaf != null)
                        writeRaf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return part.getPath();
        }

    }

    interface Action {
        void doInAction();
    }

    interface ReadAndWriteAction {
        void doInAction(RandomAccessFile readRaf, RandomAccessFile writeRaf) throws IOException;
    }

}