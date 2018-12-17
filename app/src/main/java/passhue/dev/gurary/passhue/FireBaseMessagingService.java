package passhue.dev.gurary.passhue;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static android.content.ContentValues.TAG;

public class FireBaseMessagingService extends FirebaseMessagingService {

    private static final int PASSHUE_ALARM_ID = 233;
    public int experimentDuration = 14; // in days

    public FireBaseMessagingService() {
        super();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {

            //THERE IS NO REASON ON GOD'S GREEN EARTH THIS SHOULD BE REQUIRED TO SEND A NOTIFICATION
            //TO ANDROID <7.0 BUT IT IS GODDAMIT

            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                Intent intent = new Intent(this, LoginPage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT);


            //This will not over-ride the firebase notification settings.
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.passhue_launcher)
                        .setContentTitle("PassHue Time!")
                        .setContentText("Please enter your Passhue.")
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.notify(PASSHUE_ALARM_ID , notificationBuilder.build());
            }

            //THIS IS REQUIRED FOR 9.0+ COMPLIANCE, POSSIBLE FOR 8.1 AS WELL TODO test on 8.1
               showNotification();

            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

    public void showNotification(){
        //builds a notification to display in then notifications bar
        Notification.Builder builder = new Notification.Builder(this);
        //TODO different message during guessing application?
        builder.setContentTitle("PassHue Time");
        builder.setContentText("When you have a minute, please enter your PassHue.");
        builder.setSmallIcon(R.mipmap.passhue_launcher);
        builder.setAutoCancel(true);

        Intent notifyIntent = new Intent(this, LoginPage.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, PASSHUE_ALARM_ID, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Launches the application from the notification
        builder.setContentIntent(pendingIntent);

        Notification notificationCompat = builder.build();

        //This smoldering pile of s*** is required on Android 8.0+ but is not backwards compatible
        //It does literally nothing but notifications will not appear without it
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel("passhue.dev.gurary.passhue.ONE",
                    "PassHue", NotificationManager.IMPORTANCE_DEFAULT);
            builder.setChannelId("passhue.dev.gurary.passhue.ONE");
            mChannel.setShowBadge(true);
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(mChannel);
            notificationManager.notify(PASSHUE_ALARM_ID, notificationCompat);
        }else {
            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
            managerCompat.notify(PASSHUE_ALARM_ID, notificationCompat);
        }
    }


    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d(TAG, "Refreshed token: " + s);
    }
}
