package com.ymlion.fingerprint_gesture.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;
import android.util.Log;

import com.ymlion.fingerprint_gesture.constants.Action;
import com.ymlion.fingerprint_gesture.util.BroadcastUtil;
import com.ymlion.fingerprint_gesture.util.SPUtil;

/**
 * Created by ymlion on 2017/1/16
 */

public class FingerService extends Service {

    private static final String TAG = "FingerService";
    private static int CHECK_INTERVAL = 1000;

    private boolean isOpen = false;
    private FingerprintManagerCompat fm;
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
        return START_STICKY;
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
        fm.authenticate(null, 0, signal, new FingerprintManagerCompat.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                super.onAuthenticationError(errMsgId, errString);
                CHECK_INTERVAL = 60000;
                stop();
            }

            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                super.onAuthenticationHelp(helpMsgId, helpString);
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                BroadcastUtil.send(FingerService.this, getActionType());
                stop();
                super.onAuthenticationSucceeded(result);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
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
            fm = FingerprintManagerCompat.from(this);
        }
    }

    private void stop() {
        signal.cancel();
        signal = null;
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
