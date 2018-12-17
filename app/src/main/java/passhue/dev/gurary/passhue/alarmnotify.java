package passhue.dev.gurary.passhue;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.Scanner;


public class alarmnotify extends BroadcastReceiver {

    //Note: this must match the ID in intentservice
    private static final int PASSHUE_ALARM_ID = 233;
    public int experimentDuration = 14; // in days

    public void onReceive(Context context, Intent intent) {

        //if a device reboot invokes this method, it restarts the alarm
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            boolean experimentOver;
            experimentOver = checkDate(context);
            if (!experimentOver) {
                setAlarm(context);
            }
            //other invokers should open the main application
        }// ONE TIME NOTIFICATION FOR OLD USERS THAT THE NEW EXPERIMENT IS READY
        //NOTE: KEEPS SPAMMING PEOPLE, SOMEHOW DEFECTIVE?
        else if ("android.intent.action.MY_PACKAGE_REPLACED".equals(intent.getAction())
                || "android.intent.action.PACKAGE_CHANGED".equals(intent.getAction())
                || "android.intent.action.PACKAGE_ADDED".equals(intent.getAction()) ) {
/*
            boolean experimentOver;
            experimentOver = checkDate(context);
            if (experimentOver) {
                setAlarmOnce(context);
            }
            */

        }else if(Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())){
            /*
        //DEFECT LIKELY HERE
            if(intent.getData().getSchemeSpecificPart().equals(context.getPackageName())) {
                boolean experimentOver;
                experimentOver = checkDate(context);
                if (experimentOver) {
                    setAlarmOnce(context);
                }
            }
            */
        }
        else {
            //Before starting the main application, start the intent service
            Intent i = new Intent(context, intentservice.class);
            context.startService(i);
        }

    }

    /**
     * Starts a recurring daily notification to start the application
     */
    public void setAlarm(Context context) {
        //first write a record of the recurring alarm's start time
        Calendar c = Calendar.getInstance();
        String sDate = c.getTimeInMillis() + " "
                + c.get(Calendar.YEAR) + " "
                + c.get(Calendar.MONTH)+1
                + " " + c.get(Calendar.DAY_OF_MONTH)
                + " " + c.get(Calendar.HOUR_OF_DAY)
                + " " + c.get(Calendar.MINUTE) + "\n";
        backgroundwrite write = new backgroundwrite();
        //Appends reboot times, but it ignores them for now, just using the original start time
        //Each reboot gets a line
        //The last line is always the time of the most recent reboot or alarm startup
        write.execute("ExperimentStart.txt", sDate, "true");

        //now the set the alarm
        AlarmManager am =(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, alarmnotify.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, PASSHUE_ALARM_ID, i, PendingIntent.FLAG_UPDATE_CURRENT);
    //    Log.d("alrm", "setting recurring alarm");
        //RECURRING ALARM DISABLED IN FIREBASE VERSION
     //  am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), AlarmManager.INTERVAL_DAY, pi); // Millisec * Second * Minute * Hour
    //    am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+500, pi); //for testing

    }

    /**
     * Set a single alarm notification
     */
    public void setAlarmOnce(Context context) {
        //first write a record of the recurring alarm's start time
        Calendar c = Calendar.getInstance();
        String sDate = c.getTimeInMillis() + " "
                + c.get(Calendar.YEAR) + " "
                + c.get(Calendar.MONTH)+1
                + " " + c.get(Calendar.DAY_OF_MONTH)
                + " " + c.get(Calendar.HOUR_OF_DAY)
                + " " + c.get(Calendar.MINUTE) + "\n";
        backgroundwrite write = new backgroundwrite();
        //Appends reboot times, but it ignores them for now, just using the original start time
        //Each reboot gets a line
        //The last line is always the time of the most recent reboot or alarm startup
        write.execute("ShoulderSurfTime.txt", sDate, "true");

        //now the set the alarm
        AlarmManager am =(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, alarmnotify.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, PASSHUE_ALARM_ID, i, PendingIntent.FLAG_UPDATE_CURRENT);
      //ONE TIME ALARMS ARE STIlL USED IN FIREBASE VERSION
        am.set(AlarmManager.RTC, System.currentTimeMillis(),pi);

    }

    //returns true if the experiment has elapsed the number of days defined
    public boolean checkDate(Context context){

        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, "/PassHueData/ExperimentStart.txt");
        String line=""; //The saved calender entry is stored as one line
        long millis=0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            if ((line = br.readLine()) != null) {

            }
            br.close();
        }
        catch (IOException e) {
        }

        //Scans the line and extracts individual features
        Scanner read = new Scanner(line);
        if(read.hasNext()){
            millis=read.nextLong();
        }
        read.close();
        Calendar c = Calendar.getInstance();
        long currentMillis = c.getTimeInMillis();

        //convert the milli time into days
        long elapsedDays = (currentMillis-millis)/1000/60/60/24;
    //    long elapsedMillis = (currentMillis-millis);

        //for testing
      //  if(elapsedMillis>2000){
       //     return true;
       // }

        if(elapsedDays > experimentDuration){
             return true;
        }else {
            return false;
        }
    }

    /**
     * Cancels the recurring daily notification started with setAlarm()
     */
    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, alarmnotify.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, PASSHUE_ALARM_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);

        /* Old testing intent had an id of 0 or 23
        Intent intent1 = new Intent(context, alarmnotify.class);
        PendingIntent sender1 = PendingIntent.getBroadcast(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent intent2 = new Intent(context, alarmnotify.class);
        PendingIntent sender2 = PendingIntent.getBroadcast(context, 23, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(sender1);
        */
    }
}
