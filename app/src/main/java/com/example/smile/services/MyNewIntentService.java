package com.example.smile.services;

import android.Manifest;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;

import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationManagerCompat;

import com.example.smile.R;
import com.example.smile.tts.TTSManager;
import com.example.smile.ui.MainActivity;

public class MyNewIntentService extends IntentService {
    private static final int NOTIFICATION_ID = 3;
    private TTSManager ttsManager;

    public MyNewIntentService() {
        super("MyNewIntentService");
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @Override
    protected void onHandleIntent(Intent intent) {
        String name = intent.getStringExtra("name");
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Напоминание");
        builder.setContentText(name);
        builder.setSmallIcon(R.drawable.__2025_10_14_215029);
        NotificationChannel channel = new NotificationChannel("7", "Напоминания", NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableLights(true);
        channel.setLightColor(Color.green(1));
        channel.setShowBadge(true);
        manager.createNotificationChannel(channel);
        builder.setChannelId("7");

        Intent notifyIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 2, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        //to be able to launch your activity from the notification
        builder.setContentIntent(pendingIntent);
        Notification notificationCompat = builder.build();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);

        managerCompat.notify(NOTIFICATION_ID, notificationCompat);

        ttsManager = new TTSManager(getApplicationContext(), new TTSManager.TTSListener() {
            @Override
            public void onInit() {
                ttsManager.speak("Напоминаю " + name);
            }

            @Override public void onSpeakStart(String utteranceId) {}
            @Override public void onSpeakDone(String utteranceId) {}
            @Override public void onError(String utteranceId) {}
        });

    }
}