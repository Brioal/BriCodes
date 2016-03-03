package com.brioal.bricodes.base;

import android.content.Context;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

/**
 * Created by brioal on 16-2-28.
 */
public class MainIndexs extends BmobObject {
    private BmobFile headImage ;
    private String index ;
    private int isShow = 0  ; //显示
    private String url ;
    public MainIndexs() {

    }


    public String getUrl(Context context) {
        if (headImage == null) {
            return url;
        }
        return headImage.getFileUrl(context);
    }

    public BmobFile getHeadImage() {
        return headImage;
    }


    public int getIsShow() {
        return isShow;
    }


    public MainIndexs(String index, int isShow,String url) {
        this.index = index;
        this.isShow = isShow;
        this.url = url;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }
}
