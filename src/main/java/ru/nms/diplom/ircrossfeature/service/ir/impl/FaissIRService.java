package ru.nms.diplom.ircrossfeature.service.ir.impl;

import ru.nms.diplom.ircrossfeature.client.PythonServerClient;
import ru.nms.diplom.ircrossfeature.dto.KnnRequest;
import ru.nms.diplom.ircrossfeature.dto.KnnResponse;
import ru.nms.diplom.ircrossfeature.dto.MinScoreRequest;
import ru.nms.diplom.ircrossfeature.dto.SimilarityScoreRequest;
import ru.nms.diplom.ircrossfeature.service.CrossFeatureRequest;
import ru.nms.diplom.ircrossfeature.service.PassageReader;
import ru.nms.diplom.ircrossfeature.service.ir.IRService;

import java.util.*;

public class FaissIRService implements IRService {

    private final PythonServerClient pythonServerClient = new PythonServerClient();
    private final PassageReader passageReader = new PassageReader();

    @Override
    public Map<String, Map<Integer, Float>> knn(List<String> queries, int k) {
        Map<String, Map<Integer, Float>> queryToResultPassages = new HashMap<>();

        for (String query : queries) {
            KnnRequest knnRequest = new KnnRequest(query, k, false);
            KnnResponse knnResponse = pythonServerClient.searchVector(knnRequest);
            if (knnResponse == null) continue; //todo check
            Map<Integer, Float> neighbours = knnResponse.getNeighbours();
            queryToResultPassages.put(query, neighbours);
        }
        return queryToResultPassages;
    }

    @Override
    public Map<Integer, Float> getSimilarityScoresForDocIds(Set<Integer> ids, String query) {
        Map<Integer, float[]> idsToTexts = new HashMap<>();
        for (Integer id : ids) {
            idsToTexts.put(id, passageReader.getVectorById(id));
        }
        return pythonServerClient.getSimilarityScoresFromFaiss(new SimilarityScoreRequest(query, idsToTexts))
                .getIdsToScore();
    }

    @Override
    public void normalizeScores(Map<String, Map<Integer, Float>> queryToDocuments) {
        for (Map.Entry<String, Map<Integer, Float>> entry : queryToDocuments.entrySet()) {
            Map<Integer, Float> docScores = entry.getValue();
//            System.out.println("resulting ids for faiss: " + docScores.keySet());

            float maxDistance = docScores.values().stream().max(Float::compare).get();
                float minDistance = docScores.values().stream().min(Float::compare).get();
                docScores.replaceAll((docId, score) -> 1 - (score - minDistance) / (maxDistance - minDistance));
        }
    }

    @Override
    public void tuneParameters(CrossFeatureRequest request) {

    }

//    @Override
//    public float getMinScoreForQuery(String query) {
//        return pythonServerClient.getMinScoreFromFaiss(new MinScoreRequest(query)).getDistance();
//    }
}
