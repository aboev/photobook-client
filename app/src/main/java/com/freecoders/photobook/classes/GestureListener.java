package com.freecoders.photobook.classes;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.etsy.android.grid.StaggeredGridView;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.utils.ImageUtils;

/**
 * Created by Alex on 2015-03-23.
 */
public class GestureListener extends GestureDetector.SimpleOnGestureListener
        implements View.OnTouchListener
{
    Context context;
    GestureDetector mGestureDetector;
    ViewGroup mViewGroup;
    AbsListView mAbsListView;

    private Boolean boolOpen = false;
    private Boolean boolPullingDown = false;
    private Boolean boolPullingUp = false;

    private float startY = 0;

    public GestureListener(Context context, ViewGroup viewGroup, AbsListView absListView) {
        this.context = context;
        this.mViewGroup = viewGroup;
        this.mAbsListView = absListView;
        this.mGestureDetector = new GestureDetector(context, this);
        ViewGroup.LayoutParams params = mViewGroup.getLayoutParams();
        this.boolOpen = params.height > 0;

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        return super.onFling(e1, e2, velocityX, velocityY);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {

        return super.onSingleTapConfirmed(e);
    }

    public boolean onTouch(View v, MotionEvent event) {
        ViewGroup.LayoutParams params = mViewGroup.getLayoutParams();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            startY = event.getRawY();
            if (checkTopPosition()) return true;
        } else if ((event.getAction() == MotionEvent.ACTION_MOVE) ) {
            float dY = event.getRawY() - startY;
            if (checkTopPosition() && (dY > 0) && (!boolOpen)) {
                setBookmarkHeight(Math.min((int) dY / 2, Constants.BOOKMARKS_HEIGHT));
                return true;
            } else if (checkTopPosition() && (dY < 0) && boolOpen) {
                setBookmarkHeight(Math.max(Constants.BOOKMARKS_HEIGHT + (int) dY / 2, 0));
                return true;
            }
        } else if ((event.getAction() == MotionEvent.ACTION_UP) ) {
            if ((params.height < Constants.BOOKMARKS_HEIGHT / 2) && (params.height > 0)) {
                closeBookmarkTab();
                return true;
            } else if (params.height >= Constants.BOOKMARKS_HEIGHT / 2) {
                openBookmarkTab();
                return true;
            }
        }
        return mGestureDetector.onTouchEvent(event);
    }

    private Boolean checkTopPosition (){
        if (mAbsListView instanceof StaggeredGridView)
            return ((((StaggeredGridView) mAbsListView).getDistanceToTop() == 0)
                    && (mAbsListView.getFirstVisiblePosition() == 0));
        else
            return ((mAbsListView.getScrollY() == 0)
                    && (mAbsListView.getFirstVisiblePosition() == 0));
    }

    private void setPullDirection (Boolean boolDown) {
        boolPullingDown = boolDown;
        boolPullingUp = !boolDown;
    }

    private void closeBookmarkTab () {
        setBookmarkHeight(0);
        boolOpen = false;
    }

    private void openBookmarkTab () {
        setBookmarkHeight(Constants.BOOKMARKS_HEIGHT);
        boolOpen = true;
    }

    private void setBookmarkHeight (int height) {
        ViewGroup.LayoutParams params = mViewGroup.getLayoutParams();
        params.height = height;
        mViewGroup.setLayoutParams(params);
    }
}
