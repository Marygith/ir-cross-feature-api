package ru.nms.diplom.ircrossfeature;

import ru.nms.diplom.ircrossfeature.service.FeatureService;
import ru.nms.diplom.ircrossfeature.service.FeatureStoreService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.nms.diplom.ircrossfeature.service.DataFrameLoader.loadRelevantPassages;

public class FeatureStoreBuilder {

    public static void main(String[] args) {
        FeatureService featureService = new FeatureService();
        FeatureStoreService featureStoreService = new FeatureStoreService();
        Map<String, Integer> relevantPassages = loadRelevantPassages("D:\\diplom\\data\\relevant_passages.csv");
        List<String> queries = new ArrayList<>(relevantPassages.keySet());
        int index = 0;
        Map<String, Map<Integer, Float>> enrichedAndNormalizedLuceneResults = new HashMap<>();
        Map<String, Map<Integer, Float>> enrichedAndNormalizedFaissResults = new HashMap<>();

        while(index < queries.size()) {
            int endIndex = Math.min(index + 1000, queries.size() - 1);
            featureService.getFeatures(queries.subList(index, endIndex), enrichedAndNormalizedLuceneResults, enrichedAndNormalizedFaissResults, 1000);

            System.out.println("Successfully got results from index " + index + " to index " + endIndex + " queries");
            try {
                featureStoreService.storeResults(enrichedAndNormalizedFaissResults, enrichedAndNormalizedLuceneResults, relevantPassages);
            } catch (IOException e) {
                throw new RuntimeException("Did not manage to store results", e);
            }
            System.out.println("Successfully stores results from index " + index + " to index " + endIndex + " queries");
            index += 1000;
            enrichedAndNormalizedFaissResults.clear();
            enrichedAndNormalizedLuceneResults.clear();

        }
    }
}
