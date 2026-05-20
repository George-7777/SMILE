package com.example.smile.data;


import android.content.Context;
import android.content.SharedPreferences;

import com.example.smile.utils.ObjectSerializer;

import java.io.IOException;
import java.util.ArrayList;

public class PreferencesManager {
    private static final String PREF_NAME = "smile_prefs";

    // ключи
    private static final String KEY_BALANCE = "BALANCE";
    private static final String KEY_SOS_NUMBER = "NUMBER";
    private static final String KEY_TASKS = "TASKS";
    private static final String DEFAULT_BALANCE = "1000";
    private static final String DEFAULT_SOS_NUMBER = "";

    private final SharedPreferences sharedPreferences;

    public PreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // ==================== SOS NUMBER ====================

    public String getSosNumber() {
        return sharedPreferences.getString(KEY_SOS_NUMBER, DEFAULT_SOS_NUMBER);
    }

    public void setSosNumber(String number) {
        sharedPreferences.edit()
                .putString(KEY_SOS_NUMBER, number)
                .apply();
    }

    public boolean hasSosNumber() {
        String number = getSosNumber();
        return number != null && !number.isEmpty();
    }

    // ==================== TASKS ====================

    public ArrayList<String> getTasks() {
        String tasksJson = sharedPreferences.getString(KEY_TASKS, "");
        if (tasksJson.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            return (ArrayList<String>) ObjectSerializer.deserialize(tasksJson);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void saveTasks(ArrayList<String> tasks) throws IOException {
        String tasksJson = ObjectSerializer.serialize(tasks);
        sharedPreferences.edit()
                .putString(KEY_TASKS, tasksJson)
                .apply();
    }

    public void addTask(String task) throws IOException {
        ArrayList<String> tasks = getTasks();
        tasks.add(task);
        saveTasks(tasks);
    }

    public void removeTask(int position) throws IOException {
        ArrayList<String> tasks = getTasks();
        if (position >= 0 && position < tasks.size()) {
            tasks.remove(position);
            saveTasks(tasks);
        }
    }

    // ==================== ДОПОЛНИТЕЛЬНО ====================

    public void clearAll() {
        sharedPreferences.edit().clear().apply();
    }

    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }
}