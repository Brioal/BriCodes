package com.brioal.bricodes;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brioal.bricodes.activity.SettingsActivity;
import com.brioal.bricodes.base.Constants;
import com.brioal.bricodes.base.MainIndexs;
import com.brioal.bricodes.fragment.AboutFragment;
import com.brioal.bricodes.fragment.MainFragment;
import com.brioal.bricodes.util.DataBaseHelper;
import com.brioal.bricodes.util.Util;
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
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

// 详情页面显示标签， 标签列表设置 通过搜索即可
// 删除行号显示
//TODO 代码整理
//滑动返回
// 代码收藏管理 搜索即可
// 设置界面 设置代码着色风格  字体大小 行号显示或关闭
public class MainActivity extends AppCompatActivity {
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
    @Bind(R.id.main_todo)
    LinearLayout btn_todo;
    @Bind(R.id.main_setting)
    LinearLayout btn_setting ;


    private MainFragment mainFragment;
    private AboutFragment aboutFragment;

    private DataBaseHelper helper;
    private ImageButton btn_refresh;
    private long lastClicked;
    private List<MainIndexs> allLists;// 全部列表
    private List<MainIndexs> showLists;// 要显示的列表
    private MyBaseAdapter adapter;
    protected ImageLoader imageLoader;
    private DisplayImageOptions options;


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
        Bmob.initialize(this, Constants.appID);


        ButterKnife.bind(this);
         //透明状态栏
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            Log.i(TAG, "onCreate: 4.4");
            drawerLayout.setFitsSystemWindows(false);

        } else {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

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
        btn_todo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbar.setTitle("关于");
                if (aboutFragment == null) {
                    aboutFragment = new AboutFragment();
                }
                if (!aboutFragment.isAdded()) {    // 先判断是否被add过
                    getFragmentManager().beginTransaction().add(mainContainer.getId(), aboutFragment).commit(); // 隐藏当前的fragment，add下一个到Activity中
                } else {
                    getFragmentManager().beginTransaction().hide(mainFragment).commit();
                    getFragmentManager().beginTransaction().show(aboutFragment).commit(); // 隐藏当前的fragment，显示下一个
                }

                drawerLayout.closeDrawer(GravityCompat.START);
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
                    if (aboutFragment != null) {
                        getFragmentManager().beginTransaction().hide(aboutFragment).commit();
                    }

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
        } else if (toolbar.getTitle().toString().startsWith("搜索")) {
            if (aboutFragment != null && aboutFragment.isVisible()) {
                getFragmentManager().beginTransaction().hide(aboutFragment).commit();
            }
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

}
