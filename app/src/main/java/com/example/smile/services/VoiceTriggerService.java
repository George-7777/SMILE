package com.example.smile.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.smile.R;

import java.util.ArrayList;

public class VoiceTriggerService extends Service {
    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private boolean isListening = false;
    private final String triggerPhrase = "смайлик";
    public static final String ACTION_TRIGGER_DETECTED = "com.yourpackage.ACTION_TRIGGER_DETECTED";
    public static final String EXTRA_TRIGGER_PHRASE = "СМАЙЛИК";
    public static final String EXTRA_SOS_PHRASE = "сос";

    @Override
    public void onCreate() {
        super.onCreate();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        initializeSpeechRecognizer();
        startForegroundServiceNotification();
    }

    private void initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d("VoiceTrigger", "Готов к распознаванию");
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d("VoiceTrigger", "Начало речи");
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // Визуализация уровня звука (опционально)
            }

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                Log.d("VoiceTrigger", "Конец речи");
            }

            @Override
            public void onError(int error) {
                String errorMessage;
                switch (error) {
                    case SpeechRecognizer.ERROR_AUDIO:
                        errorMessage = "Ошибка аудио";
                        break;
                    case SpeechRecognizer.ERROR_CLIENT:
                        errorMessage = "Ошибка клиента";
                        break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        errorMessage = "Недостаточно прав";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK:
                        errorMessage = "Ошибка сети";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        errorMessage = "Таймаут сети";
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        errorMessage = "Не распознано";
                        break;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        errorMessage = "Распознаватель занят";
                        break;
                    case SpeechRecognizer.ERROR_SERVER:
                        errorMessage = "Ошибка сервера";
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        errorMessage = "Таймаут речи";
                        break;
                    default:
                        errorMessage = "Неизвестная ошибка";
                }
                Log.e("VoiceTrigger", "Ошибка распознавания: " + errorMessage);
                restartListening();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                );

                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0).toLowerCase();
                    Log.d("VoiceTrigger", "Распознано: " + spokenText);

                    // Проверяем триггерную фразу
                    if (spokenText.contains(triggerPhrase.toLowerCase())) {
                        onTriggerDetected(spokenText);
                    }

                    if (spokenText.contains(EXTRA_SOS_PHRASE) || spokenText.contains("sons") || spokenText.contains("sos")){
                        Context context = getApplicationContext();
                        SharedPreferences sharedPref = context.getSharedPreferences(
                                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                        dialPhoneNumber(sharedPref.getString("NUMBER", "89124661718"));
                    }
                }

                restartListening();
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        // Настраиваем intent для распознавания
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);



        // Для отключения звука начала записи
        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_SIL, 0);

        startListening();
    }
    public void dialPhoneNumber(String phoneNumber) {
        System.out.println("SOOOOOOOOOOOOOOOOOS");
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" +phoneNumber));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void startForegroundServiceNotification() {
        NotificationChannel channel = new NotificationChannel(
                "voice_assistant_channel",
                "СМАЙЛИК",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Фоновое прослушивание триггерной фразы");

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        Notification notification = new NotificationCompat.Builder(this, "voice_assistant_channel")
                .setContentTitle("СМАЙЛИК")
                .setContentText("Ожидание: \"" + triggerPhrase + "\"")
                .setSmallIcon(R.drawable.__2025_10_14_215029) // Добавьте свою иконку
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();

        startForeground(1, notification);
    }

    private void startListening() {

            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            // Сохраняем текущие настройки
            int originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);

            // Отключаем системные звуки
            audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);

            // После распознавания возвращаем
            new Handler().postDelayed(() -> {
                audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, originalVolume, 0);
            }, 3000);

//        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        int current_volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        audio.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        if (!isListening) {
            try {
                speechRecognizer.startListening(recognizerIntent);
                isListening = true;
                Log.d("VoiceTrigger", "Начато прослушивание");
            } catch (Exception e) {
                Log.e("VoiceTrigger", "Ошибка запуска: " + e.getMessage());
            }
        }
    }

    private void stopListening() {
        if (isListening && speechRecognizer != null) {
            speechRecognizer.stopListening();
            isListening = false;
        }
        //AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //audio.setStreamVolume(AudioManager.STREAM_MUSIC, audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
               // AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

    private void restartListening() {
        stopListening();
        // Задержка перед перезапуском
        new Handler(Looper.getMainLooper()).postDelayed(this::startListening, 500);
    }

    private void onTriggerDetected(String spokenText) {
        Log.d("VoiceTrigger", "Триггерная фраза обнаружена: " + spokenText);
        stopListening();
        sendTriggerBroadcast(spokenText);
        // звуковой сигнал
        //playNotificationSound();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startListening();
        }, 5000);
    }

    private void sendTriggerBroadcast(String spokenText) {
        Intent broadcastIntent = new Intent(ACTION_TRIGGER_DETECTED);
        broadcastIntent.putExtra(EXTRA_TRIGGER_PHRASE, spokenText);
        broadcastIntent.putExtra("timestamp", System.currentTimeMillis());

        // Отправляем broadcast
        sendBroadcast(broadcastIntent);

        // Для дополнительной безопасности можно использовать
        // sendBroadcast(broadcastIntent, "com.yourpackage.permission.VOICE_TRIGGER");

        Log.d("VoiceTrigger", "Broadcast отправлен");
    }

    private void playNotificationSound() {
//        try {
//            // Используем системный звук или ваш собственный
//            Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//            Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notificationUri);
//            if (ringtone != null) {
//                ringtone.play();
//            }
//        } catch (Exception e) {
//            Log.e("VoiceTrigger", "Ошибка воспроизведения звука: " + e.getMessage());
//        }
    }

    @Override
    public void onDestroy() {
        stopListening();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Статический метод для запуска сервиса
    public static void startService(Context context) {
        Intent serviceIntent = new Intent(context, VoiceTriggerService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            System.out.println("printtt");
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    public static void stopService(Context context) {
        Intent serviceIntent = new Intent(context, VoiceTriggerService.class);
        context.stopService(serviceIntent);
    }
}
