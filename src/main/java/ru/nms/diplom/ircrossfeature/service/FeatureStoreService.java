package ru.nms.diplom.ircrossfeature.service;

import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.util.*;

public class FeatureStoreService {
    private static final String FEATURE_STORE_BIN = "D:\\diplom\\data_v2\\feature_store.bin"; // Binary file
    private static final String FEATURE_STORE_CSV = "D:\\diplom\\data_v2\\feature_store.csv"; // CSV file
    private final Map<Integer, Integer> passageToLength = new HashMap<>();

    public FeatureStoreService() {
        try (BufferedReader br = new BufferedReader(new FileReader("D:\\diplom\\data\\all_passages.csv"))) {
            String line;
            int i = 0;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] args = line.split("\\^");
                passageToLength.put(Integer.parseInt(args[1]), args[0].replaceAll("\"", "").split("\\s+").length);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void storeResults(
            Map<String, Map<Integer, Float>> resultFromFaiss,
            Map<String, Map<Integer, Float>> resultFromLucene,
            Map<String, Integer> queryToPassages
    ) throws IOException {
        // Open both binary and CSV writers
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(FEATURE_STORE_BIN, true));
             BufferedWriter writer = new BufferedWriter(new FileWriter(FEATURE_STORE_CSV, true))) {

            // Write CSV header if file is empty
            File csvFile = new File(FEATURE_STORE_CSV);
            if (csvFile.length() == 0) {
                writer.write("query^passage_id^relevant^faiss_score^lucene_score^query_length^doc_length\n");
            }

            Set<String> queries = new HashSet<>(resultFromFaiss.keySet());
            queries.addAll(resultFromLucene.keySet());

            for (String query : queries) {
                if (!resultFromFaiss.containsKey(query) || !resultFromLucene.containsKey(query)) {
                    System.out.println("Query " + query + " is missing in one of the maps.");
                    continue;
                }

                Map<Integer, Float> faissScores = resultFromFaiss.get(query);
                Map<Integer, Float> luceneScores = resultFromLucene.get(query);

                if (!faissScores.keySet().equals(luceneScores.keySet())) {
                    throw new IllegalStateException("Passage ID sets do not match for query: " + query);
                }

                // Get relevant passage ID
                int relevantPassageId = queryToPassages.get(query);
                int queryLength = query.length();

                // Write to binary file (original format)
                dos.writeInt(relevantPassageId);
                dos.writeInt(faissScores.keySet().size());

                // Process each passage
                for (Integer passageId : faissScores.keySet()) {
                    float faissScore = faissScores.get(passageId);
                    float luceneScore = luceneScores.get(passageId);


                    // Write to binary file
                    dos.writeInt(passageId);
                    dos.writeFloat(faissScore);
                    dos.writeFloat(luceneScore);

                    writer.write(String.format("%s^%d^%d^%s^%s^%d^%d\n",
                            query, passageId, (passageId == relevantPassageId ? 1 : 0),
                            String.format(Locale.US, "%.6f", faissScore),
                            String.format(Locale.US, "%.6f", luceneScore),
                            queryLength, passageToLength.get(passageId)));
                }
            }
        }
    }
}
