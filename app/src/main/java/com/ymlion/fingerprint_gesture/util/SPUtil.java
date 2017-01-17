package com.ymlion.fingerprint_gesture.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ymlion on 2017/1/17.
 */

public class SPUtil {

    private SharedPreferences sp;

    public SPUtil(Context context, String name) {
        sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public int getInt(String key) {
        return sp.getInt(key, -1);
    }

    public int getInt(String key, int defValue) {
        return sp.getInt(key, defValue);
    }

    public boolean getBoolean(String key) {
        return sp.getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return sp.getBoolean(key, defValue);
    }

    public String getString(String key) {
        return sp.getString(key, "");
    }

    public String getString(String key, String defValue) {
        return sp.getString(key, defValue);
    }
}
