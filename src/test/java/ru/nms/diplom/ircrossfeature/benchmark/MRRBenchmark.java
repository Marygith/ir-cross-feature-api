package ru.nms.diplom.ircrossfeature.benchmark;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import ru.nms.diplom.ircrossfeature.service.CrossFeatureRequest;
import ru.nms.diplom.ircrossfeature.service.CrossFeatureResponse;
import ru.nms.diplom.ircrossfeature.service.CrossFeatureServiceGrpc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MRRBenchmark {
    private static final ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8081)
        .usePlaintext()
        .build();
    private static final CrossFeatureServiceGrpc.CrossFeatureServiceBlockingStub stub = CrossFeatureServiceGrpc.newBlockingStub(channel);

    public static void main(String[] args) {
        int[] kValues = {500};
        int[] queriesAmounts = {5000};
        float[] bm25K1Values = {1.0f, 1.2f, 1.5f};
        float[] bm25BValues = {0.5f, 0.75f, 1.0f};
        float[] faissRsfCoefficients = {0.75f, 0.85f, 1.25f};
        int[] rrfAlfas = {1};

        List<Result> results = new ArrayList<>();

        for (int k : kValues) {
            for (int queriesAmount : queriesAmounts) {
                for (float bm25K1 : bm25K1Values) {
                    for (float bm25B : bm25BValues) {
                        for (float faissRsfCoefficient : faissRsfCoefficients) {
                                    for (int rrfAlfa : rrfAlfas) {
                                        CrossFeatureRequest request = CrossFeatureRequest.newBuilder()
                                                .setK(k)
                                                .setQueriesAmount(queriesAmount)
                                                .setK1(bm25K1)
                                                .setB(bm25B)
                                                .setFaissRsfCoefficient(faissRsfCoefficient)
                                                .setRrfAlfa(rrfAlfa)
                                                .build();

                                        CrossFeatureResponse response = stub.calculateMRR(request);

                                        Result result = new Result(k, queriesAmount, bm25K1, bm25B,
                                                response.getFaissMRR(),
                                                response.getMrrWithRRF(),
                                                response.getMrrWithRSF(),
                                                response.getBm25MRR(),
                                                faissRsfCoefficient,
                                                rrfAlfa);
                                        results.add(result);
                                        System.out.println("Completed iteration with result: " + result);
                                    }

                        }
                    }
                }
            }
        }

        List<Result> sortedResults = results.stream()
                .sorted(Comparator.comparingDouble(result -> 1 - result.rsfMRR)).toList();
        printResults(sortedResults);

        channel.shutdown();
    }

    private static void printResults(List<Result> results) {
        System.out.printf("%-5s %-12s %-8s %-8s %-12s %-12s %-12s %-12s %-12s %-12s%n",
                "K", "Queries", "BM25 K1", "BM25 B", "Faiss MRR", "RRF MRR", "RSF MRR", "Lucene MRR", "Faiss coeff", "RRF alfa");

        for (Result result : results) {
            System.out.printf("%-5d %-12d %-8.2f %-8.2f %-12.4f %-12.4f %-12.4f %-12.4f %-12.4b %-12d%n",
                    result.k, result.queriesAmount, result.bm25K1, result.bm25B, result.faissMRR,
                    result.rrfMRR, result.rsfMRR, result.luceneMRR, result.faissCoefficient, result.rrfAlfa);
        }
    }

    public static class Result {
        int k;
        int queriesAmount;
        float bm25K1;
        float bm25B;
        float faissMRR;
        float rrfMRR;
        float rsfMRR;
        float luceneMRR;
        float faissCoefficient;
        int rrfAlfa;

        public Result(int k, int queriesAmount, float bm25K1, float bm25B, float faissMRR,
                      float rrfMRR, float rsfMRR, float luceneMRR, float faissCoefficient, int rrfAlfa) {
            this.k = k;
            this.queriesAmount = queriesAmount;
            this.bm25K1 = bm25K1;
            this.bm25B = bm25B;
            this.faissMRR = faissMRR;
            this.rrfMRR = rrfMRR;
            this.rsfMRR = rsfMRR;
            this.luceneMRR = luceneMRR;
            this.faissCoefficient = faissCoefficient;
            this.rrfAlfa = rrfAlfa;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "k=" + k +
                    ", queriesAmount=" + queriesAmount +
                    ", bm25K1=" + bm25K1 +
                    ", bm25B=" + bm25B +
                    ", faissMRR=" + faissMRR +
                    ", rrfMRR=" + rrfMRR +
                    ", rsfMRR=" + rsfMRR +
                    ", luceneMRR=" + luceneMRR +
                    ", faissCoefficient=" + faissCoefficient +
                    ", rrfAlfa=" + rrfAlfa +
                    '}';
        }
    }
}