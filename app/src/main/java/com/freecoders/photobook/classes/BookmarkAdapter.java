package com.freecoders.photobook.classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private onItemSelectedListener listener;
    public int selectedPosition = 0;

    public BookmarkAdapter(Context context, ViewGroup parentView, String[] items) {
        this.context = context;
        this.items = items;
        this.parentView = parentView;
        redraw();
    }

    private View getView(final int position) {
        String inflater = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater vi = (LayoutInflater)context.getSystemService(inflater);
        View view;
        if (position == selectedPosition)
            view = vi.inflate(R.layout.item_bookmark_selected, null);
        else
            view = vi.inflate(R.layout.item_bookmark, null);
        TextView tv = (TextView)view.findViewById(R.id.txtViewBookmark);
        tv.setText(items[position]);

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
}
