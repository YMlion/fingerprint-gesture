package com.ymlion.fingerprint_gesture.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.text.TextUtils;

/**
 * <p>广播工具类<p/>
 *
 * Created by ymlion on 2017/1/17.
 */

public class BroadcastUtil {

    public static void register(Context context, BroadcastReceiver receiver, String action) {
        IntentFilter filter = new IntentFilter(action);
        context.registerReceiver(receiver, filter);
    }

    public static void register(Context context, BroadcastReceiver receiver, @NonNull String... actions) {
        IntentFilter filter = new IntentFilter();
        for (String a : actions) {
            filter.addAction(a);
        }
        context.registerReceiver(receiver, filter);
    }

    public static void send(Context context, String action) {
        if (TextUtils.isEmpty(action)) {
            return;
        }
        Intent intent = new Intent();
        intent.setPackage(context.getPackageName());
        intent.setAction(action);
        context.sendBroadcast(intent);
    }
}
