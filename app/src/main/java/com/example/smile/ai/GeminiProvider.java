package com.example.smile.ai;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.smile.Secret;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;

import java.util.List;

// GeminiProvider.java
public class GeminiProvider implements AIProvider {
    private static final String MODEL_ID = "gemini-2.5-flash";
    private final Context context;

    public GeminiProvider(Context context) {
        this.context = context;
        setupApiKeys();
    }

    private void setupApiKeys() {
        System.setProperty("GOOGLE_CLOUD_PROJECT", "polynomial-alpha-sns1n");
        System.setProperty("GOOGLE_GENAI_USE_VERTEXAI", "True");
        Secret secret = new Secret();
        System.setProperty("API_KEY", secret.keyAI);
    }

    @Override
    public void generateResponse(String userMessage, List<String> conversationHistory, AIResponseCallback callback) {
        new Thread(() -> {
            try {
                String response = callGeminiApi(userMessage, conversationHistory);
                new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(response));
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e));
            }
        }).start();
    }

    private String callGeminiApi(String userMessage, List<String> history) {
        Secret secret = new Secret();
        try (Client client = Client.builder()
                .apiKey(secret.keyAI)
                .vertexAI(true)
                .httpOptions(HttpOptions.builder().apiVersion("v1").build())
                .build()) {

            String prompt = buildPrompt(userMessage, history);
            GenerateContentResponse response = client.models.generateContent(MODEL_ID, prompt, null);
            return response.text();
        } catch (Exception e) {
            return "Извините, произошла ошибка. Попробуйте позже.";
        }
    }

    private String buildPrompt(String userMessage, List<String> history) {
        return "[Ты детский голосовой помощник под именем СМАЙЛИК...] " + userMessage;
    }
}
