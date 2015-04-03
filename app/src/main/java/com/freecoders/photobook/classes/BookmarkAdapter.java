package com.freecoders.photobook.classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.freecoders.photobook.R;

import java.util.ArrayList;

/**
 * Created by aleksey.boev on 2015-03-25.
 */
public class BookmarkAdapter {
    private Context context;
    private String[] items;
    private ViewGroup parentView;
    private View colorSelector;
    private onItemSelectedListener listener;
    private int[] colors;
    public int selectedPosition = 0;

    public BookmarkAdapter(Context context, ViewGroup parentView, View colorSelector,
                           String[] items) {
        this.context = context;
        this.items = items;
        this.parentView = parentView;
        this.colorSelector = colorSelector;
        this.colors = context.getResources().getIntArray(R.array.bookmark_colors);;
        redraw();
    }

    private View getView(final int position) {
        String inflater = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater vi = (LayoutInflater)context.getSystemService(inflater);
        View view;
        if (position == selectedPosition) {
            view = vi.inflate(R.layout.item_bookmark_selected, parentView, false);
            if (colors.length > 0)
                colorSelector.setBackgroundColor(colors[position % colors.length]);
        } else
            view = vi.inflate(R.layout.item_bookmark, parentView, false);
        TextView tv = (TextView)view.findViewById(R.id.txtViewBookmark);
        tv.setText(items[position]);
        if (colors.length > 0)
            tv.setBackgroundColor(colors[position % colors.length]);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPosition = position;
                redraw();
                if (listener != null)
                    listener.onItemSelected(position);
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
}
