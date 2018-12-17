package passhue.dev.gurary.passhue;

import android.app.IntentService;
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


/**
 * Created by Commander Fish on 7/11/2017.
 */

public class intentservice extends IntentService {

    //Note: this must match the ID in alarmnotify
    private static final int PASSHUE_ALARM_ID = 233;

    public intentservice() {
        super("intentservice");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
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
}
