package com.freecoders.photobook.network;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.freecoders.photobook.FriendsListAdapter;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.common.Preferences;
import com.freecoders.photobook.db.FriendEntry;
import com.freecoders.photobook.db.ImageEntry;
import com.freecoders.photobook.gson.UserProfile;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Alex on 2014-11-27.
 */
public class ServerInterface {

    public static final void postContactsRequest(Context context,
        ArrayList<String> contacts, String userId,
        final Response.Listener<String> responseListener,
        final Response.ErrorListener errorListener) {
        Gson gson = new Gson();
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "*/*");
        headers.put("userid", userId);
        Log.d(Constants.LOG_TAG, "Sending post contacts request for " + gson.toJson(contacts));
        StringRequest request = new StringRequest(Request.Method.POST,
                Constants.SERVER_URL+Constants.SERVER_PATH_CONTACTS,
                gson.toJson(contacts), headers,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        responseListener.onResponse(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        errorListener.onErrorResponse(error);

                    }
                }
        );

        VolleySingleton.getInstance(context).addToRequestQueue(request);

    }

    public static final void updateProfileRequest(Context context,
                                                 UserProfile profile, String userId,
                                                 final Response.Listener<String> responseListener,
                                                 final Response.ErrorListener errorListener) {
        Gson gson = new Gson();
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "*/*");
        headers.put("userid", userId);
        profile.setNullFields();
        Log.d(Constants.LOG_TAG, "Update profile request");
        StringRequest request = new StringRequest(Request.Method.PUT,
                Constants.SERVER_URL+Constants.SERVER_PATH_USER ,
                gson.toJson(profile), headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(Constants.LOG_TAG, "Response: " + response);
                        if (responseListener != null) responseListener.onResponse(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (errorListener != null) errorListener.onErrorResponse(error);
                    }
                }
        );

        VolleySingleton.getInstance(context).addToRequestQueue(request);

    }

    public static final void addFriendRequest(final ArrayList<FriendEntry> friendList,
        FriendsListAdapter adapter, int pos, Context context,
        final String[] friendIds) {
        Gson gson = new Gson();
        String userId = Photobook.getPreferences().strUserID;
        if (userId.isEmpty()) return;
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "*/*");
        headers.put("userid", userId);
        final int position = pos;
        final FriendsListAdapter friendsListAdapter = adapter;
        Log.d(Constants.LOG_TAG, "Add friend request");
        StringRequest request = new StringRequest(Request.Method.PUT,
                Constants.SERVER_URL+Constants.SERVER_PATH_FRIENDS ,
                gson.toJson(friendIds), headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject resJson = new JSONObject(response);
                            String strRes = resJson.getString("result");
                            if (strRes.equals("OK")) {
                                friendList.get(position).
                                        setStatus(FriendEntry.INT_STATUS_FRIEND);
                                friendsListAdapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            Log.d(Constants.LOG_TAG, "Exception " + e.getLocalizedMessage());
                        }
                        Log.d(Constants.LOG_TAG, "Response: " + response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(Constants.LOG_TAG, "Error: " + error.getLocalizedMessage());
                    }
                }
        );
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }
}
