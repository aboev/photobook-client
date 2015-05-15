package com.freecoders.photobook.network;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.gson.CommentEntryJson;
import com.freecoders.photobook.gson.ImageJson;
import com.freecoders.photobook.gson.ServerResponse;
import com.freecoders.photobook.gson.UserProfile;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alex on 2014-11-27.
 */

public class ServerInterface {

    private static Gson gson = new Gson();

    private static String LOG_TAG = "ServerInterface";

    public static final void postContactsRequest(Context context, ArrayList<String> contacts,
            final Response.Listener<Map<String, UserProfile>> responseListener,
            final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = makeHTTPHeaders();
        Log.d(LOG_TAG, "Sending post contacts request: " + gson.toJson(contacts));
        StringRequest request = new StringRequest(Request.Method.POST,
            Constants.SERVER_URL + Constants.SERVER_PATH_CONTACTS,
            gson.toJson(contacts), headers,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(LOG_TAG, "Response: " + response);
                    Gson gson = new Gson();
                    Type type = new TypeToken<ServerResponse
                            <Map<String, UserProfile>>>(){}.getType();
                    try {
                        ServerResponse<Map<String, UserProfile>> res = gson.fromJson(response, type);
                        if ( res != null && res.isSuccess() && res.data != null
                                && responseListener != null)
                            responseListener.onResponse(res.data);
                        else if (errorListener != null)
                            errorListener.onErrorResponse(new VolleyError());
                    } catch (Exception e) {
                        Log.d(LOG_TAG, "Exception: " + e.getLocalizedMessage());
                        if (errorListener != null) errorListener.onErrorResponse(
                            new VolleyError());
                    }
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

    public static final void updateProfileRequest(Context context, UserProfile profile,
             final Response.Listener<String> responseListener,
             final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = makeHTTPHeaders();
        Log.d(LOG_TAG, "Update profile request");
        StringRequest request = new StringRequest(Request.Method.PUT,
            Constants.SERVER_URL + Constants.SERVER_PATH_USER ,
            gson.toJson(profile), headers,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(LOG_TAG, "Response: " + response);
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

    public static final void addFriendRequest(Context context,
            final ArrayList<String> friendIds,
            final Response.Listener<String> responseListener,
            final Response.ErrorListener errorListener) {
        if (Photobook.getPreferences().strUserID.isEmpty()) return;
        HashMap<String, String> headers = makeHTTPHeaders();
        Log.d(LOG_TAG, "Add friend request");
        StringRequest request = new StringRequest(Request.Method.PUT,
            Constants.SERVER_URL+Constants.SERVER_PATH_FRIENDS,
            gson.toJson(friendIds), headers,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(LOG_TAG, "Response: " + response);
                    Type type = new TypeToken<ServerResponse<ArrayList<String>>>(){}.getType();
                    try {
                        ServerResponse<ArrayList<String>> res = gson.fromJson(response, type);
                        if ( res != null && res.isSuccess() && res.data != null
                                && responseListener != null) {
                            responseListener.onResponse("");
                        } else if (errorListener != null)
                            errorListener.onErrorResponse(new VolleyError());
                    } catch (Exception e) {
                        Log.d(LOG_TAG, "Exception: " + e.getLocalizedMessage());
                        if (errorListener != null) errorListener.onErrorResponse(
                                new VolleyError());
                    }
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

    public static final void addFriendRequest(Response.Listener<String> responseListener,
            Context context, final String[] friendIds) {
        if (Photobook.getPreferences().strUserID.isEmpty()) return;
        HashMap<String, String> headers = makeHTTPHeaders();
        Log.d(LOG_TAG, "Add friend request");
        Gson gson = new Gson();
        StringRequest request = new StringRequest(Request.Method.PUT,
            Constants.SERVER_URL + Constants.SERVER_PATH_FRIENDS ,
            gson.toJson(friendIds), headers, responseListener,
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(LOG_TAG, "Error: " + error.getLocalizedMessage());
                }
            }
        );
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public static final void removeFriendRequest(Response.Listener<String> responseListener,
            Context context, final String[] friendIds) {
        if (Photobook.getPreferences().strUserID.isEmpty()) return;
        String idList = "";
        if (friendIds.length > 0) {
            idList = friendIds[0];
            for (int i = 1; i < friendIds.length; i++)
                idList = idList + ", " + friendIds[i];
        }
        HashMap<String, String> headers = makeHTTPHeaders();
        headers.put(Constants.KEY_ID, idList);
        Log.d(LOG_TAG, "Remove friend request");
        StringRequest request = new StringRequest(Request.Method.DELETE,
            Constants.SERVER_URL+Constants.SERVER_PATH_FRIENDS ,
            gson.toJson(friendIds), headers, responseListener,
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(LOG_TAG, "Error: " + error.getLocalizedMessage());
                }
            }
        );
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public static final void getFriendsRequest(Context context,
            final Response.Listener<ArrayList<String>> responseListener,
            final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = new HashMap<String,String>();
        headers.put(Constants.HEADER_USERID, Photobook.getPreferences().strUserID);
        headers.put("Accept", "*/*");
        Log.d(LOG_TAG, "Get friends request");
        StringRequest getChannelsRequest = new StringRequest(Request.Method.GET,
            Constants.SERVER_URL+Constants.SERVER_PATH_FRIENDS, "", headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(LOG_TAG, response);
                        Type type = new TypeToken<ServerResponse
                                <ArrayList<String>>>(){}.getType();
                        try {
                            ServerResponse<ArrayList<String>> res = gson.fromJson(response, type);
                            if (res != null && res.isSuccess() && res.data != null
                                    && responseListener != null)
                                responseListener.onResponse(res.data);
                            else if (errorListener != null)
                                errorListener.onErrorResponse(new VolleyError());
                        } catch (Exception e) {
                            if (errorListener != null) errorListener.onErrorResponse(
                                    new VolleyError());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if ((error != null) && (error.networkResponse != null)
                                && (error.networkResponse.data != null))
                            Log.d(LOG_TAG, "Error: " +
                                    new String(error.networkResponse.data));
                        if (errorListener != null) errorListener.onErrorResponse(error);
                    }
                }
        );
        VolleySingleton.getInstance(context).addToRequestQueue(getChannelsRequest);
    }

    public static final void likeRequest(Context context, String imageId,
          final Response.Listener<String> responseListener,
          final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = makeHTTPHeaders();
        headers.put(Constants.KEY_ID, imageId);
        Log.d(LOG_TAG, "Like request");
        StringRequest request = new StringRequest(Request.Method.POST,
            Constants.SERVER_URL + Constants.SERVER_PATH_LIKE , "", headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(LOG_TAG, "Response: " + response);
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

    public static final void unLikeRequest(Context context, String imageId,
             final Response.Listener<String> responseListener,
             final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = makeHTTPHeaders();
        headers.put(Constants.KEY_ID, imageId);
        Log.d(LOG_TAG, "Unlike request");
        StringRequest request = new StringRequest(Request.Method.DELETE,
            Constants.SERVER_URL + Constants.SERVER_PATH_LIKE , "", headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(LOG_TAG, "Response: " + response);
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

    public static final void getCommentsRequest (Context context, String imageId, Boolean withModTime,
            final Response.Listener<ServerResponse<ArrayList<CommentEntryJson>>> responseListener,
            final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = makeHTTPHeaders();
        if ((imageId != null) && (!imageId.isEmpty()))
            headers.put(Constants.HEADER_IMAGEID, imageId);
        if (withModTime)
            headers.put(Constants.HEADER_MODTIME, Photobook.getPreferences().strCommentsTimestamp);
        Log.d(LOG_TAG, "Load comments request with timestamp " +
                Photobook.getPreferences().strCommentsTimestamp);
        StringRequest getCommentsRequest = new StringRequest(Request.Method.GET,
            Constants.SERVER_URL + Constants.SERVER_PATH_COMMENTS,
            "", headers,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(LOG_TAG, "Response: " + response);
                    Type type = new TypeToken<ServerResponse
                            <ArrayList<CommentEntryJson>>>(){}.getType();
                    try {
                        ServerResponse<ArrayList<CommentEntryJson>> res =
                                gson.fromJson(response, type);
                        if ( res != null && res.isSuccess()
                                && res.data != null
                                && responseListener != null)
                            responseListener.onResponse(res);
                        else if (errorListener != null)
                            errorListener.onErrorResponse(new VolleyError());
                    } catch (Exception e) {
                        Log.d(LOG_TAG, "Exception: " + e.getLocalizedMessage());
                        if (errorListener != null) errorListener.onErrorResponse(
                                new VolleyError());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (errorListener != null) errorListener.onErrorResponse(error);
                }
            }
        );
        VolleySingleton.getInstance(context).addToRequestQueue(getCommentsRequest);
    }

    public static final void postCommentRequest(Context context,
            String imageId, String strText, long replyTo,
            final Response.Listener<String> responseListener,
            final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = makeHTTPHeaders();
        CommentEntryJson comment = new CommentEntryJson(Request.Method.POST);
        comment.text = strText;
        comment.image_id = imageId;
        comment.reply_to = replyTo;
        Log.d(LOG_TAG, "Post comment request");
        StringRequest request = new StringRequest(Request.Method.POST,
            Constants.SERVER_URL + Constants.SERVER_PATH_COMMENTS ,
            gson.toJson(comment), headers,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(LOG_TAG, "Response: " + response);
                    Type type = new TypeToken<ServerResponse
                            <HashMap<String, String>>>(){}.getType();
                    try {
                        ServerResponse<HashMap<String, String>> res =
                                gson.fromJson(response, type);
                        if ( res != null && res.isSuccess() && res.data != null
                                && res.data.containsKey(Constants.KEY_ID)
                                && responseListener != null)
                            responseListener.onResponse(res.data.get(Constants.KEY_ID));
                        else if (errorListener != null)
                            errorListener.onErrorResponse(new VolleyError());
                    } catch (Exception e) {
                        Log.d(LOG_TAG, "Exception: " + e.getLocalizedMessage());
                        if (errorListener != null) errorListener.onErrorResponse(
                                new VolleyError());
                    }
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

    public static final void deleteCommentRequest(Context context, String commendId,
            final Response.Listener<String> responseListener,
            final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = makeHTTPHeaders();
        headers.put(Constants.HEADER_COMMENTID, commendId);
        Log.d(LOG_TAG, "Delete comment request");
        StringRequest request = new StringRequest(Request.Method.DELETE,
            Constants.SERVER_URL + Constants.SERVER_PATH_COMMENTS, "", headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(LOG_TAG, "Response: " + response);
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

    public static final void getUserProfileRequest (Context context, String[] userIds,
            final Response.Listener<HashMap<String, UserProfile>> responseListener,
            final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = makeHTTPHeaders();
        String strIdHeader = userIds.length > 0 ? userIds[0] : "";
        for (int i = 1; i < userIds.length; i++) strIdHeader = strIdHeader + "," + userIds[i];
        headers.put(Constants.KEY_ID, strIdHeader);
        Log.d(LOG_TAG, "Get user profile request");
        StringRequest request = new StringRequest(Request.Method.GET,
            Constants.SERVER_URL + Constants.SERVER_PATH_USER, "", headers,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(LOG_TAG, response);
                    Type type = new TypeToken<ServerResponse
                            <HashMap<String, UserProfile>>>(){}.getType();
                    try {
                        ServerResponse<HashMap<String, UserProfile>> res =
                                gson.fromJson(response, type);
                        if (res != null && res.isSuccess() && res.data != null
                                && responseListener != null)
                            responseListener.onResponse(res.data);
                        else if (responseListener != null)
                            responseListener.onResponse(new HashMap<String, UserProfile>());
                    } catch (Exception e) {
                        if (responseListener != null)
                            responseListener.onResponse(new HashMap<String, UserProfile>());
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    if ((error != null) && (error.networkResponse != null)
                            && (error.networkResponse.data != null))
                        Log.d(LOG_TAG, "Error: " +
                                new String(error.networkResponse.data));
                    if (errorListener != null) errorListener.onErrorResponse(error);
                }
            }
        );
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public static final void getImageDetailsRequest(Context context, String imageId, String userId,
            final Response.Listener<ArrayList<ImageJson>> responseListener,
            final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = makeHTTPHeaders();
        if ((imageId != null) && !imageId.isEmpty())
            headers.put(Constants.HEADER_IMAGEID, imageId);
        if ((userId != null) && !userId.isEmpty())
            headers.put(Constants.HEADER_ID, userId);
        Log.d(LOG_TAG, "Get image details request");
        StringRequest imageRequest = new StringRequest(Request.Method.GET,
            Constants.SERVER_URL + Constants.SERVER_PATH_IMAGE, "", headers,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(LOG_TAG, response);
                    Type type = new TypeToken<ServerResponse
                        <ArrayList<ImageJson>>>(){}.getType();
                    try {
                        ServerResponse<ArrayList<ImageJson>> res =
                                gson.fromJson(response, type);
                        if ( res != null && res.isSuccess() && res.data != null
                                && responseListener != null)
                            responseListener.onResponse(res.data);
                        else if (responseListener != null)
                            responseListener.onResponse(
                                    new ArrayList<ImageJson>());
                    } catch (Exception e) {
                        if (responseListener != null)
                            responseListener.onResponse(
                                    new ArrayList<ImageJson>());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (errorListener != null) errorListener.onErrorResponse(error);
                }
            }
        );
        VolleySingleton.getInstance(context).addToRequestQueue(imageRequest);
    }

    public static final void unShareImageRequest(Context context, String imageId,
            final Response.Listener<String> responseListener,
            final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = makeHTTPHeaders();
        if ((imageId != null) && !imageId.isEmpty())
            headers.put(Constants.HEADER_IMAGEID, imageId);
        Log.d(LOG_TAG, "Unshare image request");
        StringRequest request = new StringRequest(Request.Method.DELETE,
            Constants.SERVER_URL + Constants.SERVER_PATH_IMAGE, "", headers,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(LOG_TAG, response);
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

    public static final void getSMSCodeRequest (Context context, String strPhoneNumber,
            final Response.Listener<String> responseListener,
            final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = makeHTTPHeaders();
        headers.put(Constants.KEY_NUMBER, strPhoneNumber);
        Log.d(LOG_TAG, "Receive sms code request");
        StringRequest request = new StringRequest(Request.Method.GET,
            Constants.SERVER_URL + Constants.SERVER_PATH_USER + "/code", "", headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(LOG_TAG, response);
                        if (responseListener != null) responseListener.onResponse(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if ((error != null) && (error.networkResponse != null)
                                && (error.networkResponse.data != null))
                            Log.d(LOG_TAG, "Error: " +
                                    new String(error.networkResponse.data));
                        if (errorListener != null) errorListener.onErrorResponse(error);
                    }
                }
        );
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public static final void getServerInfoRequest (Context context,
            final Response.Listener<HashMap<String, String>> responseListener,
            final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = makeHTTPHeaders();
        Log.d(LOG_TAG, "Get server info request");
        StringRequest request = new StringRequest(Request.Method.GET,
            Constants.SERVER_URL + Constants.SERVER_PATH_INFO, "", headers,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(LOG_TAG, response);
                    Type type = new TypeToken<ServerResponse
                            <HashMap<String, String>>>(){}.getType();
                    try {
                        ServerResponse<HashMap<String, String>> res =
                                gson.fromJson(response, type);
                        if ( res != null && res.isSuccess() && res.data != null
                                && responseListener != null)
                            responseListener.onResponse(res.data);
                        else if (errorListener != null)
                            errorListener.onErrorResponse(new VolleyError());
                    } catch (Exception e) {
                        if (errorListener != null)
                            errorListener.onErrorResponse(new VolleyError());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if ((error != null) && (error.networkResponse != null)
                            && (error.networkResponse.data != null))
                        Log.d(LOG_TAG, "Error: " +
                                new String(error.networkResponse.data));
                    if (errorListener != null) errorListener.onErrorResponse(error);
                }
            }
        );
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public static final void getChannelsRequest (Context context,
            final Response.Listener<ArrayList<UserProfile>> responseListener,
            final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = makeHTTPHeaders();
        Log.d(LOG_TAG, "Get channels request");
        StringRequest request = new StringRequest(Request.Method.GET,
            Constants.SERVER_URL + Constants.SERVER_PATH_CHANNELS, "", headers,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(LOG_TAG, response);
                    Type type = new TypeToken<ServerResponse
                            <ArrayList<UserProfile>>>(){}.getType();
                    try {
                        ServerResponse<ArrayList<UserProfile>> res =
                                gson.fromJson(response, type);
                        if (res != null && res.isSuccess() && res.data != null
                                && responseListener != null)
                            responseListener.onResponse(res.data);
                        else if (errorListener != null)
                            errorListener.onErrorResponse(new VolleyError());
                    } catch (Exception e) {
                        if (errorListener != null) errorListener.onErrorResponse(
                                new VolleyError());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if ((error != null) && (error.networkResponse != null)
                            && (error.networkResponse.data != null))
                        Log.d(LOG_TAG, "Error: " +
                                new String(error.networkResponse.data));
                    if (errorListener != null) errorListener.onErrorResponse(error);
                    }
                }
        );
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public static final void getFollowersRequest(Context context, String userId,
            final Response.Listener<HashMap<String, UserProfile>> responseListener,
            final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = makeHTTPHeaders();
        headers.put("id", userId);
        StringRequest request = new StringRequest(Request.Method.GET,
            Constants.SERVER_URL + Constants.SERVER_PATH_USER + Constants.SERVER_PATH_FOLLOWERS,
            "", headers, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (responseListener != null) {
                        Type type = new TypeToken<ServerResponse
                                <HashMap<String, UserProfile>>>(){}.getType();
                        try {
                            ServerResponse<HashMap<String, UserProfile>> res =
                                    gson.fromJson(response, type);
                            if ( res != null && res.isSuccess() && res.data != null) {
                                responseListener.onResponse(res.data);
                            } else {
                                errorListener.onErrorResponse(new VolleyError());
                            }
                        } catch (Exception e) {
                            Log.d(LOG_TAG, "Error: " + e.getLocalizedMessage());
                            if (errorListener != null) errorListener.onErrorResponse(
                                    new VolleyError());
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if ((error != null) && (error.networkResponse != null)
                            && (error.networkResponse.data != null))
                        Log.d(LOG_TAG, "Error: " +
                                new String(error.networkResponse.data));
                    if (errorListener != null) errorListener.onErrorResponse(error);
                }
            }
        );
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    private static HashMap<String, String> makeHTTPHeaders() {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "*/*");
        headers.put(Constants.HEADER_USERID, Photobook.getPreferences().strUserID);
        return headers;
    }
}