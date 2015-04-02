package com.freecoders.photobook;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.freecoders.photobook.classes.CallbackInterface;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.common.Preferences;
import com.freecoders.photobook.db.FriendEntry;
import com.freecoders.photobook.db.ImageEntry;
import com.freecoders.photobook.gson.FeedEntryJson;
import com.freecoders.photobook.gson.ImageJson;
import com.freecoders.photobook.gson.UserProfile;
import com.freecoders.photobook.network.MultiPartRequest;
import com.freecoders.photobook.network.ServerInterface;
import com.freecoders.photobook.network.VolleySingleton;
import com.freecoders.photobook.utils.FileUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.soundcloud.android.crop.Crop;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivityHandler {
    private static String LOG_TAG = "MainActivityHandler";
    
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
        progress = ProgressDialog.show(activity,
                activity.getResources().getString(R.string.dialog_uploading_avatar),
                activity.getResources().getString(R.string.dialog_please_wait), true);
        File avatarImage = new File(activity.getFilesDir(), Constants.FILENAME_AVATAR);
        HashMap<String, String> params = new HashMap<String, String>();
        final String strUserID = Photobook.getPreferences().strUserID;
        params.put(Constants.HEADER_USERID, strUserID);
        MultiPartRequest avatarRequest = new MultiPartRequest(Constants.SERVER_URL+"/image",
                avatarImage, params,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d(LOG_TAG, response.toString());
                        try {
                            JSONObject obj = new JSONObject( response);
                            String strUrl = obj.getJSONObject(Constants.RESPONSE_DATA).
                                    getString(Constants.KEY_URL_SMALL);
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
                            Log.d(LOG_TAG, "Exception " + e.getLocalizedMessage());
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                progress.dismiss();
                Log.d(LOG_TAG, "Error: " + error.getMessage());
            }
        }
        );
        VolleySingleton.getInstance(activity).addToRequestQueue(avatarRequest);
    }

    public void checkLatestVersion () {
        ServerInterface.getServerInfoRequest(activity,
            new Response.Listener<HashMap<String,String>>() {
                @Override
                public void onResponse(HashMap<String,String> response) {
                    PackageInfo pInfo = null;
                    try {
                        pInfo = activity.getPackageManager().getPackageInfo(
                                activity.getPackageName(), 0);
                    } catch (PackageManager.NameNotFoundException e) {
                    }
                    if (response.containsKey(Constants.KEY_LATEST_APK_VER) &&
                            response.containsKey(Constants.KEY_LATEST_APK_URL) &&
                            response.containsKey(Constants.KEY_MIN_CLIENT_VERSION) &&
                            pInfo != null) {
                        int intLatestAPKVersion = Integer.
                            valueOf(response.get(Constants.KEY_LATEST_APK_VER));
                        int intMinClientVersion = Integer.
                            valueOf(response.get(Constants.KEY_MIN_CLIENT_VERSION));
                        String strLatestAPKURL = response.get(Constants.KEY_LATEST_APK_URL);
                        String strLocalFilename = intLatestAPKVersion + ".apk";
                        if (intMinClientVersion > pInfo.versionCode)
                            showUpdateDialog(true, strLatestAPKURL, strLocalFilename);
                        else if (intLatestAPKVersion > pInfo.versionCode)
                            showUpdateDialog(false, strLatestAPKURL, strLocalFilename);
                    }
                }
            }, null);
    }

    public void showUpdateDialog (Boolean boolMandatory, final String strURL,
                                  final String strLocalFilename) {
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        if (boolMandatory)
            alert.setMessage(R.string.alert_update_required);
        else
            alert.setMessage(R.string.alert_new_version_available);
        alert.setPositiveButton(R.string.alert_ok_button,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    new FileUtils.DownloadTask(strURL, strLocalFilename,
                        new CallbackInterface() {
                            public void onResponse(Object obj) {
                                Intent promptInstall = new Intent(Intent.ACTION_VIEW)
                                        .setDataAndType(Uri.fromFile(new File(strLocalFilename)),
                                                "application/vnd.android.package-archive");
                                activity.startActivity(promptInstall);
                            }
                        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
        if (!boolMandatory) {
            alert.setNegativeButton(R.string.alert_cancel_button,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
        }
        alert.show();
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
                    Log.d(LOG_TAG, "Received push id = " + strPushID);
                } catch (IOException ex) {
                    Log.d(LOG_TAG, "Error: " + ex.getMessage());
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
                    Log.d(LOG_TAG, "Sending pushId " + strPushID + " to server");
                    ServerInterface.updateProfileRequest(activity, profile,
                            Photobook.getPreferences().strUserID,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Photobook.getPreferences().strPushRegID = strPushID;
                                    Photobook.getPreferences().savePreferences();
                                    Log.d(LOG_TAG, "Delivered pushId to server");
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
                Log.i(LOG_TAG, "This device is not supported for google play services");
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
            Log.d(LOG_TAG, "Handling data from intent "+ strData );
            try {
                JSONObject dataJson = new JSONObject(strData);
                Gson gson = new Gson();
                if (dataJson.has(Constants.KEY_IMAGEID)) {
                    String strImageId = dataJson.getString(Constants.KEY_IMAGEID);
                    ImageEntry imageEntry =
                            Photobook.getImagesDataSource().getImageByServerID(strImageId);
                    Photobook.setGalleryImageDetails(imageEntry);
                    if (imageEntry == null) {
                        Toast.makeText(activity, R.string.toast_img_not_found,
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    Intent mIntent = new Intent(Photobook.getMainActivity(),
                            ImageDetailsActivity.class);
                    Bundle b = new Bundle();
                    b.putBoolean(Photobook.intentExtraImageDetailsSource, true);
                    mIntent.putExtras(b);
                    activity.mViewPager.setCurrentItem(1);
                    Photobook.getMainActivity().startActivity(mIntent);
                }
            } catch (JSONException e) {
                Log.d(LOG_TAG, "Json parse error");
            }
        } else if ((i.getIntExtra("event_type", 0) == Constants.EVENT_NEW_IMAGE) &&
                i.hasExtra("data")) {
            String strData = i.getStringExtra("data");
            Log.d(LOG_TAG, "Handling data from intent "+ strData );
            try {
                JSONObject dataJson = new JSONObject(strData);
                Gson gson = new Gson();
                if ((dataJson.has(Constants.KEY_IMAGE)) &&
                        (dataJson.has(Constants.KEY_AUTHOR))) {
                    ImageJson image = gson.fromJson(dataJson.getString(Constants.KEY_IMAGE),
                            ImageJson.class);
                    UserProfile author = gson.fromJson(dataJson.getString(Constants.KEY_AUTHOR),
                            UserProfile.class);
                    FeedEntryJson feedEntry = new FeedEntryJson();
                    feedEntry.author = author;
                    feedEntry.image = image;
                    Photobook.setImageDetails(feedEntry);
                    Intent mIntent = new Intent(Photobook.getMainActivity(),
                            ImageDetailsActivity.class);
                    activity.mViewPager.setCurrentItem(2);
                    Photobook.getMainActivity().startActivity(mIntent);
                }
            } catch (JSONException e) {
                Log.d(LOG_TAG, "Json parse error");
            }
        }
    }

}
