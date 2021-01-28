package com.kpi.parallelcomputing.service;

import com.kpi.parallelcomputing.counting.IndexThread;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SearchWordService {
    public String[] searchWordInFiles(String word) {
        List<String> fileNames = new ArrayList<>();
        for (Path path : IndexThread.getWordData(word.toLowerCase())){
            fileNames.add(path.getFileName().toString());
        }
        return fileNames.toArray(new String[fileNames.size()]);
    }
}
