package com.freecoders.photobook.network;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.freecoders.photobook.FriendsFragmentTab;
import com.freecoders.photobook.GalleryAdapter;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.common.Preferences;
import com.freecoders.photobook.db.FriendEntry;
import com.freecoders.photobook.db.ImageEntry;
import com.freecoders.photobook.gson.CommentEntryJson;
import com.freecoders.photobook.gson.UserProfile;
import com.freecoders.photobook.utils.FileUtils;
import com.freecoders.photobook.utils.ImageUtils;
import com.freecoders.photobook.utils.PhoneUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alex on 2014-12-05.
 */
public class ImageUploader {

    private int mPosition;
    private ArrayList<ImageEntry> mImgList;
    private GalleryAdapter mAdapter;
    private String strLocalURI;

    public void uploadImage(ArrayList<ImageEntry> imgList, int position,
                         GalleryAdapter adapter) {
        this.mPosition = position;
        this.mAdapter = adapter;
        this.mImgList = imgList;
        imgList.get(position).setStatus(ImageEntry.INT_STATUS_SHARING);
        Photobook.getImagesDataSource().updateImage(imgList.get(position));
        adapter.notifyDataSetChanged();
        HashMap<String, String> headers = new HashMap<String,String>();
        headers.put("userid", Photobook.getPreferences().strUserID);
        MultiPartRequest uploadRequest = new MultiPartRequest(
                Constants.SERVER_URL+Constants.SERVER_PATH_IMAGE,
                imgList.get(position).getOrigUri(),
                headers,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d(Constants.LOG_TAG, response.toString());
                        try {
                            JSONObject obj = new JSONObject( response);
                            String strId = obj.getJSONObject("data").getString("id");
                            mImgList.get(mPosition).setServerId(strId);
                            Photobook.getImagesDataSource().updateImage(mImgList.get(mPosition));
                            putMetaData(strId, mImgList.get(mPosition).getTitle());
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d(Constants.LOG_TAG, "Exception " + e.getLocalizedMessage());
                            handleFailure();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.d(Constants.LOG_TAG, "Error: " + error.getMessage());
                        handleFailure();
                    }
                }
        );
        /*uploadRequest.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                */
        VolleySingleton.getInstance(Photobook.getMainActivity()).addToRequestQueue(uploadRequest);
    }

    private void putMetaData(String strId, String strTitle){
        Gson gson = new Gson();
        HashMap<String, String> reqBody = new HashMap<String, String>();
        reqBody.put("id", strId);
        reqBody.put("timestamp", String.valueOf(System.currentTimeMillis()));
        reqBody.put("title", strTitle);
        HashMap<String, String> headers = new HashMap<String,String>();
        headers.put("userid", Photobook.getPreferences().strUserID);
        headers.put("Accept", "*/*");
        StringRequest putMetaDataRequest = new StringRequest(Request.Method.PUT,
                Constants.SERVER_URL+Constants.SERVER_PATH_IMAGE ,
                gson.toJson(reqBody), headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(Constants.LOG_TAG, response.toString());
                        try {
                            JSONObject resJson = new JSONObject(response);
                            String strRes = resJson.getString("result");
                            if (strRes.equals("OK")) {
                                mImgList.get(mPosition).setStatus(ImageEntry.INT_STATUS_SHARED);
                                Photobook.getImagesDataSource().
                                        updateImage(mImgList.get(mPosition));

                                Log.d(Constants.LOG_TAG, "Saving new ImageEntry with " +
                                        mImgList.get(mPosition).getMediaStoreID());
                                mAdapter.notifyDataSetChanged();
                            }
                        } catch (Exception e) {
                            Log.d(Constants.LOG_TAG, "Exception " + e.getLocalizedMessage());
                            handleFailure();
                        }
                    }
                }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(Constants.LOG_TAG, "Error: " + new String(error.networkResponse.data));
                            handleFailure();
                        }
                }
        );
        VolleySingleton.getInstance(Photobook.getMainActivity()).
                addToRequestQueue(putMetaDataRequest);
    }

    public void handleFailure(){
        mImgList.get(mPosition).setStatus(ImageEntry.INT_STATUS_DEFAULT);
        Photobook.getImagesDataSource().
                updateImage(mImgList.get(mPosition));
        mAdapter.notifyDataSetChanged();
    }

    public void uploadImageS3(ArrayList<ImageEntry> imgList, int position,
                            String strLocalURI,
                            GalleryAdapter adapter) {
        this.mPosition = position;
        this.mAdapter = adapter;
        this.mImgList = imgList;
        this.strLocalURI = strLocalURI;
        imgList.get(position).setStatus(ImageEntry.INT_STATUS_SHARING);
        Photobook.getImagesDataSource().updateImage(imgList.get(position));
        adapter.notifyDataSetChanged();
        HashMap<String, String> headers = new HashMap<String,String>();
        headers.put("Accept", "*/*");
        headers.put("userid", Photobook.getPreferences().strUserID);
        Log.d(Constants.LOG_TAG, "Get pre-signed url request");
        StringRequest getPresignedURLRequest = new StringRequest(Request.Method.GET,
                Constants.SERVER_URL+Constants.SERVER_PATH_IMAGE+"/upload_url",
                "", headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(Constants.LOG_TAG, response.toString());
                        try {
                            JSONObject resJson = new JSONObject(response);
                            String strRes = resJson.getString("result");
                            if (strRes.equals("OK") && resJson.has("data")) {
                                JSONObject data = new JSONObject(resJson.getString("data"));
                                if (data.has("url") && data.has("id")) {
                                    String strPresignedURL = data.getString("url");
                                    String strImageID = data.getString("id");
                                    new S3UploaderTask(strPresignedURL, strImageID).
                                            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                }
                            }
                        } catch (Exception e) {
                            Log.d(Constants.LOG_TAG, "Exception " + e.getLocalizedMessage());
                            handleFailure();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(Constants.LOG_TAG, "Error: " +
                                new String(error.getMessage()));
                        handleFailure();
                    }
                }
        );
        VolleySingleton.getInstance(Photobook.getMainActivity()).
                addToRequestQueue(getPresignedURLRequest);
    }

    public class S3UploaderTask extends AsyncTask<String, Void, Boolean> {

        private String strImageID;
        private String strPresignedURL;

        public S3UploaderTask(String strPresignedURL, String strImageID)
        {
            this.strImageID = strImageID;
            this.strPresignedURL = strPresignedURL;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Boolean res = false;
            try {
                Bitmap bitmap = ImageUtils.
                        decodeSampledBitmap(mImgList.get(mPosition).getOrigUri(), 2048, 1536);
                URL url = new URL(strPresignedURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "image/jpeg");
                connection.setRequestMethod("PUT");
                OutputStream outputStream = connection.getOutputStream();

                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.close();
                int responseCode = connection.getResponseCode();

                if (responseCode == 200) {
                    res = true;
                    Log.d(Constants.LOG_TAG, "Upload to S3 successful");
                } else {
                    Log.d(Constants.LOG_TAG, "Upload to S3 failed with code " + responseCode);
                }
            } catch (Exception e) {
                Log.d(Constants.LOG_TAG, "Exception " + e.getLocalizedMessage());
                handleFailure();
            }
            return res;
        }

        @Override
        protected void onPostExecute(Boolean res) {
            if (res)
                postMetaData(strImageID);
            else
                handleFailure();
        }
    }

    private void postMetaData(final String strImageID) {
        Gson gson = new Gson();
        HashMap<String, String> reqBody = new HashMap<String, String>();
        reqBody.put("local_uri", strLocalURI);
        reqBody.put("title", mImgList.get(mPosition).getTitle());
        HashMap<String, String> headers = new HashMap<String,String>();
        headers.put("userid", Photobook.getPreferences().strUserID);
        headers.put("imageid", strImageID);
        headers.put("Accept", "*/*");
        StringRequest putMetaDataRequest = new StringRequest(Request.Method.POST,
                Constants.SERVER_URL+Constants.SERVER_PATH_IMAGE+"/remote" ,
                gson.toJson(reqBody), headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(Constants.LOG_TAG, response.toString());
                        try {
                            JSONObject resJson = new JSONObject(response);
                            String strRes = resJson.getString("result");
                            if (strRes.equals("OK")) {
                                mImgList.get(mPosition).setServerId(strImageID);
                                mImgList.get(mPosition).setStatus(ImageEntry.INT_STATUS_SHARED);
                                Photobook.getImagesDataSource().
                                        updateImage(mImgList.get(mPosition));

                                Log.d(Constants.LOG_TAG, "Saving new ImageEntry with " +
                                        mImgList.get(mPosition).getMediaStoreID());
                                mAdapter.notifyDataSetChanged();
                            }
                        } catch (Exception e) {
                            Log.d(Constants.LOG_TAG, "Exception " + e.getLocalizedMessage());
                            handleFailure();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(Constants.LOG_TAG, "Error: " +
                                new String(error.networkResponse.data));
                        handleFailure();
                    }
        }
        );
        VolleySingleton.getInstance(Photobook.getMainActivity()).
                addToRequestQueue(putMetaDataRequest);
    }
}
