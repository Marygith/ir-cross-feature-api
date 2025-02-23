package ru.nms.diplom.ircrossfeature.dto;

public class MinScoreRequest {
    private String query;

    public MinScoreRequest(String query) {
        this.query = query;
    }

    public MinScoreRequest() {
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public String toString() {
        return "MinScoreRequest{" +
                "query='" + query + '\'' +
                '}';
    }
}
