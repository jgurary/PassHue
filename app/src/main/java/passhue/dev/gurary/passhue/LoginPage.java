package passhue.dev.gurary.passhue;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileReader;
import android.os.Environment;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.Calendar;
import java.util.Random;

import android.support.annotation.ColorInt;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.madrapps.eyedropper.EyeDropper;

public class LoginPage extends AppCompatActivity {

    public int[] currentPassword = new int[4]; //password currently being entered
    public boolean[] latch = new boolean[4]; //sets to true when a color was entered in that slot
    public int[] loadedPassword = new int[4]; //password loaded from memory
    public boolean PWisLoaded; //was the password loaded from memory successfully
    public String userID; //loaded from memory, the users's initial ID stamp
    public String timeStamps; // Initialization, first touch, second..., last touch
    AlertDialog alertDialog; //pops up when the user gets the pass right to say they are done
    ImageView display; //this is the colored wheel thing

    private static final int REQUEST_WRITE_STORAGE = 112; //write storage constant, I don't know why
    private static final int maxDiff = 100; //the tolerance that users can be off by for color formula

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( this,  new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                Log.e("newToken",newToken);
            }
        });

        //Check for read/write permission on shitty new SDK 23+ (v6.0+)
        //Should be backwards compatible but who knows really.
      //  if(Build.VERSION.SDK_INT > 22){
        if (ContextCompat.checkSelfPermission(LoginPage.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                      ActivityCompat.requestPermissions(LoginPage.this,
                         new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_STORAGE);
            //Initializes below in the "onpermissionsgranted" method
        }else {
            Initialize();
        }

        //Test code for OLD AND BUSTED encryption/decryption

        /*
        String enctext="C9E02C201A47C6763636E3623EE72E72558991E20101F7BCFA758A0CA2927EBC69B33D15B2069017B31206F31908A652F3BB2192B18F7D89C5FAD092C1A0F17BC943A0A72B95E1079973C28C7B157172B5FDF75ECF646799262BDD8039B2D8287ED646B5F1CE4057A05C204A6F97D41F";
        String dectext;
        AESHelper helper = new AESHelper();
        dectext = helper.decrypt(enctext);
        Log.d("dec:", dectext);
        */


      //  enctext=helper.encrypt("blahblahblah");
      //  dectext=helper.decrypt(enctext);
      //  Log.d("dec2:", dectext);
       // }

        //Test code for NEW HOTNESS encryption

/*
        String enctext = "";
        AESImplementation helper = new AESImplementation();

        enctext=helper.encrypt("SANDWHICHESAREGREAT!!", helper.getKey());
        Log.d("enc1:", enctext);

        String decrypted="";
        //Passes the previously read byte array along for encryption
        decrypted = helper.decrypt(enctext, helper.getKey());
        Log.d("dec1:", decrypted);
*/


    }

    /**
     * Runs on creation, invokes all the other initialization methods
     */
    private void Initialize(){

        checkID();
        if(checkFirstRun()) {
            checkPassword();
        }

        buildNotificationChannel();

        //Titles the alert box that will pop up if the user gets the password right
        alertDialog = new AlertDialog.Builder(this).create();

        //Initialization timestamp
        timeStamps=Long.toString(System.currentTimeMillis())+", ";

        //certain users will use the rotating verison, picked at random based on userID
        char lastNum=userID.charAt(userID.length()-1);
        if(lastNum=='1' || lastNum == '4' ||lastNum == '5' ||lastNum=='8' ||lastNum == '0') {
            display = (ImageView) findViewById(R.id.targetview);
            int randomRot;
            Random rand = new Random();
            randomRot=rand.nextInt(360);
            display.setRotation((float) randomRot);
        }

        //Start listening for touch inputs
        setUp();

    }

    /**
     * On Android 8.0+, a notification channel is required for no f***ing reason.
     * Firebase notifications will not come through unless this channel exists.
     * Supposedly re-creating this does nothing so it can be done at the start with no issue.
     */
    private void buildNotificationChannel(){

        //This smoldering pile of s*** is required on Android 8.0+ but is not backwards compatible
        //It does literally nothing but notifications will not appear without it
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel("passhue.dev.gurary.passhue.ONE",
                    "PassHue", NotificationManager.IMPORTANCE_DEFAULT);
            mChannel.setShowBadge(true);
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(mChannel);
        }
    }


    /**
     * Checks to see if terms of use have been accepted, if not, sends to terms activity
     * Checks to see if demographics have been filled out, if not, sends to demo activity
     * (Completion of these activities is marked by files in PassHue directory)
     */
    private boolean checkFirstRun() {
        //Check for ""terms accepted" file
        File sdcard = Environment.getExternalStorageDirectory();
        File file;
        file = new File(sdcard, "/PassHueData/AcceptedTerms.txt");
        if (!file.exists())
        {
            Intent myIntent = new Intent(getApplicationContext(), terms.class);
            myIntent.putExtra("params0", userID); //Optional parameters
            startActivity(myIntent);
            return false;
        }else {
            //terms exist
        }
        file = new File(sdcard, "/PassHueData/Demographics.txt");
        if (!file.exists())
        {
            Intent myIntent = new Intent(getApplicationContext(), demographic.class);
            myIntent.putExtra("params0", userID); //Optional parameters
            startActivity(myIntent);
            return false;
        }else {
        //Set the experiment notification alarm if the user has completed the demographic portion, at least
            file = new File(sdcard, "/PassHueData/ExperimentStart.txt");
            alarmnotify alarm = new alarmnotify();
            if (!file.exists()) {
                //Set the recurring notification alarms for later, save the start date/time
                //  alarm.cancelAlarm(this);
                alarm.setAlarm(this);
            }else{
                //leads to the survey page and terminates the experiment when alarm file is 14 days old
                boolean experimentOver;
                experimentOver = alarm.checkDate(this);
                if(experimentOver){
                    alarm.cancelAlarm(this);

                    //if the survey is filled out, go to the Guesser, otherwise go to the survey
                    File endfile = new File(sdcard, "/PassHueData/EndExperiment.txt");
                    if (!endfile.exists()) {
                        Intent myIntent = new Intent(getApplicationContext(), endexperiment.class);
                        myIntent.putExtra("params0", userID); //Optional parameters
                        startActivity(myIntent);
                    }else {
                        Intent myIntent = new Intent(getApplicationContext(), Guesser.class);
                        myIntent.putExtra("params0", userID); //Optional parameters
                        startActivity(myIntent);
                    }
                    return false;
                }else{
                    //do nothing, continue as usual
                }
            }
        }

        return true;
    }

    /**
     * Check if a user ID file exists, if not, generates/saves one from current system time
     * This methods loads the ID file into userID if it exists
     */
    private void checkID() {

    //Check for username stamp file, stored in local memory
    File sdcard = Environment.getExternalStorageDirectory();
    File file = new File(sdcard, "/PassHueData/userID.txt");
    //if the username stamp file does not exist, generate it using a system timestamp
    if (!file.exists())
    {
       //     Toast toast = Toast.makeText(getApplicationContext(), "Welcome, please set a new password", Toast.LENGTH_LONG);
       //     toast.show();
            String ID = Long.toString(System.currentTimeMillis());
            userID = ID;
            backgroundwrite write = new backgroundwrite();
            write.execute("userID.txt", ID, "false");
        setTitle("Initialization");
        return;
    }else{
        //if the username stamp file does exist, load the userID into memory
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            if ((line = br.readLine()) != null) {
                userID=line;
            }
            br.close();
        }
        catch (IOException e) {
            Toast toast = Toast.makeText(getApplicationContext(), "Error reading device memory", Toast.LENGTH_SHORT);
            toast.show();
        }
    } //end else if file doesn't exist
}

    /**
    * Tests if a password file exists. If not, send the user to the passwordsetup activity
    * If yes, invoke method to load the password from memory and set the PW loaded flag
    **/
    public void checkPassword(){
    //See if a password file exists, and if so, load it
    File sdcard = Environment.getExternalStorageDirectory();
    File PWfile = new File(sdcard, "/PassHueData/" + userID + "password.txt");
    File PWveriffile = new File(sdcard, "/PassHueData/" + "passwordVerified.txt");
    if (!PWfile.exists() || !PWveriffile.exists()) {
        PWisLoaded=false;
        Intent myIntent = new Intent(getApplicationContext(), passwordsetup.class);
        myIntent.putExtra("params0", userID); //Optional parameters
        startActivity(myIntent);
    }else{
        setTitle("PassHue - Unlock");
        loadPassword();
        PWisLoaded=true;
    }
}

    /**
     * Eyedropper control methods, handles picking colors with the eyedropper and saving picked
     * colors to the password array. When all 4 colors are entered, invokes testPassword
     */
    private void setUp() {
        final View targetView = findViewById(R.id.targetview);

        final EyeDropper eyeDropper = new EyeDropper(targetView, new EyeDropper.ColorSelectionListener() {
            @Override
            public void onColorSelected(@ColorInt int color) {
                Button bg;
                if(color!=0) { //prevents people from picking white using the outside of the wheel
                    if (latch[0] == false) {
                        bg = (Button) findViewById(R.id.bg1);
                        currentPassword[0] = color;
                    } else if (latch[1] == false) {
                        bg = (Button) findViewById(R.id.bg2);
                        currentPassword[1] = color;
                    } else if (latch[2] == false) {
                        bg = (Button) findViewById(R.id.bg3);
                        currentPassword[2] = color;
                    } else {
                        bg = (Button) findViewById(R.id.bg4);
                        currentPassword[3] = color;
                    }
                    bg.setBackgroundColor(color);
                }
            }
        });

        eyeDropper.setSelectionListener(new EyeDropper.SelectionListener() {
            @Override
            public void onSelectionStart(MotionEvent event) {
                Button bg;
                @ColorInt int defaultColor;
                defaultColor = ContextCompat.getColor(getApplicationContext(), android.R.color.white);

                     //Resets password and resets colorboxes (use in case of incorrect PW entry)
                if(latch[0]==true && latch[1]==true && latch[2]==true && latch[3]==true){
                    for(int i=0; i<4; i++){
                        currentPassword[i]=0;
                        latch[i]=false;
                    }
                    bg = (Button) findViewById(R.id.bg1);
                    bg.setBackgroundColor(defaultColor);
                    bg = (Button) findViewById(R.id.bg2);
                    bg.setBackgroundColor(defaultColor);
                    bg = (Button) findViewById(R.id.bg3);
                    bg.setBackgroundColor(defaultColor);
                    bg = (Button) findViewById(R.id.bg4);
                    bg.setBackgroundColor(defaultColor);
                    //signifies a second attempt following initialization, so initial time doesn't matter, just read previous end time
                    timeStamps="NA, ";
                }

            }

            @Override
            public void onSelectionEnd(MotionEvent event) {
                //TODO sometimes more than 5 stamps are somehow generated, find out how this occurs. Maybe very fast touches?
                timeStamps+=Long.toString(System.currentTimeMillis())+ ", ";
                //If a color has been entered, latch it in when the user releases the press
            if(currentPassword[0]!=0){
                latch[0]=true;}
            if(currentPassword[1]!=0){
                latch[1]=true;}
            if(currentPassword[2]!=0){
                latch[2]=true;}
            if(currentPassword[3]!=0){
                latch[3]=true;}

            if(latch[0]==true && latch[1]==true && latch[2]==true && latch[3]==true) {
                if(PWisLoaded) {
                    testPassword();
                }else{
                    //Should be unreachable
                }
            }
            }
        });
    }

    /**
     * Tests the current password against the loaded password.
     * Sends result to server with relevant information
     * This data is not encrypted.
     */
    public void testPassword(){
        double[] difference = new double[4];
        double netdifference=0;
        String differences="";
        boolean passed=false;
        String RGBraw="";

        for(int i=0; i<4; i++) {
            //Gets the alpha and RBG color values from the color int
            int AC = (currentPassword[i] >> 24) & 0xff; // or color >>> 24
            int RC = (currentPassword[i] >> 16) & 0xff;
            int GC = (currentPassword[i] >> 8) & 0xff;
            int BC = (currentPassword[i]) & 0xff;
            RGBraw+= Integer.toString(RC) + ", " + Integer.toString(GC) + ", " + Integer.toString(BC)+ "; ";

            int AL = (loadedPassword[i] >> 24) & 0xff; // or color >>> 24
            int RL = (loadedPassword[i] >> 16) & 0xff;
            int GL = (loadedPassword[i] >> 8) & 0xff;
            int BL = (loadedPassword[i]) & 0xff;

            long redMean = (RC+RL)/2;
            long redDiff = RC-RL;
            long blueDiff = BC-BL;
            long greenDiff = GC-GL;
            //Test color similarity using CompuPhase Algorithm
            double redCalc = ((512+redMean)*redDiff*redDiff)>>8;
            double greenCalc = 4*greenDiff*greenDiff;
            double blueCalc = ((767-redMean)*blueDiff*blueDiff)>>8;

            difference[i] = Math.sqrt( redCalc+ greenCalc + blueCalc );
            differences += Integer.toString(((int)difference[i])) + ", ";
            netdifference += difference[i];
        }

        if(difference[0] < maxDiff && difference[1] < maxDiff && difference[2] < maxDiff && difference [3] < maxDiff) {

            //  Toast.makeText(this, differences + " net: "+  Double.toString(netdifference)+ " : " + correctAttempts, Toast.LENGTH_LONG).show();
                alertDialog.setTitle("Correct!");
                alertDialog.setMessage("That's right, you're all done for today! Feel free to close the application.");
                alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Close", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                finishAndRemoveTask();
                            }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                finishAffinity();
                            }else{
                                finish();
                            }
                        }
                    }
                  );
                alertDialog.show();
            passed =true;
        }else{
            passed=false;
            //Log differences here for diagnostic purposes
            Toast.makeText(this, "Sorry, try again", Toast.LENGTH_SHORT).show();
        }

        //For saving this data to local storage, deprecated
