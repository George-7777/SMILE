package com.gvayt.smile.model.tts;


import java.util.Locale;

public interface TTSManager {
    void speak(String text);
    void setLocale(Locale locale);
    void shutdown();
}
