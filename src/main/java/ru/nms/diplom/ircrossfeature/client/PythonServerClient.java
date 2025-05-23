package ru.nms.diplom.ircrossfeature.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import ru.nms.diplom.ircrossfeature.dto.KnnRequest;
import ru.nms.diplom.ircrossfeature.dto.KnnResponse;
import ru.nms.diplom.ircrossfeature.dto.SimilarityScoreRequest;
import ru.nms.diplom.ircrossfeature.dto.SimilarityScoreResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class PythonServerClient {

    private static final String VECTOR_SEARCH_URL = "http://127.0.0.1:5000/search";
    private static final String SIMILARITY_SCORES_FAISS_URL = "http://127.0.0.1:5000/getSimilarityScores";
    private static final String MIN_SCORE_FAISS_URL = "http://127.0.0.1:5000/getMinScore";

    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KnnResponse searchVector(KnnRequest request) {
        return search(request);
    }

    private KnnResponse search(KnnRequest request) {
        HttpPost postRequest = new HttpPost(PythonServerClient.VECTOR_SEARCH_URL);
//        System.out.println("sending request: " + request);
        try {

            String jsonRequest = objectMapper.writeValueAsString(request);


            postRequest.setHeader("Content-Type", "application/json; charset=UTF-8");
            postRequest.setEntity(new StringEntity(jsonRequest, StandardCharsets.UTF_8));


            try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());

                if (statusCode >= 200 && statusCode < 300) {

                    JsonNode responseJson = objectMapper.readTree(responseBody);
                    Map<Integer, Float> neighbours = objectMapper.convertValue(responseJson, new TypeReference<Map<Integer, Float>>() {});
                    return new KnnResponse(neighbours);
                } else {
                    System.out.println("HTTP error: " + statusCode + " - " + responseBody);
                    return null;
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize request to JSON", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to execute HTTP request", e);
        }
    }

    public SimilarityScoreResponse getSimilarityScoresFromFaiss(SimilarityScoreRequest request) {
        HttpPost postRequest = new HttpPost(SIMILARITY_SCORES_FAISS_URL);
//        System.out.println("sending request: " + request);
        try {

            String jsonRequest = objectMapper.writeValueAsString(request);


            postRequest.setHeader("Content-Type", "application/json; charset=UTF-8");
            postRequest.setEntity(new StringEntity(jsonRequest, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());

                if (statusCode >= 200 && statusCode < 300) {

                    JsonNode responseJson = objectMapper.readTree(responseBody);
                    Map<Integer, Float> idsToScore = objectMapper.convertValue(responseJson, new TypeReference<Map<Integer, Float>>() {});
                    return new SimilarityScoreResponse(idsToScore);
                } else {
                    System.out.println("HTTP error: " + statusCode + " - " + responseBody);
                    return null;
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize request to JSON", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to execute HTTP request", e);
        }
    }


//    public MinScoreResponse getMinScoreFromFaiss(MinScoreRequest request) {
//        HttpPost postRequest = new HttpPost(MIN_SCORE_FAISS_URL);
//        System.out.println("sending request: " + request);
//        try {
//
//            String jsonRequest = objectMapper.writeValueAsString(request);
//
//
//            postRequest.setHeader("Content-Type", "application/json; charset=UTF-8");
//            postRequest.setEntity(new StringEntity(jsonRequest, StandardCharsets.UTF_8));
//
//            try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
//                int statusCode = response.getStatusLine().getStatusCode();
//                String responseBody = EntityUtils.toString(response.getEntity());
//
//                if (statusCode >= 200 && statusCode < 300) {
//
//                    JsonNode responseJson = objectMapper.readTree(responseBody);
//                    return objectMapper.convertValue(responseJson, MinScoreResponse.class);
//                } else {
//                    System.out.println("HTTP error: " + statusCode + " - " + responseBody);
//                    return null;
//                }
//            }
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException("Failed to serialize request to JSON", e);
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to execute HTTP request", e);
//        }
//    }
//
//
//    public void close() throws IOException {
//        httpClient.close();
//    }
}
