package com.brioal.bricodes.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brioal.bricodes.R;
import com.brioal.bricodes.activity.CodeDetailActivity;
import com.brioal.bricodes.base.CodeItem;
import com.brioal.bricodes.util.DataBaseHelper;
import com.brioal.bricodes.util.Util;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.listener.FindListener;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragmentInfo";
    @Bind(R.id.fragment_main_recycler)
    RecyclerView fragmentMainRecycler;
    @Bind(R.id.fragment_main_refresh)
    SwipeRefreshLayout fragmentMainRefresh;
    @Bind(R.id.fragment_main_error)
    RelativeLayout fragmentMainError;

    //TODO launcherActivity退出逻辑修改 ，防止图片下载未完成退出


    private List<CodeItem> items;
    private MyAdapter adapter;
    private String index ;
    private DataBaseHelper dataBaseHelper;
    private Context context ;



    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x123) {
                if (fragmentMainRefresh.isRefreshing()) {
                    fragmentMainRefresh.setRefreshing(false);
                }
                SortComparator comparator = new SortComparator();
                Collections.sort(items, comparator);
                setViews();

            }
        }
    };
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            initData();
        }
    };


    public MainFragment(Context context ,String index) {
        this.context = context;
        setIndex(index);
    }

    public void setIndex(String index) {
        this.index = index;

        new Thread(runnable).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);
        fragmentMainRefresh.setColorSchemeResources(R.color.color_refresh1, R.color.color_refresh2, R.color.color_refresh3, R.color.color_refresh4);
        fragmentMainRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(runnable).start();
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public void saveData() {
        dataBaseHelper = new DataBaseHelper(getActivity(),"codeitem.db3",1,DataBaseHelper.TYPE_CODES);
        dataBaseHelper.getReadableDatabase().delete("CodeItems", "_id>0", null);
        for (int i = 0; i < items.size(); i++) {
            CodeItem item = items.get(i);
                dataBaseHelper.getReadableDatabase().execSQL("insert into CodeItems values(null,?,?,?,?)", new String[]{item.getmTitle(), item.getmCode(), item.getmTime(),item.getIndex()});

        }
        if (dataBaseHelper != null) {
            dataBaseHelper.hashCode();
        }
    }

    public void initData() {
        if (Util.isNetworkConnected(context)) {
            BmobQuery<CodeItem> query = new BmobQuery<CodeItem>();
//查询playerName叫“比目”的数据
            if (index.equals("热门")) {
                Date date = new Date();
                date.setTime(System.currentTimeMillis());
                query.addWhereLessThan("updatedAt", new BmobDate(date));
            } else {
                query.addWhereContains("index", index);
            }
//返回50条数据，如果不加上这条语句，默认返回10条数据
            query.setLimit(50);
//执行查询方法
            query.findObjects(getActivity(), new FindListener<CodeItem>() {
                @Override
                public void onSuccess(List<CodeItem> list) {
                    if (items != null) {
                        items.clear();
                        items = null;
                    }
                    items = list;
                    Log.i(TAG, "onSuccess: "+items.size());
//                    if (items != null) {
                        saveData();
                        handler.sendEmptyMessage(0x123);
//                    }
                }

                @Override
                public void onError(int i, String s) {
                    Log.i(TAG, "onError: 获取数据失败,读取本地数据" + s);
                }
            });
        } else {
            getSavedData();
        }


    }

    private void getSavedData() {
        if (items == null) {
            items = new ArrayList<>();
        } else {
            items.clear();
        }
        dataBaseHelper = new DataBaseHelper(context, "codeitem.db3", 1, DataBaseHelper.TYPE_CODES);
        Cursor cursor = null;
        if (index.equals("热门")) {

            cursor = dataBaseHelper.getReadableDatabase().rawQuery("select * from CodeItems where _id > 0", null);
        } else {
            cursor = dataBaseHelper.getReadableDatabase().rawQuery("select * from CodeItems where mIndex like ?", new String[]{index});

        }
        CodeItem item = null;
        while (cursor.moveToNext()) {
            item = new CodeItem(Integer.valueOf(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
            items.add(item);
        }
        handler.sendEmptyMessage(0x123);

    }

    public void setViews() {
        if (items.size() == 0) { //查询不到数据
            fragmentMainRecycler.setVisibility(View.GONE);
            fragmentMainError.setVisibility(View.VISIBLE);
        } else {
            fragmentMainRecycler.setVisibility(View.VISIBLE);
            fragmentMainError.setVisibility(View.GONE);
            adapter = new MyAdapter();
            fragmentMainRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
            DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
            dividerLine.setSize(10);
            dividerLine.setColor(getResources().getColor(R.color.color_trans));
            fragmentMainRecycler.addItemDecoration(dividerLine);
            fragmentMainRecycler.setAdapter(adapter);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


            return new MyViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.item_code, parent, false));
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final CodeItem item = items.get(position);
            Log.i(TAG, "onBindViewHolder: " + item.getmTitle());
            Log.i(TAG, "onBindViewHolder: " + item.getUpdatedAt());
            holder.mTitle.setText(item.getmTitle());
            holder.mTime.setText(item.getmTime());
            String url = item.getmHead().getFileUrl(getActivity());
            Picasso.with(getActivity()).load(url).error(R.drawable.icon).into(holder.mHead);
            holder.mDesc.setText(item.getmDesc());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), CodeDetailActivity.class);
                    intent.putExtra("mTitle", item.getmTitle());
                    intent.putExtra("mCode", item.getmCode());
                    intent.putExtra("mTime", item.getmTime());
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView mTitle;
        private TextView mTime;
        private View itemView;
        private ImageView mHead;
        private TextView mDesc ;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            mTime = (TextView) itemView.findViewById(R.id.item_code_tv_time);
            mTitle = (TextView) itemView.findViewById(R.id.item_code_tv_title);
            mHead = (ImageView) itemView.findViewById(R.id.item_code_tv_image);
            mDesc = (TextView) itemView.findViewById(R.id.item_code_tv_desc);
        }
    }

    public class DividerLine extends RecyclerView.ItemDecoration {
        /**
         * 水平方向
         */
        public static final int HORIZONTAL = LinearLayoutManager.HORIZONTAL;

        /**
         * 垂直方向
         */
        public static final int VERTICAL = LinearLayoutManager.VERTICAL;

        // 画笔
        private Paint paint;

        // 布局方向
        private int orientation;
        // 分割线颜色
        private int color;
        // 分割线尺寸
        private int size;

        public DividerLine() {
            this(VERTICAL);
        }

        public DividerLine(int orientation) {
            this.orientation = orientation;

            paint = new Paint();
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDrawOver(c, parent, state);

            if (orientation == VERTICAL) {
                drawHorizontal(c, parent);
            } else {
                drawVertical(c, parent);
            }
        }

        /**
         * 设置分割线颜色
         *
         * @param color 颜色
         */
        public void setColor(int color) {
            this.color = color;
            paint.setColor(color);
        }

        /**
         * 设置分割线尺寸
         *
         * @param size 尺寸
         */
        public void setSize(int size) {
            this.size = size;
        }

        // 绘制垂直分割线
        protected void drawVertical(Canvas c, RecyclerView parent) {
            final int top = parent.getPaddingTop();
            final int bottom = parent.getHeight() - parent.getPaddingBottom();

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int left = child.getRight() + params.rightMargin;
                final int right = left + size;
                c.drawRect(left, top, right, bottom, paint);
            }
        }

        // 绘制水平分割线
        protected void drawHorizontal(Canvas c, RecyclerView parent) {
            final int left = parent.getPaddingLeft();
            final int right = parent.getWidth() - parent.getPaddingRight();

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int top = child.getBottom() + params.bottomMargin;
                final int bottom = top + size;
                c.drawRect(left, top, right, bottom, paint);

            }
        }
    }
    //从大到小排序
    public class SortComparator implements Comparator {
        @Override
        public int compare(Object lhs, Object rhs) {
            CodeItem first = (CodeItem) lhs;
            CodeItem second = (CodeItem) rhs;
            return  -(first.getId() - second.getId());
        }
    }
}
