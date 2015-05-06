package com.freecoders.photobook.classes;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.freecoders.photobook.R;

import java.util.ArrayList;

/**
 * Created by aleksey.boev on 2015-03-25.
 */
public class BookmarkAdapter {
    private Context context;
    private String[] items;
    private TypedArray icons;
    private ViewGroup parentView;
    private View colorSelector;
    private onItemSelectedListener listener;
    private int[] colors;
    public int selectedPosition = 0;

    public BookmarkAdapter(Context context, ViewGroup parentView, View colorSelector,
                           String[] items, int iconsResID) {
        this.context = context;
        this.items = items;
        this.parentView = parentView;
        this.colorSelector = colorSelector;
        this.colors = context.getResources().getIntArray(R.array.bookmark_colors);
        this.icons = context.getResources().obtainTypedArray(iconsResID);
        redraw();
    }

    public void setParentView(ViewGroup parentView) {
        this.parentView = parentView;
        redraw();
    }

    private View getView(final int position) {
        String inflater = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater vi = (LayoutInflater)context.getSystemService(inflater);
        View view;
        if (position == selectedPosition)
            view = vi.inflate(R.layout.item_tab_selected, parentView, false);
        else
            view = vi.inflate(R.layout.item_tab, parentView, false);
        TextView tv = (TextView)view.findViewById(R.id.txtViewBookmark);
        ImageView iv = (ImageView)view.findViewById(R.id.imgViewIcon);
        tv.setText(items[position]);
        iv.setImageResource(icons.getResourceId(position % icons.length(), -1));

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedPosition(position);
            }
        });
        return view;
    }

    private void redraw(){
        if (parentView.getChildCount() > 0)
            parentView.removeAllViews();
        for (int i = 0; i < items.length; i++) {
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

    public String[] getItems() {
        return items;
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
        redraw();
        if (listener != null)
            listener.onItemSelected(position);
    }
}
