package com.brioal.bricodes;

import android.database.Cursor;
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
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brioal.bricodes.base.MainIndexs;
import com.brioal.bricodes.fragment.MainFragment;
import com.brioal.bricodes.fragment.TodoListFragment;
import com.brioal.bricodes.util.DataBaseHelper;
import com.brioal.bricodes.util.Util;
import com.brioal.bricodes.view.CircleImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

//TODO 沉浸状态栏到M
//TODO 添加标签功能  详情页面显示标签， 列表显示标签 ，标签列表设置
public class MainActivity extends AppCompatActivity
        implements View.OnClickListener {
    private static final String TAG = "MainInfo";
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.main_container)
    RelativeLayout mainContainer;
    @Bind(R.id.fab)
    FloatingActionButton fab;
    @Bind(R.id.head_back)
    ImageView headBack;
    @Bind(R.id.list_setting)
    ImageButton listSetting; //设置显示列表
    @Bind(R.id.menu_list)
    ListView menuList; //显示列表
    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;


    private MainFragment mainFragment;
    private TodoListFragment todoListFragment;

    private ImageView headImage;
    private CircleImageView headCircle;
    private DataBaseHelper helper;
    private ImageButton btn_refresh;
    private LinearLayout btn_todo;
    private long lastClicked;
    private List<MainIndexs> allLists;// 全部列表
    private List<MainIndexs> showLists;// 要显示的列表
    private MyBaseAdapter adapter;

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
        //透明状态栏
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            drawerLayout.setFitsSystemWindows(false);
        } else {
//            AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.layout_main).findViewById(R.id.app_bar_layout);
//            appBarLayout.setPadding(0, getStatusBarHeight(), 0, 0);

        }
        ButterKnife.bind(this);
        setData();
        setView();
        mainFragment = new MainFragment(MainActivity.this, "热门");
        getFragmentManager().beginTransaction().add(mainContainer.getId(), mainFragment).commit();

    }

    private void setData() {
        getSavedData();
        if (Util.isNetworkConnected(MainActivity.this)) {
            BmobQuery<MainIndexs> query = new BmobQuery<MainIndexs>();
//查询playerName叫“比目”的数据
//返回50条数据，如果不加上这条语句，默认返回10条数据
            query.setLimit(50);
//执行查询方法
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

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public void setView() {

        setSupportActionBar(toolbar);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (todoListFragment.isVisible()) {
                    if (todoListFragment.isDeleting()) {// 处于删除模式
                        todoListFragment.delete();
                    } else {
                        todoListFragment.showDelete();
                    }
                }
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);


        toggle.syncState();
        drawerLayout.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                if (event.getAction() == DragEvent.ACTION_DRAG_ENDED) {
                    //拖拽结束，刷新
                    Log.i(TAG, "onDrag: 拖拽结束");
//                    initMenu();
                }
                return true;
            }
        });


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
                    if (todoListFragment != null) {
                        if (todoListFragment.isAdded()) {
                            getFragmentManager().beginTransaction().hide(todoListFragment).commit();
                        }
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

    private void startAnimation() {
        Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_headimage);
        headImage.setAnimation(animation);
        animation.setRepeatCount(10);
        animation.setRepeatMode(Animation.RESTART);
        animation.start();
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


    }


    private void saveData(List<MainIndexs> list) {
        helper = new DataBaseHelper(MainActivity.this, "listData.db3", 1, DataBaseHelper.TYPE_LISTS);
        helper.getReadableDatabase().delete("Lists", "_id>0", null);


        for (int i = 0; i < list.size(); i++) {
            MainIndexs index = list.get(i);
            helper.getReadableDatabase().execSQL("insert into Lists values(null,?,?,?)", new Object[]{index.getIndex(), index.getIsShow(), index.getUrl(MainActivity.this)});
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
            index = new MainIndexs(cursor.getString(1), cursor.getInt(2), cursor.getString(3));
            allLists.add(index);
            if (index.getIsShow() == 0) {
                showLists.add(index);
                Log.i(TAG, "getSavedData: " + index.getUrl(MainActivity.this));
            }
        }
        handler.sendEmptyMessage(0x123);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if ((System.currentTimeMillis() - lastClicked) < 3000) {
            super.onBackPressed();
        } else {
            Snackbar.make(fab, "再一次点击退出", Snackbar.LENGTH_SHORT).show();
            lastClicked = System.currentTimeMillis();

        }

    }


//    @SuppressWarnings("StatementWithEmptyBody")
//    @Override
//    public boolean onNavigationItemSelected(MenuItem item) {
//        // Handle navigation view item clicks here.
//        int id = item.getItemId();
//        String index = String.valueOf(item.getTitle());
//        mainFragment.setIndex(index);
//        toolbar.setTitle(index);
//        if (todoListFragment != null) {
//            if (todoListFragment.isAdded()) {
//                getFragmentManager().beginTransaction().hide(todoListFragment).commit();
//            }
//        }
//
//        if (!mainFragment.isAdded()) {    // 先判断是否被add过
//            getFragmentManager().beginTransaction().add(mainContainer.getId(), mainFragment).commit(); // 隐藏当前的fragment，add下一个到Activity中
//        } else {
//            getFragmentManager().beginTransaction().show(mainFragment).commit(); // 隐藏当前的fragment，显示下一个
//        }
//
//
//        drawerLayout.closeDrawer(GravityCompat.START);
//        return true;
//    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.list_setting) {
            //选择要显示的列表
        }
        if (v.getId() == R.id.head_todo) {
            if (todoListFragment == null) {
                todoListFragment = new TodoListFragment();
            }
            toolbar.setTitle("随记");
            if (!todoListFragment.isAdded()) {
                getFragmentManager().beginTransaction().hide(mainFragment).add(mainContainer.getId(), todoListFragment).commit();
            } else {
                getFragmentManager().beginTransaction().hide(mainFragment).show(todoListFragment).commit();
            }
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
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
            if (url != null) {
                Picasso.with(MainActivity.this).load(url).error(R.drawable.icon).into(holder.head);
            }
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

}
