package com.brioal.bricodes.base;

import android.content.Context;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

/**
 * Created by brioal on 16-2-28.
 */
public class CodeItem extends BmobObject{
    private int id;
    private String mTitle ;
    private String mCode ;
    private String index ;
    private String mTime ;
    private String mUrl ;
    private BmobFile mHead ;
    private String mDesc ;
    private String mAuther ;
    private int mRead ;
    private int mLike ;

    public CodeItem() {
    }

    public CodeItem(int id, String mTitle, String mCode,  String mTime,String index,String mUrl , String mDesc,String mAuther , int mRead ,int mLike) {
        this.id = id;
        this.mTitle = mTitle;
        this.mCode = mCode;
        this.index = index;
        this.mTime = mTime;
        this.mUrl = mUrl;
        this.mDesc = mDesc;
        this.mAuther = mAuther;
        this.mRead = mRead;
        this.mLike = mLike;
    }

    public int getmLike() {
        return mLike;
    }

    public String getmAuther() {
        return mAuther;
    }

    public int getmRead() {
        return mRead;
    }

    public String getUrl(Context context) {
        if (mUrl!=null) {
            return mUrl;
        }

        if (mHead == null) {
            return null;
        }
        return mHead.getFileUrl(context);
    }

    public BmobFile getmHead() {
        return mHead;
    }

    public String getmDesc() {
        return mDesc;
    }

    public String getIndex() {
        return index;
    }

    public String getmTitle() {
        return mTitle;
    }


    public String getmCode() {
        return mCode;
    }




    public String getmTime() {
        if (super.getUpdatedAt() == null) {
            return mTime;
        }
        return super.getUpdatedAt();
    }

    public int getId() {
        return id;
    }

    public void setmRead(int mRead) {
        this.mRead = mRead;
    }

    public void setmLike(int mLike) {
        this.mLike = mLike;
    }
}
