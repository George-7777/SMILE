package com.gvayt.smile.model.ai;

import java.util.List;

public interface AIProvider {
    // TODO: перенести ИИ на сервер
    void generateResponse(String userMessage, List<String> conversationHistory, AIResponseCallback callback);

    interface AIResponseCallback {
        void onSuccess(String response);
        void onError(Exception e);
    }
}

