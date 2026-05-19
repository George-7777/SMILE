package com.example.smile.tts;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;

import java.util.Locale;
import java.util.Set;

// TTSManager.java
public class TTSManager {
    private TextToSpeech textToSpeech;
    private Context context;
    private TTSListener listener;

    public interface TTSListener {
        void onSpeakDone(String utteranceId);
    }

    public TTSManager(Context context, TTSListener listener) {
        this.context = context;
        this.listener = listener;
        initTTS();
    }

    private void initTTS() {
        textToSpeech = new TextToSpeech(context, status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(new Locale("RU"));
                setupVoice();
                speak("Привет! Чем могу сегодня помочь?");
            }
        });

        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {}

            @Override
            public void onDone(String utteranceId) {
                if (listener != null) listener.onSpeakDone(utteranceId);
            }

            @Override
            public void onError(String utteranceId) {}
        });
    }

    private void setupVoice() {
        Set<Voice> voices = textToSpeech.getVoices();

        // Вывести все доступные голоса для отладки
        for (Voice voice : voices) {
            if(voice.getName().contains("ru")) {
                Log.d("TTS Voice", "Name: " + voice.getName() +
                        ", Locale: " + voice.getLocale() +
                        ", Features: " + voice.getFeatures());
            }
        }

        // Выбрать голос по имени или характеристикам
        Voice selectedVoice = null;
        for (Voice voice : voices) {
            // Пример выбора по различным критериям
            if (voice.getName().contains("ru")) {
                System.out.println(voice.getLocale().getCountry());
                selectedVoice = voice;
                break;
            }
        }

        if (selectedVoice != null) {
            textToSpeech.setVoice(selectedVoice);
        }
        // TODO: выбрать голос, конкретный
    }

    public void speak(String text, String utteranceId) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    public void speak(String text) {
        speak(text, null);
    }

    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
