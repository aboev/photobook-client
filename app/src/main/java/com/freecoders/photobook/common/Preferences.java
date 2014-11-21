package com.freecoders.photobook.common;


import android.content.Context;
import android.content.SharedPreferences;

public final class Preferences {
	private SharedPreferences settings;
	
	private String KEY_USERID = "userid";
	
	public String strUserID = "";
	
	public Preferences(Context context) {
		settings = context.getSharedPreferences(Constants.PREFS_NAME, 0);
	}
	
	public final Boolean loadPreferences(){
		strUserID = settings.getString(KEY_USERID, "");
		return strUserID.length() != 0;
	}
	
	public final void savePreferences(){
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(KEY_USERID, strUserID);
		editor.commit();
	}
	
}
