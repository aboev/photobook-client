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
                Log.d(Constants.LOG_TAG, "Received push message " + extras.toString());
                if (extras.containsKey("event") && extras.containsKey("msg")) {
                   Integer intEventType = extras.getInt("event");
                   String strData = extras.getString("msg");
                   showNotification(context, intEventType, strData);
                }
            }
        }
        setResultCode(Activity.RESULT_OK);
    }

    public void showNotification(Context context, Integer intEventType, String strData) {
        String strMessage = "";
        try {
            Log.d(Constants.LOG_TAG, "Parsing " + strData);
            JSONObject resJson = new JSONObject(strData);
            String strAuthor = resJson.getString("author").replaceAll("\\\"", "\"");
            JSONObject author = new JSONObject(strAuthor);
            Log.d(Constants.LOG_TAG, "Author =  " + strAuthor);
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
        i.addFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP );
        i.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(context.getResources().
                                getString(R.string.notification_new_comment))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(strMessage))
                        .setContentText(strMessage)
                        .setAutoCancel(true)
                        .setVibrate(new long[] { 200, 500, 200 })
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
