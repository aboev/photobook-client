package com.freecoders.photobook.gcm;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.freecoders.photobook.ImageDetailsActivity;
import com.freecoders.photobook.MainActivity;
import com.freecoders.photobook.R;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Alex on 2015-03-08.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    public static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        String messageType = gcm.getMessageType(intent);
        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                try {
                    JSONObject resJson = new JSONObject(extras.toString());
                    Log.d(Constants.LOG_TAG, "Received push message " + extras.toString());
                    if (resJson.has("event") && resJson.has("msg")) {
                       Integer intEventType = resJson.getInt("event");
                       String strData = resJson.getString("msg");
                       showNotification(context, intEventType, strData);
                    }
                } catch (JSONException e) {
                    Log.d(Constants.LOG_TAG, "JSON parsing error");
                }
            }
        }
        setResultCode(Activity.RESULT_OK);
    }

    public void showNotification(Context context, Integer intEventType, String strData) {
        String strMessage = "";
        try {
            JSONObject resJson = new JSONObject(strData);
            JSONObject author = resJson.getJSONObject("author");
            String strText = resJson.getString("text");
            strMessage = strText;
            if (author.has("name"))
                strMessage = author.getString("name") + ": " + strMessage;
        } catch (JSONException e) {
            Log.d(Constants.LOG_TAG, "JSON parsing error");
        }

        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent i = new Intent(context, MainActivity.class);
        i.putExtra("event_type", intEventType);
        i.putExtra("data", strData);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Photobook")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(strMessage))
                        .setContentText(strMessage)
                        .setAutoCancel(true);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
