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

    private float startY = 0;

    public GestureListener(Context context, ViewGroup viewGroup, AbsListView absListView) {
        this.context = context;
        this.mViewGroup = viewGroup;
        this.mAbsListView = absListView;
        this.mGestureDetector = new GestureDetector(context, this);
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
            startY = event.getY();
            if (checkTopPosition() && (mAbsListView.getFirstVisiblePosition() == 0)) return true;
        } else if ((event.getAction() == MotionEvent.ACTION_MOVE) ) {
            float dY = event.getY() - startY + params.height;
            if (checkTopPosition() && (mAbsListView.getFirstVisiblePosition() == 0) && (dY > 0)) {
                params.height = Math.min((int) dY / 2, Constants.BOOKMARKS_HEIGHT);
                mViewGroup.setLayoutParams(params);
                return true;
            } else if ((dY < 0) && (params.height > 0)) {
                params.height = 0;
                mViewGroup.setLayoutParams(params);
                return false;
            }
        } else if ((event.getAction() == MotionEvent.ACTION_UP) ) {
            if (params.height < Constants.BOOKMARKS_HEIGHT / 2)
                params.height = 0;
            else
                params.height = Constants.BOOKMARKS_HEIGHT;
            mViewGroup.setLayoutParams(params);
            return false;
        }
        return mGestureDetector.onTouchEvent(event);
    }

    private Boolean checkTopPosition(){
        if (mAbsListView instanceof StaggeredGridView)
            return ((StaggeredGridView) mAbsListView).getDistanceToTop() == 0;
        else
            return mAbsListView.getScrollY() == 0;
    }
}