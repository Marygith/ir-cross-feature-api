package ru.nms.diplom.ircrossfeature.optimization;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static ru.nms.diplom.ircrossfeature.util.Constants.FEATURE_STORE_FILE;

public class Utils {

    public static void main(String[] args) throws IOException {
        List<Map.Entry<Integer, List<Passage>>> data = loadData(FEATURE_STORE_FILE);
        double baseMrr = computeMRR(data, 1.0, 1.0);
        System.out.println("MRR with both coefficients equal to 1: " + baseMrr);
        double faissMrr = computeMRR(data, 1.0, 0.0);
        System.out.println("MRR for faiss: " + faissMrr);

    }

    public static List<Map.Entry<Integer, List<Passage>>> loadData(String filePath) throws IOException {
        List<Map.Entry<Integer, List<Passage>>> data = new ArrayList<>();

        try (DataInputStream dis = new DataInputStream(new FileInputStream(filePath))) {
            while (dis.available() > 0) {
                int relevantPassageId = dis.readInt();
                int mapSize = dis.readInt();  // Read the size of the map

                List<Passage> passages = new ArrayList<>();
                for (int i = 0; i < mapSize; i++) {
                    int passageId = dis.readInt();
                    float faissScore = dis.readFloat();
                    float luceneScore = dis.readFloat();
                    passages.add(new Passage(passageId, faissScore, luceneScore));
                }

                data.add(new AbstractMap.SimpleEntry<>(relevantPassageId, passages));
            }
        }
        return data;
    }

    public static double computeMRR(List<Map.Entry<Integer, List<Passage>>> data, double p, double w) {
        double totalMRR = 0.0;

        for (Map.Entry<Integer, List<Passage>> entry : data) {
            int relevantPassageId = entry.getKey();
            List<Passage> passages = new ArrayList<>(entry.getValue());

            // Compute final scores and sort in descending order
            passages.sort(Comparator.comparingDouble((Passage passage) -> -(p * passage.faissScore + w * passage.luceneScore)));

            // Find rank of the relevant passage
            for (int i = 0; i < passages.size(); i++) {
                if (passages.get(i).passageId == relevantPassageId) {
                    totalMRR += 1.0 / (i + 1);
                    break;
                }
            }
        }

        return totalMRR / data.size();
    }
}
