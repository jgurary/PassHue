package passhue.dev.gurary.passhue;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
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

public class Guesser extends AppCompatActivity {

    public int[] currentPassword = new int[4]; //password currently being entered
    public boolean[] latch = new boolean[4]; //determines if the same index in password is
    public int[][][] loadedPassword = new int[4][4][3]; //in order: PW#0-3,Color#0-3,RGB
    public String timeStamps; // Initialization, first touch, second..., last touch
    private static final int maxDiff = 100; //the tolerance that users can be off by
    String userID="111111";
    AlertDialog alertDialog; //pops up when the user gets the pass right to say they are done
    AlertDialog alertDialogNegatives; //this dialog box also has a negative button
    Button viewsguesses;
    AnimationDrawable frameAnimation;

    int currentGuess; //1,2,3,4 for the first 1-4 passwords respectively. //TODO end experiment after this >3
    boolean isRotating; //true if the user is part of the "rotating" group
    boolean haveRotated; //only rotate once per password, then hold stationary
    boolean redraw; //determines if the ui updater thread needs to redraw the wheel
    int guesses; //number of guesses remaining
    int views; //number of views remaining
    int viewsUsed;
    int guessesUsed;
    boolean firstRound; //each PW has 2 rounds, first a single view, then 2 more


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guesser);

        viewsguesses = (Button) findViewById(R.id.viewsguesses);
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialogNegatives = new AlertDialog.Builder(this).create();

        loadPasswords();


        //users start with one view and 3 guesses
        currentGuess=1;
        guesses=3;
        views=1;
        guessesUsed=0;
        viewsUsed=0;
        firstRound=true;

        refreshText();

        setUp();

        checkID();
        char lastNum=userID.charAt(userID.length()-1);
        if(lastNum=='1' || lastNum == '4' ||lastNum == '5' ||lastNum=='8' ||lastNum == '0') {
            isRotating=true;
        }else{
            isRotating=false;
        }
        haveRotated=false;
        redraw=true;

        new Thread(new Runnable() {
            public void run() {

                while(true) {
                    //if the animation has ended, load the high-res passHue wheel back into the imageview.
                    //then rotate the wheel if needed
                    //NOTE: this is a pretty heavy task and it might break on slower phones, in particular it may
                    //lead to lag in selecting the first color
                    if (frameAnimation != null) {
                        if (frameAnimation.getCurrent() != frameAnimation.getFrame(frameAnimation.getNumberOfFrames() - 1)) {
                            //animation is still running
                        } else if(redraw){
                            //animation is over
                            //rotate if necessary
                            redraw=false;

                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        ImageView display = (ImageView) findViewById(R.id.targetview);
                                        display.setImageResource(R.drawable.colorwheelnodpi);
                                        if (isRotating && !haveRotated) {
                                            haveRotated = true;
                                            int randomRot;
                                            Random rand = new Random();
                                            randomRot = rand.nextInt(360);
                                            display.setRotation((float) randomRot);
                                        }
                                    }
                                });

                        }
                    } else {
                        //animation hasn't started yet
                    }
                }
            }
        }).start();


    }

    private void refreshText(){

        viewsguesses.setText("Views: " + views + " Guesses: " + guesses);
    }

    public void watchagain(View view) {

        //restore default rotation
        ImageView display = (ImageView) findViewById(R.id.targetview);
        display.setImageResource(R.drawable.watchagain);
        display.setRotation(0);

        viewsUsed=viewsUsed+1;
        if(views>0) {
            views=views-1;
            refreshText();
            ImageView mImageView;
            mImageView = (ImageView) findViewById(R.id.targetview);
            if (isRotating) {
                switch (currentGuess) {
                    case 1:
                        mImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ss1rimages, null));
                        break;
                    case 2:
                        mImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ss2rimages, null));
                        break;
                    case 3:
                        mImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ss3rimages, null));
                        break;
                    case 4:
                        mImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ss4rimages, null));
                        break;
                    default:
                        //Do something here when the experiment is over
                        break;
                }
            } else { //not rotating condition
                switch (currentGuess) {
                    case 1:
                        mImageView.setImageDrawable(getResources().getDrawable(R.drawable.ss1images));
                        break;
                    case 2:
                        mImageView.setImageDrawable(getResources().getDrawable(R.drawable.ss2images));
                        break;
                    case 3:
                        mImageView.setImageDrawable(getResources().getDrawable(R.drawable.ss3images));
                        break;
                    case 4:
                        mImageView.setImageDrawable(getResources().getDrawable(R.drawable.ss4images));
                        break;
                    default:
                        //Do something here when the experiment is over
                        break;
                }
            }
            frameAnimation = (AnimationDrawable) mImageView.getDrawable();
            frameAnimation.start();
            redraw=true;
        }else{ //no more views
                 Toast toast = Toast.makeText(getApplicationContext(), "Out of views!", Toast.LENGTH_LONG);
                 toast.show();
        }

        return;
    }

    /**
     * Eyedropper control methods, handles picking colors with the eyedropper and saving picked
     * colors to the password array. When all 4 colors are entered, invokes testPassword
     */
    private void setUp() {
        final View targetView = findViewById(R.id.targetview);
        //for purposes of changing the display
        final ImageView display = (ImageView) findViewById(R.id.targetview);

        final EyeDropper eyeDropper = new EyeDropper(targetView, new EyeDropper.ColorSelectionListener() {
            @Override
            public void onColorSelected(@ColorInt int color) {
                Button bg;


                //now pick the colors
                if(color!=0 && color!= Color.BLACK) { //prevents people from picking white/black using the outside of the wheel
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
            //    Log.d("State: ", frameAnimation.getState().toString());
                if(true) {
                    //TODO extra timestamps are generated when the user touches the wheel during the animation period!
                    //attempted to fix this issue by kludging it together with the wheel drawing flag, but this is *maybe* thread-unsafe.
                    //Fixed for now, timestamps generated in error are prepended with an "E", could be useful somehow.
                    if(!redraw) {
                        timeStamps += Long.toString(System.currentTimeMillis()) + ", ";
                    }else {
                        timeStamps += "E"+ Long.toString(System.currentTimeMillis()) + ", ";
                    }
                    //If a color has been entered, latch it in when the user releases the press
                    if (currentPassword[0] != 0) {
                        latch[0] = true;
                    }
                    if (currentPassword[1] != 0) {
                        latch[1] = true;
                    }
                    if (currentPassword[2] != 0) {
                        latch[2] = true;
                    }
                    if (currentPassword[3] != 0) {
                        latch[3] = true;
                    }

                    if (latch[0] == true && latch[1] == true && latch[2] == true && latch[3] == true) {
                        //checks against the set passwords
                        testPassword();
                    }
                } //end if animation running
            }
        });
    }

    private boolean testPassword(){

        double[] difference = new double[4];
        double netdifference=0;
        String differences="";
        boolean passed=false;
        String RGBraw="";

        for(int i=0; i<4; i++) {
            //Gets the alpha and RBG color values from the color int
            int RC = (currentPassword[i] >> 16) & 0xff;
            int GC = (currentPassword[i] >> 8) & 0xff;
            int BC = (currentPassword[i]) & 0xff;
            RGBraw+= Integer.toString(RC) + ", " + Integer.toString(GC) + ", " + Integer.toString(BC)+ "; ";

            int RL = loadedPassword[currentGuess-1][i][0];
            int GL = loadedPassword[currentGuess-1][i][1];
            int BL = loadedPassword[currentGuess-1][i][2];

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
        //Note: Teal is very hard to do

        guessesUsed=guessesUsed+1;
        if(difference[0] < maxDiff && difference[1] < maxDiff && difference[2] < maxDiff && difference [3] < maxDiff) {

            //  Toast.makeText(this, differences + " net: "+  Double.toString(netdifference)+ " : " + correctAttempts, Toast.LENGTH_LONG).show();
            alertDialog.setTitle("Correct!");
            alertDialog.setMessage("Looks like you're a pretty good hacker, you got it!");
            if(alertDialog.getButton(Dialog.BUTTON_POSITIVE)!=null){
                alertDialog.getButton(Dialog.BUTTON_POSITIVE).setText("Give me the next one!");}
            alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Give me the next one!", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            nextGuess();
                        }
                    }
            );
            alertDialog.show();
            passed =true;
        }else{ //guessed incorrectly
            passed=false;
            //Log differences here for diagnostic purposes
            Toast.makeText(this, "Sorry, that's not it", Toast.LENGTH_SHORT).show();

            if(guesses>1){
                guesses=guesses-1;
                refreshText();
            }else{ //out of guesses
                if(firstRound) {
                    alertDialog.setTitle("Out of guesses, for now.");
                    alertDialog.setMessage("Don't worry, you can still get 2 more views and 3 more guesses. But careful, this is all you'll get for this PassHue.");
                    if(alertDialog.getButton(Dialog.BUTTON_POSITIVE)!=null){
                         alertDialog.getButton(Dialog.BUTTON_POSITIVE).setText("Give me the views!");}
                    alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Give me the views!", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    views=2;
                                    guesses=3;
                                    firstRound=false;
                                    refreshText();
                                }
                            }
                    );
                    alertDialog.show();
                }else{ //second round is over, out of views and/or guesses
                    alertDialog.setTitle("Out of guesses!");
                    alertDialog.setMessage("That's all you get for this PassHue, sorry. Move on to the next one and try again.");
                    if(alertDialog.getButton(Dialog.BUTTON_POSITIVE)!=null){
                        alertDialog.getButton(Dialog.BUTTON_POSITIVE).setText("Next PassHue");}
                    alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Next PassHue", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    nextGuess();
                                }
                            }
                    );
                    alertDialog.show();

                }
            }
        }

