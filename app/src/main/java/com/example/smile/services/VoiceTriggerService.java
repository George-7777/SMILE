package com.example.smile.services;

import static com.example.smile.Constant.SOS_WORD;
import static com.example.smile.Constant.WAKE_WORD;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.smile.R;
import com.example.smile.data.PreferencesManager;
import com.example.smile.tts.TTSManager;
import com.example.smile.ui.CallActivity;

import org.json.JSONObject;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.StorageService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VoiceTriggerService extends Service {
    private static final String TAG = "VoiceTrigger";

    // Vosk components
    private Model voskModel;
    private Recognizer recognizer;
    private AudioRecord audioRecord;
    private HandlerThread audioThread;
    private boolean isListening = false;

    // Audio parameters
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    public static final String ACTION_TRIGGER_DETECTED = "com.smile.ACTION_TRIGGER_DETECTED";
    public static final String EXTRA_TRIGGER_PHRASE = "trigger_phrase";
    public static final String MODEL_NAME_RUS = "vosk-model-small-ru-0.22";

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private PreferencesManager preferencesManager;
    private static boolean isSpeaking;
    public static final TTSManager.TTSListener ttsStateListener = new TTSManager.TTSListener() {
        @Override
        public void onInit() {

        }

        @Override
        public void onSpeakStart(String utteranceId) {
            isSpeaking = true;
            Log.d(TAG, "TTS начал говорить, wake word отключен");
        }

        @Override
        public void onSpeakDone(String utteranceId) {
            isSpeaking = false;
            Log.d(TAG, "TTS закончил говорить, wake word включен");
        }

        @Override
        public void onError(String utteranceId) {
            isSpeaking = false;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Нет разрешения на запись аудио");
            return;
        }

        preferencesManager = new PreferencesManager(this);
        initVosk();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForegroundService();
        }
        startListening();
    }

    private void initVosk() {
        StorageService.unpack(this, MODEL_NAME_RUS, MODEL_NAME_RUS,
                (model) -> {
                    try {
                        voskModel = model;
                        recognizer = new Recognizer(voskModel, SAMPLE_RATE);
                        setupGrammar();
                        Log.d(TAG, "Vosk инициализирован успешно");
                    } catch (IOException e) {
                        Log.e(TAG, "Ошибка загрузки модели: ", e);
                        stopSelf();
                    }
                },
                (exception) -> {
                    Log.e("Vosk", "Ошибка загрузки модели: " + exception.getMessage());
                    stopSelf();
                }
        );

    }

    private void setupGrammar() {
        List<String> grammarWords = new ArrayList<>();
        grammarWords.add(WAKE_WORD);
        grammarWords.add(SOS_WORD);

        grammarWords.add("[unk]");

        StringBuilder grammarJson = new StringBuilder("[");
        for (int i = 0; i < grammarWords.size(); i++) {
            if (i > 0) grammarJson.append(",");
            grammarJson.append("\"").append(grammarWords.get(i)).append("\"");
        }
        grammarJson.append("]");

        recognizer.setGrammar(grammarJson.toString());
        Log.d(TAG, "Grammar настроен: " + grammarJson);
    }

    private void startListening() {
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // человек должен был это разрешение дать ещё при входе в приложение
            return;
        }
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
        );

        audioThread = new HandlerThread("VoskAudioThread");
        audioThread.start();
        Handler audioHandler = new Handler(audioThread.getLooper());

        audioRecord.startRecording();
        isListening = true;

        audioHandler.post(new Runnable() {
            private final byte[] buffer = new byte[bufferSize];

            @Override
            public void run() {
                while (isListening) {
                    int bytesRead = audioRecord.read(buffer, 0, buffer.length);
                    if (bytesRead > 0 && recognizer != null) {
                        if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                            String result = recognizer.getResult();
                            processResult(result);
                        } else {
                            String partial = recognizer.getPartialResult();
                            if (partial != null && !partial.isEmpty()) {
                                processPartialResult(partial);
                            }
                        }
                    }
                }
            }
        });

        Log.d(TAG, "Начато прослушивание через Vosk");
    }

    private void processResult(String result) {
        try {
            JSONObject json = new JSONObject(result);
            String text = json.optString("text", "").toLowerCase();

            if (!text.isEmpty()) {
                Log.d(TAG, "Распознано (final): " + text);
                checkForWakeWords(text);
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка обработки результата", e);
        }
    }

    private void processPartialResult(String partial) {
        try {
            JSONObject json = new JSONObject(partial);
            String text = json.optString("partial", "").toLowerCase();

            if (!text.isEmpty()) {
                Log.d(TAG, "Распознано (partial): " + text);
                checkForWakeWords(text);
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка обработки partial результата", e);
        }
    }

    private void checkForWakeWords(String text) {
        if (text.contains(WAKE_WORD.toLowerCase())) {
            if (isSpeaking) {
                Log.d(TAG, "Wake word ignored (TTS is speaking): " + text);
                return;
            }
            Log.d(TAG, "Услышано: " + text);
            onWakeWordDetected();
        } else if (text.contains(SOS_WORD.toLowerCase()) ||
                text.contains("sos") ||
                text.contains("sons")) {
            onSosDetected();
        }
    }

    private void onWakeWordDetected() {
        Log.d(TAG, "Wake word detected: " + WAKE_WORD);

        stopListening();
        Log.d("test", "wakeword");

        sendTriggerBroadcast(WAKE_WORD);

        mainHandler.postDelayed(() -> {
            if (!isListening) {
                startListening();
            }
        }, 5000);
    }

    private void onSosDetected() {
        Log.d(TAG, "SOS detected!");
        stopListening();

        String phoneNumber = preferencesManager.getSosNumber();
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            dialPhoneNumber(phoneNumber);
        }

        sendTriggerBroadcast(SOS_WORD);

        mainHandler.postDelayed(() -> {
            if (!isListening) {
                startListening();
            }
        }, 30000);
    }

    private void dialPhoneNumber(String phoneNumber) {
        Log.d(TAG, "Звонок...");
        try {
            Intent intent = new Intent(this, CallActivity.class);
            intent.putExtra("phone_number", phoneNumber);

            ActivityOptions options = ActivityOptions.makeBasic();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                options.setPendingIntentBackgroundActivityStartMode(
                        ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED);
            }

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE,
                    options.toBundle()
            );

            @SuppressLint("FullScreenIntentPolicy") Notification notification = new NotificationCompat.Builder(this, "sos_channel")
                    .setContentTitle("ЭКСТРЕННЫЙ ВЫЗОВ")
                    .setContentText("Совершается звонок родителям...")
                    .setSmallIcon(R.drawable.baseline_mic_24)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setFullScreenIntent(pendingIntent, true)
                    .setAutoCancel(true)
                    .setCategory(NotificationCompat.CATEGORY_CALL)
                    .build();

            NotificationManager manager = getSystemService(NotificationManager.class);
            NotificationChannel channel = new NotificationChannel(
                    "sos_channel",
                    "SOS вызовы",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(channel);

            manager.notify(999, notification);

        } catch (Exception e) {
            Log.e(TAG, "Ошибка звонка: " + e.getMessage());
        }
    }

    private void sendTriggerBroadcast(String spokenText) {
        Intent broadcastIntent = new Intent(ACTION_TRIGGER_DETECTED);
        broadcastIntent.putExtra(EXTRA_TRIGGER_PHRASE, spokenText);
        broadcastIntent.putExtra("timestamp", System.currentTimeMillis());
        sendBroadcast(broadcastIntent);
    }

    private void stopListening() {
        isListening = false;

        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }

        if (audioThread != null) {
            audioThread.quitSafely();
            audioThread = null;
        }

        if (recognizer != null) {
            recognizer.reset();
        }

        Log.d(TAG, "Прослушивание остановлено");
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void startForegroundService() {
        NotificationChannel channel = new NotificationChannel(
                "voice_assistant_channel",
                "СМАЙЛИК",
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        Notification notification = new NotificationCompat.Builder(this, "voice_assistant_channel")
                .setContentTitle("СМАЙЛИК")
                .setContentText("Ожидание: \"СМАЙЛИК\" или \"СОС\"")
                .setSmallIcon(R.drawable.baseline_mic_24)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();

        int serviceTypes = 0;
        serviceTypes = ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE;

        try {
            Log.d(TAG, "Получилось запустить foreground с типами");
            startForeground(7, notification, serviceTypes);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при старте foreground сервиса с типами", e);
            startForeground(7, notification);
        }
    }

    @Override
    public void onDestroy() {
        stopListening();

        if (recognizer != null) {
            recognizer.close();
        }

        if (voskModel != null) {
            voskModel.close();
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void startService(Context context) {
        Intent serviceIntent = new Intent(context, VoiceTriggerService.class);
        context.startForegroundService(serviceIntent);
    }

    public static void stopService(Context context) {
        Intent serviceIntent = new Intent(context, VoiceTriggerService.class);
        context.stopService(serviceIntent);
    }
    public static void setSpeakingState(boolean speaking) {
        isSpeaking = speaking;
        Log.d(TAG, "Speaking state changed to: " + speaking);
    }
}