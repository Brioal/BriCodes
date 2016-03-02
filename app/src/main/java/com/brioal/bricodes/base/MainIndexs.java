package com.brioal.bricodes.base;

import cn.bmob.v3.BmobObject;

/**
 * Created by brioal on 16-2-28.
 */
public class MainIndexs extends BmobObject {
    private String index ;

    public MainIndexs() {
    }

    public MainIndexs(String index) {
        this.index = index;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }
}
