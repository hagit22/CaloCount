package com.example.calocount.utils;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.FutureCallback;
import android.util.Log;

public class CalorieEstimator {
    private GenerativeModel gm;
    private Context context;

    public CalorieEstimator(Context context) {
        gm = new GenerativeModel("gemini-1.5-flash", "AIzaSyBcAfi6sLwV1gEqRGHBe77eoY--ZZvwwOs");
        this.context = context;
    }

    public void estimateCalories(String foodDescription, CalorieListener calorieListener) {
        Log.d("CalorieEstimator", "Sending request to AI: " + foodDescription);

        String prompt = "Estimate calories for: " + foodDescription +
                ". Respond with only a number, no text.";

        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                Log.d("CalorieEstimator", "AI response received: " + result.getText() + " for " + foodDescription);
                String calorieText = result.getText().trim();
                try {
                    int calories = Integer.parseInt(calorieText.replaceAll("[^0-9]", ""));
                    calorieListener.onSuccess(calories);
                }
                catch (NumberFormatException e) {
                    calorieListener.onError("Couldn't parse calories");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("CalorieEstimator", "AI request failed: " + t.getMessage());
                calorieListener.onError(t.getMessage());
            }
        }, ContextCompat.getMainExecutor(context));
    }

    public interface CalorieListener {
        void onSuccess(int calories);
        void onError(String error);
    }
}
