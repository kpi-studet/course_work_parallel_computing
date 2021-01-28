package com.kpi.parallelcomputing.service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SearchWordService {
    private ConcurrentHashMap<String, List<Integer>> invIndex;
    private List<Path> filesList;

    public SearchWordService(ConcurrentHashMap<String, List<Integer>> invIndex, List<Path> filesList) {
        this.invIndex = invIndex;
        this.filesList = filesList;
    }

    public String[] searchWordInFiles(String word) {
        List<String> fileNames = new ArrayList<>();
        for (Path path : getWordData(word.toLowerCase())){
            fileNames.add(path.getFileName().toString());
        }
        return fileNames.toArray(new String[fileNames.size()]);
    }

    private ArrayList<Path> getWordData(String word) {
        List<Integer> idList = invIndex.get(word);
        ArrayList<Path> pathList = new ArrayList<>();
        if (!(idList == null)) {
            for (Integer id : idList) {
                Path path = filesList.get(id);
                pathList.add(path);
            }
        } else pathList.add(Paths.get("Not found"));

        return pathList;
    }

}
