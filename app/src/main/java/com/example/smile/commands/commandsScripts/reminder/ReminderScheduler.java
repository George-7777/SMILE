package com.example.smile.commands.commandsScripts.reminder;


import static android.app.PendingIntent.FLAG_IMMUTABLE;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.smile.services.MyReceiver;

import java.util.Calendar;

public class ReminderScheduler {
    private final Context context;

    public ReminderScheduler(Context context) {
        this.context = context.getApplicationContext();
    }

    public void scheduleReminder(ReminderData data) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        long triggerTime = calculateTriggerTime(data, calendar);

        Intent notifyIntent = new Intent(context, MyReceiver.class);
        notifyIntent.putExtra("name", data.getMessage());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE
        );

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    }

    private long calculateTriggerTime(ReminderData data, Calendar calendar) {
        switch (data.getType()) {
            case MINUTES:
                calendar.add(Calendar.MINUTE, data.getMinutes());
                return calendar.getTimeInMillis();
            case HOURS:
                calendar.add(Calendar.HOUR_OF_DAY, data.getHours());
                return calendar.getTimeInMillis();
            case TIME_STRING:
                calendar.set(Calendar.HOUR_OF_DAY, data.getHour());
                calendar.set(Calendar.MINUTE, data.getMinute());
                if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                }
                return calendar.getTimeInMillis();
            default:
                return System.currentTimeMillis() + 10 * 60 * 1000;
        }
    }

    public String getFormattedTriggerTime(ReminderData data) {
        Calendar calendar = Calendar.getInstance();
        long triggerTime = calculateTriggerTime(data, calendar);
        return new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date(triggerTime));
    }
}
