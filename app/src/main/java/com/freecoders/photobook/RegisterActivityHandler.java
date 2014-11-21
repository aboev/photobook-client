package com.freecoders.photobook;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.JsonObjectRequest;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Preferences;
import com.freecoders.photobook.network.VolleySingleton;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class RegisterActivityHandler {
	private Context context;
	
	public RegisterActivityHandler(Context context){
		this.context = context;
	}
	
	public void doRegister(String strName, String strEmail){
		
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("name", strName);
		params.put("email", strEmail);
		params.put("phone", "+82 111-2222-3333");
		
		final ProgressDialog pDialog = new ProgressDialog(context);
		pDialog.setMessage("Creating account...");
		pDialog.show();   
		
		JsonObjectRequest registerRequest = new JsonObjectRequest(Method.POST,
				Constants.SERVER_URL, new JSONObject(params),
				new Response.Listener<JSONObject>() {
		 
		                    @Override
		                    public void onResponse(JSONObject response) {
		                        Log.d(Constants.LOG_TAG, response.toString());
		                        pDialog.hide();
		                        Integer intID = 0;
								try {
									String strResult = response.getString("result");
									if (strResult.equals("OK")) {
										String strData = response.getString("data");
										JSONObject obj = new JSONObject(strData);
										intID = obj.getInt("id");
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
		                        Toast.makeText(context, "Registration success " + intID,
		                        		   Toast.LENGTH_LONG).show();
		                		Preferences prefs = new Preferences(context);
		                		prefs.strUserID = intID.toString();
		                		prefs.savePreferences();
		                        ((Activity) context).finish();
		                        
		                    }
		                }, new Response.ErrorListener() {
		 
		                    @Override
		                    public void onErrorResponse(VolleyError error) {
		                        Log.d(Constants.LOG_TAG, "Error: " + error.getMessage());
		                        pDialog.hide();
		                    }
		                }
				);
		VolleySingleton.getInstance(context).addToRequestQueue(registerRequest);
	}

}
