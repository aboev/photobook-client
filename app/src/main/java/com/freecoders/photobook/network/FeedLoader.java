package com.freecoders.photobook.network;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.freecoders.photobook.FeedAdapter;
import com.freecoders.photobook.FeedListView;
import com.freecoders.photobook.GalleryAdapter;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.db.ImageEntry;
import com.freecoders.photobook.gson.FeedEntryJson;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Alex on 2014-12-10.
 */
public class FeedLoader {
    //private ArrayList<FeedEntryJson> mFeedList;
    //private FeedAdapter mAdapter;
    private FeedListView mFeedListView;
    private Integer mPageSize = 100;
    private Integer mOffset = 0;
    private Boolean isLoading = false;

    public FeedLoader(FeedListView feedListView) {
        //this.mAdapter = adapter;
        //this.mFeedList = feedList;
        this.mFeedListView = feedListView;
    }

    public void loadFeed() {
        HashMap<String, String> headers = new HashMap<String,String>();
        headers.put("userid", Photobook.getPreferences().strUserID);
        headers.put("Accept", "*/*");
        Log.d(Constants.LOG_TAG, "Load feed request with offset = " + mOffset +
                " and limit " + mPageSize);
        StringRequest getFeedRequest = new StringRequest(Request.Method.GET,
                Constants.SERVER_URL+Constants.SERVER_PATH_FEED+
                "?offset=" + mOffset + "&limit=" + mPageSize,
                "", headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(Constants.LOG_TAG, response.toString());
                        try {
                            Log.d(Constants.LOG_TAG, "Response " + response);
                            Gson gson = new Gson();
                            JSONObject resJson = new JSONObject(response);
                            String strRes = resJson.getString("result");
                            if ((strRes.equals("OK")) && (resJson.has("data"))) {
                                Type type = new TypeToken<ArrayList<FeedEntryJson>>(){}.getType();
                                ArrayList<FeedEntryJson> feedList = gson.fromJson(
                                        resJson.get("data").toString(), type);
                                if (mOffset == 0) {
                                    mFeedListView.addNewData(feedList, true);
                                    //mFeedList.clear();
                                    //mFeedList.addAll(feedList);
                                    //mAdapter.add();
                                } else {
                                    mFeedListView.addNewData(feedList, false);
                                    //mFeedList.addAll(feedList);
                                    //mAdapter.notifyDataSetChanged();
                                }
                                mOffset = mFeedListView.mFeedList.size();
                                isLoading = false;
                                Log.d(Constants.LOG_TAG, "Loaded  " + feedList.size()
                                        + " feed items");
                                //mAdapter.notifyDataSetChanged();
                            }
                        } catch (Exception e) {
                            Log.d(Constants.LOG_TAG, "Exception " + e.getLocalizedMessage());
                            handleFailure();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                 if ((error != null) && (error.networkResponse != null)
                         && (error.networkResponse.data != null))
                    Log.d(Constants.LOG_TAG, "Error: " +
                            new String(error.networkResponse.data));
                    handleFailure();
                }
            }
        );
        if (!isLoading) VolleySingleton.getInstance(Photobook.getMainActivity()).
                addToRequestQueue(getFeedRequest);
    }

    public void handleFailure(){mFeedListView.addNewData(null, false);isLoading = false;}
}
