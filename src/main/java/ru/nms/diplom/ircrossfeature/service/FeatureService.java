package ru.nms.diplom.ircrossfeature.service;

import ru.nms.diplom.ircrossfeature.service.ir.IRService;
import ru.nms.diplom.ircrossfeature.service.ir.impl.FaissIRService;
import ru.nms.diplom.ircrossfeature.service.ir.impl.LuceneIRService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class FeatureService {

    private final IRService luceneIRService = new LuceneIRService();
    private final IRService faissIRService = new FaissIRService();

    public void getFeatures(
            List<String> queries,
            Map<String, Map<Integer, Float>> enrichedAndNormalizedLuceneResults,
            Map<String, Map<Integer, Float>> enrichedAndNormalizedFaissResults,
            int k)
    {

//        luceneIRService.tuneParameters(request);

        CompletableFuture<Map<String, Map<Integer, Float>>> luceneFuture = CompletableFuture.supplyAsync(() -> luceneIRService.knn(queries, k));
        CompletableFuture<Map<String, Map<Integer, Float>>> faissFuture = CompletableFuture.supplyAsync(() -> faissIRService.knn(queries, k));

        CompletableFuture<Void> allDone = CompletableFuture.allOf(luceneFuture, faissFuture);

        allDone.join();
        Map<String, Map<Integer, Float>> luceneResults;
        Map<String, Map<Integer, Float>> faissResults;
        try {
            luceneResults = luceneFuture.get();
            System.out.println("Successfully got result from lucene");
            faissResults = faissFuture.get();
            System.out.println("Successfully got result from faiss");
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("error occured while getting result from IR services", e);
        }

        luceneFuture = CompletableFuture.supplyAsync(() -> luceneIRService.enrichAndNormalizeResults(luceneResults, faissResults, queries));
        faissFuture = CompletableFuture.supplyAsync(() -> faissIRService.enrichAndNormalizeResults(faissResults, luceneResults, queries));

        allDone = CompletableFuture.allOf(luceneFuture, faissFuture);
        allDone.join();

        try {
            enrichedAndNormalizedLuceneResults.putAll(luceneFuture.get());
            System.out.println("Successfully enriched result from lucene");
            enrichedAndNormalizedFaissResults.putAll(faissFuture.get());
            System.out.println("Successfully enriched result from faiss");
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("error occured while enriching results from IR services", e);
        }
    }
}
