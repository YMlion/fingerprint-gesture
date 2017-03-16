package com.ymlion.fingerprint_gesture.ui;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.ymlion.fingerprint_gesture.AppContext;
import com.ymlion.fingerprint_gesture.R;
import com.ymlion.fingerprint_gesture.constants.Action;
import com.ymlion.fingerprint_gesture.util.BroadcastUtil;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener {

    private static final String TAG = "SettingsActivity";

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            Log.d(TAG, "onPreferenceChange: " + stringValue);

            if (preference instanceof ListPreference) {
                if (preference.getKey().equals("action_list")) {
                    BroadcastUtil.send(AppContext.getInstance(), Action.GESTURE_CHANGED);
                }
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                switch (preference.getKey()) {
                    case "fingerprint_status":
                        if (value.equals(Boolean.FALSE)) {
                            BroadcastUtil.send(AppContext.getInstance(), Action.FINGERPRINT_CLOSE);
                        } else {
                            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            AppContext.getInstance().startActivity(intent);
                        }
                        break;
                    case "fingerprint_icon_status":
                        PackageManager p = AppContext.getInstance().getPackageManager();
                        ComponentName componentName = ComponentName.createRelative(AppContext.getInstance(), "com.ymlion.fingerprint_gesture.ui.SplashActivity");
                        if (value.equals(Boolean.FALSE)) {
                            p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                        } else {
                            p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                        }
                        break;
                    case "fingerprint_ignore_right":
                        break;
                    default:
                        // For all other preferences, set the summary to the value's
                        // simple string representation.
                        preference.setSummary(stringValue);
                        break;
                }
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        if (preference instanceof ListPreference) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);
        bindPreferenceSummaryToValue(findPreference("action_list"));
        bindPreferenceSummaryToValue(findPreference("fingerprint_status"));
        bindPreferenceSummaryToValue(findPreference("fingerprint_ignore_right"));
        bindPreferenceSummaryToValue(findPreference("fingerprint_icon_status"));
        findPreference("fingerprint_about").setOnPreferenceClickListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case "fingerprint_about":
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            default:
                break;
        }
        return false;
    }
}
