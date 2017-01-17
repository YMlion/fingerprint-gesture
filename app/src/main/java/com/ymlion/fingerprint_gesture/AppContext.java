package com.ymlion.fingerprint_gesture;

import android.app.Application;

/**
 * Created by ymlion on 2017/1/16
 */

public class AppContext extends Application {

    private static AppContext instance;

    public static AppContext getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
