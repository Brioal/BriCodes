package com.brioal.bricodes.base;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

/**
 * Created by brioal on 16-2-27.
 */
public class LauncherData extends BmobObject {
    private String launcherTitle ;
    private BmobFile launcherImage ;

    public LauncherData() {
        setTableName("LauncherData");
    }

    public String getLauncherTitle() {
        return launcherTitle;
    }

    public void setLauncherTitle(String launcherTitle) {
        this.launcherTitle = launcherTitle;
    }

    public BmobFile getLauncherImage() {
        return launcherImage;
    }

    public void setLauncherImage(BmobFile launcherImage) {
        this.launcherImage = launcherImage;
    }
}
