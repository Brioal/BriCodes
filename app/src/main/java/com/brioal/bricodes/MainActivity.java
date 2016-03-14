package com.brioal.bricodes;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brioal.bricodes.activity.SettingsActivity;
import com.brioal.bricodes.activity.UserActivity;
import com.brioal.bricodes.activity.UserInfoActivity;
import com.brioal.bricodes.base.MainIndexs;
import com.brioal.bricodes.base.User;
import com.brioal.bricodes.fragment.MainFragment;
import com.brioal.bricodes.util.DataBaseHelper;
import com.brioal.bricodes.util.ImageReader;
import com.brioal.bricodes.util.Util;
import com.brioal.bricodes.view.CircleImageView;
import com.lapism.searchview.view.SearchView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;
import me.imid.swipebacklayout.lib.BaseActivity;
import me.imid.swipebacklayout.lib.StatusBarUtils;

// 详情页面显示标签， 标签列表设置 通过搜索即可
// 删除行号显示
//滑动返回
// 代码收藏管理 搜索即可
// 设置界面 设置代码着色风格  字体大小 行号显示或关闭
public class MainActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "MainInfo";
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.main_container)
    RelativeLayout mainContainer;
    @Bind(R.id.fab)
    FloatingActionButton fab;
    @Bind(R.id.menu_list)
    ListView menuList; //显示列表
    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @Bind(R.id.searchView)
    SearchView searchView;
    @Bind(R.id.main_setting)
    LinearLayout btn_setting;
    @Bind(R.id.main_head_back)
    ImageView head_back;
    @Bind(R.id.main_user_head)
    CircleImageView user_head;
    @Bind(R.id.main_head_name)
    TextView tv_head_name;
    @Bind(R.id.main_head)
    RelativeLayout main_head;


    private MainFragment mainFragment;

    private DataBaseHelper helper;
    private ImageButton btn_refresh;
    private long lastClicked;
    private List<MainIndexs> allLists;// 全部列表
    private List<MainIndexs> showLists;// 要显示的列表
    private MyBaseAdapter adapter;
    protected ImageLoader imageLoader;
    private DisplayImageOptions options;
    public static boolean hasLogined = false;

    User userInfo;


    private android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x123) {
                setMenu();


            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setData();
        setView();
        mainFragment = new MainFragment(MainActivity.this, "首页");
        getFragmentManager().beginTransaction().add(mainContainer.getId(), mainFragment).commit();

    }

    private void setData() {
        getSavedData();
        if (Util.isNetworkConnected(MainActivity.this)) {
            BmobQuery<MainIndexs> query = new BmobQuery<MainIndexs>();
            query.setLimit(50);
            query.findObjects(MainActivity.this, new FindListener<MainIndexs>() {
                @Override
                public void onSuccess(List<MainIndexs> list) {
                    //获取网络的数据 ：与现有全部数据比较 ， 不包含则添加到all 和show
                    //包含则不发生改变

                    for (int i = 0; i < list.size(); i++) {
                        if (!isContains(allLists, list.get(i).getIndex())) { // 如果是新添加的
                            Log.i(TAG, "onSuccess: 新增加的" + list.get(i).getUrl(MainActivity.this));
                            allLists.add(list.get(i)); //新增加的默认为显示
                        }
                    }
                    showLists.clear();

                    for (int i = 0; i < allLists.size(); i++) {
                        if (allLists.get(i).getIsShow() == 0) {

                            showLists.add(allLists.get(i));
                        }
                    }
                    Collections.sort(showLists, new SortComparator());
                    handler.sendEmptyMessage(0x123);
                    saveData(allLists);
                }

                @Override
                public void onError(int i, String s) {
                    Log.i(TAG, "onError: 获取数据失败" + s);
                }
            });
        }
    }

    public boolean isContains(List<MainIndexs> list, String index) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getIndex().equals(index)) {
                return true;
            }
        }
        return false;
    }

    public void setView() {
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.icon) //设置图片在下载期间显示的图片
                .showImageForEmptyUri(R.drawable.icon)//设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(R.drawable.icon)  //设置图片加载/解码过程中错误时候显示的图片
                .cacheInMemory(true)//设置下载的图片是否缓存在内存中
                .cacheOnDisc(true)//设置下载的图片是否缓存在SD卡中
                .considerExifParams(true)  //是否考虑JPEG图像EXIF参数（旋转，翻转）
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)//设置图片以如何的编码方式显示
                .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型//
                .resetViewBeforeLoading(true)//设置图片在下载前是否重置，复位
                .displayer(new RoundedBitmapDisplayer(20))//是否设置为圆角，弧度为多少
                .displayer(new FadeInBitmapDisplayer(100))//是否图片加载好后渐入的动画时间
                .build();//构建完成
        setSupportActionBar(toolbar);

        searchView.hide(true);
        fab.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                searchView.show(true);
                searchView.setOnSearchViewListener(new SearchView.SearchViewListener() {
                    @Override
                    public void onSearchViewShown() { // 菜单显示

                    }

                    @Override
                    public void onSearchViewClosed() { //菜单关闭

                    }
                });
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) { // 提交搜索
                        mainFragment.setIndex(query);
                        toolbar.setTitle("搜索..  " + query);

                        if (!mainFragment.isAdded()) {    // 先判断是否被add过
                            getFragmentManager().beginTransaction().add(mainContainer.getId(), mainFragment).commit(); // 隐藏当前的fragment，add下一个到Activity中
                        } else {
                            getFragmentManager().beginTransaction().show(mainFragment).commit(); // 隐藏当前的fragment，显示下一个
                        }

                        searchView.hide(true);

                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) { // 输入改变
                        return false;
                    }
                });
            }
        });
        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);


        toggle.syncState();
        head_back.setImageBitmap(ImageReader.readBitmap(MainActivity.this, R.drawable.headimage));

        userInfo = BmobUser.getCurrentUser(MainActivity.this, User.class);

        if (userInfo != null) {
            hasLogined = true;
            //根据本地的账户信息 获取头像和用户名
            tv_head_name.setText(userInfo.getUsername());
            if (Util.isNetworkConnected(MainActivity.this)) {
                BmobQuery<User> query = new BmobQuery<User>();
                query.addWhereEqualTo("email", userInfo.getEmail());
                query.findObjects(this, new FindListener<User>() {
                    @Override
                    public void onSuccess(List<User> object) {
                        hasLogined = true;
                        // 允许用户使用应用
                        Log.i(TAG, "onSuccess: " + object.get(0).getUserHead().getFileUrl(MainActivity.this));
                        ImageLoader.getInstance().displayImage(object.get(0).getUserHead().getFileUrl(MainActivity.this), user_head, options);
                        tv_head_name.setText(object.get(0).getUsername());
                        userInfo.setUsername(object.get(0).getUsername());
                        userInfo.setUserHead(object.get(0).getUserHead());
                    }

                    @Override
                    public void onError(int code, String msg) {
                        //获取用户失败
                        hasLogined = false;
                    }
                });
            }


        } else {
            //缓存用户对象为空时， 可打开用户注册界面…
            user_head.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.default_head));
            tv_head_name.setText("点击登陆");
            hasLogined = false;
        }

        main_head.setOnClickListener(this);
        toolbar.setTitle("首页");
    }

    @Override
    protected void onResume() {
        if (!hasLogined) {
            tv_head_name.setText("点击登陆");
            user_head.setImageResource(R.drawable.default_head);
        }
        super.onResume();
    }

    public void setMenu() {
        if (adapter == null) {
            adapter = new MyBaseAdapter();
            menuList.setAdapter(adapter);
            menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String index = String.valueOf(showLists.get(position).getIndex());
                    mainFragment.setIndex(index);
                    toolbar.setTitle(index);

                    if (!mainFragment.isAdded()) {    // 先判断是否被add过
                        getFragmentManager().beginTransaction().add(mainContainer.getId(), mainFragment).commit(); // 隐藏当前的fragment，add下一个到Activity中
                    } else {
                        getFragmentManager().beginTransaction().show(mainFragment).commit(); // 隐藏当前的fragment，显示下一个
                    }


                    drawerLayout.closeDrawer(GravityCompat.START);
                }
            });
        } else {
            adapter.notifyDataSetChanged();
        }
        Log.i(TAG, "setView: list的大小" + showLists.size());
        toolbar.setTitle("首页");
    }


    private void saveData(List<MainIndexs> list) {
        helper = new DataBaseHelper(MainActivity.this, "listData.db3", 1, DataBaseHelper.TYPE_LISTS);
        helper.getReadableDatabase().delete("Lists", "_id>0", null);
        Collections.sort(list, new SortComparator());

        for (int i = 0; i < list.size(); i++) {
            MainIndexs index = list.get(i);
            helper.getReadableDatabase().execSQL("insert into Lists values(null,?,?,?)", new Object[]{index.getIndex(), index.getIsShow(), index.getUrl(MainActivity.this),});
        }
        if (helper != null) {
            helper.hashCode();
        }
    }

    //读取本地数据 同时对show进行初始化
    private void getSavedData() {
        if (showLists == null) {
            showLists = new ArrayList<>();
        } else {
            showLists.clear();
        }
        if (allLists == null) {
            allLists = new ArrayList<>();
        } else {
            allLists.clear();
        }
        helper = new DataBaseHelper(MainActivity.this, "listData.db3", 1, DataBaseHelper.TYPE_LISTS);
        Cursor cursor = helper.getReadableDatabase().rawQuery("select * from Lists ", null);
        MainIndexs index = null;
        while (cursor.moveToNext()) {
            index = new MainIndexs(cursor.getString(1), cursor.getInt(2), cursor.getString(3), cursor.getInt(0));
            allLists.add(index);
            if (index.getIsShow() == 0) {
                showLists.add(index);
                Log.i(TAG, "getSavedData: " + index.getUrl(MainActivity.this));
            }
        }
        Collections.sort(showLists, new SortComparator());
        handler.sendEmptyMessage(0x123);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (searchView.isSearchOpen()) {
            searchView.hide(true);
        } else if (toolbar.getTitle().toString().startsWith("搜索")) {
            mainFragment.setIndex("首页");
            toolbar.setTitle("首页");
            if (!mainFragment.isAdded()) {    // 先判断是否被add过
                getFragmentManager().beginTransaction().add(mainContainer.getId(), mainFragment).commit(); // 隐藏当前的fragment，add下一个到Activity中
            } else {
                getFragmentManager().beginTransaction().show(mainFragment).commit(); // 隐藏当前的fragment，显示下一个
            }

            searchView.hide(true);
        } else if ((System.currentTimeMillis() - lastClicked) < 3000) {
            super.onBackPressed();
        } else {
            Snackbar.make(fab, "再一次点击退出", Snackbar.LENGTH_SHORT).show();
            lastClicked = System.currentTimeMillis();

        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_user_head:

                break;
            case R.id.main_head:
                if (!hasLogined) {
                    startActivity(new Intent(MainActivity.this, UserActivity.class));
                } else {
                    //打开个人界面
                    Intent intent = new Intent(MainActivity.this, UserInfoActivity.class);
                    intent.putExtra("mAuthor", userInfo.getUsername());
                    intent.putExtra("mEmail", userInfo.getEmail());
                    intent.putExtra("mHead", userInfo.getUserHead().getFileUrl(MainActivity.this));
                    startActivity(intent);
                }

                drawerLayout.closeDrawers();
                break;
        }
    }


    public class MyBaseAdapter extends BaseAdapter {


        @Override
        public int getCount() {

            return showLists.size();
        }

        @Override
        public Object getItem(int position) {
            return showLists.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        //添加列表前面的图片
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MyHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_menu_list, null);
                holder = new MyHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (MyHolder) convertView.getTag();
            }
            MainIndexs index = showLists.get(position);
            String url = index.getUrl(MainActivity.this);
            ImageLoader.getInstance().displayImage(url, holder.head, options);
            holder.tv.setText(index.getIndex());
            return convertView;
        }
    }

    public class MyHolder {
        private ImageView head;
        private TextView tv;

        public MyHolder(View convertView) {
            head = (ImageView) convertView.findViewById(R.id.item_menu_head);
            tv = (TextView) convertView.findViewById(R.id.item_menu_tv);
        }
    }

    //从小到达排序
    public class SortComparator implements Comparator {
        @Override
        public int compare(Object lhs, Object rhs) {
            MainIndexs first = (MainIndexs) lhs;
            MainIndexs second = (MainIndexs) rhs;
            return first.getId() - second.getId();
        }
    }


    @Override
    protected void setStatusBar() {
        StatusBarUtils.setColorForDrawerLayout(this, (DrawerLayout) findViewById(R.id.drawer_layout), getResources()
                .getColor(R.color.colorPrimary));
    }


}
