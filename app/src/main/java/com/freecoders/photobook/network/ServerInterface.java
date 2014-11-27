package com.freecoders.photobook.network;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Preferences;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Alex on 2014-11-27.
 */
public class ServerInterface {

    public static final void postContactsRequest(Context context,
        ArrayList<String> contacts, String userId,
        final Response.Listener<String> responseListener,
        final Response.ErrorListener errorListener) {
        Gson gson = new Gson();
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "*/*");
        headers.put("Content-Type", "application/json");
        headers.put("userid", userId);
        StringRequest request = new StringRequest(Request.Method.POST,
                Constants.SERVER_URL+Constants.SERVER_PATH_CONTACTS,
                gson.toJson(contacts), headers,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        responseListener.onResponse(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        errorListener.onErrorResponse(error);

                    }
                }
        );

        VolleySingleton.getInstance(context).addToRequestQueue(request);

    }
}
