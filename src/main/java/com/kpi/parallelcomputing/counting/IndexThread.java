package com.kpi.parallelcomputing.counting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class IndexThread extends Thread{
    private List<Path> filesList;
    private AtomicInteger docIdAtomic;
    private ConcurrentHashMap<String, List<Integer>> invIndex;


    public IndexThread(List<Path> filesList, AtomicInteger docIdAtomic, ConcurrentHashMap<String, List<Integer>> invIndex) {
        this.filesList = filesList;
        this.docIdAtomic = docIdAtomic;
        this.invIndex = invIndex;
        start();
    }

    @Override
    public void run() {
        int docId;
        List<String> readFile;
        Path docPath;


        HashMap<String, List<Integer>> threadLevelIndex = new HashMap<>();

        while ((docId = docIdAtomic.getAndIncrement()) < filesList.size()) {
            try {
                docPath = filesList.get(docId);
                readFile = Files.readAllLines(docPath);

                for (String docLine : readFile) {
                    docLine = processLine(docLine);
                    addStringToThreadIndex(docLine, docId, threadLevelIndex);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        combineThreadIndexes(threadLevelIndex);
    }


    private String processLine(String str) {
        String result;

        result = str.replaceAll("(^[\\W_]+)|([\\W_]+$)", "")
                .replaceAll("[\\W_]", " ")
                .replaceAll("\\s+", " ");
        return result.toLowerCase();
    }


    private void addStringToThreadIndex(String str, int docId, HashMap<String, List<Integer>> map) {
        List<Integer> documentList;
        String[] words = str.split(" ");

        for (String word : words) {

            documentList = map.get(word);

            if (documentList == null) {
                documentList = new ArrayList<>();
                map.put(word, documentList);
            }

            if (!documentList.contains(docId)) {
                documentList.add(docId);
            }
        }
    }

    private void combineThreadIndexes(HashMap<String, List<Integer>> threadLevelIndex) {
        List<Integer> documentsList;

        for (Map.Entry<String, List<Integer>> entry : threadLevelIndex.entrySet()) {

            documentsList = invIndex.get(entry.getKey());
            if (documentsList == null) {
               documentsList = Collections.synchronizedList(new ArrayList<>(entry.getValue()));//мы инициализируем documentsList через присвоение ему ArrayList и помещение в него значения с массивом с айдишниками документов
            } else {
                documentsList.addAll(entry.getValue());
            }

            invIndex.put(entry.getKey(), documentsList);
            documentsList.sort(Integer::compareTo);
        }
    }

}
