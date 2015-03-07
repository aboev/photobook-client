package com.freecoders.photobook.network;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.freecoders.photobook.CommentListAdapter;
import com.freecoders.photobook.FriendsListAdapter;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.common.Preferences;
import com.freecoders.photobook.db.FriendEntry;
import com.freecoders.photobook.db.ImageEntry;
import com.freecoders.photobook.gson.CommentEntryJson;
import com.freecoders.photobook.gson.UserProfile;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
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
                                int res = Photobook.getFriendsDataSource().updateFriend(
                                        friendList.get(position));
                                Log.d(Constants.LOG_TAG, "Updated " + res + "friend items");
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

    public static final void removeFriendRequest(final ArrayList<FriendEntry> friendList,
                                              FriendsListAdapter adapter, int pos, Context context,
                                              final String[] friendIds) {
        Gson gson = new Gson();
        String userId = Photobook.getPreferences().strUserID;
        if (userId.isEmpty()) return;
        String idList = "";
        if (friendIds.length > 0) {
            idList = friendIds[0];
            for (int i = 1; i < friendIds.length; i++)
                idList = idList + ", " + friendIds[i];
        }
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "*/*");
        headers.put("userid", userId);
        headers.put("id", idList);
        final int position = pos;
        final FriendsListAdapter friendsListAdapter = adapter;
        Log.d(Constants.LOG_TAG, "Remove friend request");
        StringRequest request = new StringRequest(Request.Method.DELETE,
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
                                        setStatus(FriendEntry.INT_STATUS_DEFAULT);
                                friendsListAdapter.notifyDataSetChanged();
                                Photobook.getFriendsDataSource().updateFriend(
                                        friendList.get(position));
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

    public static final void likeRequest(Context context,
                                              String imageId, String userId,
                                              final Response.Listener<String> responseListener,
                                              final Response.ErrorListener errorListener) {
        Gson gson = new Gson();
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "*/*");
        headers.put("userid", userId);
        headers.put("id", imageId);
        Log.d(Constants.LOG_TAG, "Like request");
        StringRequest request = new StringRequest(Request.Method.POST,
                Constants.SERVER_URL+Constants.SERVER_PATH_LIKE ,
                "", headers,
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

    public static final void unLikeRequest(Context context,
                                         String imageId, String userId,
                                         final Response.Listener<String> responseListener,
                                         final Response.ErrorListener errorListener) {
        Gson gson = new Gson();
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "*/*");
        headers.put("userid", userId);
        headers.put("id", imageId);
        Log.d(Constants.LOG_TAG, "Like request");
        StringRequest request = new StringRequest(Request.Method.DELETE,
                Constants.SERVER_URL+Constants.SERVER_PATH_LIKE ,
                "", headers,
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

    public static final void getComments (Context context,
                                         String imageId,
                                         final CommentListAdapter adapter) {
        HashMap<String, String> headers = new HashMap<String,String>();
        headers.put("userid", Photobook.getPreferences().strUserID);
        headers.put("imageid", imageId);
        headers.put("Accept", "*/*");
        Log.d(Constants.LOG_TAG, "Load comments request");
        StringRequest getCommentsRequest = new StringRequest(Request.Method.GET,
                Constants.SERVER_URL+Constants.SERVER_PATH_COMMENTS,
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
                                Type type = new TypeToken<ArrayList<CommentEntryJson>>(){}.getType();
                                ArrayList<CommentEntryJson> commentList = gson.fromJson(
                                        resJson.get("data").toString(), type);
                                adapter.mCommentList.clear();
                                adapter.mCommentList.addAll(commentList);
                                adapter.notifyDataSetChanged();
                                Log.d(Constants.LOG_TAG, "Loaded  " + commentList.size()
                                        + " comments");
                            }
                        } catch (Exception e) {
                            Log.d(Constants.LOG_TAG, "Exception " + e.getLocalizedMessage());
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if ((error != null) && (error.networkResponse != null)
                        && (error.networkResponse.data != null))
                    Log.d(Constants.LOG_TAG, "Error: " +
                            new String(error.networkResponse.data));
            }
        }
        );
        VolleySingleton.getInstance(Photobook.getMainActivity()).
                addToRequestQueue(getCommentsRequest);
    }

    public static final void postCommentRequest(Context context,
                                         String imageId, String userId,
                                         String strText,
                                         final Response.Listener<String> responseListener,
                                         final Response.ErrorListener errorListener) {
        Gson gson = new Gson();
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "*/*");
        headers.put("userid", userId);
        HashMap<String, String> reqBody = new HashMap<String, String>();
        reqBody.put("image_id", imageId);
        reqBody.put("text", strText);
        Log.d(Constants.LOG_TAG, "Comment request");
        StringRequest request = new StringRequest(Request.Method.POST,
                Constants.SERVER_URL+Constants.SERVER_PATH_COMMENTS ,
                gson.toJson(reqBody), headers,
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

    public static final void deleteCommentRequest(Context context,
                                                String commendId,
                                                final Response.Listener<String> responseListener,
                                                final Response.ErrorListener errorListener) {
        // Code here
    }

    public static final void getImageDetailsRequest (Context context,
                                          String imageId,
                                          final Response.Listener<String> responseListener,
                                          final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = new HashMap<String,String>();
        headers.put("userid", Photobook.getPreferences().strUserID);
        headers.put("imageid", imageId);
        headers.put("Accept", "*/*");
        Log.d(Constants.LOG_TAG, "Get image details request");
        StringRequest getCommentsRequest = new StringRequest(Request.Method.GET,
                Constants.SERVER_URL+Constants.SERVER_PATH_IMAGE,
                "", headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(Constants.LOG_TAG, response.toString());
                        if (responseListener != null) responseListener.onResponse(response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                        if (errorListener != null) errorListener.onErrorResponse(error);
            }
        }
        );
        VolleySingleton.getInstance(Photobook.getMainActivity()).
                addToRequestQueue(getCommentsRequest);
    }
}
