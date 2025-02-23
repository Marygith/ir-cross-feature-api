package ru.nms.diplom.ircrossfeature.service.ir.impl;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import ru.nms.diplom.ircrossfeature.service.CrossFeatureRequest;
import ru.nms.diplom.ircrossfeature.service.ir.IRService;
import ru.nms.diplom.luceneir.service.*;

import java.util.*;
import java.util.stream.Collectors;

public class LuceneIRService implements IRService {

    private final ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080)
            .usePlaintext()
            .build();
    private final SearchServiceGrpc.SearchServiceBlockingStub stub = SearchServiceGrpc.newBlockingStub(channel);

    @Override
    public Map<String, Map<Integer, Float>> knn(List<String> queries, int k) {
        Map<String, Map<Integer, Float>> result = new HashMap<>();
        for (String query: queries) {
            DocumentsResponse documentsResponse = stub.knn(SearchRequest.newBuilder()
                    .setK(k)
                    .setQuery(query)
                    .build());

//            documentsResponse.getDocumentsList().forEach(
//                    doc -> System.out.println((float)doc.getScore() + " | " + doc.getId())
//            );
            result.put(query, documentsResponse.getDocumentsList().stream().collect(Collectors.toMap(Document::getId, Document::getScore, (v1, v2) -> v1)));
        }
        return result;
    }

    @Override
    public Map<Integer, Float> getSimilarityScoresForDocIds(Set<Integer> ids, String query) {
        DocumentsResponse documentsResponse = stub.getSimilarityScores(SimilarityScoreRequest.newBuilder().addAllDocId(ids).setQuery(query).build());

        return documentsResponse.getDocumentsList().stream().collect(Collectors.toMap(Document::getId, Document::getScore));
    }

    @Override
    public void tuneParameters(CrossFeatureRequest request) {
        stub.changeSimilarityParams(BM25TuneRequest.newBuilder().setK1(request.getK1()).setB(request.getB()).build());
    }

//    @Override
//    public float getMinScoreForQuery(String query) {
//        return stub.getMinScore(MinScoreRequest.newBuilder().setQuery(query).build()).getScore();
//    }

    @Override
    public void normalizeScores(Map<String, Map<Integer, Float>> queryToDocuments) {
        for (Map.Entry<String, Map<Integer, Float>> entry : queryToDocuments.entrySet()) {
            Map<Integer, Float> docScores = entry.getValue();
//            System.out.println("resulting ids for lucene: " + docScores.keySet());
            if (docScores.values().isEmpty()) continue;
            float maxScore = docScores.values().stream().max(Float::compare).get();
            float minScore = docScores.values().stream().min(Float::compare).get();
                docScores.replaceAll((docId, score) -> (score - minScore) / (maxScore - minScore));
        }
    }
}
