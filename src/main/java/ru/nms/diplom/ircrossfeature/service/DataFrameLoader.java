package ru.nms.diplom.ircrossfeature.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DataFrameLoader {

    public static Map<String, Integer> loadRelevantPassages(String filePath){
        Map<String, Integer> relevantPassages = new HashMap<>();

        try (FileReader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader().withDelimiter('^'))) {
            for (CSVRecord csvRecord : csvParser) {
                String key = csvRecord.get("query");
                Integer value = Integer.parseInt(csvRecord.get("relevant_passage_id"));
                relevantPassages.put(key, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return relevantPassages;
    }

    public static Map<String, String> loadAllPassages(String filePath) {
        Map<String, String> allPassages = new HashMap<>();

        try (FileReader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader().withDelimiter('^'))) {
            for (CSVRecord csvRecord : csvParser) {
                String key = csvRecord.get("id");
                String value = csvRecord.get("passage_text");
                allPassages.put(key, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return allPassages;
    }
}
