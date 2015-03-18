package com.freecoders.photobook.common;


import android.content.Context;
import android.content.SharedPreferences;

public final class Preferences {
	private SharedPreferences settings;
	
	private String KEY_USERID = "userid";
    private String KEY_USERID_PUBLIC = "publicid";
    private String KEY_USERNAME = "username";
    private String KEY_CONTACTKEY = "contactkey";
    private String KEY_PUSH_REGID = "pushregid";

    public Integer intPublicID = 0;
	public String strUserID = "";
    public String strUserName = "";
    public String strContactKey = "";
    public String strPushRegID = "";
	
	public Preferences(Context context) {
		settings = context.getSharedPreferences(Constants.PREFS_NAME, 0);
	}
	
	public final Boolean loadPreferences(){
        intPublicID = settings.getInt(KEY_USERID_PUBLIC, 0);
		strUserID = settings.getString(KEY_USERID, "");
        strUserName = settings.getString(KEY_USERNAME, "");
        strContactKey = settings.getString(KEY_CONTACTKEY, "");
        strPushRegID = settings.getString(KEY_PUSH_REGID, "");
		return strUserID.length() != 0;
	}
	
	public final void savePreferences(){
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(KEY_USERID, strUserID);
        editor.putString(KEY_USERNAME, strUserName);
        editor.putString(KEY_CONTACTKEY, strContactKey);
        editor.putInt(KEY_USERID_PUBLIC, intPublicID);
        editor.putString(KEY_PUSH_REGID, strPushRegID);
		editor.commit();
	}
	
}
