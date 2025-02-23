package ru.nms.diplom.ircrossfeature.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PassageReader {

    private final Map<Integer, float[]> idToVector;

    public PassageReader() {
        this.idToVector = new HashMap<>();
        try {
            loadCSV("D:\\diplom\\data\\passage_data.csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadCSV(String csvPath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String line = br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 769) {
                    throw new IllegalArgumentException("Invalid vector length for line: " + line);
                }

                // Parse ID and vector
                Integer id = Integer.parseInt(parts[0]);
                float[] vector = new float[768];
                for (int i = 0; i < 768; i++) {
                    vector[i] = Float.parseFloat(parts[i + 1]);
                }

                // Store in the map
                idToVector.put(id, vector);
            }
        }
    }

    public float[] getVectorById(Integer id) {
        return idToVector.get(id);
    }

    public static void main(String[] args) {
        PassageReader reader = new PassageReader();
        String csvPath = "D:\\diplom\\data\\passage_data.csv";

        try {
            // Load the CSV file
            reader.loadCSV(csvPath);
            System.out.println("CSV loaded successfully.");

            // Example: Retrieve a vector by ID
            Integer testId = 12345; // Replace with a valid ID
            float[] vector = reader.getVectorById(testId);

            if (vector != null) {
                System.out.println("Vector for ID " + testId + ": " + Arrays.toString(vector));
            } else {
                System.out.println("No vector found for ID " + testId);
            }

        } catch (IOException e) {
            System.err.println("Error loading CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
