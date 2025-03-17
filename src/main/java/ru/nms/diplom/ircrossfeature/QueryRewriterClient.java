package ru.nms.diplom.ircrossfeature;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import ru.nms.diplom.ircrossfeature.service.*;

public class QueryRewriterClient {

    public static void main(String[] args) {
        // Create a gRPC channel to the server
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        // Create a stub for the service
        QueryRewriterGrpc.QueryRewriterBlockingStub stub =
                QueryRewriterGrpc.newBlockingStub(channel);

        // Create the request with a sample query and requested number of variants
        QueryRewriterService.QueryRequest request = QueryRewriterService.QueryRequest.newBuilder()
                .setQuery("Best restaurants in New York")
                .setNumVariants(3)
                .build();

        // Call the gRPC method
        QueryRewriterService.QueryResponse response = stub.rephrase(request);

        // Print the rephrased queries
        System.out.println("Expanded Queries:");
        for (String queryVariant : response.getVariantsList()) {
            System.out.println("- " + queryVariant);
        }

        // Shutdown the channel
        channel.shutdown();
    }
}
