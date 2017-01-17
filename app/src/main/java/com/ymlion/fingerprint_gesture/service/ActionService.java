package com.ymlion.fingerprint_gesture.service;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.ymlion.fingerprint_gesture.constants.Action;
import com.ymlion.fingerprint_gesture.util.BroadcastUtil;

/**
 * Created by ymlion on 2017/1/17.
 */

public class ActionService extends AccessibilityService {

    private static final String TAG = "ActionService";

    private BroadcastReceiver b = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            switch (intent.getAction()) {
                case Action.BACK:
                    performAction(GLOBAL_ACTION_BACK);
                    break;
                case Action.HOME:
                    performAction(GLOBAL_ACTION_HOME);
                    break;
                case Action.RECENT:
                    performAction(GLOBAL_ACTION_RECENTS);
                    break;
                case Action.FINGERPRINT_CLOSE:
                    stopSelf();
                    break;
                default:
                    break;
            }
        }
    };

    private void performAction(int a) {
        performGlobalAction(a);
        if (FingerService.isRunning()) {
            BroadcastUtil.send(this, Action.FINGERPRINT_SCANNER_RESTART);
        } else {
            startService(new Intent(this, FingerService.class));
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "********************* onAccessibilityEvent ********************");
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_HOVER_ENTER:
                break;
            case AccessibilityEvent.TYPE_VIEW_HOVER_EXIT:
                break;
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                break;
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                break;
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                break;
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END:
                break;
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:
                break;
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_START:
                break;
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_END:
                break;
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_START:
                break;
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_END:
                break;
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                ComponentName componentName = new ComponentName(event.getPackageName().toString(), event.getClassName().toString());
                Log.d(TAG, "onAccessibilityEvent: activity : " + componentName.flattenToShortString());
                return;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                Log.d(TAG, "onAccessibilityEvent: TYPE_NOTIFICATION_STATE_CHANGED");
                break;
        }
        Log.d(TAG, "onAccessibilityEvent: " + AccessibilityEvent.eventTypeToString(event.getEventType()) + " ; action : " + event.getAction());
        AccessibilityNodeInfo nodeInfo = event.getSource();
        Parcelable p = event.getParcelableData();

        Log.d(TAG, "onAccessibilityEvent: " + String.valueOf(nodeInfo));
        Log.d(TAG, "o nAccessibilityEvent: " + String.valueOf(p));
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "***************  onInterrupt **********");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        startService(new Intent(this, FingerService.class));
        /*ShellUtils.CommandResult commandResult = ShellUtils.execCommand("getevent -l", true, true);
        Log.e(TAG, "CommandResult: " + commandResult.result);*/
    }

    @Override
    protected boolean onGesture(int gestureId) {
        Log.d(TAG, "onGesture: " + gestureId);
        return super.onGesture(gestureId);
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        Log.d(TAG, "onKeyEvent: " + event.toString());
        return super.onKeyEvent(event);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        BroadcastUtil.register(this, b, Action.BACK, Action.HOME, Action.RECENT, Action.FINGERPRINT_CLOSE);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        unregisterReceiver(b);
    }
}
