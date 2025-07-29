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
import android.os.Handler;
import android.os.Looper;

public class CalorieEstimator {

    private static final int SHORT_RETRY_ATTEMPTS = 3;
    private static final long SHORT_DELAY = 2000;       // 2 seconds
    private static final long LONG_DELAY = 30000;       // 30 seconds

    private GenerativeModel gm;
    private Context context;

    public CalorieEstimator(Context context) {
        gm = new GenerativeModel("gemini-1.5-flash", "AIzaSyBcAfi6sLwV1gEqRGHBe77eoY--ZZvwwOs");
        this.context = context;
    }

    public void estimateCalories(String foodDescription, CalorieListener calorieListener) {
        estimateWithRetry(foodDescription, calorieListener, 0, false);
    }

    private void estimateWithRetry(String foodDescription, CalorieListener calorieListener,
                                   int attemptNumber, boolean isLongTermRetry) {
        Log.d("CalorieEstimator", "Attempt " + (attemptNumber + 1) + " - Sending request to AI: " + foodDescription);

        String prompt = "Estimate calories for: " + foodDescription + ". Respond with only a number, no text.";

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
                //calorieListener.onError(t.getMessage());

                // Retries mechanism
                handleFailure(t, foodDescription, calorieListener, attemptNumber, isLongTermRetry);
            }
        }, ContextCompat.getMainExecutor(context));
    }

    private void handleFailure(Throwable t, String foodDescription, CalorieListener calorieListener,
                               int attemptNumber, boolean isLongTermRetry) {
        String error = t.getMessage();
        Log.e("CalorieEstimator", "AI request failed (attempt " + (attemptNumber + 1) + "): " + error);

        if (isServerError(error)) {
            if (!isLongTermRetry && attemptNumber < SHORT_RETRY_ATTEMPTS - 1) {
                // Short-term retries
                long delay = SHORT_DELAY * (long)Math.pow(2, attemptNumber);
                Log.d("CalorieEstimator", "Short retry in " + delay + "ms...");

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    estimateWithRetry(foodDescription, calorieListener, attemptNumber + 1, false);
                }, delay);
            }
            else {
                // Long-term periodic retries
                Log.d("CalorieEstimator", "Switching to periodic retries every " + (LONG_DELAY/1000) + " seconds...");

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    estimateWithRetry(foodDescription, calorieListener, 0, true);
                }, LONG_DELAY);
            }
        }
        else {
            calorieListener.onError("Calorie estimation failed: " + error);
        }
    }

    private boolean isServerError(String error) {
        return error != null &&
                (error.contains("overloaded") ||
                        error.contains("503") ||
                        error.contains("UNAVAILABLE") ||
                        error.contains("timeout") ||
                        error.contains("502") ||
                        error.contains("504"));
    }

    public interface CalorieListener {
        void onSuccess(int calories);
        void onError(String error);
    }
}
