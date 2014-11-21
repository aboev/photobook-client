package com.freecoders.photobook;

import com.freecoders.photobook.common.Preferences;

import android.content.Context;
import android.content.Intent;

public class MainActivityHandler {
	private Preferences prefs;
	
	public void init(Context context) {
		prefs = new Preferences(context);
		if (!prefs.loadPreferences()) {
			Intent intent = new Intent(context, RegisterActivity.class);
		    context.startActivity(intent);
		}
	}
}
