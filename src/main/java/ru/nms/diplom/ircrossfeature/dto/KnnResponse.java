package ru.nms.diplom.ircrossfeature.dto;


import java.util.Map;


public class KnnResponse {

    private Map<Integer, Float> neighbours;

    public KnnResponse(Map<Integer, Float> neighbours) {
        this.neighbours = neighbours;
    }

    public KnnResponse() {
    }

    public Map<Integer, Float> getNeighbours() {
        return neighbours;
    }

    public void setNeighbours(Map<Integer, Float> neighbours) {
        this.neighbours = neighbours;
    }
}
