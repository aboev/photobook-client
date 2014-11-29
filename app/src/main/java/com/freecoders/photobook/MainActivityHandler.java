package com.freecoders.photobook;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.freecoders.photobook.common.Constants;
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
		prefs = new Preferences(activity);
		if (!prefs.loadPreferences()) {
			Intent intent = new Intent(activity, RegisterActivity.class);
		    activity.startActivity(intent);
		} else {
            refreshContactList();
        }
	}

    public void refreshContactList(){
        if (prefs.strUserID.isEmpty()) return;

        //final ArrayList<String> contactList = ContactListInterface.getContactList(activity);
        final ArrayList<String> contactList = new ArrayList<String>();
        for (int i = 111; i <= 129; i++) contactList.add(String.valueOf(i));
        for (int i = 0; i < contactList.size(); i++) {
            if (activity.friendsDataSource.getFriendByContactKey(contactList.get(i))==null) {
                activity.friendsDataSource.createFriend("", contactList.get(i), "", "",
                        FriendEntry.INT_STATUS_NULL);
            }
        }

        ServerInterface.postContactsRequest(activity,
                contactList, prefs.strUserID,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(Constants.LOG_TAG, "Response from server " + response.toString());
                        try {
                            JSONObject obj = new JSONObject(response);
                            JSONObject objMap = obj.getJSONObject("data");
                            Map<String, UserProfile> retMap =
                               new Gson().fromJson(objMap.toString(),
                                    new TypeToken<HashMap<String, UserProfile>>() {}.getType());
                            for (int i = 0; i < contactList.size(); i++){
                                if (retMap.containsKey(contactList.get(i))) {
                                   FriendEntry friend = activity.friendsDataSource.
                                           getFriendByContactKey(contactList.get(i));
                                    UserProfile profile = retMap.get(contactList.get(i));
                                    if (friend != null){

                                        friend.setName(profile.name);
                                        friend.setAvatar(profile.avatar);
                                        friend.setUserId(profile.id);
                                        friend.setStatus(FriendEntry.INT_STATUS_DEFAULT);
                                        activity.friendsDataSource.updateFriend(friend);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.d(Constants.LOG_TAG, "Exception" + e.getLocalizedMessage());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(Constants.LOG_TAG, "Error: " + error.getMessage());
                    }
                });

    }

}
