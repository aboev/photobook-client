package com.freecoders.photobook.classes;

import android.view.View;
import android.view.ViewGroup;

import com.freecoders.photobook.common.Constants;

/**
 * Created by aleksey.boev on 2015-04-08.
 */
public class BookmarkHandler {
    private ViewGroup bookmarkView;
    public Boolean boolOpen;
    private int defaultHeight;

    public BookmarkHandler(ViewGroup bookmarkView, int defaultHeight){
        this.bookmarkView = bookmarkView;
        this.defaultHeight = defaultHeight;
        this.boolOpen = getHeight() > 0;
    }

    public int getHeight() {
        ViewGroup.LayoutParams params = bookmarkView.getLayoutParams();
        return params.height;
    }

    public void setHeight(int height) {
        ViewGroup.LayoutParams params = bookmarkView.getLayoutParams();
        params.height = height;
        bookmarkView.setLayoutParams(params);
    }

    public void close(){
        setHeight(0);
        boolOpen = false;
    }

    public void open(){
        setHeight(defaultHeight);
        boolOpen = true;
    }

    public void stopFling() {
        if (getHeight() < Constants.BOOKMARKS_HEIGHT / 2)
            close();
        else
            open();
    }
}
