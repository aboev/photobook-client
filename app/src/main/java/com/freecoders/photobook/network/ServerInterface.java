package com.freecoders.photobook.network;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.freecoders.photobook.CommentListAdapter;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.db.ContactsRetrieverTask;
import com.freecoders.photobook.gson.CommentEntryJson;
import com.freecoders.photobook.gson.ImageJson;
import com.freecoders.photobook.gson.ServerResponse;
import com.freecoders.photobook.gson.UserProfile;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alex on 2014-11-27.
 */
public class ServerInterface {

    private ServerErrorHandler errorHandler;
    private ServerResponseHandler responseHandler;
    
    private static String LOG_TAG = "ServerInterface";

    public static final void postContactsRequest(Context context,
        ArrayList<String> contacts, String userId,
        final Response.Listener<Map<String, UserProfile>> responseListener,
        final Response.ErrorListener errorListener) {
        Gson gson = new Gson();
        HashMap<String, String> headers = createHeaders(userId);
        Log.d(LOG_TAG, "Sending post contacts request for " + gson.toJson(contacts));
        StringRequest request = new StringRequest(Request.Method.POST,
            Constants.SERVER_URL+Constants.SERVER_PATH_CONTACTS,
            gson.toJson(contacts), headers,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(LOG_TAG, "Response: " + response);
                    Gson gson = new Gson();
                    Type type = new TypeToken<ServerResponse<Map<String, UserProfile>>>(){}
                            .getType();
                    try {
                        ServerResponse<Map<String, UserProfile>> res =
                                gson.fromJson(response, type);
                        if ( res != null && res.isSuccess()
                                && res.data != null
                                && responseListener != null) {
                            responseListener.onResponse(res.data);
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



    public static final void updateProfileRequest(Context context,
                                                 UserProfile profile, String userId,
                                                 final Response.Listener<String> responseListener,
                                                 final Response.ErrorListener errorListener) {
        Gson gson = new Gson();
        HashMap<String, String> headers = createHeaders(userId);
        Log.d(LOG_TAG, "Update profile request");
        StringRequest request = new StringRequest(Request.Method.PUT,
                Constants.SERVER_URL+Constants.SERVER_PATH_USER ,
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

    public static final void addFriendRequest(Response.Listener<String> responseListener, Context context,
                                                final String[] friendIds) {
        String userId = Photobook.getPreferences().strUserID;
        if (userId.isEmpty()) return;
        HashMap<String, String> headers = createHeaders(userId);
        Log.d(LOG_TAG, "Add friend request");
        Gson gson = new Gson();
        StringRequest request = new StringRequest(Request.Method.PUT,
                Constants.SERVER_URL+Constants.SERVER_PATH_FRIENDS ,
                gson.toJson(friendIds), headers, responseListener, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(LOG_TAG, "Error: " + error.getLocalizedMessage());
                    }
                }
        );
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public static final void removeFriendRequest(Response.Listener<String> responseListener, Context context,
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
        HashMap<String, String> headers = createHeaders(userId);
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

    public static final void likeRequest(Context context,
                                              String imageId, String userId,
                                              final Response.Listener<String> responseListener,
                                              final Response.ErrorListener errorListener) {
        Gson gson = new Gson();
        HashMap<String, String> headers = createHeaders(userId);
        headers.put(Constants.KEY_ID, imageId);
        Log.d(LOG_TAG, "Like request");
        StringRequest request = new StringRequest(Request.Method.POST,
                Constants.SERVER_URL+Constants.SERVER_PATH_LIKE ,
                "", headers,
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

    public static final void unLikeRequest(Context context,
                                         String imageId, String userId,
                                         final Response.Listener<String> responseListener,
                                         final Response.ErrorListener errorListener) {
        Gson gson = new Gson();
        HashMap<String, String> headers = createHeaders(userId);
        headers.put(Constants.KEY_ID, imageId);
        Log.d(LOG_TAG, "Like request");
        StringRequest request = new StringRequest(Request.Method.DELETE,
                Constants.SERVER_URL+Constants.SERVER_PATH_LIKE ,
                "", headers,
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

    public static final void getComments (Context context,
                                         String imageId,
                                         final CommentListAdapter adapter) {
        HashMap<String, String> headers = new HashMap<String,String>();
        headers.put(Constants.HEADER_USERID, Photobook.getPreferences().strUserID);
        headers.put(Constants.HEADER_IMAGEID, imageId);
        headers.put("Accept", "*/*");
        Log.d(LOG_TAG, "Load comments request");
        StringRequest getCommentsRequest = new StringRequest(Request.Method.GET,
                Constants.SERVER_URL+Constants.SERVER_PATH_COMMENTS,
                "", headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(LOG_TAG, response.toString());
                        try {
                            Log.d(LOG_TAG, "Response " + response);
                            Gson gson = new Gson();
                            JSONObject resJson = new JSONObject(response);
                            String strRes = resJson.getString(Constants.RESPONSE_RESULT);
                            if ((strRes.equals(Constants.RESPONSE_RESULT_OK))
                                    && (resJson.has(Constants.RESPONSE_DATA))) {
                                Type type = new TypeToken<ArrayList<CommentEntryJson>>(){}.getType();
                                ArrayList<CommentEntryJson> commentList = gson.fromJson(
                                        resJson.get(Constants.RESPONSE_DATA).toString(), type);
                                adapter.mCommentList.clear();
                                adapter.mCommentList.addAll(commentList);
                                adapter.notifyDataSetChanged();
                                Log.d(LOG_TAG, "Loaded  " + commentList.size()
                                        + " comments");
                            }
                        } catch (Exception e) {
                            Log.d(LOG_TAG, "Exception " + e.getLocalizedMessage());
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if ((error != null) && (error.networkResponse != null)
                        && (error.networkResponse.data != null))
                    Log.d(LOG_TAG, "Error: " +
                            new String(error.networkResponse.data));
            }
        }
        );
        VolleySingleton.getInstance(Photobook.getMainActivity()).
                addToRequestQueue(getCommentsRequest);
    }

    public static final void getComments (String imageId,
                                          Boolean withModTime,
                                          final Response.Listener<String> responseListener,
                                          final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = new HashMap<String,String>();
        headers.put(Constants.HEADER_USERID, Photobook.getPreferences().strUserID);
        if ((imageId != null) && (!imageId.isEmpty()))
            headers.put(Constants.HEADER_IMAGEID, imageId);
        if (withModTime)
            headers.put(Constants.HEADER_MODTIME,
                    Photobook.getPreferences().strCommentsTimestamp);
        headers.put("Accept", "*/*");
        Log.d(LOG_TAG, "Load comments request with timestamp " +
                Photobook.getPreferences().strCommentsTimestamp);
        StringRequest getCommentsRequest = new StringRequest(Request.Method.GET,
                Constants.SERVER_URL+Constants.SERVER_PATH_COMMENTS,
                "", headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(LOG_TAG, "Response: " + response);
                        try {
                            Gson gson = new Gson();
                            JSONObject resJson = null;
                            resJson = new JSONObject(response);
                            String strRes = resJson.getString(Constants.RESPONSE_RESULT);
                            if ((strRes.equals(Constants.RESPONSE_RESULT_OK))
                                    && (resJson.has(Constants.KEY_TIMESTAMP))
                                    && (resJson.has(Constants.RESPONSE_DATA))) {
                                String strTimestamp = resJson.getString(Constants.KEY_TIMESTAMP);
                                Photobook.getPreferences().strCommentsTimestamp = strTimestamp;
                                String strData = resJson.getString(Constants.RESPONSE_DATA);
                                Type type = new TypeToken<ArrayList<CommentEntryJson>>(){}.getType();
                                ArrayList<CommentEntryJson> commentList = gson.fromJson(strData,
                                        type);
                                for (int i = 0; i < commentList.size(); i++) {
                                    if (!Photobook.getPreferences().hsetUnreadImages.
                                            contains(commentList.get(i).image_id))
                                        Photobook.getPreferences().hsetUnreadImages.add(
                                                commentList.get(i).image_id);
                                    int intCommentCount = Photobook.getPreferences().
                                            unreadImagesMap.containsKey(commentList.get(i).
                                            image_id) ? Photobook.getPreferences().
                                            unreadImagesMap.get(commentList.get(i).
                                            image_id) : 0;
                                    Photobook.getPreferences().unreadImagesMap.put(
                                            commentList.get(i).image_id, intCommentCount + 1);
                                }
                                Photobook.getPreferences().savePreferences();
                            }
                        } catch (JSONException e) {
                            Log.d(LOG_TAG, "JSON parsing error for " + response);
                        }
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

    public static final void getCommentsJson (String imageId,
            Boolean withModTime,
            final Response.Listener<ArrayList<CommentEntryJson>> responseListener,
            final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = new HashMap<String,String>();
        headers.put(Constants.HEADER_USERID, Photobook.getPreferences().strUserID);
        if ((imageId != null) && (!imageId.isEmpty()))
            headers.put(Constants.HEADER_IMAGEID, imageId);
        if (withModTime)
            headers.put(Constants.HEADER_MODTIME,
                    Photobook.getPreferences().strCommentsTimestamp);
        headers.put("Accept", "*/*");
        Log.d(LOG_TAG, "Load comments request with timestamp " +
                Photobook.getPreferences().strCommentsTimestamp);
        StringRequest getCommentsRequest = new StringRequest(Request.Method.GET,
            Constants.SERVER_URL+Constants.SERVER_PATH_COMMENTS,
            "", headers,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(LOG_TAG, "Response: " + response);
                    Gson gson = new Gson();
                    Type type = new TypeToken<ServerResponse
                            <ArrayList<CommentEntryJson>>>(){}.getType();
                    try {
                        ServerResponse<ArrayList<CommentEntryJson>> res =
                                gson.fromJson(response, type);
                        if ( res != null && res.isSuccess()
                                && res.data != null
                                && responseListener != null) {
                            Photobook.getPreferences().strCommentsTimestamp = res.timestamp;
                            for (int i = 0; i < res.data.size(); i++) {
                                if (!Photobook.getPreferences().hsetUnreadImages.
                                        contains(res.data.get(i).image_id))
                                    Photobook.getPreferences().hsetUnreadImages.add(
                                            res.data.get(i).image_id);
                                int intCommentCount = Photobook.getPreferences().
                                        unreadImagesMap.containsKey(res.data.get(i).
                                        image_id) ? Photobook.getPreferences().
                                        unreadImagesMap.get(res.data.get(i).
                                        image_id) : 0;
                                Photobook.getPreferences().unreadImagesMap.put(
                                        res.data.get(i).image_id, intCommentCount + 1);
                            }
                            Photobook.getPreferences().savePreferences();
                            if (responseListener != null) responseListener.onResponse(res.data);
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
        VolleySingleton.getInstance(Photobook.getMainActivity()).
                addToRequestQueue(getCommentsRequest);
    }

    public static final void postCommentRequest(Context context,
                                         String imageId, String userId,
                                         String strText,
                                         long replyTo,
                                         final Response.Listener<String> responseListener,
                                         final Response.ErrorListener errorListener) {
        Gson gson = new Gson();
        HashMap<String, String> headers = createHeaders(userId);
        CommentEntryJson comment = new CommentEntryJson(Request.Method.POST);
        comment.text = strText;
        comment.image_id = imageId;
        comment.reply_to = replyTo;
        Log.d(LOG_TAG, "Comment request");
        StringRequest request = new StringRequest(Request.Method.POST,
                Constants.SERVER_URL+Constants.SERVER_PATH_COMMENTS ,
                gson.toJson(comment), headers,
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

    public static final void postCommentRequestJson(Context context,
            String imageId, String userId,
            String strText,
            long replyTo,
            final Response.Listener<String> responseListener,
            final Response.ErrorListener errorListener) {
        Gson gson = new Gson();
        HashMap<String, String> headers = createHeaders(userId);
        CommentEntryJson comment = new CommentEntryJson(Request.Method.POST);
        comment.text = strText;
        comment.image_id = imageId;
        comment.reply_to = replyTo;
        Log.d(LOG_TAG, "Comment request");
        StringRequest request = new StringRequest(Request.Method.POST,
            Constants.SERVER_URL+Constants.SERVER_PATH_COMMENTS ,
            gson.toJson(comment), headers,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(LOG_TAG, "Response: " + response);
                    Gson gson = new Gson();
                    Type type = new TypeToken<ServerResponse
                            <HashMap<String, String>>>(){}.getType();
                    try {
                        ServerResponse<HashMap<String, String>> res =
                                gson.fromJson(response, type);
                        if ( res != null && res.isSuccess()
                                && res.data != null
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

    public static final void deleteCommentRequest(Context context,
                                                String commendId, String userId,
                                                final Response.Listener<String> responseListener,
                                                final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.HEADER_USERID, userId);
        headers.put(Constants.HEADER_COMMENTID, commendId);
        headers.put("Accept", "*/*");
        Log.d(LOG_TAG, "Comment delete request");
        StringRequest request = new StringRequest(Request.Method.DELETE,
                Constants.SERVER_URL+Constants.SERVER_PATH_COMMENTS ,
                "", headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(LOG_TAG, "Response: " + response.toString());
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

    public static final void getUserProfileRequest (Context context,
            String[] userIds,
            final Response.Listener<HashMap<String, UserProfile>> responseListener,
            final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = new HashMap<String,String>();
        headers.put(Constants.HEADER_USERID, Photobook.getPreferences().strUserID);
        String strIdHeader = userIds.length > 0 ? userIds[0] : "";
        for (int i = 1; i < userIds.length; i++) strIdHeader = strIdHeader + "," + userIds[i];
        headers.put(Constants.KEY_ID, strIdHeader);
        headers.put("Accept", "*/*");
        Log.d(LOG_TAG, "Get user profile request");
        StringRequest getUserProfileRequest = new StringRequest(Request.Method.GET,
            Constants.SERVER_URL+Constants.SERVER_PATH_USER,
            "", headers,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(LOG_TAG, response.toString());
                    Gson gson = new Gson();
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
        VolleySingleton.getInstance(Photobook.getMainActivity()).
                addToRequestQueue(getUserProfileRequest);
    }

    public static final void getImageDetailsRequest (Context context,
                                          String imageId,
                                          final Response.Listener<String> responseListener,
                                          final Response.ErrorListener errorListener) {
        sendImageRequest(imageId, Request.Method.GET, responseListener, errorListener);
    }

    public static final void getImageDetailsRequestJson (Context context,
            String imageId, String userId,
            final Response.Listener<HashMap<String, ImageJson>> responseListener,
            final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = createHeaders(Photobook.getPreferences().strUserID);
        if ((imageId != null) && !imageId.isEmpty())
            headers.put(Constants.HEADER_IMAGEID, imageId);
        if ((userId != null) && !userId.isEmpty())
            headers.put(Constants.HEADER_ID, userId);
        Log.d(LOG_TAG, "Get image details request");
        StringRequest imageRequest = new StringRequest(Request.Method.GET,
            Constants.SERVER_URL + Constants.SERVER_PATH_IMAGE,
            "", headers,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(LOG_TAG, response.toString());
                    Gson gson = new Gson();
                    Type type = new TypeToken<ServerResponse
                        <HashMap<String, ImageJson>>>(){}.getType();
                    try {
                        ServerResponse<HashMap<String, ImageJson>> res =
                                gson.fromJson(response, type);
                        if ( res != null && res.isSuccess() && res.data != null
                                && responseListener != null)
                            responseListener.onResponse(res.data);
                        else if (responseListener != null)
                            responseListener.onResponse(
                                    new HashMap<String, ImageJson>());
                    } catch (Exception e) {
                        if (responseListener != null)
                            responseListener.onResponse(
                                    new HashMap<String, ImageJson>());
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    if (errorListener != null) errorListener.onErrorResponse(error);
                }
            }
        );
        VolleySingleton.getInstance(Photobook.getMainActivity()).
                addToRequestQueue(imageRequest);
    }

    private static void sendImageRequest(String imageId, int httpMethod,
                                         final Response.Listener<String> responseListener,
                                         final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = createHeaders(Photobook.getPreferences().strUserID);
        if ((imageId != null) && !imageId.isEmpty())
            headers.put(Constants.HEADER_IMAGEID, imageId);
        Log.d(LOG_TAG, "Get image details request");
        StringRequest imageRequest = new StringRequest(httpMethod,
                Constants.SERVER_URL + Constants.SERVER_PATH_IMAGE,
                "", headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(LOG_TAG, response.toString());
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
                addToRequestQueue(imageRequest);
    }

    /**
        Implements network request to un-share image
        URL: DELETE /image,
        Headers: 'imageid' - imageId
    */
    public static final void unShareImageRequest(Context context,
                                                  String imageId,
                                                  final Response.Listener<String> responseListener,
                                                  final Response.ErrorListener errorListener) {
        sendImageRequest(imageId, Request.Method.DELETE, responseListener, errorListener);
    }

    public static final void getSMSCodeRequest (Context context,
                                            String strPhoneNumber,
                                            final Response.Listener<String> responseListener,
                                            final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = new HashMap<String,String>();
        headers.put(Constants.KEY_NUMBER, strPhoneNumber);
        headers.put("Accept", "*/*");
        Log.d(LOG_TAG, "Receive sms code request");
        StringRequest getSMSCodeRequest = new StringRequest(Request.Method.GET,
                Constants.SERVER_URL+Constants.SERVER_PATH_USER+"/code",
                "", headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(LOG_TAG, response.toString());
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
        VolleySingleton.getInstance(Photobook.getMainActivity()).
                addToRequestQueue(getSMSCodeRequest);
    }

    public static final void getServerInfoRequest (Context context,
            final Response.Listener<HashMap<String, String>> responseListener,
            final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = new HashMap<String,String>();
        headers.put("Accept", "*/*");
        headers.put(Constants.HEADER_USERID, Photobook.getPreferences().strUserID);
        Log.d(LOG_TAG, "Get server info request");
        StringRequest getServerInfoRequest = new StringRequest(Request.Method.GET,
            Constants.SERVER_URL+Constants.SERVER_PATH_INFO,
            "", headers,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(LOG_TAG, response.toString());
                    Gson gson = new Gson();
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
        VolleySingleton.getInstance(Photobook.getMainActivity()).
                addToRequestQueue(getServerInfoRequest);
    }

    public static final void getChannelsRequest (Context context,
            final Response.Listener<ArrayList<UserProfile>> responseListener,
            final Response.ErrorListener errorListener) {
        HashMap<String, String> headers = new HashMap<String,String>();
        headers.put(Constants.HEADER_USERID, Photobook.getPreferences().strUserID);
        headers.put("Accept", "*/*");
        Log.d(LOG_TAG, "Get channels request");
        StringRequest getChannelsRequest = new StringRequest(Request.Method.GET,
            Constants.SERVER_URL+Constants.SERVER_PATH_CHANNELS,
            "", headers,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(LOG_TAG, response.toString());
                    Gson gson = new Gson();
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
        VolleySingleton.getInstance(Photobook.getMainActivity()).
                addToRequestQueue(getChannelsRequest);
    }

    public void sentFollowersRequest(String userId) {
        HashMap<String, String> headers = createHeaders(Photobook.getPreferences().strUserID);
        headers.put("id", userId);
        final String request = Constants.SERVER_PATH_USER + Constants.SERVER_PATH_FOLLOWERS;
        final Response.ErrorListener errorListener =
                createErrorListener(request);
        StringRequest getServerInfoRequest = new StringRequest(Request.Method.GET,
                Constants.SERVER_URL + request,
                "", headers, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (responseHandler != null) {
                            Gson gson = new Gson();
                            Type type = new TypeToken<ServerResponse<HashMap<String, UserProfile>>>(){}.getType();
                            try {
                                ServerResponse<HashMap<String, UserProfile>> res = gson.fromJson(response, type);
                                if ( res != null && res.isSuccess() && res.data != null) {
                                    responseHandler.onFollowersResponse(res.data);
                                } else {
                                    errorListener.onErrorResponse(new VolleyError());
                                }
                            } catch (Exception e) {
                                if (errorHandler != null) {
                                    errorHandler.onServerRequestError(request, e);
                                }
                            }
                        }
                    }
            }, errorListener);
        addRequesttoQueue(getServerInfoRequest);
    }

    private void addRequesttoQueue(StringRequest request) {
        VolleySingleton.getInstance(Photobook.getMainActivity()).addToRequestQueue(request);
    }

    public void setServerErrorHandler(ServerErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public void setServerResponseHandler(ServerResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    private Response.ErrorListener createErrorListener(final String request) {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (errorHandler != null) {
                    errorHandler.onServerRequestError(request, volleyError);
                }
            }
        };
    }

    /**
     * Create hash map with the headers for http request
     * @param userId user id to be included to the headers
     * @return created hash map with the headers
     */
    private static HashMap<String, String> createHeaders(String userId) {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "*/*");
        headers.put(Constants.HEADER_USERID, userId);
        return headers;
    }
}
