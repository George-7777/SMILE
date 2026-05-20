package com.example.smile.commands;

import com.example.smile.commands.commandsScripts.reminder.ReminderData;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandParser {

    public CommandType parseCommand(String command) {
        if (command == null || command.isEmpty()) {
            return CommandType.UNKNOWN;
        }

        String lowerCommand = command.toLowerCase().trim();

        if (lowerCommand.equals("список задач")) return CommandType.SHOW_TASKS;
        if (lowerCommand.equals("включить уведомления")) return CommandType.ENABLE_NOTIFICATIONS;
        if (lowerCommand.equals("выключить уведомления")) return CommandType.DISABLE_NOTIFICATIONS;
        if (lowerCommand.equals("мой баланс")) return CommandType.MY_BALANCE;
        if (lowerCommand.equals("включи радио")) return CommandType.TURN_ON_RADIO;
        if (lowerCommand.equals("выключи радио")) return CommandType.TURN_OFF_RADIO;

        if (lowerCommand.startsWith("добавить сос-номер")) return CommandType.SET_SOS_NUMBER;
        if (lowerCommand.startsWith("напомни") || lowerCommand.startsWith("напомнить")) {
            return CommandType.SET_REMINDER;
        }

        return CommandType.CHAT_WITH_AI;
    }

    public String extractSosNumber(String command) {
        String[] parts = command.split(" ");
        if (parts.length >= 3) {
            return parts[2];
        }
        return "";
    }

    public ReminderData parseReminder(String command) {
        ReminderData data = new ReminderData();
        String lowerCommand = command.toLowerCase();

        String reminderText = command;

        Pattern minutesPattern = Pattern.compile("через\\s+(\\d+)\\s+минут");
        Matcher minutesMatcher = minutesPattern.matcher(lowerCommand);
        if (minutesMatcher.find()) {
            data.setType(ReminderData.ReminderType.MINUTES);
            data.setMinutes(Integer.parseInt(minutesMatcher.group(1)));
            reminderText = extractReminderText(command, minutesMatcher.start());
            data.setMessage(reminderText);
            return data;
        }

        Pattern hoursPattern = Pattern.compile("через\\s+(\\d+)\\s+часов");
        Matcher hoursMatcher = hoursPattern.matcher(lowerCommand);
        if (hoursMatcher.find()) {
            data.setType(ReminderData.ReminderType.HOURS);
            data.setHours(Integer.parseInt(hoursMatcher.group(1)));
            reminderText = extractReminderText(command, hoursMatcher.start());
            data.setMessage(reminderText);
            return data;
        }

        Pattern timePattern = Pattern.compile("в\\s+(\\d{1,2}):(\\d{2})");
        Matcher timeMatcher = timePattern.matcher(lowerCommand);
        if (timeMatcher.find()) {
            data.setType(ReminderData.ReminderType.TIME_STRING);
            data.setHour(Integer.parseInt(timeMatcher.group(1)));
            data.setMinute(Integer.parseInt(timeMatcher.group(2)));
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
