package com.brioal.bricodes.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.brioal.bricodes.MainActivity;
import com.brioal.bricodes.R;
import com.brioal.bricodes.base.Constants;
import com.brioal.bricodes.base.LauncherData;
import com.brioal.bricodes.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.GetListener;

public class LauncherActivity extends Activity {

    private static final String TAG = "LauncherInfo";
    @Bind(R.id.launcher_image)
    ImageView launcherImage;
    @Bind(R.id.launcher_msg)
    TextView launcherMsg;

    private String mUrl;
    private String mTitle;
    private File imageFile ;
    private SharedPreferences preferences ;
    private SharedPreferences.Editor editor ;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x123) {
                LauncherActivity.this.finish();
            }
        }
    };

    private boolean isDownloadFinish = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        ButterKnife.bind(this);
        init();
    }

    public void init() {
        File extrlDir = Environment.getExternalStorageDirectory();
        preferences = getSharedPreferences("Brioal", MODE_WORLD_READABLE);
        editor = preferences.edit();
        if (extrlDir.exists()) { // 存在外置存储
            try {
                imageFile = new File(extrlDir.getCanonicalFile() + "/launcherimage.jpg");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (Util.isNetworkConnected(LauncherActivity.this)) { // 网络可用
            Bmob.initialize(LauncherActivity.this, Constants.appID); // 初始化Sdk
            //加载首页数据
            BmobQuery<LauncherData> query = new BmobQuery<LauncherData>();
            query.getObject(LauncherActivity.this, Constants.launcherDataId, new GetListener<LauncherData>() {
                @Override
                public void onSuccess(LauncherData launcherData) {
                    mUrl = launcherData.getLauncherImage().getFileUrl(LauncherActivity.this);
                    mTitle = launcherData.getLauncherTitle();
                    launcherMsg.setText(mTitle);
                    String name = preferences.getString("launcherImageName", "sb");
                    Thread download = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            FileOutputStream fos = null;
                            InputStream is = null;
                            try {
                                fos = new FileOutputStream(imageFile);
                                URL url = new URL(mUrl);
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setConnectTimeout(5000);//设置连接报错时间为5秒
                                is = connection.getInputStream();
                                int ch = 0;
                                byte[] bytes = new byte[1024];
                                while ((ch = is.read(bytes)) != -1) {
                                    fos.write(bytes, 0, ch);
                                }
                                isDownloadFinish = true;
                                handler.sendEmptyMessage(0x123); //关闭activity
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    if (!name.equals(launcherData.getLauncherImage().getFilename())) {
                        //线程开始
                        Log.i(TAG, "onSuccess: 下载图片");
                        download.start();
                        editor.remove("launcherImageName");
                        editor.putString("launcherImageName", launcherData.getLauncherImage().getFilename());
                        editor.apply();
                        isDownloadFinish = true;
                    } else {
                        Log.i(TAG, "onSuccess: 图片存在，不下载");
                        //存在相同图片，线程不开始
                        isDownloadFinish = true;

                    }
                }

                @Override
                public void onFailure(int i, String s) {
                    Log.i(TAG, "onFailure: 首页数据获取失败、");
                }
            });

        } else { // 离线模式
            isDownloadFinish = true;
        }
        //判断本地是否存在图片，存在则显示
        if (imageFile.exists()) {
            launcherImage.setImageBitmap(BitmapFactory.decodeFile(imageFile.getAbsolutePath()));
        }
        startAnimation();
    }
        //设置动画 开始动画
    private void startAnimation() {
        Animation animation = AnimationUtils.loadAnimation(LauncherActivity.this, R.anim.anim_launcher);
        launcherImage.setAnimation(animation);
        animation.setFillAfter(true); // anim文件中设置不起作用
        animation.start();
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isDownloadFinish) {
                    startActivity(new Intent(LauncherActivity.this, MainActivity.class));
                    //进入MainActivity
                    LauncherActivity.this.finish();
                } else {
                    try {
                        Thread.sleep(1000);
                        startActivity(new Intent(LauncherActivity.this, MainActivity.class));
                        //进入MainActivity


                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


    }

}