//        backgroundwrite write = new backgroundwrite(this);
//        int timestamp = (int) System.currentTimeMillis();
//        write.execute("attempt"+ Integer.toString(timestamp)+passed+".txt", differences, "false");

        BackgroundWorker serv = new BackgroundWorker(this);
        serv.execute("push", userID, timeStamps, Boolean.toString(passed), differences, RGBraw);
    }

    /**
     * Find the password in the PassHue directory on the SD card and loads into the password array
     * Password file should be named (userID)password.txt and located in external/PassHue
     **/
    private void loadPassword(){
        //Read password from external memory into loadedPassword
        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, "/PassHueData/" + userID + "password.txt");
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            int i=0;
            while ((line = br.readLine()) != null && i<4) {
                loadedPassword[i] = Integer.parseInt(line);
                i++;
            }
            br.close();
        }
        catch (IOException e) {
            Toast toast = Toast.makeText(getApplicationContext(), "Error reading device memory", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Resets currently entered password by clearing password array and resetting buttons to white
     * Intended for use with the reset button
     */
    public void reset(View view){
        for(int i=0; i<4; i++) {
            latch[i] = false;
            currentPassword[i] = 0;
        }

        Button bg;
        @ColorInt int defaultColor;
        defaultColor = ContextCompat.getColor(getApplicationContext(), android.R.color.white);
        bg = (Button) findViewById(R.id.bg1);
        bg.setBackgroundColor(defaultColor);
        bg = (Button) findViewById(R.id.bg2);
        bg.setBackgroundColor(defaultColor);
        bg = (Button) findViewById(R.id.bg3);
        bg.setBackgroundColor(defaultColor);
        bg = (Button) findViewById(R.id.bg4);
        bg.setBackgroundColor(defaultColor);

        //in case of clear, note the time with a special RE flag. The timing of a cleared attempt
        //is irrelevant and won't be received by the server, but we still note when it occurs
        timeStamps="RE"+ Long.toString(System.currentTimeMillis()) +", ";
    }

    /**
     * Initializes the forgot your password procedure. Opens a dialog where the user confirms they forgot.
     * If confirmed, deletes the password file from memory, pushes a message to the server indicating a reset,
     * then starts the password setup procedure over.
     * @param view
     */
    public void forgot(View view){

            alertDialog.setTitle("Forgot your PassHue?");
            alertDialog.setMessage("If you're really sure you forgot your PassHue, press reset to clear it. You'll be able to set a new PassHue and finish the rest of the experiment with it." +
                    "\n\nAre you really sure you forgot your PassHue?");
            alertDialog.setButton(Dialog.BUTTON_NEGATIVE, "Reset", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            backgroundwrite write = new backgroundwrite();
                            write.deletefromSD(userID + "password.txt");
                            write.deletefromSD("passwordVerified.txt");

                            BackgroundWorker serv = new BackgroundWorker(getApplicationContext());
                            serv.execute("push", userID, Long.toString(System.currentTimeMillis()), "false", "reset order", "1 1 1");
                            PWisLoaded=false;
                            Intent myIntent = new Intent(getApplicationContext(), passwordsetup.class);
                            myIntent.putExtra("params0", userID); //Optional parameters
                            startActivity(myIntent);
                        }
                    }
            );
            alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Nevermind", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                }
            );
            alertDialog.show();


    }

    /**
     * Fancy pants permission requester with legacy support.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //write permission granted, do the normal initialisation now
            Initialize();
        } else {
            // permission denied, close the program?
            Toast toast = Toast.makeText(getApplicationContext(), "Permission denied, unable to continue", Toast.LENGTH_LONG);
            toast.show();
            if(android.os.Build.VERSION.SDK_INT >= 21)
                {finishAndRemoveTask();}
            else
                {finish();}
        }
        return;

    }

}
