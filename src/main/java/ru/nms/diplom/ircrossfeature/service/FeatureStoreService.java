package ru.nms.diplom.ircrossfeature.service;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static ru.nms.diplom.ircrossfeature.util.Constants.FEATURE_STORE_FILE;

public class FeatureStoreService {
    public void storeResults(
            Map<String, Map<Integer, Float>> resultFromFaiss,
            Map<String, Map<Integer, Float>> resultFromLucene,
            Map<String, Integer> queryToPassages
    ) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(FEATURE_STORE_FILE, true))) {
            Set<String> queries = new HashSet<>(resultFromFaiss.keySet());
            queries.addAll(resultFromLucene.keySet());
            for (String query : queries) {
                if (!resultFromFaiss.containsKey(query) || !resultFromLucene.containsKey(query)) {
                    System.out.println("Query " + query + " is missing in one of the maps.");
                    continue;
//                    throw new IllegalStateException("Query " + query + " is missing in one of the maps.");
                }

                Map<Integer, Float> faissScores = resultFromFaiss.get(query);
                Map<Integer, Float> luceneScores = resultFromLucene.get(query);

                if (!faissScores.keySet().equals(luceneScores.keySet())) {
                    throw new IllegalStateException("Passage ID sets do not match for query: " + query);
                }

                // Write relevant passage ID
                int relevantPassageId = queryToPassages.get(query);
                dos.writeInt(relevantPassageId);
                dos.writeInt(faissScores.keySet().size());
                // Write passage scores
                for (Integer passageId : faissScores.keySet()) {
                    float faissScore = faissScores.get(passageId);
                    float luceneScore = luceneScores.get(passageId);

                    dos.writeInt(passageId);
                    dos.writeFloat(faissScore);
                    dos.writeFloat(luceneScore);
                }
            }
        }
    }
}
