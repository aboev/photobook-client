package com.freecoders.photobook.classes;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by aleksey.boev on 2015-03-25.
 */
public class BookmarkAdapter {
    private Context context;
    private ArrayList<String> items;
    private ViewGroup parentView;
    private onItemSelectedListener listener;
    public int selectedPosition = 0;

    public BookmarkAdapter(Context context, ViewGroup parentView, ArrayList<String> items) {
        this.context = context;
        this.items = items;
        this.parentView = parentView;
        redraw();
    }

    private View getView(final int position) {
        View v = new View(context);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.onItemSelected(position);
            }
        });
        return v;
    }

    private void redraw(){
        if (parentView.getChildCount() > 0)
            parentView.removeAllViews();
        for (int i = 0; i < items.size(); i++) {
            View view = getView(i);
            parentView.addView(view);
        }
    }

    public void setOnItemSelectedListener(onItemSelectedListener listener){
        this.listener = listener;
    }

    public interface onItemSelectedListener {
        public void onItemSelected(int position);
    }
}
