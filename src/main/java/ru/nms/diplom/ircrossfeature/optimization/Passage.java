package ru.nms.diplom.ircrossfeature.optimization;

public class Passage {
    int passageId;
    float faissScore;
    float luceneScore;

    Passage(int passageId, float faissScore, float luceneScore) {
        this.passageId = passageId;
        this.faissScore = faissScore;
        this.luceneScore = luceneScore;
    }
}