package com.brioal.bricodes.base;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;

/**
 * Created by brioal on 16-3-8.
 */
public class User extends BmobUser {
    private BmobFile userHead;
    private int mMine;
    private int mLike ;


    public User() {

    }

    public int getmMine() {
        return mMine;
    }

    public void setmMine(int mMine) {
        this.mMine = mMine;
    }

    public void setmLike(int mLike) {
        this.mLike = mLike;
    }

    public int getmLike() {
        return mLike;
    }

    public BmobFile getUserHead() {
        return userHead;
    }

    public void setUserHead(BmobFile userHead) {
        this.userHead = userHead;
    }
}
