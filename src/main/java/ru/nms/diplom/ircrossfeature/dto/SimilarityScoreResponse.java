package ru.nms.diplom.ircrossfeature.dto;

import java.util.Map;

public class SimilarityScoreResponse {
    private Map<Integer, Float> idsToScore;

    public SimilarityScoreResponse() {
    }

    public SimilarityScoreResponse(Map<Integer, Float> idsToScore) {
        this.idsToScore = idsToScore;
    }

    public Map<Integer, Float> getIdsToScore() {
        return idsToScore;
    }

    public void setIdsToScore(Map<Integer, Float> idsToScore) {
        this.idsToScore = idsToScore;
    }
}
