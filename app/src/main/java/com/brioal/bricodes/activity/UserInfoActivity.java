package com.brioal.bricodes.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.brioal.bricodes.MainActivity;
import com.brioal.bricodes.R;
import com.brioal.bricodes.base.CodeItem;
import com.brioal.bricodes.base.Constants;
import com.brioal.bricodes.fragment.SimpleItemList;
import com.brioal.bricodes.view.CircleImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;
import me.imid.swipebacklayout.lib.StatusBarUtils;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

//显示个人信息的Activity
public class UserInfoActivity extends SwipeBackActivity {

    private static final String TAG = "UserInfoInfo";
    @Bind(R.id.user_info_name)
    TextView userInfoName;
    @Bind(R.id.user_info_sex)
    TextView userInfoSex;
    @Bind(R.id.user_info_head)
    CircleImageView userInfoHead;
    @Bind(R.id.user_info_btn_setting)
    Button userInfoBtnSetting;
    @Bind(R.id.tabs)
    TabLayout tabs;
    @Bind(R.id.container)
    ViewPager container;
    @Bind(R.id.main_content)
    LinearLayout mainContent;
    @Bind(R.id.user_info_tv_mine)
    TextView userInfoMine;
    @Bind(R.id.user_info_tv_like)
    TextView userInfoLike ;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private SwipeBackLayout swipeBackLayout;

    private String mAuthor;
    private String mEmail;
    private String mHead;
    private int mMine ;
    private int mLike ; //TODO 查询收藏的文章数量

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x123) {
                setViews();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        ButterKnife.bind(this);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        initData();
    }

    public void initData() {
        mAuthor = getIntent().getStringExtra("mAuthor");
        mEmail = getIntent().getStringExtra("mEmail");
        mHead = getIntent().getStringExtra("mHead");
        BmobQuery<CodeItem> query = new BmobQuery<>();
        query.findObjects(UserInfoActivity.this, new FindListener<CodeItem>() {
            @Override
            public void onSuccess(List<CodeItem> list) {
                Log.i(TAG, "onSuccess: 成功获取到发表的文章数量");
                mMine = list.size();
                handler.sendEmptyMessage(0x123);
            }

            @Override
            public void onError(int i, String s) {

            }
        });

    }

    public void setViews() {
        swipeBackLayout = getSwipeBackLayout();
        swipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        userInfoName.setText(mAuthor);
        userInfoSex.setText(mEmail);
        userInfoMine.setText("发表 " + mMine);
        userInfoLike.setText("收藏 "+ mLike);
        ImageLoader.getInstance().displayImage(mHead, userInfoHead, Constants.options);
        userInfoBtnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //退出登陆
                MainActivity.hasLogined = false;
                BmobUser.logOut(UserInfoActivity.this);   //清除缓存用户对象
                UserInfoActivity.this.finish();
            }
        });

    }





    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private  SimpleItemList itemList ;
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    itemList = new SimpleItemList(mAuthor);
                    itemList.setState(SimpleItemList.STATE_MINE);

                    return itemList;

                case 1:
                    itemList = new SimpleItemList(mAuthor);
                    itemList.setState(SimpleItemList.STATE_FAVORATE);
                    //TODO 收藏列表不知道怎么写
                    return itemList;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Drawable image;
            SpannableString sb ;
            ImageSpan imageSpan;
            switch (position) {
                case 0:
                    sb = new SpannableString("   发表");
                    image = UserInfoActivity.this.getResources().getDrawable(R.drawable.user_info_list);
                    image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
                    imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
                    sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    return sb;
                case 1:
                    sb = new SpannableString("   收藏");
                    image = UserInfoActivity.this.getResources().getDrawable(R.drawable.user_info_favorate);
                    image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
                    imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
                    sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    return sb;
            }
            return null;
        }
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtils.setTranslucent(UserInfoActivity.this);
    }
}
