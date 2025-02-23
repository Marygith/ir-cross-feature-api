package ru.nms.diplom.ircrossfeature.util;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Utils {

    public static final Comparator<Float> FLOAT_COMPARATOR = Float::compare;
    public static Map<String, List<Integer>> sortSByScoresAndToList(Map<String,  Map<Integer, Float>> results, Comparator<Map.Entry<Integer, Float>> comparator) {
        return results.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().entrySet().stream()
                                .sorted(comparator)
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toList())
                ));
    }

//    public static void normalizeScores(Map<String, Map<String, Float>> queryToDocuments, boolean inverted) {
//        for (Map.Entry<String, Map<String, Float>> entry : queryToDocuments.entrySet()) {
//            Map<String, Float> docScores = entry.getValue();
//
//            float maxScore = docScores.values().stream().max(Comparator.comparingFloat(v -> v)).get();
//
//            docScores.replaceAll((docId, score) -> inverted ? 1 - (score / maxScore): (score / maxScore));
//            if (inverted) {
//                float secondMaxScore = docScores.values().stream().max(Comparator.comparingFloat(v -> v)).get();
//                docScores.replaceAll((docId, score) -> score / secondMaxScore);
//            }
//        }
//    }
}
