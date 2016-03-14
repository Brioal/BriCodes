package com.brioal.bricodes.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.brioal.bricodes.MainActivity;
import com.brioal.bricodes.R;
import com.brioal.bricodes.base.Constants;

import cn.bmob.v3.Bmob;

/**
 * Created by brioal on 16-3-8.
 */
public class LauncherActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        Bmob.initialize(this, Constants.appID);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                startActivity(new Intent(LauncherActivity.this, MainActivity.class));
                finish();
                overridePendingTransition(0, R.anim.zoom_exit);
            }
        }, 2500);
    }

}
