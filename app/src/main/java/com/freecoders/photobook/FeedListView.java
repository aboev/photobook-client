package com.freecoders.photobook;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.freecoders.photobook.FeedAdapter;
import com.freecoders.photobook.gson.FeedEntryJson;
import com.freecoders.photobook.network.FeedLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex on 2014-12-12.
 */
public class FeedListView extends ListView implements AbsListView.OnScrollListener {

    private View mFooter;
    private boolean isLoading = false;
    private FeedAdapter mAdapter;
    private FeedLoader mFeedLoader;
    public ArrayList<FeedEntryJson> mFeedList;

    public void setFeedComponents(FeedLoader feedLoader, FeedAdapter adapter,
            ArrayList<FeedEntryJson> list){
        this.mFeedLoader = feedLoader;
        this.mAdapter = adapter;
        this.mFeedList = list;
        super.setAdapter(adapter);
        setLoadingView();
    }

    public FeedListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setOnScrollListener(this);
    }
    public FeedListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnScrollListener(this);
    }
    public FeedListView(Context context) {
        super(context);
        this.setOnScrollListener(this);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (getAdapter() == null)
            return ;
        if (getAdapter().getCount() == 0)
            return ;
        int l = visibleItemCount + firstVisibleItem;
        if (l >= totalItemCount && !isLoading) {
            this.addFooterView(mFooter);
            mFeedLoader.loadFeed();
            isLoading = true;
        }
    }
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {}

    public void setLoadingView() {
        LayoutInflater inflater = (LayoutInflater) super.getContext().
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mFooter = (View) inflater.inflate(R.layout.last_row_feed, null);
        this.addFooterView(mFooter);
    }

    public void addNewData(List<FeedEntryJson> data, Boolean flush) {
        this.removeFooterView(mFooter);
        if (flush) mFeedList.clear();
        if (data != null) {
            mFeedList.addAll(data);
            mAdapter.notifyDataSetChanged();
        }
        isLoading = false;
    }
}
