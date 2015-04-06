package com.freecoders.photobook.common;


import android.content.Context;
import android.content.SharedPreferences;

import com.freecoders.photobook.R;
import com.freecoders.photobook.gson.CommentEntryJson;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public final class Preferences {
	private SharedPreferences settings;
    private Context context;

    Gson gson = new Gson();
	
	private String KEY_USERID = "userid";
    private String KEY_USERID_PUBLIC = "publicid";
    private String KEY_USERNAME = "username";
    private String KEY_CONTACTKEY = "contactkey";
    private String KEY_COUNTRY_CODE = "country_code_iso";
    private String KEY_PHONE = "phone";
    private String KEY_EMAIL = "email";
    private String KEY_PUSH_REGID = "pushregid";
    private String KEY_COMMENTS_TIMESTAMP = "comments_timestamp"; //Last timestamp for comments request
    private String KEY_UNREAD_IMAGES = "images_unread";
    private String KEY_UNREAD_IMAGES2 = "images_unread2";
    private String KEY_REGISTER_STATUS = "status";
    private String KEY_SERVER_INFO = "server_info";

    public Integer intPublicID = 0;
	public String strUserID = "";
    public String strUserName = "";
    public String strContactKey = "";
    public String strCountryCode = "";
    public String strPhone = "";
    public String strEmail = "";
    public String strPushRegID = "";
    public String strCommentsTimestamp = "0";
    public String strServerInfo = "";
    public Set<String> hsetUnreadImages = new HashSet<String>(); //Image IDs that have unread comments
    public HashMap<String, Integer> unreadImagesMap =
            new HashMap<String, Integer>(); //Image IDs that have unread comments
    public Integer intRegisterStatus = 0;   // 0 - not registered, 
            // 1 - waiting for sms code, 2 registered
	
	public Preferences(Context context) {
		settings = context.getSharedPreferences(Constants.PREFS_NAME, 0);
        this.context = context;
	}
	
	public final Boolean loadPreferences(){
        intPublicID = settings.getInt(KEY_USERID_PUBLIC, 0);
		strUserID = settings.getString(KEY_USERID, "");
        strUserName = settings.getString(KEY_USERNAME, "");
        strContactKey = settings.getString(KEY_CONTACTKEY, "");
        strCountryCode = settings.getString(KEY_COUNTRY_CODE,
                context.getResources().getString(R.string.default_country_code_iso));
        strPhone = settings.getString(KEY_PHONE, "");
        strEmail = settings.getString(KEY_EMAIL, "");
        strPushRegID = settings.getString(KEY_PUSH_REGID, "");
        strCommentsTimestamp = settings.getString(KEY_COMMENTS_TIMESTAMP, "0");
        strServerInfo = settings.getString(KEY_SERVER_INFO, "");
        hsetUnreadImages = settings.getStringSet(KEY_UNREAD_IMAGES, new HashSet<String>());
        Type type = new TypeToken<HashMap<String, Integer>>(){}.getType();
        unreadImagesMap = gson.fromJson(
                settings.getString(KEY_UNREAD_IMAGES2, "{}"), type);
        intRegisterStatus = settings.getInt(KEY_REGISTER_STATUS, 0);
		return strUserID.length() != 0;
	}
	
	public final void savePreferences(){
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(KEY_USERID, strUserID);
        editor.putString(KEY_USERNAME, strUserName);
        editor.putString(KEY_CONTACTKEY, strContactKey);
        editor.putString(KEY_COUNTRY_CODE, strCountryCode);
        editor.putString(KEY_PHONE, strPhone);
        editor.putString(KEY_EMAIL, strEmail);
        editor.putInt(KEY_USERID_PUBLIC, intPublicID);
        editor.putString(KEY_PUSH_REGID, strPushRegID);
        editor.putString(KEY_COMMENTS_TIMESTAMP, strCommentsTimestamp);
        editor.putStringSet(KEY_UNREAD_IMAGES, hsetUnreadImages);
        editor.putString(KEY_UNREAD_IMAGES2, gson.toJson(unreadImagesMap));
        editor.putInt(KEY_REGISTER_STATUS, intRegisterStatus);
        editor.putString(KEY_SERVER_INFO, strServerInfo);
		editor.commit();
	}
	
}
