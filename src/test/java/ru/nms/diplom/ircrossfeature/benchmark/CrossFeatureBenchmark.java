package ru.nms.diplom.ircrossfeature.benchmark;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.openjdk.jmh.annotations.*;
import ru.nms.diplom.ircrossfeature.service.CrossFeatureRequest;
import ru.nms.diplom.ircrossfeature.service.CrossFeatureResponse;
import ru.nms.diplom.ircrossfeature.service.CrossFeatureServiceGrpc;

import java.util.concurrent.TimeUnit;

@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Warmup(iterations = 0, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 1, time = 200, timeUnit = TimeUnit.MILLISECONDS)
public class CrossFeatureBenchmark {

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
    private static final ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8081)
            .usePlaintext()
            .build();
    private static final CrossFeatureServiceGrpc.CrossFeatureServiceBlockingStub stub = CrossFeatureServiceGrpc.newBlockingStub(channel);

    @Param({"10", "100"}) // Vary `k` parameter
    public int k;

    @Param({"100", "1000"}) // Vary `queriesAmount`
    public int queriesAmount;

    @Param({"1.2", "1.5", "2.0"}) // Vary BM25 `k1` parameter
    public float bm25K1;

    @Param({"0.5", "0.75", "1.0"}) // Vary BM25 `b` parameter
    public float bm25B;

    @Setup(Level.Iteration)
    public void setup() {
        // Add any setup logic here if needed
    }

    @Benchmark
    public float calculateMRR() {
        // Create the request with varying parameters
        CrossFeatureRequest request = CrossFeatureRequest.newBuilder()
                .setK(k)
                .setQueriesAmount(queriesAmount)
                .setK1(bm25K1)
                .setB(bm25B)
                .build();

        // Call the gRPC method
        CrossFeatureResponse response = stub.calculateMRR(request);

        // Return the MRR value for benchmarking
        System.out.println("\n\nRESULT: " +
                "\nBM25 MRR: " + response.getBm25MRR() +
                "\nFAISS MRR: " + response.getFaissMRR() +
                "\nRRF MRR: " + response.getMrrWithRRF() +
                "\nRSF MRR: " + response.getMrrWithRSF());
        return response.getBm25MRR();
    }

    @TearDown(Level.Iteration)
    public void teardown() {
        // Add cleanup logic here if needed
    }
}