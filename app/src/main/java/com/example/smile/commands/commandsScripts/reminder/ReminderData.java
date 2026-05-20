package com.example.smile.commands.commandsScripts.reminder;


public class ReminderData {

    public enum ReminderType {
        MINUTES,    // через X минут
        HOURS,      // через X часов
        TIME_STRING // в 15:30
    }

    private ReminderType type;
    private String message;
    private int minutes;
    private int hours;
    private int hour;
    private int minute;

    // Constructors
    public ReminderData() {}

    public ReminderData(ReminderType type, String message) {
        this.type = type;
        this.message = message;
    }

    // Getters and Setters
    public ReminderType getType() { return type; }
    public void setType(ReminderType type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getMinutes() { return minutes; }
    public void setMinutes(int minutes) { this.minutes = minutes; }

    public int getHours() { return hours; }
    public void setHours(int hours) { this.hours = hours; }

    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }

    public int getMinute() { return minute; }
    public void setMinute(int minute) { this.minute = minute; }
}
