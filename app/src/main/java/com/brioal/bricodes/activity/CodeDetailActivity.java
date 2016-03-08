package com.brioal.bricodes.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.brioal.bricodes.R;
import com.brioal.bricodes.view.SwipeBackLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CodeDetailActivity extends AppCompatActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    AppBarLayout appBar;
    @Bind(R.id.activity_code_tv_code)
    WebView activityCodeTvCode;
    @Bind(R.id.layout)
    CoordinatorLayout layout ;
    @Bind(R.id.layout_swipeLayout)
    SwipeBackLayout swipeBackLayout ;

    private String mTitle ;
    private String mCode ;
    private String mTime ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_detail);
        ButterKnife.bind(this);

        initData();
        setView();
    }

    public void initData() {
        mTitle= getIntent().getStringExtra("mTitle");
        mCode= getIntent().getStringExtra("mCode");
        mTime = getIntent().getStringExtra("mTime");

    }

    public void setView() {
        toolbar.setTitle(mTitle);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(CodeDetailActivity.this);
        String color_index = preferences.getString("style_list", "ThemeEclipse");
        String text_Size = preferences.getString("size_list", 15+"");
        boolean line_num = preferences.getBoolean("line_num", false);
        activityCodeTvCode.getSettings().setJavaScriptEnabled(true);
        WebSettings mWebSettings = activityCodeTvCode.getSettings();
        mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        activityCodeTvCode.loadDataWithBaseURL("file:///android_asset/", getFileContent("code.html").replace("Brioal is HardWorking", mCode.replace("gutter: true", "gutter:" + line_num + "")).replace("CoreDefault", color_index).replace("15", text_Size), "text/html", null, null);
        setSupportActionBar(toolbar);

        swipeBackLayout.setCallback(new SwipeBackLayout.Callback() {
            @Override
            public void onShouldFinish() {
                finish();
                overridePendingTransition(R.anim.no_anim, R.anim.out_tp_right);
            }
        });

    }

    private String getFileContent(String file){
        String content = "";
        try {
            // 把数据从文件读入内存
            InputStream is = getResources().getAssets().open(file);
            ByteArrayOutputStream bs = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int i = is.read(buffer, 0, buffer.length);
            while(i>0){
                bs.write(buffer, 0, i);
                i = is.read(buffer, 0, buffer.length);
            }

            content = new String(bs.toByteArray(), Charset.forName("utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return content;
    }



}
