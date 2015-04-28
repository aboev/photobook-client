package com.freecoders.photobook.classes;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by Alex on 2015-04-28.
 */

public class ResizeAnimation extends Animation {

    private int startHeight;
    private int deltaHeight;
    private View view;


    public ResizeAnimation (View v) {
        this.view = v;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        view.getLayoutParams().height = (int) (startHeight + deltaHeight * interpolatedTime);
        view.requestLayout();
    }

    public void setHeight(int targetHeight) {
        this.startHeight = view.getLayoutParams().height;
        deltaHeight = targetHeight - startHeight;
    }

    @Override
    public void setDuration(long durationMillis) {
        super.setDuration(durationMillis);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}