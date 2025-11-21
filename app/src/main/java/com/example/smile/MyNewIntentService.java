package com.example.smile;

import android.Manifest;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.speech.tts.TextToSpeech;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Locale;

public class MyNewIntentService extends IntentService {
    private static final int NOTIFICATION_ID = 3;
    private TextToSpeech textToSpeech;

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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("1","Напоминания", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true);
            channel.setLightColor(Color.green(1));
            channel.setShowBadge(true);
            manager.createNotificationChannel(channel);
            builder.setChannelId("1");
        }

        Intent notifyIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 2, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        //to be able to launch your activity from the notification
        builder.setContentIntent(pendingIntent);
        Notification notificationCompat = builder.build();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);

        managerCompat.notify(NOTIFICATION_ID, notificationCompat);
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR)
                {
                    textToSpeech.setLanguage(Locale.getDefault());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak("Напоминаю " + name,TextToSpeech.QUEUE_FLUSH,null,null);
                    } else {
                        textToSpeech.speak("Напоминаю " + name, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            }
        });
    }
}