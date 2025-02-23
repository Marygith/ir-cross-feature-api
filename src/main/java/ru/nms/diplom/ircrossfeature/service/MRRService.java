package ru.nms.diplom.ircrossfeature.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MRRService {

    public static float calculateMrr(Map<String, Integer> relevantPassages, Map<String, List<Integer>> queryToResultPassages) {
        float mrr = 0;
        for (String query : queryToResultPassages.keySet()) {
            List<Integer> neighbours = queryToResultPassages.get(query);
            Integer relevantPassage = relevantPassages.get(query);

            for (int i = 0; i < neighbours.size(); i++) {
                if (neighbours.get(i).equals(relevantPassage)) {
                    mrr += (float) (1.0 / (i + 1));
                    break;
                }
            }
        }
        return (mrr / queryToResultPassages.size());
    }
}
