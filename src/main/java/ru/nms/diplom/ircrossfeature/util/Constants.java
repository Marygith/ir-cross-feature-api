package ru.nms.diplom.ircrossfeature.util;

public class Constants {
    public static final int VECTOR_SIZE = 768;
    public static final int RECORD_SIZE = 8 + 4 * VECTOR_SIZE; // ID (8 bytes) + Vector (756 floats)
    public static final long CHUNK_SIZE = (2L * 1024 * 1024 * 1024) / RECORD_SIZE; // Max records per chunk


    public static final String DATA_FILE = "D:\\diplom\\data\\passage_data.bin";
    public static final String INDEX_FILE = "D:\\diplom\\data\\passage_index.txt";
    public static final String FEATURE_STORE_FILE = "D:\\diplom\\data\\feature_store.bin";
}
