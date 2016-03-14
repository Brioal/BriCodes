package com.brioal.bricodes.fragment;

import android.annotation.SuppressLint;
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
import com.brioal.bricodes.base.Constants;
import com.brioal.bricodes.base.User;
import com.brioal.bricodes.util.DataBaseHelper;
import com.brioal.bricodes.util.Util;
import com.brioal.bricodes.view.CircleImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragmentInfo";
    @Bind(R.id.fragment_main_recycler)
    RecyclerView fragmentMainRecycler;
    @Bind(R.id.fragment_main_refresh)
    SwipeRefreshLayout fragmentMainRefresh;
    @Bind(R.id.fragment_main_error)
    RelativeLayout fragmentMainError;


    private List<CodeItem> items;
    private MyAdapter adapter;
    private String index;
    private DataBaseHelper dataBaseHelper;
    private Context context;

    protected ImageLoader imageLoader;
    private int ITEM_WITH_PIC = 0;
    private int ITEM_NO_PIC = 1;


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

    @SuppressLint("ValidFragment")
    public MainFragment() {

    }

    @SuppressLint("ValidFragment")
    public MainFragment(Context context, String index) {
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
        fragmentMainRefresh.setRefreshing(true);
        fragmentMainRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                new Thread(runnable).start();
            }
        });
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(-1, -1);
        params.setMargins(0, 80, 0, 0);
        fragmentMainRecycler.setLayoutParams(params);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public void saveData() {
        dataBaseHelper = new DataBaseHelper(getActivity(), "codeitem.db3", 1, DataBaseHelper.TYPE_CODES);
        dataBaseHelper.getReadableDatabase().delete("CodeItems", "_id>0", null);
        try {
            for (int i = 0; i < items.size(); i++) {
                CodeItem item = items.get(i);
                dataBaseHelper.getReadableDatabase().execSQL("insert into CodeItems values(null,?,?,?,?,?,?,?,?,?)", new Object[]{item.getmTitle(), item.getmCode(), item.getmTime(), item.getIndex(), item.getUrl(getActivity()), item.getmDesc(), item.getmAuther(), item.getmRead(), item.getmLike()});

            }
            if (dataBaseHelper != null) {
                dataBaseHelper.hashCode();
            }
        } catch (Exception e) {

        }
    }

    public void initData() {
        getSavedData();
        if (Util.isNetworkConnected(context)) {
            BmobQuery<CodeItem> query = new BmobQuery<CodeItem>();
            List<BmobQuery<CodeItem>> queries = new ArrayList<BmobQuery<CodeItem>>();
            if (index.equals("首页")) {
                Date date = new Date();
                date.setTime(System.currentTimeMillis());
                BmobQuery<CodeItem> eq1 = new BmobQuery<CodeItem>();
                eq1.addWhereGreaterThan("id", 0);
                queries.add(eq1);
            } else {
                BmobQuery<CodeItem> eq1 = new BmobQuery<CodeItem>();
                BmobQuery<CodeItem> eq2 = new BmobQuery<CodeItem>();
                BmobQuery<CodeItem> eq3 = new BmobQuery<CodeItem>();
                eq1.addWhereContains("index", index);
                eq2.addWhereContains("mTitle", index);
                eq3.addWhereContains("mDesc", index);


                queries.add(eq1);
                queries.add(eq2);
                queries.add(eq3);

            }
            query.or(queries);
            query.setLimit(50);
            query.findObjects(getActivity(), new FindListener<CodeItem>() {
                @Override
                public void onSuccess(List<CodeItem> list) {
                    if (items != null) {
                        items.clear();
                        items = null;
                    }
                    items = list;
                    Log.i(TAG, "onSuccess: " + items.size());
                    if (index.equals("首页")) {

                        saveData();
                    }
                    handler.sendEmptyMessage(0x123);
                }

                @Override
                public void onError(int i, String s) {
                    Log.i(TAG, "onError: 获取数据失败,读取本地数据" + s);
                }
            });
        }


    }

    private void getSavedData() {
        Log.i(TAG, "getSavedData: 获取本地数据");
        if (items == null) {
            items = new ArrayList<>();
        } else {
            items.clear();
        }
        dataBaseHelper = new DataBaseHelper(context, "codeitem.db3", 1, DataBaseHelper.TYPE_CODES);
        Cursor cursor = null;
        if (index.equals("首页")) {
            cursor = dataBaseHelper.getReadableDatabase().rawQuery("select * from CodeItems where _id > 0", null);
        } else {
            cursor = dataBaseHelper.getReadableDatabase().rawQuery("select * from CodeItems where mIndex like '%" + index + "%' or mTitle like '%" + index + "%' or mDesc like '%" + index + "%'", null);//TODO记录

        }
        CodeItem item = null;
        while (cursor.moveToNext()) {
            Log.i(TAG, "getSavedData: " + cursor.getString(4));
            item = new CodeItem(Integer.valueOf(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getInt(8), cursor.getInt(9));
            items.add(item);
        }
        handler.sendEmptyMessage(0x123);

    }

    public void setViews() {
        imageLoader = ImageLoader.getInstance();

        if (items.size() == 0) { //查询不到数据
            fragmentMainRecycler.setVisibility(View.GONE);
            fragmentMainError.setVisibility(View.VISIBLE);
        } else {
            fragmentMainRecycler.setVisibility(View.VISIBLE);
            fragmentMainError.setVisibility(View.GONE);
           DividerLine dividerLine =new  DividerLine(DividerLine.VERTICAL);
            dividerLine.setSize(10);
            dividerLine.setColor(getResources().getColor(R.color.color_trans));
            if (adapter == null) {
                fragmentMainRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
                adapter = new MyAdapter();
                fragmentMainRecycler.addItemDecoration(dividerLine);
                fragmentMainRecycler.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    public class MyAdapter extends RecyclerView.Adapter {


        @Override
        public int getItemViewType(int position) {
            if (items.get(position).getUrl(getActivity()) == null) {
                return ITEM_NO_PIC;
            } else {
                return ITEM_WITH_PIC;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            if (viewType == ITEM_WITH_PIC) {
                return new MyViewHolder_pic(LayoutInflater.from(getActivity()).inflate(R.layout.item_code_pic, parent, false));
            } else {
                return new MyViewHolder((LayoutInflater.from(getActivity()).inflate(R.layout.item_code, parent, false)));
            }
        }
        //TODO 还是存在错位问题
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//            holder.setIsRecyclable(false);
            final CodeItem item = items.get(position);
            if (holder instanceof MyViewHolder_pic) { // 包含图片
                final MyViewHolder_pic curholder = (MyViewHolder_pic) holder;
                curholder.mTitle.setText(item.getmTitle());
                curholder.mTime.setText(item.getmTime());
                curholder.mTime.setTag(item.getId());
                ImageLoader.getInstance().displayImage(item.getUrl(getActivity()), ((MyViewHolder_pic) holder).imageView);
                curholder.mDesc.setText(item.getmDesc());
                curholder.mIndex.setText(item.getIndex());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), CodeDetailActivity.class);
                        intent.putExtra("id", item.getObjectId());
                        intent.putExtra("mTitle", item.getmTitle());
                        intent.putExtra("mCode", item.getmCode());
                        intent.putExtra("mTime", item.getmTime());
                        intent.putExtra("mAuther", item.getmAuther());
                        intent.putExtra("mRead", item.getmRead());
                        startActivity(intent);
                    }
                });
                BmobQuery<User> query = new BmobQuery<User>();
                query.addWhereEqualTo("username", item.getmAuther());
                query.findObjects(getActivity(), new FindListener<User>() {
                    @Override
                    public void onSuccess(List<User> object) {
                        // 允许用户使用应用
                        Log.i(TAG, "onSuccess: " + object.get(0).getUserHead().getFileUrl(getActivity()));
                        ImageLoader.getInstance().displayImage(object.get(0).getUserHead().getFileUrl(getActivity()), curholder.mAuthorHead, Constants.options);
                    }

                    @Override
                    public void onError(int code, String msg) {
                        //获取用户失败
                    }
                });
            } else if (holder instanceof MyViewHolder) { // 不包含图片
                final MyViewHolder curholder1 = (MyViewHolder) holder;
                curholder1.mTitle.setText(item.getmTitle());
                curholder1.mTime.setText(item.getmTime());
                curholder1.mTime.setTag(item.getId());
                curholder1.mDesc.setText(item.getmDesc());
                curholder1.mIndex.setText(item.getIndex());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), CodeDetailActivity.class);
                        intent.putExtra("id", item.getObjectId());
                        intent.putExtra("mTitle", item.getmTitle());
                        intent.putExtra("mCode", item.getmCode());
                        intent.putExtra("mTime", item.getmTime());
                        intent.putExtra("mAuther", item.getmAuther());
                        intent.putExtra("mRead", item.getmRead());
                        startActivity(intent);
                    }
                });
                BmobQuery<User> query = new BmobQuery<User>();
                query.addWhereEqualTo("username", item.getmAuther());
                query.findObjects(getActivity(), new FindListener<User>() {
                    @Override
                    public void onSuccess(List<User> object) {
                        // 允许用户使用应用
                        Log.i(TAG, "onSuccess: " + object.get(0).getUserHead().getFileUrl(getActivity()));
                        ImageLoader.getInstance().displayImage(object.get(0).getUserHead().getFileUrl(getActivity()), curholder1.mAuthorHead, Constants.options);
                    }

                    @Override
                    public void onError(int code, String msg) {
                        //获取用户失败
                    }
                });
            }
        }


        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    public class MyViewHolder_pic extends RecyclerView.ViewHolder {
        private TextView mTitle;
        private TextView mTime;
        private View itemView;
        private TextView mIndex;
        private TextView mDesc;
        private CircleImageView mAuthorHead;
        private ImageView imageView;

        public MyViewHolder_pic(View itemView) {
            super(itemView);
            this.itemView = itemView;
            mTime = (TextView) itemView.findViewById(R.id.item_code_tv_time_pic);
            mTitle = (TextView) itemView.findViewById(R.id.item_code_tv_title_pic);
            mDesc = (TextView) itemView.findViewById(R.id.item_code_tv_desc_pic);
            mIndex = (TextView) itemView.findViewById(R.id.item_code_tv_index_pic);
            mAuthorHead = (CircleImageView) itemView.findViewById(R.id.item_code_author_head_pic);
            imageView = (ImageView) itemView.findViewById(R.id.item_code_image_pic);
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView mTitle;
        private TextView mTime;
        private View itemView;
        private TextView mIndex;
        private TextView mDesc;
        private CircleImageView mAuthorHead;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            mTime = (TextView) itemView.findViewById(R.id.item_code_tv_time);
            mTitle = (TextView) itemView.findViewById(R.id.item_code_tv_title);
            mDesc = (TextView) itemView.findViewById(R.id.item_code_tv_desc);
            mIndex = (TextView) itemView.findViewById(R.id.item_code_tv_index);
            mAuthorHead = (CircleImageView) itemView.findViewById(R.id.item_code_author_head);
        }
    }

    public class DividerLine extends RecyclerView.ItemDecoration {
        public static final int HORIZONTAL = LinearLayoutManager.HORIZONTAL;
        public static final int VERTICAL = LinearLayoutManager.VERTICAL;
        private Paint paint;
        private int orientation;
        private int color;
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

        public void setColor(int color) {
            this.color = color;
            paint.setColor(color);
        }

        public void setSize(int size) {
            this.size = size;
        }

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
    public static class SortComparator implements Comparator {
        @Override
        public int compare(Object lhs, Object rhs) {
            CodeItem first = (CodeItem) lhs;
            CodeItem second = (CodeItem) rhs;
            return -(first.getmTime().compareTo(second.getmTime()));
        }
    }
}
