package com.freecoders.photobook;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.JsonObjectRequest;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.common.Preferences;
import com.freecoders.photobook.gson.UserProfile;
import com.freecoders.photobook.network.MultiPartRequest;
import com.freecoders.photobook.network.ServerInterface;
import com.freecoders.photobook.network.StringRequest;
import com.freecoders.photobook.network.VolleySingleton;
import com.freecoders.photobook.utils.PhoneUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivityHandler {
	private RegisterActivity activity;
	
	public RegisterActivityHandler(RegisterActivity activity){
		this.activity = activity;
	}

    public void populateView(){
        if (Photobook.getPreferences().intRegisterStatus == Constants.STATUS_SMS_WAIT) {
            if (Photobook.getPreferences().strUserName != null)
                activity.nameEditText.setText(Photobook.getPreferences().strUserName);
            if (Photobook.getPreferences().strEmail != null)
                activity.emailEditText.setText(Photobook.getPreferences().strEmail);
            if (Photobook.getPreferences().strPhone != null)
                activity.phoneEditText.setText(Photobook.getPreferences().strPhone);
            final File avatar = new File(Photobook.getMainActivity().getFilesDir(),
                    Constants.FILENAME_AVATAR);
            if (avatar.exists()) {
                activity.avatarImage.setImageURI(Uri.fromFile(avatar));
                activity.boolAvatarSelected = true;
            }
            showSMSCodeDialog();
        }
    }

    public void showSMSCodeDialog(){
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(R.string.alert_sms_title);
        alert.setMessage(R.string.alert_sms_message);

        final EditText input = new EditText(activity);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        alert.setView(input);

        alert.setPositiveButton(R.string.alert_ok_button,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        doRegister(activity.nameEditText.getText().toString(),
                                activity.emailEditText.getText().toString(),
                                input.getText().toString(), activity.boolAvatarSelected);
                    }
                });

        alert.setNegativeButton(R.string.alert_cancel_button,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Photobook.getPreferences().intRegisterStatus =
                                Constants.STATUS_UNREGISTERED;
                    }
                });
        alert.show();
    }

    public void sendAvatar(){
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
                                            File avatar = new File(Photobook.getMainActivity().
                                                    getFilesDir(), Constants.FILENAME_AVATAR);
                                            if (avatar.exists())
                                                Photobook.getMainActivity().mDrawerAvatarImage.
                                                        setImageURI(Uri.fromFile(avatar));
                                        }
                                    }, null);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d(Constants.LOG_TAG, "Exception " + e.getLocalizedMessage());
                        }
                        //Toast.makeText(context, "Avatar downloaded ",
                        //        Toast.LENGTH_LONG).show();
                        //((Activity) context).finish();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                    Log.d(Constants.LOG_TAG, "Error: " + error.getMessage());
            }
        }
        );
        VolleySingleton.getInstance(activity).addToRequestQueue(avatarRequest);
    }

	public void doRegister(final String strName, String strEmail, String strCode,
                           final Boolean boolUploadAvatar){
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "*/*");
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("name", activity.nameEditText.getText().toString() );
		params.put("email", activity.emailEditText.getText().toString() );
        params.put("code", strCode);
        String strPhoneNumber = PhoneUtils.getPhoneNumber();
        if (!activity.phoneEditText.getText().toString().isEmpty()) {
            strPhoneNumber = PhoneUtils.getNormalizedPhoneNumber(
                    activity.phoneEditText.getText().toString());
            params.put("phone", activity.phoneEditText.getText().toString());
        } else {
            params.put("phone", strPhoneNumber);
        }
        Photobook.getPreferences().strContactKey = strEmail;
        if ((strPhoneNumber != null) && (strPhoneNumber.isEmpty() == false))
            Photobook.getPreferences().strContactKey = strPhoneNumber;

		final ProgressDialog pDialog = new ProgressDialog(activity);
		pDialog.setMessage("Creating account...");
		pDialog.show();

        StringRequest registerRequest = new StringRequest(Request.Method.POST,
                Constants.SERVER_URL+"/user",
                new Gson().toJson(params), headers,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d(Constants.LOG_TAG, response.toString());
                        pDialog.dismiss();
                        String strID = "";
                        Integer intPublicID = 0;
                        try {
                            JSONObject resJson = new JSONObject(response);
                            String strResult = resJson.getString("result");
                            if (strResult.equals("OK")) {
                                String strData = resJson.getString("data");
                                JSONObject obj = new JSONObject(strData);
                                strID = obj.getString("id");
                                intPublicID = obj.getInt("public_id");
                                Photobook.getPreferences().strUserID = strID;
                                Photobook.getPreferences().intPublicID = intPublicID;
                                Photobook.getPreferences().strUserName = strName;
                                Photobook.getPreferences().intRegisterStatus =
                                        Constants.STATUS_REGISTERED;
                                Photobook.getPreferences().savePreferences();

                                if (boolUploadAvatar) sendAvatar();

                                if (Photobook.getFriendsFragmentTab() != null)
                                    Photobook.getFriendsFragmentTab().refreshContactList();
                                if (Photobook.getGalleryFragmentTab() != null)
                                    Photobook.getGalleryFragmentTab().syncGallery();
                                Photobook.getMainActivity().mHandler.registerPushID();
                                activity.finish();
                            } else if (resJson.has("code") &&
                                    resJson.getInt("code") == 121) {
                                Toast.makeText(activity, R.string.alert_wrong_sms,
                                        Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Log.d(Constants.LOG_TAG, "Json parse error");
                        }
                        if ((strID == null) || (strID.isEmpty() == true))
                            Toast.makeText(activity, "Registration failed",
                                    Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(Constants.LOG_TAG, "Error: " + error.getMessage());
                        pDialog.dismiss();
                    }
                }
        );
		VolleySingleton.getInstance(activity).addToRequestQueue(registerRequest);
	}

    public void doRegister(){
        final ProgressDialog pDialog = new ProgressDialog(activity);
        pDialog.setMessage("Creating account...");
        pDialog.show();
        ServerInterface.getSMSCodeRequest(
            activity, activity.phoneEditText.getText().toString(),
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(Constants.LOG_TAG, response);
                    pDialog.dismiss();
                    try {
                        JSONObject obj = new JSONObject(response);
                        String strResult = obj.getString("result");
                        if (strResult.equals("OK")) {
                            Photobook.getPreferences().strUserName =
                                    activity.nameEditText.getText().toString();
                            Photobook.getPreferences().strPhone =
                                    activity.phoneEditText.getText().toString();
                            Photobook.getPreferences().strEmail =
                                    activity.emailEditText.getText().toString();
                            Photobook.getPreferences().intRegisterStatus =
                                    Constants.STATUS_SMS_WAIT;
                            Photobook.getPreferences().savePreferences();
                            showSMSCodeDialog();
                        }
                    } catch (JSONException e) {
                        Log.d(Constants.LOG_TAG, "Json parse error");
                    }
                }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(Constants.LOG_TAG, "Error: " + error.getMessage());
                pDialog.dismiss();
            }
        });
    }
}

