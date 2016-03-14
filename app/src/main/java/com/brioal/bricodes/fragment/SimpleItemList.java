package com.brioal.bricodes.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.brioal.bricodes.R;
import com.brioal.bricodes.activity.CodeDetailActivity;
import com.brioal.bricodes.base.CodeItem;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;


public class SimpleItemList extends Fragment {
    private static final String TAG = "SimpleItemListInfo";
    public static int STATE_MINE = 1;
    public static int STATE_FAVORATE = 2;
    @Bind(R.id.fragment_simple_list_listview)
    ListView fragmentSimpleListListview;

    private int state;
    private MyBaseAdapter adapter;
    private String mAuthor;
    private List<CodeItem> lists;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x123) {
                setView();
            }
        }
    };

    public SimpleItemList(String mAuthor) {
        this.mAuthor = mAuthor;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_simple_item_list, null);
        new Thread(new Runnable() {
            @Override
            public void run() {
                initData();
            }
        }).start();
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    public void initData() {

        if (state == SimpleItemList.STATE_MINE) { // 我的文章列表
            BmobQuery<CodeItem> query = new BmobQuery<>();
            query.addWhereEqualTo("mAuther", mAuthor);
            query.findObjects(getActivity(), new FindListener<CodeItem>() {
                @Override
                public void onSuccess(List<CodeItem> list) {
                    lists = list;
                    Collections.sort(lists, new MainFragment.SortComparator());
                    handler.sendEmptyMessage(0x123);
                    Log.i(TAG, "onSuccess: 个人发布列表获取成功");
                }

                @Override
                public void onError(int i, String s) {
                    Log.i(TAG, "onError: 个人发布列表获取失败");
                }
            });
        } else { //收藏的文章列表

        }


    }

    public void setView() {
        if (adapter == null) {
            adapter = new MyBaseAdapter();
            fragmentSimpleListListview.setAdapter(adapter);
            fragmentSimpleListListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    CodeItem item = lists.get(position);
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
        } else {
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }


    class MyBaseAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return lists.size();
        }

        @Override
        public Object getItem(int position) {
            return lists.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder ;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_code_simple, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();

            }

            CodeItem item = lists.get(position);

            holder.itemCodeSimpleName.setText(item.getmTitle());
            holder.itemCodeSimpleUpdateat.setText(item.getmTime());
            holder.itemCodeSimpleRead.setText(item.getmRead()+"");
            holder.itemCodeSimpleFavorate.setText(item.getmLike()+"");
            // CodeItem内添加 ，DatabaseHelper添加 ， saveData添加， getSaveData添加 ，数据库添加
            return convertView;
        }

         class ViewHolder {
            @Bind(R.id.item_code_simple_name)
            TextView itemCodeSimpleName;
            @Bind(R.id.item_code_simple_updateat)
            TextView itemCodeSimpleUpdateat;
            @Bind(R.id.item_code_simple_read)
            TextView itemCodeSimpleRead;
            @Bind(R.id.item_code_simple_favorate)
            TextView itemCodeSimpleFavorate;

            ViewHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }
    }





}
