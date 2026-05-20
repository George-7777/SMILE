package com.example.smile.commands;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.widget.ArrayAdapter;

import com.example.smile.commands.commandsScripts.radio.RadioPlayer;
import com.example.smile.commands.commandsScripts.reminder.ReminderData;
import com.example.smile.data.PreferencesManager;
import com.example.smile.ui.commandsView.CopilkaAndBalance;
import com.example.smile.ui.commandsView.MainActivity2;
import com.example.smile.ai.AIProvider;
import com.example.smile.tts.TTSManager;
import com.example.smile.commands.commandsScripts.reminder.ReminderScheduler;

import java.util.ArrayList;

public class CommandExecutor {

    private final Context context;
    private final TTSManager ttsManager;
    private final PreferencesManager preferencesManager;
    private final RadioPlayer radioPlayer;
    private final AIProvider aiProvider;
    private final ArrayAdapter<String> messagesAdapter;

    private Intent tasksIntent;
    private Intent piggyBankIntent;
    private CommandParser commandParser;
    private ReminderScheduler reminderScheduler;

    public CommandExecutor(Context context, TTSManager ttsManager,
                           PreferencesManager preferencesManager,
                           RadioPlayer radioPlayer, AIProvider aiProvider,
                           ArrayAdapter<String> messagesAdapter, ReminderScheduler reminderScheduler) {
        this.context = context;
        this.ttsManager = ttsManager;
        this.preferencesManager = preferencesManager;
        this.radioPlayer = radioPlayer;
        this.aiProvider = aiProvider;
        this.messagesAdapter = messagesAdapter;

        this.tasksIntent = new Intent(context, MainActivity2.class);
        this.piggyBankIntent = new Intent(context, CopilkaAndBalance.class);
        this.commandParser = new CommandParser();
        this.reminderScheduler = reminderScheduler;
    }

    public void execute(String command, CommandCallback callback) {
        CommandType type = new CommandParser().parseCommand(command);

        switch (type) {
            case SHOW_TASKS:
                openTasksScreen();
                break;

            case ENABLE_NOTIFICATIONS:
            case DISABLE_NOTIFICATIONS:
                openNotificationSettings();
                ttsManager.speak("Выключить уведомления ты можешь перейдя в настройки приложения");
                if (callback != null) callback.onResult("Открываю настройки уведомлений");
                break;


            case TURN_ON_RADIO:
                turnOnRadio();
                if (callback != null) callback.onResult("Радио включено");
                break;

            case TURN_OFF_RADIO:
                turnOffRadio();
                if (callback != null) callback.onResult("Радио выключено");
                break;

            case SET_SOS_NUMBER:
                setSosNumber(command);
                if (callback != null) callback.onResult("SOS номер установлен");
                break;

            case SET_REMINDER:
                setReminder(command);
                if (callback != null) callback.onResult("Напоминание установлено");
                break;

            case CHAT_WITH_AI:
                chatWithAI(command, callback);
                break;

            case UNKNOWN:
                ttsManager.speak("Извините, я не понял команду");
                if (callback != null) callback.onError("Команда не распознана");
                break;
        }
    }

    private void openTasksScreen() {
        context.startActivity(tasksIntent);
    }

    private void openNotificationSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    private void openPiggyBankScreen() {
        context.startActivity(piggyBankIntent);
    }

    private void turnOnRadio() {
        radioPlayer.play();
        ttsManager.speak("Включаю радио");
        addMessageToChat("СМАЙЛИК: Включаю радио");
    }

    private void turnOffRadio() {
        radioPlayer.stop();
        ttsManager.speak("Выключаю радио");
        addMessageToChat("СМАЙЛИК: Выключаю радио");
    }

    private void setSosNumber(String command) {
        String number = new CommandParser().extractSosNumber(command);
        if (!number.isEmpty()) {
            preferencesManager.setSosNumber(number);
            ttsManager.speak("Номер задан");
            addMessageToChat("СМАЙЛИК: Номер задан");
        } else {
            ttsManager.speak("Не удалось задать номер. Используйте формат: добавить сос-номер +79123456789");
            addMessageToChat("СМАЙЛИК: Не удалось задать номер");
        }
    }

    private void setReminder(String command) {
        ReminderData data = commandParser.parseReminder(command);

        if (data.getMessage() == null || data.getMessage().isEmpty()) {
            ttsManager.speak("Не удалось создать напоминание. Скажите, о чём напомнить");
            addMessageToChat("СМАЙЛИК: Не удалось создать напоминание");
            return;
        }

        reminderScheduler.scheduleReminder(data);
        String timeStr = reminderScheduler.getFormattedTriggerTime(data);

        ttsManager.speak("Напоминание установлено на " + timeStr);
        addMessageToChat("СМАЙЛИК: Напоминание \"" + data.getMessage() + "\" установлено на " + timeStr);
    }

    private void chatWithAI(String command, CommandCallback callback) {
        addMessageToChat("*СМАЙЛИК ДУМАЕТ*");

        aiProvider.generateResponse(command, new ArrayList<>(), new AIProvider.AIResponseCallback() {
            @Override
            public void onSuccess(String response) {
                ttsManager.speak(response);
                addMessageToChat("СМАЙЛИК: " + response);
                if (callback != null) callback.onResult(response);
            }

            @Override
            public void onError(Exception e) {
                String errorMsg = "Извините, произошла ошибка. Попробуйте позже.";
                ttsManager.speak(errorMsg);
                addMessageToChat("СМАЙЛИК: " + errorMsg);
                if (callback != null) callback.onError(errorMsg);
            }
        });
    }

    private void addMessageToChat(String message) {
        if (messagesAdapter != null) {
            messagesAdapter.add(message);
            messagesAdapter.notifyDataSetChanged();
        }
    }

    public interface CommandCallback {
        void onResult(String message);
        void onError(String error);
    }
}
