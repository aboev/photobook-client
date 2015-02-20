package com.freecoders.photobook.common;


import android.content.Context;
import android.content.SharedPreferences;

public final class Preferences {
	private SharedPreferences settings;
	
	private String KEY_USERID = "userid";
    private String KEY_USERNAME = "username";
    private String KEY_CONTACTKEY = "contactkey";
	
	public String strUserID = "";
    public String strUserName = "";
    public String strContactKey = "";
	
	public Preferences(Context context) {
		settings = context.getSharedPreferences(Constants.PREFS_NAME, 0);
	}
	
	public final Boolean loadPreferences(){
		strUserID = settings.getString(KEY_USERID, "");
        strUserName = settings.getString(KEY_USERNAME, "");
        strContactKey = settings.getString(KEY_CONTACTKEY, "");
		return strUserID.length() != 0;
	}
	
	public final void savePreferences(){
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(KEY_USERID, strUserID);
        editor.putString(KEY_USERNAME, strUserName);
        editor.putString(KEY_CONTACTKEY, strContactKey);
		editor.commit();
	}
	
}
