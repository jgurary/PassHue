package passhue.dev.gurary.passhue;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.madrapps.eyedropper.EyeDropper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

public class passwordsetup extends AppCompatActivity {

    public int[] currentPassword = new int[4]; //password currently being entered
    public boolean[] latch = new boolean[4]; //determines if the same index in password is
    public int[] loadedPassword = new int[4]; //password loaded from memory
    public boolean PWisLoaded; //was the password loaded from memory successfully
    public String userID; //loaded from memory, the users's initial ID stamp
    public int correctAttempts; //how many times the user has authenticated successfully
    public String distances;
    public long startTime;
    AlertDialog alertDialog; //pops up when password is done to end the session
    ImageView display; //this is the colored wheel thing

    private static final int maxDiff = 100; //the tolerance that users can be off by

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passwordsetup);
        checkID();
        correctAttempts=0;
        setTitle("Set a Password");
        setUp();
        startTime=System.currentTimeMillis();

        //Stupid kludge for 8.0+ systems that ignore finishaffinity because they suck
        //If this activity is reopened from recents, kicks you back to loginpage
        File sdcard = Environment.getExternalStorageDirectory();
        File PWfile = new File(sdcard, "/PassHueData/" + userID + "password.txt");
        File PWveriffile = new File(sdcard, "/PassHueData/" + "passwordVerified.txt");
        if (PWfile.exists() && PWveriffile.exists()) {
            Intent myIntent = new Intent(getApplicationContext(), LoginPage.class);
            myIntent.putExtra("params0", userID); //Optional parameters
            startActivity(myIntent);
        }

    }

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
                }
            }

            @Override
            public void onSelectionEnd(MotionEvent event) {
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
                        //checks against just-saved password, increments successes
                        testPassword();
                    }else {
                        //saves the passsword
                        Toast.makeText(getApplicationContext(), "Password Set", Toast.LENGTH_SHORT).show();
                        setPassword();
                    }
                }
            }
        });
    }

    public void setPassword(){
        String password="";
        backgroundwrite write = new backgroundwrite();
        //Write the password data to a local file
        for(int i=0; i<4; i++) {
            password += Integer.toString(currentPassword[i]) + "\r\n";
        }
        write.execute(userID+"password.txt", password, "true");

        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Great!");
        alertDialog.setMessage("Good, now enter your PassHue THREE MORE TIMES to make sure you got it, or reset it if you made a mistake.");
        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                }
        );
        alertDialog.show();

        //this session will not execute a load, so store the newly set password directly
        for(int i=0; i<4; i++) {
            loadedPassword[i] = currentPassword[i];
        }
        PWisLoaded=true;
    }

    public void testPassword(){
        double[] difference = new double[4];
        double netdifference=0;
        String differences="";
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
            double redCalc = ((512+redMean)*redDiff*redDiff)>>8;
            double greenCalc = 4*greenDiff*greenDiff;
            double blueCalc = ((767-redMean)*blueDiff*blueDiff)>>8;

            difference[i] = Math.sqrt( redCalc+ greenCalc + blueCalc );
            differences += Integer.toString(((int)difference[i])) + ", ";
            netdifference += difference[i];
        }
    //    Log.d("Differences: ", differences + ": " + Double.toString(netdifference));
        //TODO see if there is a fix for teal being hard to do

        if(difference[0] < maxDiff && difference[1] < maxDiff && difference[2] < maxDiff && difference [3] < maxDiff) {
            correctAttempts++;
            distances += Double.toString(netdifference) + ", ";

            //if assigned to the "rotating" version, rotate between attempts
            char lastNum;
            //TODO generate a checkID class to reduce redundancy
             /*on certain devices, the userID is not being correctly read at this point and results in a crash.
             this issued stemed from an empty passed intent, so activities now manually fetch the userID from file.
             the crash appears to affect only this class, but the patch has been applied to all of them.
             */
//            Log.d("USER ID:", userID);
            if(userID.length()>1) { //final sanity check
                lastNum = userID.charAt(userID.length() - 1);
            }else{
                lastNum='E';
                userID="CRITICALREADERROR";
            }
            if(lastNum=='1' || lastNum == '4' ||lastNum == '5' ||lastNum=='8' ||lastNum == '0') {
                display = (ImageView) findViewById(R.id.targetview);
                int randomRot;
                Random rand = new Random();
                randomRot=rand.nextInt(360);
                display.setRotation((float) randomRot);
            }

            //  Toast.makeText(this, differences + " net: "+  Double.toString(netdifference)+ " : " + correctAttempts, Toast.LENGTH_LONG).show();

            if (correctAttempts > 2) {
                Toast.makeText(this, "Password set, please remember it!", Toast.LENGTH_SHORT).show();

                //Success and exit application
                backgroundwrite write = new backgroundwrite();
                write.execute("passwordVerified.txt", distances, "false");


                BackgroundWorker serv = new BackgroundWorker(this);
                serv.execute("push", userID.toString(), Long.toString(startTime) + ", " + Long.toString(System.currentTimeMillis()), "true", "original", RGBraw);

                alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Great!");
                alertDialog.setMessage("Great PassHue, now remember those colors, that's it for today! Feel free to close the application or go back and change your PassHue using the reset button. " +
                        "After you leave this screen, you'll have to remember your PassHue.");
                alertDialog.setButton(Dialog.BUTTON_POSITIVE, "I remember it!", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    finishAffinity();//finishandremovetask has issues here
                                }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    finishAffinity();
                                }else{
                                    finish();
                                }
                            }
                        }
                );

                alertDialog.setButton(Dialog.BUTTON_NEGATIVE, "Go Back", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        }
                );
                alertDialog.show();
              //  Intent myIntent = new Intent(getApplicationContext(), LoginPage.class);
              //  myIntent.putExtra("params0", ""); //Optional parameters
              //  startActivity(myIntent);
            }else{
                Toast.makeText(this, "Correct! Please enter " + (3 - correctAttempts) + " more times.", Toast.LENGTH_SHORT).show();
            }
        }else{
            //For dianostic purposes only
          //  Toast.makeText(this, "Off by " + differences, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Not quite, try again", Toast.LENGTH_SHORT).show();
        }
    }

    //clears the password from temporary memory and erases the password file
    public void resetPassword(View view){
        for(int i=0; i<4; i++) {
            loadedPassword[i] = 0;
            latch[i] = false;
            currentPassword[i] = 0;
        }
        PWisLoaded = false;
        distances = "";
        correctAttempts = 0;

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

        backgroundwrite write = new backgroundwrite();
        write.deletefromSD(userID + "password.txt");
        write.deletefromSD("passwordVerified.txt");

        Toast.makeText(getApplicationContext(), "Password Cleared", Toast.LENGTH_SHORT).show();
    }

    public void restartTutorial(View view){
        Intent myIntent = new Intent(getApplicationContext(), Tutorial.class);
        myIntent.putExtra("params0", ""); //Optional parameters
        startActivity(myIntent);
    }

    /**
     * Loads a user ID file from the local storage
     */
    private void checkID() {

        //Check for username stamp file, stored in local memory
        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, "/PassHueData/userID.txt");
        //if the username stamp file does not exist, generate it using a system timestamp
        if (!file.exists())
        {
            Toast toast = Toast.makeText(getApplicationContext(), "Error reading device memory, did you give the app permissions?", Toast.LENGTH_SHORT);
            toast.show();
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
                Toast toast = Toast.makeText(getApplicationContext(), "Critical error reading device memory", Toast.LENGTH_SHORT);
                toast.show();
            }
        } //end else if file doesn't exist
    }

}
