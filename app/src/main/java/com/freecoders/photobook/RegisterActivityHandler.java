package com.freecoders.photobook;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.JsonObjectRequest;
import com.freecoders.photobook.classes.CallbackInterface;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.common.Preferences;
import com.freecoders.photobook.db.FriendEntry;
import com.freecoders.photobook.gson.UserProfile;
import com.freecoders.photobook.network.ImageDownloader;
import com.freecoders.photobook.network.MultiPartRequest;
import com.freecoders.photobook.network.ServerInterface;
import com.freecoders.photobook.network.StringRequest;
import com.freecoders.photobook.network.VolleySingleton;
import com.freecoders.photobook.utils.FileUtils;
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
import android.provider.ContactsContract;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivityHandler {
    private static String LOG_TAG = "RegisterActivityHandler";
    
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
        } else {
            String strPhone = PhoneUtils.getPhoneNumber();
            if (!strPhone.isEmpty())
                activity.phoneEditText.setText(
                        PhoneUtils.getNormalizedPhoneNumber(strPhone));
        }
        activity.privacyPolcyTextView.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openPrivacyPolicy();
                }
            }
        );
        activity.eulaTextView.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openEULA();
                }
            }
        );
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
                                        File avatar = new File(Photobook.getMainActivity().
                                                getFilesDir(), Constants.FILENAME_AVATAR);
                                        if (avatar.exists())
                                            Photobook.getMainActivity().mDrawerAvatarImage.
                                                    setImageURI(Uri.fromFile(avatar));
                                    }
                                }, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d(LOG_TAG, "Exception " + e.getLocalizedMessage());
                    }
                    //Toast.makeText(context, "Avatar downloaded ",
                    //        Toast.LENGTH_LONG).show();
                    //((Activity) context).finish();
                }
            }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                    Log.d(LOG_TAG, "Error: " + error.getMessage());
            }
        }
        );
        VolleySingleton.getInstance(activity).addToRequestQueue(avatarRequest);
    }

	public void doRegister(final String strName, String strEmail, String strCode,
                           final Boolean boolUploadAvatar){
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "*/*");
        headers.put(Constants.KEY_CODE, strCode);
        final String strPhoneNumber = PhoneUtils.getNormalizedPhoneNumber(
                activity.phoneEditText.getText().toString());
        headers.put(Constants.KEY_NUMBER, strPhoneNumber );
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(Constants.KEY_NAME, activity.nameEditText.getText().toString() );
		params.put(Constants.KEY_EMAIL, activity.emailEditText.getText().toString() );
        params.put(Constants.KEY_PHONE, strPhoneNumber);
        Photobook.getPreferences().strContactKey = strPhoneNumber;

		final ProgressDialog pDialog = new ProgressDialog(activity);
		pDialog.setMessage(activity.getResources().getString(R.string.dialog_creating_account));
		pDialog.show();

        StringRequest registerRequest = new StringRequest(Request.Method.POST,
            Constants.SERVER_URL+"/user",
            new Gson().toJson(params), headers,
            new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    Log.d(LOG_TAG, response.toString());
                    pDialog.dismiss();
                    String strID = "";
                    Integer intPublicID = 0;
                    try {
                        JSONObject resJson = new JSONObject(response);
                        String strResult = resJson.getString(Constants.RESPONSE_RESULT);
                        if (strResult.equals(Constants.RESPONSE_RESULT_OK)) {
                            String strData = resJson.getString(Constants.RESPONSE_DATA);
                            JSONObject obj = new JSONObject(strData);
                            strID = obj.getString(Constants.KEY_ID);
                            intPublicID = obj.getInt(Constants.KEY_PUBLIC_ID);
                            Photobook.getPreferences().strUserID = strID;
                            Photobook.getPreferences().intPublicID = intPublicID;
                            Photobook.getPreferences().strUserName = strName;
                            Photobook.getPreferences().intRegisterStatus =
                                    Constants.STATUS_REGISTERED;
                            Photobook.getPreferences().strCountryCode = PhoneUtils.
                                    getCountryCode(strPhoneNumber);
                            Photobook.getPreferences().savePreferences();

                            if (boolUploadAvatar) sendAvatar();

                            if (Photobook.getFriendsFragmentTab() != null) {
                                Photobook.getFriendsFragmentTab().bookmarkAdapter.
                                        setSelectedPosition(Photobook.getFriendsFragmentTab().
                                        BOOKMARK_ID_CHANNELS);
                                Photobook.getFriendsFragmentTab().reloadContactList(null);
                            }
                            if (Photobook.getGalleryFragmentTab() != null)
                                Photobook.getGalleryFragmentTab().syncGallery();
                            Photobook.getMainActivity().mHandler.registerPushID();
                            Photobook.getMainActivity().mHandler.showChannelsDialog();
                            Photobook.getMainActivity().mHandler.syncAvatar();
                            activity.finish();
                        } else if (resJson.has(Constants.RESPONSE_CODE) &&
                                resJson.getInt(Constants.RESPONSE_CODE) == 121) {
                            Toast.makeText(activity, R.string.alert_wrong_sms,
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.d(LOG_TAG, "Json parse error");
                    }
                    if ((strID == null) || (strID.isEmpty() == true))
                        Toast.makeText(activity, "Registration failed",
                                Toast.LENGTH_LONG).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(LOG_TAG, "Error: " + error.getMessage());
                    pDialog.dismiss();
                }
            }
        );
		VolleySingleton.getInstance(activity).addToRequestQueue(registerRequest);
	}

    public void doRegister(){
        if (!validateInput()) return;
        final ProgressDialog pDialog = new ProgressDialog(activity);
		pDialog.setMessage(activity.getResources().getString(R.string.dialog_creating_account));
        pDialog.show();
        final String strPhoneNumber = PhoneUtils.getNormalizedPhoneNumber(
                activity.phoneEditText.getText().toString());
        ServerInterface.getSMSCodeRequest(
            activity, strPhoneNumber,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(LOG_TAG, response);
                    pDialog.dismiss();
                    try {
                        JSONObject obj = new JSONObject(response);
                        String strResult = obj.getString(Constants.RESPONSE_RESULT);
                        if (strResult.equals(Constants.RESPONSE_RESULT_OK)) {
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
                        Log.d(LOG_TAG, "Json parse error");
                    }
                }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(LOG_TAG, "Error: " + error.getMessage());
                pDialog.dismiss();
            }
        });
    }

    public Boolean validateInput () {
        Boolean res = true;
        if (activity.nameEditText.getText().toString().isEmpty()) {
            res = false;
            Toast.makeText(activity, R.string.alert_input_name,
                    Toast.LENGTH_LONG).show();
        } else if (activity.phoneEditText.getText().toString().isEmpty() ||
                activity.phoneEditText.getText().toString().charAt(0) != '+' ||
                !activity.phoneEditText.getText().toString().substring(1).matches("[0-9]+")) {
            res = false;
            Toast.makeText(activity, R.string.alert_input_phone,
                    Toast.LENGTH_LONG).show();
        }
        return res;
    }

    private void openEULA() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                activity.getResources().getString(R.string.link_eula)));
        activity.startActivity(browserIntent);
    }

    private void openPrivacyPolicy() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                activity.getResources().getString(R.string.link_privacy_policy)));
        activity.startActivity(browserIntent);
    }
}
