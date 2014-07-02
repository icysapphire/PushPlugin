package com.plugin.gcm;

import java.util.List;

import com.google.android.gcm.GCMBaseIntentService;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.IOException;

@SuppressLint("NewApi")
public class GCMIntentService extends GCMBaseIntentService {

public static final int NOTIFICATION_ID = 237;
private static final String TAG = "GCMIntentService";

public GCMIntentService() {
super("GCMIntentService");
}

@Override
public void onRegistered(Context context, String regId) {

Log.v(TAG, "onRegistered: "+ regId);

JSONObject json;

try
{
json = new JSONObject().put("event", "registered");
json.put("regid", regId);

Log.v(TAG, "onRegistered: " + json.toString());

// Send this JSON data to the JavaScript application above EVENT should be set to the msg type
// In this case this is the registration ID
PushPlugin.sendJavascript( json, "" );

}
catch( JSONException e)
{
// No message to the user is sent, JSON failed
Log.e(TAG, "onRegistered: JSON exception");
}
}

@Override
public void onUnregistered(Context context, String regId) {
Log.d(TAG, "onUnregistered - regId: " + regId);
}

@Override
protected void onMessage(Context context, Intent intent) {
Log.d(TAG, "onMessage - context: " + context);

// Extract the payload from the message
Bundle extras = intent.getExtras();
if (extras != null)
{
           Log.v(TAG, "TargetPage: " + extras.getString("targetPage"));
// if we are in the foreground, just surface the payload, else post it to the statusbar
            if (PushPlugin.isInForeground() && PushPlugin.getPage().equals(extras.getString("targetPage"))) {
extras.putBoolean("foreground", true);
                PushPlugin.sendExtras(extras, "");
}
else {
extras.putBoolean("foreground", false);

                // Send a notification if there is a message
                PushPlugin.sendExtras(extras, "");
                if (extras.getString("message") != null && extras.getString("message").length() != 0) {
                 if(extras.getString("message").equals("cancelAll")) {
                 cancelNotification(context); }
                 else { createNotification(context, extras); }
                }
            }
        }
}

public void createNotification(Context context, Bundle extras)
{
NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
String appName = getAppName(this);

Intent notificationIntent = new Intent(this, PushHandlerActivity.class);
notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
notificationIntent.putExtra("pushBundle", extras);

PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
String userImage = extras.getString("large_icon");

long[] pattern = {0, 500, 100, 500, 100};
NotificationCompat.Builder mBuilder =
new NotificationCompat.Builder(context)
.setDefaults(Notification.DEFAULT_ALL)
.setSmallIcon(context.getApplicationInfo().icon)
.setWhen(System.currentTimeMillis())
.setContentTitle(extras.getString("title"))
.setTicker(extras.getString("title"))
.setContentIntent(contentIntent);
if(extras.getString("vibrate").equals("pattern")) mBuilder.setVibrate(pattern);
if(extras.getString("large_icon")!=null)   {Bitmap bitmap = getBitmapFromURL(userImage);
mBuilder.setLargeIcon(bitmap);}
String message = extras.getString("message");
if (message != null) {
mBuilder.setContentText(message);
} else {
mBuilder.setContentText("<missing message content>");
}

String msgcnt = extras.getString("msgcnt");
if (msgcnt != null) {
mBuilder.setNumber(Integer.parseInt(msgcnt));
}

mNotificationManager.notify((String) appName, NOTIFICATION_ID, mBuilder.build());
}

public static void cancelNotification(Context context)
{
NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
mNotificationManager.cancel((String)getAppName(context), NOTIFICATION_ID);	
}

private static String getAppName(Context context)
{
CharSequence appName =
context
.getPackageManager()
.getApplicationLabel(context.getApplicationInfo());

return (String)appName;
}

@Override
public void onError(Context context, String errorId) {
Log.e(TAG, "onError - errorId: " + errorId);
}

public Bitmap getBitmapFromURL(String strURL) {

try {

URL url = new URL(strURL);

HttpURLConnection connection = (HttpURLConnection) url.openConnection();
connection.setDoInput(true);
connection.connect();
InputStream input = connection.getInputStream();
Bitmap myBitmap = BitmapFactory.decodeStream(input);
return myBitmap;
} catch (IOException e) {
 e.printStackTrace();
 return null;
}
}

}

