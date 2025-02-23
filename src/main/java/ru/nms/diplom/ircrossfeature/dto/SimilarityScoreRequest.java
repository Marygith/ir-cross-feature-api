package ru.nms.diplom.ircrossfeature.dto;

import java.util.Map;

public class SimilarityScoreRequest {
    private String query;
    private Map<Integer, float[]> idsToVector;

    public SimilarityScoreRequest() {
    }

    public SimilarityScoreRequest(String query, Map<Integer, float[]> idsToVector) {
        this.query = query;
        this.idsToVector = idsToVector;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Map<Integer, float[]> getIdsToVector() {
        return idsToVector;
    }

    public void setIdsToVector(Map<Integer, float[]> idsToVector) {
        this.idsToVector = idsToVector;
    }

    @Override
    public String toString() {
        return "SimilarityScoreRequest{" +
                "query='" + query + '\'' +
                ", idsToVector=" + idsToVector +
                '}';
    }
}
