package com.brioal.bricodes.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;
import android.webkit.WebView;

import com.brioal.bricodes.R;

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

    private String mTitle ;
    private String mCode ;
    private String mTime ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_detail);
        //透明状态栏
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
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
        activityCodeTvCode.getSettings().setJavaScriptEnabled(true);
        activityCodeTvCode.loadDataWithBaseURL("file:///android_asset/", getFileContent("code.html").replace("Brioal is HardWorking",mCode), "text/html", null, null);
        setSupportActionBar(toolbar);

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
