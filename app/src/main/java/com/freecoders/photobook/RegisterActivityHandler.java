package com.freecoders.photobook;

import java.io.File;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

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
import com.freecoders.photobook.network.VolleySingleton;
import com.freecoders.photobook.utils.PhoneUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class RegisterActivityHandler {
	private Context context;
	
	public RegisterActivityHandler(Context context){
		this.context = context;
	}

    public void sendAvatar(){
        File avatarImage = new File(context.getFilesDir(), Constants.FILENAME_AVATAR);
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
                            ServerInterface.updateProfileRequest(context, profile,
                                    strUserID, null, null);
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
        VolleySingleton.getInstance(context).addToRequestQueue(avatarRequest);
    }

	public void doRegister(String strName, String strEmail, final Boolean boolUploadAvatar){
		
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("name", strName);
		params.put("email", strEmail);
        String strPhoneNumber = PhoneUtils.getPhoneNumber();
        params.put("phone", strPhoneNumber);
		
		final ProgressDialog pDialog = new ProgressDialog(context);
		pDialog.setMessage("Creating account...");
		pDialog.show();   
		
		JsonObjectRequest registerRequest = new JsonObjectRequest(Method.POST,
				Constants.SERVER_URL+"/user", new JSONObject(params),
				new Response.Listener<JSONObject>() {
		 
		                    @Override
		                    public void onResponse(JSONObject response) {
		                        Log.d(Constants.LOG_TAG, response.toString());
		                        pDialog.dismiss();
		                        String strID = "";
								try {
									String strResult = response.getString("result");
									if (strResult.equals("OK")) {
										String strData = response.getString("data");
										JSONObject obj = new JSONObject(strData);
										strID = obj.getString("id");
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
                                if ((strID == null) || (strID.isEmpty() == true))
		                            Toast.makeText(context, "Registration failed",
		                        		   Toast.LENGTH_LONG).show();
                                Photobook.getPreferences().strUserID = strID;
                                Photobook.getPreferences().savePreferences();

                                if (boolUploadAvatar) sendAvatar();

                                if (Photobook.getFriendsFragmentTab() != null)
                                    Photobook.getFriendsFragmentTab().refreshContactList();
		                        ((Activity) context).finish();
                            }
		                }, new Response.ErrorListener() {
		 
		                    @Override
		                    public void onErrorResponse(VolleyError error) {
		                        Log.d(Constants.LOG_TAG, "Error: " + error.getMessage());
                                pDialog.dismiss();
		                    }
		                }
				);
		VolleySingleton.getInstance(context).addToRequestQueue(registerRequest);
	}

}
