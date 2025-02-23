package ru.nms.diplom.ircrossfeature.service;

import io.grpc.stub.StreamObserver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.nms.diplom.ircrossfeature.service.DataFrameLoader.loadRelevantPassages;
import static ru.nms.diplom.ircrossfeature.service.MRRService.calculateMrr;
import static ru.nms.diplom.ircrossfeature.util.Utils.sortSByScoresAndToList;

public class CrossFeatureServiceImpl extends CrossFeatureServiceGrpc.CrossFeatureServiceImplBase {
    private final FeatureService featureService = new FeatureService();

    @Override
    public void calculateMRR(CrossFeatureRequest request, StreamObserver<CrossFeatureResponse> responseObserver) {
        Map<String, Integer> relevantPassages = loadRelevantPassages("D:\\diplom\\data\\relevant_passages.csv");
        List<String> queries = relevantPassages.keySet().stream().limit(request.getK()).collect(Collectors.toList());
        Map<String, Map<Integer, Float>> enrichedAndNormalizedFaissResults = new HashMap<>();
        Map<String, Map<Integer, Float>> enrichedAndNormalizedLuceneResults = new HashMap<>();

        featureService.getFeatures(queries, enrichedAndNormalizedLuceneResults, enrichedAndNormalizedFaissResults, request.getK());

        Map<String, List<Integer>> resultFromFaiss = sortSByScoresAndToList(enrichedAndNormalizedFaissResults, Map.Entry.<Integer, Float>comparingByValue().reversed());
        Map<String, List<Integer>> resultFromLucene = sortSByScoresAndToList(enrichedAndNormalizedLuceneResults, Map.Entry.<Integer, Float>comparingByValue().reversed() );

        float faissMrr = calculateMrr(relevantPassages, resultFromFaiss);
        float bm25MRR = calculateMrr(relevantPassages, resultFromLucene);

        float mrrWithRRF = calculateMrr(relevantPassages, combineViaRRF(resultFromFaiss, resultFromLucene, queries, request.getK(),request.getRrfAlfa()));
        float mrrWithRSF = calculateMrr(relevantPassages, combineViaRSF(enrichedAndNormalizedFaissResults, enrichedAndNormalizedLuceneResults, queries, request.getFaissRsfCoefficient()));

        CrossFeatureResponse response = CrossFeatureResponse.newBuilder()
                .setBm25MRR(bm25MRR)
                .setFaissMRR(faissMrr)
                .setMrrWithRRF(mrrWithRRF)
                .setMrrWithRSF(mrrWithRSF)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    //reciprocal rank fusion
    private Map<String, List<Integer>> combineViaRRF(Map<String,  List<Integer>> resultFromFaiss, Map<String,  List<Integer>> resultFromBM25, List<String> queries, int k, int alfa) {
        Map<String, List<Integer>> combinedQueryToPassages = new HashMap<>();
        for(String query: queries) {
            if (!resultFromFaiss.containsKey(query) || !resultFromBM25.containsKey(query)) {
                System.out.println("Result for query " + query + " is missing from results");
                continue;
            }
            List<Integer> faissList = resultFromFaiss.get(query);
            List<Integer> bm25List = resultFromBM25.get(query);
            var combinedNeighbours = reorderNeighboursViaRRF(faissList, bm25List, k, alfa);
            combinedQueryToPassages.put(query, combinedNeighbours);
        }
        return combinedQueryToPassages;
    }

    //relative score fusion
    private Map<String, List<Integer>> combineViaRSF(Map<String,  Map<Integer, Float>> resultFromFaiss, Map<String,  Map<Integer, Float>> resultFromBM25, List<String> queries, float faissCoefficient) {
        Map<String, List<Integer>> combinedQueryToPassages = new HashMap<>();
        for(String query: queries) {
            if (!resultFromFaiss.containsKey(query) || !resultFromBM25.containsKey(query)) {
                System.out.println("Result for query " + query + " is missing from results");
                continue;
            }
            var faissResultMap = resultFromFaiss.get(query);
            var bm25ResultMap = resultFromBM25.get(query);
            var combinedNeighbours = reorderNeighboursViaRSF(faissResultMap, bm25ResultMap, faissCoefficient);
            combinedQueryToPassages.put(query, combinedNeighbours);
        }
        return combinedQueryToPassages;
    }

    private List<Integer> reorderNeighboursViaRSF(Map<Integer, Float> neighboursFromFaiss, Map<Integer, Float> neighboursFromBM25, float faissCoefficient) {
        Map<Integer, Float> combinedMap = new HashMap<>(neighboursFromFaiss);

        for (Map.Entry<Integer, Float> entry : neighboursFromBM25.entrySet()) {
            combinedMap.merge(entry.getKey(), entry.getValue() * faissCoefficient, Float::sum);
        }

        return combinedMap.entrySet().stream()
                .sorted(Map.Entry.<Integer, Float>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private List<Integer> reorderNeighboursViaRRF(List<Integer> neighboursFromFaiss, List<Integer> neighboursFromBM25, int k, int alfa) {
        Map<Integer, Float> passageToRank = new HashMap<>();
        for(int i = 0; i < neighboursFromFaiss.size(); i++) {
            int bm25Ind = neighboursFromBM25.indexOf(neighboursFromFaiss.get(i));
//            if (bm25Ind == -1) continue;
            bm25Ind = bm25Ind == -1 ? k - 1 : bm25Ind;
            float RRFScore = (float) (1.0 / (i + 1 + alfa) + 1.0 / (bm25Ind + 1 + alfa));
            passageToRank.put(neighboursFromFaiss.get(i), RRFScore);
        }
        for(int i = 0; i < neighboursFromBM25.size(); i++) {
            if(passageToRank.containsKey(neighboursFromBM25.get(i))) continue;
            int faissInd = neighboursFromFaiss.indexOf(neighboursFromBM25.get(i));
            faissInd = faissInd == -1 ? k - 1 : faissInd;
            float RRFScore = (float) (1.0 / (i + 1 + alfa) + (faissInd == -1 ? 0 : 1.0 / (faissInd + 1 + alfa)));
            passageToRank.put(neighboursFromBM25.get(i), RRFScore);
        }
        return passageToRank.entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Float>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();
    }
}
