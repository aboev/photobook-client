package com.freecoders.photobook;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.common.Preferences;
import com.freecoders.photobook.db.FriendEntry;
import com.freecoders.photobook.db.ImageEntry;
import com.freecoders.photobook.gson.UserProfile;
import com.freecoders.photobook.network.MultiPartRequest;
import com.freecoders.photobook.network.ServerInterface;
import com.freecoders.photobook.network.VolleySingleton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.soundcloud.android.crop.Crop;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivityHandler {
	private Preferences prefs;
    private MainActivity activity;
    private ProgressDialog progress;

    private GoogleCloudMessaging gcm;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	
	public void init(MainActivity activity) {
        this.activity = activity;
        Photobook.setMainActivity(activity);
        prefs = new Preferences(activity);
		if (!prefs.loadPreferences()) {
			Intent intent = new Intent(activity, RegisterActivity.class);
		    activity.startActivity(intent);
		}

        File avatar = new File(activity.getFilesDir(), Constants.FILENAME_AVATAR);
        if (avatar.exists()) {
            activity.mDrawerAvatarImage.setImageURI(Uri.fromFile(avatar));
        }
        activity.mDrawerAvatarImage.setOnClickListener(avatarClickListener);
        activity.mDrawerUserName.setText(prefs.strUserName);
        activity.mDrawerContactKey.setText(prefs.strContactKey);

        if (Photobook.getPreferences().strPushRegID.isEmpty() &&
                !Photobook.getPreferences().strUserID.isEmpty())
            registerPushID();
	}

    private View.OnClickListener avatarClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            activity.startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                    Constants.INTENT_PICK_IMAGE);
        }
    };

    public void updateAvatar(){
        progress = ProgressDialog.show(activity, "Uploading avatar", "Please wait", true);
        File avatarImage = new File(activity.getFilesDir(), Constants.FILENAME_AVATAR);
        HashMap<String, String> params = new HashMap<String, String>();
        final String strUserID = Photobook.getPreferences().strUserID;
        params.put("userid", strUserID);
        MultiPartRequest avatarRequest = new MultiPartRequest(Constants.SERVER_URL+"/image",
                avatarImage, params,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d(Constants.LOG_TAG, response.toString());
                        try {
                            JSONObject obj = new JSONObject( response);
                            String strUrl = obj.getJSONObject("data").getString("url_small");
                            UserProfile profile = new UserProfile();
                            profile.setNullFields();
                            profile.avatar = strUrl;
                            ServerInterface.updateProfileRequest(activity, profile,
                                    strUserID,
                                    new Response.Listener<String>() {

                                        @Override
                                        public void onResponse(String response) {
                                            activity.mDrawerAvatarImage.setImageResource(0);
                                            File avatar = new
                                                    File(activity.getFilesDir(),
                                                    Constants.FILENAME_AVATAR);
                                            if (avatar.exists()) {
                                                activity.mDrawerAvatarImage.setImageURI(
                                                        Uri.fromFile(avatar));
                                            }
                                            progress.dismiss();
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            progress.dismiss();
                                        }});
                        } catch (Exception e) {
                            progress.dismiss();
                            e.printStackTrace();
                            Log.d(Constants.LOG_TAG, "Exception " + e.getLocalizedMessage());
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                progress.dismiss();
                Log.d(Constants.LOG_TAG, "Error: " + error.getMessage());
            }
        }
        );
        VolleySingleton.getInstance(activity).addToRequestQueue(avatarRequest);
    }

    public void registerPushID() {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                String strPushID = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(activity);
                    }
                    strPushID = gcm.register(Constants.PUSH_SENDER_ID);
                    Log.d(Constants.LOG_TAG, "Received push id = " + strPushID);
                } catch (IOException ex) {
                    Log.d(Constants.LOG_TAG, "Error: " + ex.getMessage());
                }
                return strPushID;
            }
            @Override
            protected void onPostExecute(Object res) {
                final String strPushID = res != null ? (String) res : "";
                if (!strPushID.isEmpty()) {
                    UserProfile profile = new UserProfile();
                    profile.setNullFields();
                    profile.pushid = strPushID;
                    Log.d(Constants.LOG_TAG, "Sending pushId " + strPushID + " to server");
                    ServerInterface.updateProfileRequest(activity, profile,
                            Photobook.getPreferences().strUserID,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Photobook.getPreferences().strPushRegID = strPushID;
                                    Photobook.getPreferences().savePreferences();
                                    Log.d(Constants.LOG_TAG, "Delivered pushId to server");
                                }
                            }, null);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, Photobook.getMainActivity(),
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(Constants.LOG_TAG, "This device is not supported for google play services");
            }
            return false;
        }
        return true;
    }

    public void handleIntent(Intent i) {
        if (!i.hasExtra("event_type")) return;
        if ((i.getIntExtra("event_type", 0) == Constants.EVENT_NEW_COMMENT) &&
                i.hasExtra("data")) {
            String strData = i.getStringExtra("data");
            Photobook.getGalleryFragmentTab().syncComments();
            try {
                JSONObject dataJson = new JSONObject(strData);
                Gson gson = new Gson();
                if (dataJson.has("image_id")) {
                    String strImageId = dataJson.getString("image_id");
                    ImageEntry imageEntry =
                            Photobook.getImagesDataSource().getImageByServerID(strImageId);
                    Photobook.setGalleryImageDetails(imageEntry);
                    if (imageEntry == null) return;
                    Intent mIntent = new Intent(Photobook.getMainActivity(),
                            ImageDetailsActivity.class);
                    Bundle b = new Bundle();
                    b.putBoolean(Photobook.intentExtraImageDetailsSource, true);
                    mIntent.putExtras(b);
                    Photobook.getMainActivity().startActivity(mIntent);
                }
            } catch (JSONException e) {
                Log.d(Constants.LOG_TAG, "Json parse error");
            }
        }
    }

}
