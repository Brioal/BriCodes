package com.brioal.bricodes.fragment;

import android.app.Fragment;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.brioal.bricodes.R;
import com.brioal.bricodes.base.TodoListItem;
import com.brioal.bricodes.util.DataBaseHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by brioal on 16-3-1.
 */
public class TodoListFragment extends Fragment {
    private static final String TAG = "TodoInfo";
    @Bind(R.id.fragment_todo_list)
    ListView fragmentTodoList;
    private View rootView;
    private BaseAdapter adapter;
    private List<TodoListItem> lists;
    private boolean isDeleting = false;
    private List<TodoListItem> deleteIndexs;
    private DataBaseHelper helper;
    private int sum, finsihed;
    private TextView tv_state;
    private ImageButton btn_add;
    private boolean canAdd = false;
    private Comparator comparator;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        rootView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_todo, null);
        deleteIndexs = new ArrayList<>();
        comparator = new Sort();
        if (readData().size() == 0) {
            lists = new ArrayList<>();
        } else {
            lists = readData();
        }
        tv_state = (TextView) rootView.findViewById(R.id.fragment_todo_detail);
        //初始化详情
        sum = lists.size();
        for (int i = 0; i < lists.size(); i++) {
            if (lists.get(i).isFinish()) {
                finsihed++;
            }
        }
        tv_state.setText((sum - finsihed) + "/" + sum + " 未完成");
        btn_add = (ImageButton) rootView.findViewById(R.id.fragment_todo_add); // 点击事件，添加一个空的item
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canAdd || lists.size() == 0) {
                    if (lists != null) {
                        lists.add(new TodoListItem(lists.size() + 1, "", false));
                        adapter.notifyDataSetChanged();
                    }
                }

            }
        });

        adapter = new BaseAdapter() {
            @Override
            public void notifyDataSetChanged() {
                super.notifyDataSetChanged();
                finsihed = 0;
                sum = lists.size();
                for (int i = 0; i < lists.size(); i++) {
                    if (lists.get(i).isFinish()) {
                        finsihed++;
                    }
                }
                tv_state.setText((sum - finsihed) + "/" + sum + " 未完成");
            }

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
                return lists.get(position).getId();
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
//                MyViewHolder holder;
//                if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_todo_list, null);
//                    holder = new MyViewHolder(convertView);
//                    convertView.setTag(holder);
//                } else {
//                    holder = (MyViewHolder) convertView.getTag();
//                }
                CheckBox radio = (CheckBox) convertView.findViewById(R.id.item_todo_check);
                EditText title = (EditText) convertView.findViewById(R.id.item_todo_tv);

                CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.item_todo_delete);
                final TodoListItem item = lists.get(position);
                if (item.isFinish()) {
                    radio.setChecked(true);
                    title.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    radio.setChecked(false);
                    title.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG);
                }

                title.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (s.toString().equals("")) {
                            canAdd = false;
                            Log.i(TAG, "afterTextChanged: 不能添加新的");
                        } else {
                            item.setmContent(s.toString());
                            Log.i(TAG, "afterTextChanged: 能添加新的");
                            canAdd = true;
                        }
                    }
                });

                radio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            Log.i(TAG, "onCheckedChanged: " + item.getId());
                            lists.get(position).setIsFinish(true);
                            adapter.notifyDataSetChanged();
                        } else {
                            lists.get(position).setIsFinish(false);
                            adapter.notifyDataSetChanged();
                        }
                    }
                });

                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                        if (isChecked) {
                        deleteIndexs.add(item);
                        Log.i(TAG, "onCheckedChanged: 勾选第" + position);
//                        } else {
//                            deleteIndexs.remove(position);
//                        }
                    }
                });
                title.setText(item.getmContent());

                if (isDeleting) {
                    checkBox.setVisibility(View.VISIBLE);
                } else {
                    checkBox.setVisibility(View.GONE);
                }

                return convertView;
            }
        };
        ButterKnife.bind(this, rootView);
        fragmentTodoList.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();

        saveData(lists);
    }

    public boolean isDeleting() {
        return isDeleting;
    }


    @Override
    public void onDetach() {
        saveData(lists);
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        saveData(lists);
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        saveData(lists);
    }

    //显示删除模式
    public void showDelete() {
        Log.i(TAG, "showDelete: 显示删除");
        isDeleting = true;
        adapter.notifyDataSetChanged();
        if (deleteIndexs == null) {
            deleteIndexs = new ArrayList<>();
        } else {
            deleteIndexs.clear();
        }
    }


    //保存数据
    public void saveData(List<TodoListItem> lists) {
        List<TodoListItem> listItems = new ArrayList<>();
        for (int i = 0; i < lists.size(); i++) {
            if (!lists.get(i).getmContent().equals("")) {
                listItems.add(lists.get(i));
            }
        }
        helper = new DataBaseHelper(getActivity(), "todos.db3", 1, DataBaseHelper.TYPE_TODOS);
        helper.getReadableDatabase().execSQL("delete from Todos where _id > 0");
        TodoListItem item = null;
        for (int i = 0; i < listItems.size(); i++) {
            item = listItems.get(i);
            int n = 0;
            if (item.isFinish()) {
                n = 1;
            }
            Log.i(TAG, "saveData: " + item.getmContent());
            helper.getReadableDatabase().execSQL("insert into Todos values(null,?,?)", new Object[]{item.getmContent(), n});

        }
    }



    public List<TodoListItem> readData() {
        List<TodoListItem> list = new ArrayList<>();
        helper = new DataBaseHelper(getActivity(), "todos.db3", 1, DataBaseHelper.TYPE_TODOS);

        Cursor cursor = helper.getReadableDatabase().rawQuery("select * from Todos where _id >0", null);
        TodoListItem item = null;
        while (cursor.moveToNext()) {
            boolean isFinish = false;
            if (cursor.getInt(2) == 1) {
                isFinish = true;
            }
            item = new TodoListItem(cursor.getInt(0), cursor.getString(1), isFinish);
            list.add(item);
        }
        Collections.sort(list, comparator);
        return list;
    }

    //删除
    public void delete() {
        Log.i(TAG, "delete: 开始删除");
        for (int i = 0; i < deleteIndexs.size(); i++) {
            lists.remove(deleteIndexs.get(i));
            Log.i(TAG, "delete: " + deleteIndexs.get(i).getId());
            Collections.sort(lists, comparator);
        }
        isDeleting = false;
        adapter.notifyDataSetChanged();
    }

    public class Sort implements Comparator<TodoListItem> {

        @Override
        public int compare(TodoListItem lhs, TodoListItem rhs) {
            if (!lhs.isFinish()) {
                lhs.setId(lhs.getId() * 10);
            }
            if (!rhs.isFinish()) {
                rhs.setId(rhs.getId() * 10);
            }
            return rhs.getId() - lhs.getId();
        }
    }


}
