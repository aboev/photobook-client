package com.freecoders.photobook.network;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
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
    private static String LOG_TAG = "ImageUploader";
    
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
        headers.put(Constants.HEADER_USERID, Photobook.getPreferences().strUserID);
        MultiPartRequest uploadRequest = new MultiPartRequest(
                Constants.SERVER_URL+Constants.SERVER_PATH_IMAGE,
                imgList.get(position).getOrigUri(),
                headers,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d(LOG_TAG, response.toString());
                        try {
                            JSONObject obj = new JSONObject( response);
                            String strId = obj.getJSONObject(Constants.RESPONSE_DATA).
                                    getString(Constants.KEY_ID);
                            mImgList.get(mPosition).setServerId(strId);
                            Photobook.getImagesDataSource().updateImage(mImgList.get(mPosition));
                            putMetaData(strId, mImgList.get(mPosition).getTitle());
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d(LOG_TAG, "Exception " + e.getLocalizedMessage());
                            handleFailure();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.d(LOG_TAG, "Error: " + error.getMessage());
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
        reqBody.put(Constants.KEY_ID, strId);
        reqBody.put(Constants.KEY_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        reqBody.put(Constants.KEY_TITLE, strTitle);
        HashMap<String, String> headers = new HashMap<String,String>();
        headers.put(Constants.HEADER_USERID, Photobook.getPreferences().strUserID);
        headers.put("Accept", "*/*");
        StringRequest putMetaDataRequest = new StringRequest(Request.Method.PUT,
                Constants.SERVER_URL+Constants.SERVER_PATH_IMAGE ,
                gson.toJson(reqBody), headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(LOG_TAG, response.toString());
                        try {
                            JSONObject resJson = new JSONObject(response);
                            String strRes = resJson.getString(Constants.RESPONSE_RESULT);
                            if (strRes.equals(Constants.RESPONSE_RESULT_OK)) {
                                mImgList.get(mPosition).setStatus(ImageEntry.INT_STATUS_SHARED);
                                Photobook.getImagesDataSource().
                                        updateImage(mImgList.get(mPosition));

                                Log.d(LOG_TAG, "Saving new ImageEntry with " +
                                        mImgList.get(mPosition).getMediaStoreID());
                                mAdapter.notifyDataSetChanged();
                            }
                        } catch (Exception e) {
                            Log.d(LOG_TAG, "Exception " + e.getLocalizedMessage());
                            handleFailure();
                        }
                    }
                }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(LOG_TAG, "Error: " + new String(error.networkResponse.data));
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
        Photobook.getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
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
        headers.put(Constants.HEADER_USERID, Photobook.getPreferences().strUserID);
        Log.d(LOG_TAG, "Get pre-signed url request");
        StringRequest getPresignedURLRequest = new StringRequest(Request.Method.GET,
                Constants.SERVER_URL+Constants.SERVER_PATH_IMAGE+"/upload_url",
                "", headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(LOG_TAG, response.toString());
                        try {
                            JSONObject resJson = new JSONObject(response);
                            String strRes = resJson.getString(Constants.RESPONSE_RESULT);
                            if (strRes.equals(Constants.RESPONSE_RESULT_OK) &&
                                    resJson.has(Constants.RESPONSE_DATA)) {
                                JSONObject data = new JSONObject(resJson.
                                        getString(Constants.RESPONSE_DATA));
                                if (data.has(Constants.KEY_URL) && data.has(Constants.KEY_ID)) {
                                    String strPresignedURL = data.getString(Constants.KEY_URL);
                                    String strImageID = data.getString(Constants.KEY_ID);
                                    new S3UploaderTask(strPresignedURL, strImageID).
                                            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                }
                            }
                        } catch (Exception e) {
                            Log.d(LOG_TAG, "Exception " + e.getLocalizedMessage());
                            handleFailure();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(LOG_TAG, "Error: " +
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
                int orientation = ImageUtils.
                        getExifOrientation(mImgList.get(mPosition).getOrigUri());
                Bitmap b = ImageUtils.
                        decodeSampledBitmap(mImgList.get(mPosition).getOrigUri(), 1536, 1152);
                Bitmap bitmap;
                if ((orientation == 90) || (orientation == 270)) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(orientation);
                    bitmap = Bitmap.createBitmap(b, 0, 0,
                            b.getWidth(), b.getHeight(), matrix, true);
                } else bitmap = b;
                URL url = new URL(strPresignedURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "image/jpeg");
                connection.setRequestMethod("PUT");
                OutputStream outputStream = connection.getOutputStream();

                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
                outputStream.close();
                int responseCode = connection.getResponseCode();

                if (responseCode == 200) {
                    res = true;
                    Log.d(LOG_TAG, "Upload to S3 successful");
                } else {
                    Log.d(LOG_TAG, "Upload to S3 failed with code " + responseCode);
                }
            } catch (Exception e) {
                Log.d(LOG_TAG, "Exception " + e.getLocalizedMessage());
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
        reqBody.put(Constants.KEY_LOCAL_URI, strLocalURI);
        reqBody.put(Constants.KEY_TITLE, mImgList.get(mPosition).getTitle());
        HashMap<String, String> headers = new HashMap<String,String>();
        headers.put(Constants.HEADER_USERID, Photobook.getPreferences().strUserID);
        headers.put(Constants.HEADER_IMAGEID, strImageID);
        headers.put("Accept", "*/*");
        StringRequest putMetaDataRequest = new StringRequest(Request.Method.POST,
                Constants.SERVER_URL+Constants.SERVER_PATH_IMAGE+"/remote" ,
                gson.toJson(reqBody), headers,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(LOG_TAG, response.toString());
                        try {
                            JSONObject resJson = new JSONObject(response);
                            String strRes = resJson.getString(Constants.RESPONSE_RESULT);
                            if (strRes.equals(Constants.RESPONSE_RESULT_OK)) {
                                mImgList.get(mPosition).setServerId(strImageID);
                                mImgList.get(mPosition).setStatus(ImageEntry.INT_STATUS_SHARED);
                                Photobook.getImagesDataSource().
                                        updateImage(mImgList.get(mPosition));

                                Log.d(LOG_TAG, "Saving new ImageEntry with " +
                                        mImgList.get(mPosition).getMediaStoreID());
                                mAdapter.notifyDataSetChanged();
                            }
                        } catch (Exception e) {
                            Log.d(LOG_TAG, "Exception " + e.getLocalizedMessage());
                            handleFailure();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(LOG_TAG, "Error: " +
                                new String(error.networkResponse.data));
                        handleFailure();
                    }
        }
        );
        VolleySingleton.getInstance(Photobook.getMainActivity()).
                addToRequestQueue(putMetaDataRequest);
    }
}
