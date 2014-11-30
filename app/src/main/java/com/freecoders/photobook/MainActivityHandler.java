package com.freecoders.photobook;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.common.Preferences;
import com.freecoders.photobook.db.ContactListInterface;
import com.freecoders.photobook.db.FriendEntry;
import com.freecoders.photobook.gson.UserProfile;
import com.freecoders.photobook.network.ServerInterface;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivityHandler {
	private Preferences prefs;
    private MainActivity activity;
	
	public void init(MainActivity activity) {
        this.activity = activity;
        Photobook.setMainActivity(activity);
        prefs = new Preferences(activity);
		if (!prefs.loadPreferences()) {
			Intent intent = new Intent(activity, RegisterActivity.class);
		    activity.startActivity(intent);
		}
	}

}
