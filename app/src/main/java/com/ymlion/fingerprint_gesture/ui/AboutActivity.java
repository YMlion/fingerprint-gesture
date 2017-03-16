package com.ymlion.fingerprint_gesture.ui;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import com.ymlion.fingerprint_gesture.R;

public class AboutActivity extends Activity {

    private TextView mAboutVersionTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        mAboutVersionTv = (TextView) findViewById(R.id.tv_about_version);
        initData();
    }

    private void initData() {
        try {
            mAboutVersionTv.setText("V " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
