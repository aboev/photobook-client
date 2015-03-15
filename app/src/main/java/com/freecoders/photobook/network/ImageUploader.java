package com.freecoders.photobook.network;

import android.app.Activity;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.freecoders.photobook.GalleryAdapter;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.common.Preferences;
import com.freecoders.photobook.db.ImageEntry;
import com.freecoders.photobook.gson.UserProfile;
import com.freecoders.photobook.utils.FileUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Alex on 2014-12-05.
 */
public class ImageUploader {

    private int mPosition;
    private ArrayList<ImageEntry> mImgList;
    private GalleryAdapter mAdapter;

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
}
