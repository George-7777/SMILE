package com.example.smile.ai;

import java.util.List;

public interface AIProvider {
    void generateResponse(String userMessage, List<String> conversationHistory, AIResponseCallback callback);

    interface AIResponseCallback {
        void onSuccess(String response);
        void onError(Exception e);
    }
}

