package com.example.smile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

public class MyReceiver extends BroadcastReceiver {
    public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent intent1 = new Intent(context, MyNewIntentService.class);
        intent1.putExtra("name", intent.getStringExtra("name"));
        context.startService(intent1);
        //ContextCompat.startForegroundService(context.getApplicationContext(), intent1);
    }
}