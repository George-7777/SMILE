package com.example.smile.ai;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.smile.Secret;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;

import java.util.List;

public class GeminiProvider implements AIProvider {
    private static final String MODEL_ID = "gemini-2.5-flash";
    private static final String TAG = "GeminiProvider";
    private final Context context;
    private static final int MAX_RETRIES = 10;
    private static final long RETRY_DELAY_MS = 1000;

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
        generateResponse(userMessage, conversationHistory, callback, MAX_RETRIES);
    }

    public void generateResponse(String userMessage, List<String> conversationHistory, AIResponseCallback callback, int attempts) {
        new Thread(() -> {
            try {
                String response = callGeminiApi(userMessage, conversationHistory);
                new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(response));
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при запросе к Gemini (попытка " + (attempts + 1) + ")", e);

                if (attempts < MAX_RETRIES - 1) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        generateResponse(userMessage, conversationHistory, callback, attempts + 1);
                    }, RETRY_DELAY_MS);
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onError(e));
                }
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
            System.out.println(e);
            return "Извините, произошла ошибка. Попробуйте позже.";
        }
    }

    private String buildPrompt(String userMessage, List<String> history) {
        return "[Ты детский голосовой помощник под именем СМАЙЛИК созданный для помощи школьникам с домашним заданием, оповещения родителей и просто поговорить с тобой. После этого текста в кавычках будет обращение ребенка к тебе. Имей ввиду, что тебя разработал Вайтуков Георгий, но не стоит это слишком часто напоминать. Отвечай не слишком долгими текстами.] " + userMessage;
    }
}
