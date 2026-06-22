package com.gvayt.smile.model.local;

import java.io.IOException;
import java.util.List;

public interface LocalStorage {
    boolean getBoolean(String key, boolean defaultValue);
    String getString(String key,String defaultValue);
    Integer getInt(String key, Integer defaultValue);
    void putString(String key, String string);
    void putBoolean(String key, boolean bool);
    void putInt(String key, Integer integer);
    <T> void saveList(String key, List<T> list, Class<T> clazz) throws IOException;
    <T> List<T> getList(String key, Class<T> clazz, List<T> defaultValue) throws IOException;
    void clear();
}
