package com.gvayt.smile.model.tts;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;

import com.gvayt.smile.services.VoiceTriggerService;

import java.util.Locale;
import java.util.Set;

public class TTSManagerDefault implements TTSManager {
    private TextToSpeech textToSpeech;
    private Context context;
    private TTSListener listener;
    private Locale language;

    public interface TTSListener {
        void onInit();
        void onSpeakStart(String utteranceId);
        void onSpeakDone(String utteranceId);
        void onError(String utteranceId);
    }

    public TTSManagerDefault(Context context, TTSListener listener, Locale lang) {
        this.context = context;
        this.listener = listener;
        this.language = lang;
        initTTS(lang);
    }

    private void initTTS(Locale lang) {
        textToSpeech = new TextToSpeech(context, status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(lang);
                setupVoice();
                textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        if (listener != null) listener.onSpeakStart(utteranceId);
                        Log.d("test", "govor");
                        VoiceTriggerService.setSpeakingState(true);
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        if (listener != null) listener.onSpeakDone(utteranceId);
                        Log.d("test", "notGovor");
                        VoiceTriggerService.setSpeakingState(false);
                    }

                    @Override
                    public void onError(String utteranceId) {
                        if (listener != null) listener.onError(utteranceId);
                        VoiceTriggerService.setSpeakingState(false);
                    }
                });
                listener.onInit();
            }
        });
    }

    private void setupVoice() {
        Set<Voice> voices = textToSpeech.getVoices();

        // Вывести все доступные голоса для отладки
        for (Voice voice : voices) {
            if(voice.getName().contains(language.getLanguage().toLowerCase())) {
                Log.d("TTS Voice", "Name: " + voice.getName() +
                        ", Locale: " + voice.getLocale() +
                        ", Features: " + voice.getFeatures());
            }
        }

        Voice selectedVoice = null;
        for (Voice voice : voices) {
            if (voice.getName().contains(language.getLanguage().toLowerCase())) {
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
        speak(text, "some id");
    }

    @Override
    public void setLocale(Locale locale) {
        language = locale;
    }

    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
