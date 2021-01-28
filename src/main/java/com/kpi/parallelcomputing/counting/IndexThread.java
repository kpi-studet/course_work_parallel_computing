package com.kpi.parallelcomputing.counting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class IndexThread extends Thread{
    private static List<Path> filesList; //массив в котором будут храниться адреса файлов
    private static AtomicInteger docIdAtomic = new AtomicInteger(0); //переменная для идентификаторов
    private static ConcurrentHashMap<String, List<Integer>> invIndex; //готовый индекс

    public IndexThread(List<Path> filesList) {
        this.filesList = filesList;
        start();
    }

    @Override
    public void run() {
        int docId;
        List<String> readFile;
        Path docPath;


        HashMap<String, List<Integer>> threadLevelIndex = new HashMap<>();

        while ((docId = docIdAtomic.getAndIncrement()) < filesList.size()) {//инкрементируем идентификатор документу и запускаем процесс пока есть записи в таблице адресов файлов
            try {                                                       //разделение документов между потоками происходит из-за функции docFile.getAndIncrement(), например: первый поток вошел, docId будет присвоен 0, а Atomic docFile 1, и следующий поток уже будет брать документ с другим id и соответственно будет вытаскивать из массива другой файл
                docPath = filesList.get(docId); //достаем адрес документа по номеру его идентификатора из Арейлиста с записями адресов файлов
                readFile = Files.readAllLines(docPath);//вычитываем файл построчно и записываем строки в массив типа Лист

                for (String docLine : readFile) {//проходимся по массиву содержащему строки файла
                    docLine = processLine(docLine);//каждую строку обрабатываем регулярным выражением убирая лишние символы (кроме слов)
                    addStringToThreadIndex(docLine, docId, threadLevelIndex);//разделяем строку на слова и добавляем слова в промежуточный индекс
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        combineThreadIndexes(threadLevelIndex);//помещаем threadLevelIndex в мапу ConcurrentHashMap
    }


   public static void initIndex(int numOfThreads) {
            invIndex = new ConcurrentHashMap<>(50,50, numOfThreads);
            //ConcurrentHashMap построена так чтобы ей было удобно пользоваться нескольким потокам. Она разбивается на сегменты (их количество соответствует количеству потоков)
            //при обновлении логика ее работы ипользует CAS операции. ConcurrentMap гарантирует согласованность памяти при операциях ключ/значение в многопоточной среде.
            //Под капотом ConcurrentHashMap отчасти похож на HashMap ** , с доступом к данным и их обновлением на основе хэш-таблицы (хотя и более сложной).
            //initialCapacity - начальная емкость. Реализация выполняет внутреннюю калибровку для размещения такого количества элементов. Должно быть примерно равным количеству отображений, которые вы ожидаете разместить на карте.
            //loadFactor - LoadFactor означает, что если корзины заполнены более чем на 50%, необходимо изменить размер map.
            //concurrencyLevel - приблизительное количество потоков, которое используеться для установки обьемов размеров по сегментам
    }

    private String processLine(String str) {
        String result;

        result = str.replaceAll("(^[\\W_]+)|([\\W_]+$)", "")// с начала строки любые не буквенно-числовые символы один или множество или до конца строки любые небуквенно-цифровые один или множество символы удалить
                .replaceAll("[\\W_]", " ")// внутри строки любые небуквенно цифровые символы заменить на пробелы
                .replaceAll("\\s+", " ");// заменить множество пробелов на один пробел

        // System.out.println(result);
        return result.toLowerCase();
    }


    private void addStringToThreadIndex(String str, int docId, HashMap<String, List<Integer>> map) {
        List<Integer> documentList;//это список в котором будут идентификаторы документов в которых будует присутствовать слово
        String[] words = str.split(" "); //переданую строку из файла мы разбиваем на массив слов по разделителю пробел

        for (String word : words) {//проходимся по всем словам в массиве

            documentList = map.get(word);//из мапы localIndex по ключу слова пытаемся достать данные

            if (documentList == null) {//если в мапе такого ключа слова нет
                documentList = new ArrayList<>();
                map.put(word, documentList);//мы добавляем в мапу слово ключ и пустой Лист массив для того чтобы в него складывать идент документов в которых есть данное слово
            }

            if (!documentList.contains(docId)) {//если массив идентификаторов документов не содержит такого идентификатора
                documentList.add(docId);//то мы добавляем идентификатор в него
            }
        }
    }

    private void combineThreadIndexes(HashMap<String, List<Integer>> threadLevelIndex) {
        List<Integer> documentsList;

        for (Map.Entry<String, List<Integer>> entry : threadLevelIndex.entrySet()) {

            documentsList = invIndex.get(entry.getKey());//достаем значение ключа из главной таблицы, если в ней еще нет значения на это слво то оно присвоит documentsList значение null

            if (documentsList == null) {//если это слово еще не записывалось в главную таблицу то нам нужно инициализировать documentsList
                documentsList = Collections.synchronizedList(new ArrayList<>(entry.getValue()));//мы инициализируем documentsList через присвоение ему ArrayList и помещение в него значения с массивом с айдишниками документов
            } else {
                documentsList.addAll(entry.getValue());//если documentsList уже был проинициализирован мы добавляем в него значение массива с айдишниками документов
            }

            invIndex.put(entry.getKey(), documentsList);//помещаем documentsList в окончательную мапу под соответствующее слово, насколько я могу судить то значения всех доументов по слову сливаются сдесь
            documentsList.sort(Integer::compareTo);// сортируем documentsList по порядку
        }
    }

    public static ArrayList<Path> getWordData(String word) {
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
