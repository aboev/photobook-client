package com.freecoders.photobook;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.etsy.android.grid.StaggeredGridView;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.db.ImageEntry;
import com.freecoders.photobook.gson.FeedEntryJson;
import com.freecoders.photobook.network.FeedLoader;
import com.freecoders.photobook.network.ImageUploader;
import com.freecoders.photobook.utils.FileUtils;
import com.freecoders.photobook.utils.ImageUtils;
import com.google.gson.Gson;

import java.io.File;
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
        //listView.setOnItemClickListener(OnItemClickListener);
        //mFeedLoader.loadFeed();
        setRetainInstance(true);
        return rootView;
    }

    AdapterView.OnItemClickListener OnItemClickListener
            = new AdapterView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            FeedEntryJson feedItem = mAdapter.getItem(position);
            Intent mIntent = new Intent(Photobook.getMainActivity(), ImageDetailsActivity.class);
            Photobook.setImageDetails(feedItem);
            startActivity(mIntent);
        }
    };

}
