package com.ymlion.fingerprint_gesture.service;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.ymlion.fingerprint_gesture.constants.Action;
import com.ymlion.fingerprint_gesture.util.BroadcastUtil;
import com.ymlion.fingerprint_gesture.util.SPUtil;

import org.jetbrains.annotations.Nullable;

/**
 * Created by ymlion on 2017/1/16
 */

public class FingerService extends Service {

    private static final String TAG = "FingerService";
    private static int CHECK_INTERVAL = 1000;

    private boolean isOpen = false;
    private FingerprintManager fm;
    private CancellationSignal signal;
    private Handler handler;
    private BroadcastReceiver b;
    private String actionType;
    private static boolean isRunning = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (!isOpen && !"-1".equals(actionType)) {
                        handler.sendEmptyMessage(0);
                    }
                    boolean c = false;
                    if (CHECK_INTERVAL > 1000) {
                        c = true;
                    }
                    SystemClock.sleep(CHECK_INTERVAL);
                    if (c) {
                        CHECK_INTERVAL = 1000;
                    }
                }
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        isRunning = true;
        actionType = new SPUtil(this, getPackageName() + "_preferences").getString("action_list");
        initBroadcast();
        initHandler();
        initManager();
        reconnect();
    }

    private void initBroadcast() {
        b = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null) {
                    return;
                }
                switch (intent.getAction()) {
                    case Action.GESTURE_CHANGED:
                        actionType = new SPUtil(FingerService.this, getPackageName() + "_preferences").getString("action_list");
                        break;
                    case Action.FINGERPRINT_SCANNER_RESTART:
                        reconnect();
                        break;
                    case Action.FINGERPRINT_CLOSE:
                        if (isRunning) {
                            stopSelf();
                        }
                        break;
                }
            }
        };
        BroadcastUtil.register(this, b, Action.FINGERPRINT_SCANNER_RESTART, Action.GESTURE_CHANGED, Action.FINGERPRINT_CLOSE);
    }

    private void initHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        initManager();
                        reconnect();
                        return true;
                }
                return false;
            }
        });
    }

    private void reconnect() {
        if (isOpen || "-1".equals(actionType)) {
            return;
        }
        isOpen = true;
        signal = new CancellationSignal();
        if (checkSelfPermission(Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fm.authenticate(null, signal, 0, new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                Log.d(TAG, "onAuthenticationError");
                super.onAuthenticationError(errMsgId, errString);
                CHECK_INTERVAL = 60000;
                stop();
            }

            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                super.onAuthenticationHelp(helpMsgId, helpString);
                Log.d(TAG, "onAuthenticationHelp");
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                BroadcastUtil.send(FingerService.this, getActionType());
                stop();
                super.onAuthenticationSucceeded(result);
                Log.d(TAG, "onAuthenticationSucceeded");
            }

            @Override
            public void onAuthenticationFailed() {
                Log.d(TAG, "onAuthenticationFailed");
                super.onAuthenticationFailed();
                SharedPreferences sp = getSharedPreferences("com.ymlion.fingerprint_gesture_preferences", MODE_PRIVATE);
                boolean act = sp.getBoolean("fingerprint_ignore_right", false);
                if (act) {
                    BroadcastUtil.send(FingerService.this, getActionType());
                    stop();
                }
            }
        }, null);
    }

    private String getActionType() {
        switch (actionType) {
            case "0":
                return Action.BACK;
            case "1":
                return Action.HOME;
            case "2":
                return Action.RECENT;
        }
        return null;
    }

    private void initManager() {
        if (fm == null) {
            fm = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
        }
    }

    private void stop() {
        if (signal != null) {
            signal.cancel();
            signal = null;
        }
        isOpen = false;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        isRunning = false;
    }

    public static boolean isRunning() {
        return isRunning;
    }
}
