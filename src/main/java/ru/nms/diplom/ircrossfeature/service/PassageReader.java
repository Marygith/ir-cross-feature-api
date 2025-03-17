package ru.nms.diplom.ircrossfeature.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static ru.nms.diplom.ircrossfeature.util.Constants.EMBEDDINGS_FILE;
import static ru.nms.diplom.ircrossfeature.util.Constants.VECTOR_SIZE;

public class PassageReader {

    private final Map<Integer, float[]> idToVector;

    public PassageReader() {
        this.idToVector = new HashMap<>();
        try {
            loadCSV(EMBEDDINGS_FILE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadCSV(String csvPath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String line = br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != VECTOR_SIZE + 1) {
                    throw new IllegalArgumentException("Invalid vector length for line: " + line);
                }

                // Parse ID and vector
                Integer id = Integer.parseInt(parts[0]);
                float[] vector = new float[VECTOR_SIZE];
                for (int i = 0; i < VECTOR_SIZE; i++) {
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

        try {
            // Load the CSV file
            reader.loadCSV(EMBEDDINGS_FILE);
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
