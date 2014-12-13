package com.freecoders.photobook;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.etsy.android.grid.StaggeredGridView;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.gson.FeedEntryJson;
import com.freecoders.photobook.network.FeedLoader;
import com.freecoders.photobook.network.ImageUploader;

import java.util.ArrayList;

@SuppressLint("NewApi") 
public class FeedFragmentTab extends Fragment {

    private FeedAdapter mAdapter;
    private FeedLoader mFeedLoader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_feed, container, false);
        FeedListView listView = (FeedListView) rootView.findViewById(R.id.feedList);
        ArrayList<FeedEntryJson> feedEntryList = new ArrayList<FeedEntryJson>();
        mAdapter = new FeedAdapter(getActivity(), R.layout.item_feed, feedEntryList);
        mFeedLoader = new FeedLoader(listView);
        listView.setFeedComponents(mFeedLoader, mAdapter, feedEntryList);
        //mFeedLoader.loadFeed();
        setRetainInstance(true);
        return rootView;
    }

}
