package com.freecoders.photobook.common;


import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public final class Preferences {
	private SharedPreferences settings;
	
	private String KEY_USERID = "userid";
    private String KEY_USERID_PUBLIC = "publicid";
    private String KEY_USERNAME = "username";
    private String KEY_CONTACTKEY = "contactkey";
    private String KEY_PUSH_REGID = "pushregid";
    private String KEY_COMMENTS_TIMESTAMP = "comments_timestamp"; //Last timestamp for comments request
    private String KEY_UNREAD_IMAGES = "images_unread";

    public Integer intPublicID = 0;
	public String strUserID = "";
    public String strUserName = "";
    public String strContactKey = "";
    public String strPushRegID = "";
    public String strCommentsTimestamp = "0";
    public Set<String> hsetUnreadImages = new HashSet<String>(); //Image IDs that have unread comments
	
	public Preferences(Context context) {
		settings = context.getSharedPreferences(Constants.PREFS_NAME, 0);
	}
	
	public final Boolean loadPreferences(){
        intPublicID = settings.getInt(KEY_USERID_PUBLIC, 0);
		strUserID = settings.getString(KEY_USERID, "");
        strUserName = settings.getString(KEY_USERNAME, "");
        strContactKey = settings.getString(KEY_CONTACTKEY, "");
        strPushRegID = settings.getString(KEY_PUSH_REGID, "");
        strCommentsTimestamp = settings.getString(KEY_COMMENTS_TIMESTAMP, "0");
        hsetUnreadImages = settings.getStringSet(KEY_UNREAD_IMAGES, new HashSet<String>());
		return strUserID.length() != 0;
	}
	
	public final void savePreferences(){
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(KEY_USERID, strUserID);
        editor.putString(KEY_USERNAME, strUserName);
        editor.putString(KEY_CONTACTKEY, strContactKey);
        editor.putInt(KEY_USERID_PUBLIC, intPublicID);
        editor.putString(KEY_PUSH_REGID, strPushRegID);
        editor.putString(KEY_COMMENTS_TIMESTAMP, strCommentsTimestamp);
        editor.putStringSet(KEY_UNREAD_IMAGES, hsetUnreadImages);
		editor.commit();
	}
	
}
