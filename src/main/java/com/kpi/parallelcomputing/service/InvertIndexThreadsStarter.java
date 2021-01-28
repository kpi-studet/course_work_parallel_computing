package com.kpi.parallelcomputing.service;

import com.kpi.parallelcomputing.counting.IndexThread;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InvertIndexThreadsStarter {
   // private  ConcurrentHashMap<String, List<Integer>> invIndex; //готовый индекс

    public long startThreads(int numberOfThreads) {
        ArrayList<Path> files = createFilesList();
        IndexThread[] threads = new IndexThread[numberOfThreads];
        IndexThread.initIndex(numberOfThreads);
        // invIndex = new ConcurrentHashMap<>(50,50, numberOfThreads);


        // start threads
        long startTime = System.currentTimeMillis();

        try {
            for (int i = 0; i < numberOfThreads; i++) {//запускаем потоки формирования индекса помещая их в массив threads
                threads[i] = new IndexThread(files);
            }
            for (int i = 0; i < numberOfThreads; i++) {
                threads[i].join();
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        // finish threads
        long threadsWorkingTime = System.currentTimeMillis() - startTime;

        System.out.println(threadsWorkingTime);
        return threadsWorkingTime;
    }


    private ArrayList<Path> createFilesList() {
        ArrayList<Path> files = new ArrayList<>();
        try (Stream<Path> writer = Files.walk(Paths.get("g:\\Literature\\datasets\\datasets\\aclImdb\\test\\pos\\"))) {
            writer.filter(Files::isRegularFile).collect(Collectors.toCollection(() -> files));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }

}
