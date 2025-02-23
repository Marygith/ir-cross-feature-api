package ru.nms.diplom.ircrossfeature.service.ir;

import ru.nms.diplom.ircrossfeature.service.CrossFeatureRequest;

import java.util.*;

public interface IRService {
    Map<String, Map<Integer, Float>> knn(List<String> queries, int k);

    Map<Integer, Float> getSimilarityScoresForDocIds(Set<Integer> ids, String query);

    default Map<String, Map<Integer, Float>> enrichWithScores(Map<String, Map<Integer, Float>> IRResults, Map<String, Map<Integer, Float>> resultsFromOtherIRApi, List<String> queries) {
        Map<String,  Map<Integer, Float>> enrichedIRResults = new HashMap<>();
        for (String query: IRResults.keySet()) {

            if (!IRResults.containsKey(query) || !resultsFromOtherIRApi.containsKey(query)) {
                System.out.println("Result for query " + query + " is missing from results");
//                throw new RuntimeException("Result for query " + query + " is missing from results");
                continue;
            }
            Set<Integer> missingIds = new HashSet<>(resultsFromOtherIRApi.get(query).keySet());
            enrichedIRResults.put(query, new HashMap<>(IRResults.get(query)));
            missingIds.removeAll(IRResults.get(query).keySet());
//            System.out.println("missing ids are: " + missingIds + ", present ids are: " + IRResults.get(query).keySet());

            enrichedIRResults.get(query).putAll(getSimilarityScoresForDocIds(missingIds, query));
//            checkScores(query, enrichedIRResults.get(query));
        }

        return enrichedIRResults;
    }

    default Map<String, Map<Integer, Float>> enrichAndNormalizeResults(Map<String, Map<Integer, Float>> IRResults, Map<String, Map<Integer, Float>> resultsFromOtherIRApi, List<String> queries) {
        Map<String, Map<Integer, Float>> results = enrichWithScores(IRResults, resultsFromOtherIRApi, queries);
        normalizeScores(results);
        return results;
    }

    void normalizeScores(Map<String, Map<Integer, Float>> queryToDocuments);
    private void checkScores(String query, Map<Integer, Float> origScores) {
        Map<Integer, Float> simScores = getSimilarityScoresForDocIds(origScores.keySet(), query);
        for (Map.Entry<Integer, Float> docToScore: origScores.entrySet()) {
            if (simScores.get(docToScore.getKey()) == null || Math.abs(simScores.get(docToScore.getKey()) - docToScore.getValue()) > 0.0001) {
                if (simScores.get(docToScore.getKey()) == null) System.out.println("doc with given id is absent, id: " + docToScore.getKey());
                else {
                    System.out.println("scores are different, orig score: " + docToScore.getValue() + ", calculated score: " + simScores.get(docToScore.getKey()));
                }
                throw new RuntimeException("results for query are not consistent, query: " + query);
            }
        }
    }

    void tuneParameters(CrossFeatureRequest request);

}
