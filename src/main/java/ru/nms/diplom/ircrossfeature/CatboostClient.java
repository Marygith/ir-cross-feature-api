package ru.nms.diplom.ircrossfeature;

import ai.catboost.CatBoostModel;
import ai.catboost.CatBoostPredictions;

import java.io.File;

public class CatboostClient {
    public static void main(String[] args) {
        try (CatBoostModel model = CatBoostModel.loadModel("D:\\diplom\\data_v2\\catboost_model.bin")) {

            // Example input: [faiss_score, lucene_score, query_length, doc_length]
            float[] inputFeatures = {0.902f, 0.5719f, 27.0f, 52.0f};

            CatBoostPredictions predictions = model.predict(inputFeatures, (String[]) null);

            double logit = predictions.get(0, 0);

            double relevanceScore = 1 / (1 + Math.exp(-logit));
            System.out.println("Predicted Relevance Score: " + relevanceScore);
            System.out.println("Predicted logit: " + logit);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
