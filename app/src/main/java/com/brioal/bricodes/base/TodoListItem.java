package com.brioal.bricodes.base;

/**
 * Created by brioal on 16-3-1.
 */
public class TodoListItem {
    private int id ;
    private String mContent ;
    private boolean isFinish  ;

    public TodoListItem(int id, String mContent, boolean isFinish) {
        this.id = id;
        this.mContent = mContent;
        this.isFinish = isFinish;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getmContent() {
        return mContent;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public void setIsFinish(boolean isFinish) {
        this.isFinish = isFinish;
    }

    public void setmContent(String mContent) {
        this.mContent = mContent;
    }
}
