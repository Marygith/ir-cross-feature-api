package ru.nms.diplom.ircrossfeature.dto;

public class MinScoreResponse {
    private float distance;

    public MinScoreResponse(float distance) {
        this.distance = distance;
    }

    public MinScoreResponse() {
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}