//        backgroundwrite write = new backgroundwrite(this);
//        int timestamp = (int) System.currentTimeMillis();
//        write.execute("attempt"+ Integer.toString(timestamp)+passed+".txt", differences, "false");

        BackgroundWorker serv = new BackgroundWorker(this);
        serv.execute("push", Integer.toString(currentGuess) + "_" + userID, timeStamps,
                Integer.toString(currentGuess) + ", " + Boolean.toString(passed) + ", " + Integer.toString(viewsUsed) + ", " + Integer.toString(guessesUsed), differences, RGBraw);

        return false;
    }

    /**
     * Resets currently entered password by clearing password array and resetting buttons to white
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

    public void giveup(View view){

        alertDialogNegatives.setTitle("Can't get it?");
        if(firstRound) {
            alertDialogNegatives.setMessage("Give Up to get 2 more views and 3 more guesses? You can only do this once!");
            if(alertDialogNegatives.getButton(Dialog.BUTTON_NEGATIVE)!=null){
                alertDialogNegatives.getButton(Dialog.BUTTON_NEGATIVE).setText("Give up");}
            alertDialogNegatives.setButton(Dialog.BUTTON_NEGATIVE, "Give up", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            views=2;
                            guesses=3;
                            refreshText();
                            firstRound=false;
                        }
                    }
            );
            alertDialogNegatives.setButton(Dialog.BUTTON_POSITIVE, "Nevermind", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    }
            );
            alertDialogNegatives.show();
        }else{
            alertDialogNegatives.setMessage("Give Up to move on to the next PassHue?");
            if(alertDialogNegatives.getButton(Dialog.BUTTON_NEGATIVE)!=null){
                alertDialogNegatives.getButton(Dialog.BUTTON_NEGATIVE).setText("Give up");}
            alertDialogNegatives.setButton(Dialog.BUTTON_NEGATIVE, "Give up", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            nextGuess();
                        }
                    }
            );
            alertDialogNegatives.setButton(Dialog.BUTTON_POSITIVE, "Nevermind", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    }
            );
            alertDialogNegatives.show();
        }
    }

    private void nextGuess(){
        if(currentGuess<4) {
            views = 1;
            guesses = 3;
            currentGuess = currentGuess + 1;
            firstRound=true;
            guessesUsed = 0;
            viewsUsed = 0;
            refreshText();
            haveRotated=false;
            ImageView display = (ImageView) findViewById(R.id.targetview);
            display.setImageResource(R.drawable.watchagain);
            //FIX THIS FOR ROTATING MODE!!
            display.setRotation(0);
        }else{
            Intent myIntent = new Intent(getApplicationContext(), EndPage.class);
            myIntent.putExtra("params0", ""); //Optional parameters
            startActivity(myIntent);
        }
    }

    //four passwords manually entered here
    private void loadPasswords(){

        //FIRST PASSWORD
        //color1
        loadedPassword[0][0][0]=255;
        loadedPassword[0][0][1]=236;
        loadedPassword[0][0][2]=95;

        //color2
        loadedPassword[0][1][0]=254;
        loadedPassword[0][1][1]=179;
        loadedPassword[0][1][2]=66;

        //color3
        loadedPassword[0][2][0]=255;
        loadedPassword[0][2][1]=130;
        loadedPassword[0][2][2]=245;

        //color4
        loadedPassword[0][3][0]=170;
        loadedPassword[0][3][1]=13;
        loadedPassword[0][3][2]=255;

        //SECOND PASSWORD
        //color1
        loadedPassword[1][0][0]=255;
        loadedPassword[1][0][1]=199;
        loadedPassword[1][0][2]=22;

        //color2
        loadedPassword[1][1][0]=255;
        loadedPassword[1][1][1]=53;
        loadedPassword[1][1][2]=17;

        //color3
        loadedPassword[1][2][0]=0;
        loadedPassword[1][2][1]=255;
        loadedPassword[1][2][2]=70;

        //color4
        loadedPassword[1][3][0]=0;
        loadedPassword[1][3][1]=20;
        loadedPassword[1][3][2]=255;

        //THIRD PASSWORD
        //color1
        loadedPassword[2][0][0]=2;
        loadedPassword[2][0][1]=15;
        loadedPassword[2][0][2]=254;

        //color2
        loadedPassword[2][1][0]=0;
        loadedPassword[2][1][1]=255;
        loadedPassword[2][1][2]=1;

        //color3
        loadedPassword[2][2][0]=249;
        loadedPassword[2][2][1]=242;
        loadedPassword[2][2][2]=255;

        //color4
        loadedPassword[2][3][0]=255;
        loadedPassword[2][3][1]=225;
        loadedPassword[2][3][2]=1;

        //FOURTH PASSWORD
        //color1
        loadedPassword[3][0][0]=255;
        loadedPassword[3][0][1]=108;
        loadedPassword[3][0][2]=255;

        //color2
        loadedPassword[3][1][0]=255;
        loadedPassword[3][1][1]=11;
        loadedPassword[3][1][2]=14;

        //color3
        loadedPassword[3][2][0]=254;
        loadedPassword[3][2][1]=135;
        loadedPassword[3][2][2]=25;

        //color4
        loadedPassword[3][3][0]=255;
        loadedPassword[3][3][1]=200;
        loadedPassword[3][3][2]=5;

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
