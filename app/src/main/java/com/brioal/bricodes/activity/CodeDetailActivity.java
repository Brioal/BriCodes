package com.brioal.bricodes.activity;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.brioal.bricodes.R;
import com.brioal.bricodes.base.CodeItem;
import com.brioal.bricodes.base.User;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class CodeDetailActivity extends SwipeBackActivity {

    private static final String TAG = "CodeInfo";

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    AppBarLayout appBar;
    @Bind(R.id.activity_code_tv_code)
    WebView activityCodeTvCode;
    @Bind(R.id.layout)
    CoordinatorLayout layout;
    @Bind(R.id.code_update)
    TextView head_update;
    @Bind(R.id.code_auther)
    TextView head_auther;
    @Bind(R.id.code_read)
    TextView head_read;
    private String id ;
    private String mTitle;
    private String mCode;
    private String mTime;
    private String mAuther;
    private int mRead;
    private SwipeBackLayout swipeBackLayout;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_detail);
        ButterKnife.bind(this);

        initData();
        setView();
    }

    public void initData() {
        mTitle = getIntent().getStringExtra("mTitle");
        mCode = getIntent().getStringExtra("mCode");
        mTime = getIntent().getStringExtra("mTime");
        mAuther = getIntent().getStringExtra("mAuther");
        mRead = getIntent().getIntExtra("mRead", 0);
        id = getIntent().getStringExtra("id");
        BmobQuery<CodeItem> query = new BmobQuery<>();
        query.addWhereEqualTo("objectId", id);
        query.findObjects(CodeDetailActivity.this, new FindListener<CodeItem>() {
            @Override
            public void onSuccess(List<CodeItem> list) {
                final CodeItem item = list.get(0);
                item.increment("mRead"); // 分数递增1
                item.update(CodeDetailActivity.this, new UpdateListener() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "onSuccess: 更新成功");
                        head_read.setText("阅读 "+item.getmRead());
                    }

                    @Override
                    public void onFailure(int i, String s) {
                        Log.i(TAG, "onFailure: 更新失败"+s);
                    }
                });
            }

            @Override
            public void onError(int i, String s) {

            }
        });

    }

    public void setView() {
        BmobQuery<User> query = new BmobQuery<User>();
        query.addWhereEqualTo("username", mAuther);
        query.findObjects(CodeDetailActivity.this, new FindListener<User>() {
            @Override
            public void onSuccess(List<User> object) {
                Log.i(TAG, "onSuccess: 加载成功");
                // 允许用户使用应用
                Bitmap bitmap = ImageLoader.getInstance().loadImageSync(object.get(0).getUserHead().getFileUrl(CodeDetailActivity.this));
                Bitmap roundBitmap = scaleBitmap(getRound(bitmap), 130, 130);
                toolbar.setLogo(new BitmapDrawable(roundBitmap));
            }

            @Override
            public void onError(int code, String msg) {
                //获取用户失败
            }
        });


        toolbar.setTitle(mTitle);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(CodeDetailActivity.this);
        String color_index = preferences.getString("style_list", "ThemeEclipse");
        String text_Size = preferences.getString("size_list", 15 + "");
        boolean line_num = preferences.getBoolean("line_num", false);
        activityCodeTvCode.getSettings().setJavaScriptEnabled(true);
        WebSettings mWebSettings = activityCodeTvCode.getSettings();
        mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        activityCodeTvCode.loadDataWithBaseURL("file:///android_asset/", getFileContent("code.html").replace("Brioal is HardWorking", mCode.replace("gutter: true", "gutter:" + line_num + "")).replace("CoreDefault", color_index).replace("15", text_Size), "text/html", null, null);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //设置滑动方向，可设置EDGE_LEFT, EDGE_RIGHT, EDGE_ALL, EDGE_BOTTOM
        swipeBackLayout = getSwipeBackLayout();
        swipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        head_update.setText(mTime);
        head_auther.setText(mAuther);
        head_read.setText("阅读 " + mRead);
    }


    public synchronized static Bitmap scaleBitmap(Bitmap bitmap, float w,
                                                  float h) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleW = w / (float) width;
        float scaleH = h / (float) height;
        matrix.postScale(scaleW, scaleH);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return bitmap;
    }

    private String getFileContent(String file) {
        String content = "";
        try {
            // 把数据从文件读入内存
            InputStream is = getResources().getAssets().open(file);
            ByteArrayOutputStream bs = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int i = is.read(buffer, 0, buffer.length);
            while (i > 0) {
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }


        return true;
    }


    //暂时删除菜单选项
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }


    public Bitmap getRound(Bitmap bitmap) {
        int width = bitmap.getWidth() - 40;
        int height = bitmap.getHeight() - 40;
        float roundPx;
        float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
        if (width <= height) {
            roundPx = width / 2 - 5;
            top = 0;
            bottom = width;
            left = 0;
            right = width;
            height = width;
            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else {
            roundPx = height / 2 - 5;
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }

        Bitmap output = Bitmap.createBitmap(width,
                height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
        final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
        final RectF rectF = new RectF(dst_left + 15, dst_top + 15, dst_right - 20, dst_bottom - 20);

        paint.setAntiAlias(true);

        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);

        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, paint);
        return output;
    }

}
