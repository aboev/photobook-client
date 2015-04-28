package com.freecoders.photobook.gcm;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.freecoders.photobook.ImageDetailsActivity;
import com.freecoders.photobook.MainActivity;
import com.freecoders.photobook.R;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.gson.ImageJson;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Alex on 2015-03-08.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    private static String LOG_TAG = "GcmBroadcastReceiver";
    private Gson gson = new Gson();
    
    public static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        String messageType = gcm.getMessageType(intent);
        if (!extras.isEmpty()) {
            Log.d(LOG_TAG, "Received push message type " + messageType);
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Log.d(LOG_TAG, "Received push message " + extras.toString());
                if (extras.containsKey("event") && extras.containsKey("msg")) {
                   Integer intEventType = Integer.valueOf(extras.getString("event"));
                   String strData = extras.getString("msg");
                   showNotification(context, intEventType, strData);
                }
            }
        }
        setResultCode(Activity.RESULT_OK);
    }

    public void showNotification(Context context, Integer intEventType, String strData) {
        String strNotificationMessage = "";
        try {
            Log.d(LOG_TAG, "Parsing " + strData);
            JSONObject resJson = new JSONObject(strData);
            String strAuthor = resJson.getString("author").replaceAll("\\\"", "\"");
            JSONObject author = new JSONObject(strAuthor);
            Log.d(LOG_TAG, "Author =  " + strAuthor);
            if (intEventType == Constants.EVENT_NEW_COMMENT) {
                String strText = resJson.getString(Constants.KEY_TEXT);
                strNotificationMessage = strText;
                if (author.has(Constants.KEY_NAME))
                    strNotificationMessage = author.getString(Constants.KEY_NAME) + ": " + strNotificationMessage;
            } else if (intEventType == Constants.EVENT_NEW_IMAGE) {
                String strImage = resJson.getString("image").replaceAll("\\\"", "\"");
                ImageJson image = (ImageJson) gson.fromJson(strImage, ImageJson.class);
                strNotificationMessage = image.title;
            }
        } catch (JSONException e) {
            Log.d(LOG_TAG, "JSON parsing error");
        }

        String strNotificationTitle = "";
        if (intEventType == Constants.EVENT_NEW_COMMENT)
            strNotificationTitle = context.getResources().
                    getString(R.string.notification_new_comment);
        else if (intEventType == Constants.EVENT_NEW_IMAGE)
            strNotificationTitle = context.getResources().
                    getString(R.string.notification_new_image);

        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent i = new Intent(context, MainActivity.class);
        i.putExtra("event_type", intEventType);
        i.putExtra("data", strData);
        i.addFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP );
        i.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(strNotificationTitle)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(strNotificationMessage))
                        .setContentText(strNotificationMessage)
                        .setAutoCancel(true)
                        .setVibrate(new long[] { 200, 500, 200 })
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}

