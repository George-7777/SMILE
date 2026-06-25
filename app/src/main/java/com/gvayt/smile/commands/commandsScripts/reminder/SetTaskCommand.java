package com.gvayt.smile.commands.commandsScripts.reminder;

import android.content.Context;

import com.gvayt.smile.R;
import com.gvayt.smile.commands.VoiceCommand;
import com.gvayt.smile.contract.MainContract;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SetTaskCommand implements VoiceCommand {
    private final Context context;
    private final MainContract.View view;
    private final ReminderScheduler reminderScheduler;

    public SetTaskCommand(Context context, MainContract.View view, ReminderScheduler reminderScheduler) {
        this.context = context;
        this.view = view;
        this.reminderScheduler = reminderScheduler;
    }

    @Override
    public boolean matches(String voiceRequest) {
        String lowerCommand = voiceRequest.toLowerCase();
        return lowerCommand.startsWith("напомни") || lowerCommand.startsWith("напомнить");
    }

    @Override
    public void execute(String voiceRequest) {
        ReminderData data = parseReminder(voiceRequest);

        if (data.getMessage() == null || data.getMessage().isEmpty()) {
            view.addDialog(context.getString(R.string.dialog_reminder_failed), MainContract.DialogRole.SMILE);
            return;
        }

        reminderScheduler.scheduleReminder(data);
        String timeStr = reminderScheduler.getFormattedTriggerTime(data);

        view.addDialog(context.getString(R.string.dialog_reminder_success, data.getMessage(), timeStr), MainContract.DialogRole.SMILE);
    }


    // Парсер из запроса
    private ReminderData parseReminder(String command) {
        ReminderData data = new ReminderData();
        String lowerCommand = command.toLowerCase();

        String reminderText = command;

        Pattern minutesPattern = Pattern.compile("через\\s+(\\d+)\\s+минут");
        Matcher minutesMatcher = minutesPattern.matcher(lowerCommand);
        if (minutesMatcher.find()) {
            data.setType(ReminderData.ReminderType.MINUTES);
            data.setMinutes(Integer.parseInt(Objects.requireNonNull(minutesMatcher.group(1))));
            reminderText = extractReminderText(command, minutesMatcher.start());
            data.setMessage(reminderText);
            return data;
        }

        Pattern hoursPattern = Pattern.compile("через\\s+(\\d+)\\s+часов");
        Matcher hoursMatcher = hoursPattern.matcher(lowerCommand);
        if (hoursMatcher.find()) {
            data.setType(ReminderData.ReminderType.HOURS);
            data.setHours(Integer.parseInt(Objects.requireNonNull(hoursMatcher.group(1))));
            reminderText = extractReminderText(command, hoursMatcher.start());
            data.setMessage(reminderText);
            return data;
        }

        Pattern timePattern = Pattern.compile("в\\s+(\\d{1,2}):(\\d{2})");
        Matcher timeMatcher = timePattern.matcher(lowerCommand);
        if (timeMatcher.find()) {
            data.setType(ReminderData.ReminderType.TIME_STRING);
            data.setHour(Integer.parseInt(Objects.requireNonNull(timeMatcher.group(1))));
            data.setMinute(Integer.parseInt(Objects.requireNonNull(timeMatcher.group(2))));
            reminderText = extractReminderText(command, timeMatcher.start());
            data.setMessage(reminderText);
            return data;
        }

        data.setType(ReminderData.ReminderType.MINUTES);
        data.setMinutes(10);
        data.setMessage(command.replaceFirst("(?i)(напомни|напомнить)\\s*", ""));

        return data;
    }

    private String extractReminderText(String command, int timeStartIndex) {
        String reminder = command.substring(0, timeStartIndex);
        reminder = reminder.replaceFirst("(?i)(напомни|напомнить)\\s*", "");
        return reminder.trim();
    }
}
