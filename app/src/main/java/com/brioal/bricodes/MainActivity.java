package com.brioal.bricodes;

import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.brioal.bricodes.base.MainIndexs;
import com.brioal.bricodes.fragment.MainFragment;
import com.brioal.bricodes.fragment.TodoListFragment;
import com.brioal.bricodes.util.DataBaseHelper;
import com.brioal.bricodes.util.Util;
import com.brioal.bricodes.view.CircleImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

//TODO 沉浸状态栏到M
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private static final String TAG = "MainInfo";
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.main_container)
    RelativeLayout mainContainer;
    @Bind(R.id.fab)
    FloatingActionButton fab;
    @Bind(R.id.nav_view)
    NavigationView navView;
    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @Bind(R.id.app_bar_layout)
    AppBarLayout appBarLayout;


    private MainFragment mainFragment;
    private TodoListFragment todoListFragment;
    private View haedView;
    private ImageView headImage;
    private CircleImageView headCircle;
    private DataBaseHelper helper;
    private ImageButton btn_refresh;
    private LinearLayout btn_todo;
    private long lastClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //透明状态栏
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            drawerLayout.setFitsSystemWindows(false);
        } else {
            AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.layout_main).findViewById(R.id.app_bar_layout);
            appBarLayout.setPadding(0, getStatusBarHeight(), 0, 0);

        }
        ButterKnife.bind(this);

        setFrontView();
        mainFragment = new MainFragment(MainActivity.this, "热门");
        getFragmentManager().beginTransaction().add(mainContainer.getId(), mainFragment).commit();

    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public void setFrontView() {
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
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.KITKAT) {
            drawerLayout.setFitsSystemWindows(true);
//            layout.setFitsSystemWindows(true);
        }


        toggle.syncState();
        navView.setNavigationItemSelectedListener(this);
        haedView = LayoutInflater.from(MainActivity.this).inflate(R.layout.nav_header_main, null);
        navView.addHeaderView(haedView);
        headImage = (ImageView) haedView.findViewById(R.id.head_back);

        Picasso.with(MainActivity.this).load(R.drawable.headimage).into(headImage);

        headCircle = (CircleImageView) haedView.findViewById(R.id.head_headimage);
        headCircle.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.head));
        btn_refresh = (ImageButton) haedView.findViewById(R.id.head_refresh);
        btn_todo = (LinearLayout) haedView.findViewById(R.id.head_todo);
        btn_refresh.setOnClickListener(this);
        btn_todo.setOnClickListener(this);

        initMenu();

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

    public void initMenu() {
        if (Util.isNetworkConnected(MainActivity.this)) {
            BmobQuery<MainIndexs> query = new BmobQuery<MainIndexs>();
//查询playerName叫“比目”的数据
//返回50条数据，如果不加上这条语句，默认返回10条数据
            query.setLimit(50);
//执行查询方法
            query.findObjects(MainActivity.this, new FindListener<MainIndexs>() {
                @Override
                public void onSuccess(List<MainIndexs> list) {
                    navView.getMenu().clear();
                    navView.getMenu().add("热门");

                    for (int i = 0; i < list.size(); i++) {
                        Log.i(TAG, "onSuccess: " + list.get(i).getIndex());
                        navView.getMenu().add(list.get(i).getIndex());
                    }
                    saveData(list);
                }

                @Override
                public void onError(int i, String s) {
                    Log.i(TAG, "onError: 获取数据失败" + s);
                }
            });
        } else {
            getSavedData();
        }
    }

    private void saveData(List<MainIndexs> list) {
        helper = new DataBaseHelper(MainActivity.this, "listData.db3", 1, DataBaseHelper.TYPE_LISTS);
        helper.getReadableDatabase().delete("Lists", "_id>0", null);

        for (int i = 0; i < list.size(); i++) {
            MainIndexs index = list.get(i);
            helper.getReadableDatabase().execSQL("insert into Lists values(null,?)", new String[]{index.getIndex()});
        }
        if (helper != null) {
            helper.hashCode();
        }
    }

    private void getSavedData() {
        helper = new DataBaseHelper(MainActivity.this, "listData.db3", 1, DataBaseHelper.TYPE_LISTS);
        Cursor cursor = helper.getReadableDatabase().rawQuery("select * from Lists ", null);
        while (cursor.moveToNext()) {
            navView.getMenu().add(cursor.getString(1));
        }
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


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        String index = String.valueOf(item.getTitle());
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

//        getFragmentManager().beginTransaction().replace(mainContainer.getId(), mainFragment).commit();
//        getFragmentManager().beginTransaction().hide(todoListFragment).commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.head_refresh) {
            Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.romate);
            btn_refresh.setAnimation(animation);
            animation.start();
            initMenu();
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
}
