package com.gvayt.smile.model.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.gvayt.smile.Constant;
import com.gvayt.smile.utils.ObjectSerializer;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SharedPrefStorage implements LocalStorage {
    private final SharedPreferences sharedPreferences;
    public SharedPrefStorage(Context context) {
        this.sharedPreferences = context.getSharedPreferences(Constant.PREF_NAME, Context.MODE_PRIVATE);
    }
    @Override
    public boolean getBoolean(String key, boolean d) {
        return sharedPreferences.getBoolean(key, d);
    }

    @Override
    public String getString(String key, String d) {
        return sharedPreferences.getString(key, d);
    }

    @Override
    public Integer getInt(String key, Integer d) {
        return sharedPreferences.getInt(key, d);
    }

    @Override
    public void putString(String key, String string) {
        sharedPreferences.edit().putString(key, string).apply();
    }

    @Override
    public void putBoolean(String key, boolean bool) {
        sharedPreferences.edit().putBoolean(key, bool).apply();
    }

    @Override
    public void putInt(String key, Integer integer) {
        sharedPreferences.edit().putInt(key, integer).apply();
    }

    @Override
    public <T> void saveList(String key, List<T> list, Class<T> clazz) throws IOException {
        sharedPreferences.edit().putString(key, ObjectSerializer.serialize((Serializable) list)).apply();
    }

    @Override
    public <T> List<T> getList(String key, Class<T> clazz, List<T> d) throws IOException {
        return (List<T>) ObjectSerializer.deserialize(sharedPreferences.getString(key, ObjectSerializer.serialize(new ArrayList<>())));
    }

    @Override
    public void clear() {
        sharedPreferences.edit().clear().apply();
    }
}
