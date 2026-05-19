package com.example.smile.ai;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.smile.Secret;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;

import java.util.List;

// AIProvider.java (интерфейс)
public interface AIProvider {
    void generateResponse(String userMessage, List<String> conversationHistory, AIResponseCallback callback);

    interface AIResponseCallback {
        void onSuccess(String response);
        void onError(Exception e);
    }
}

