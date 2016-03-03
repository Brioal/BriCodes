package com.brioal.bricodes.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by brioal on 16-2-29.
 */
public class DataBaseHelper extends SQLiteOpenHelper {
    public static int TYPE_CODES = 0;
    public static int TYPE_LISTS = 1;
    public static int TYPE_TODOS = 2;
    private int type ;
    final String CREATE_CODE_TABLE = "create table CodeItems(_id integer primary key autoincrement , mTitle , mCode , mTime,mIndex)";
    final String CREATE_LIST_TABLE = "create table Lists(_id integer primary key autoincrement , mTitle ,integer isShow,url)";
    final String CREATE_TODO_TABLE = "create table Todos(_id integer primary key autoincrement , mContent , integer isFinish)";

    public DataBaseHelper(Context context, String name, int version, int type) {
        super(context, name, null, version);
        this.type = type;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        if (type == DataBaseHelper.TYPE_CODES) {
            db.execSQL(CREATE_CODE_TABLE);
        } else if (type == DataBaseHelper.TYPE_LISTS) {
            db.execSQL(CREATE_LIST_TABLE);
        } else {
            db.execSQL(CREATE_TODO_TABLE);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
