package com.freecoders.photobook.utils;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import com.freecoders.photobook.classes.CallbackInterface;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;

public class Permission {
    public static void requestPermissions(CallbackInterface callback) {
        String[] permissions = {android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WAKE_LOCK};
        int res = Photobook.getMainActivity().checkCallingOrSelfPermission(permissions[0]);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (res != PackageManager.PERMISSION_GRANTED)
                Photobook.getMainActivity().requestPermissions(permissions, 1);

        if (callback != null) {
            if (res == PackageManager.PERMISSION_GRANTED) {
                callback.onResponse(null);
            } else {
                Photobook.getPermissionHandlers().put(1, callback);
            }
        }
    }
}
