package passhue.dev.gurary.passhue;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;

public class terms extends AppCompatActivity {

    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);
        setTitle("Consent Form");

        //for some reason, this portion never recieves a bad intent and crashes, this merits some further study
        userID = getIntent().getExtras().getString("params0");
        if(userID.length()<1){
            checkID();
        }

        //Battery Optimization code piggy-backed on terms
        File sdcard = Environment.getExternalStorageDirectory();
        File file;
        file = new File(sdcard, "/PassHueData/AcceptedTerms.txt");
        //THE FIREBASE VERSION DOES NOT REQUIRE BATTERY OPTIMIZATIONS
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            file = new File(sdcard, "/PassHueData/BatteryOptimized.txt");
//            if (!file.exists()) {
//                //We're going to piggy-back on the firstrun behavior to try to deoptimize the battery
//                //This will fire once regardless, and it won't bother the user again if they decline.
//                RequestNoBatteryOptimization();
//                // return false;
//            }else{
//                Log.d("DeOp", "Already attempted to optimize battery once.");
//            }
//        }else{
//            Log.d("VersionError", "Not Android O or greater");
//        }

        String terms = "We are Dr. Jonathan Gurary and James Millard, professor and undergraduate researcher in the department of Mathematics and Computer Science at John Carroll. We are asking you to participate in a research study. If you accept, you will complete some of the following tasks: \n\n" +
                "--Download and install this Android smartphone application.\n" +
                "--Fill out a survey with your age, gender, and estimated skill level with an Android device.  This is optional.\n" +
                "--Use the application to generate a standard password with letters, numbers, and special characters.\n" +
                "--Use the application to generate a unique type of password. This will be a kind of password we have developed and will not be available anywhere else.\n" +
                "--Remember the passwords you made and enter them again when we ask you. We might ask you to do this up to once a day over the next 14 days. If you miss any of these requests, there is no penalty, and you can still finish the experiment.\n" +
                "--Attempt to guess passwords which we made using hints that we provide you.\n" +
                "\n" +
                "We will only share group results, like average age, with others. We can't be sure that your results will reach us and only us, but your responses are optional and you can chose not to answer any of the questions.\n" +
                "\n" +
                "We will record all passwords you enter. These passwords will not be used to secure anything, they are just for research. For your security, please do not use a password which has ever or will ever be used to secure real information. For example don't use the same password you use for your email. Again, do not use a password that you have ever used or plan to use, or even one that's really similar.\n" +
                "\n" +
                "We might share examples of passwords generated in this experiment with others, but the examples will never be linked to you. We might also share group information about the passwords, like average length. We will record how long it takes you to enter a password, what parts of the screen you touch, what mistakes you make, and other things you do while you use the application. We might share some of that information too, like how much time it took you to enter a certain password, but we will never link the information to you. We will not record anything you do outside the application. We won't use your camera, microphone, fingerprint reader, or anything else not related to entering your password on the screen.\n" +
                "\n" +
                "Participation is completely voluntary and you can stop at any time without penalty. There is no consequence for not participating. There is no benefit, no payment, and no reimbursement for participating. You can uninstall the application from your device at any time.\n" +
                "\n" +
                "Any risks associated with this research do not exceed those of daily living. \n" +
                "\n" +
                "The study should take about 30 minutes of your time, not counting the guessing part at the end. It should take about 20 minutes to fill out the survey and set your passwords. It should take another 10 minutes to enter the passwords again when the application asks you to in 1-14 days. \n" +
                "\n" +
                "If you try to guess the passwords we made, at the end of this experiment, you can take as little or as long as you want. We anticipate you will take about 20 minutes.\n" +
                "\n" +
                "For further information regarding this research please contact us at jgurary@jcu.edu\n" +
                "\n" +
                "If you have any questions about the rights and welfare of research participants, please contact the John Carroll University Institutional Review Board Administrator, Carole Krus at 216-397-1527 or ckrus@jcu.edu. \n" +
                "\n" +
                "You can view our complete Privacy Policy at: http://passworks.dyndns-remote.com/PassHue_Privacy_Policy.html" +
                "\n" +
                "Please take a screenshot so that you have a copy of this form. If you agree to participate in our research study, please accept below.";
        TextView termsText = (TextView) findViewById(R.id.termsText);
        termsText.setText(terms);
        termsText.setMovementMethod(new ScrollingMovementMethod());
    }

    /**
     * Battery de-optimization is required to properly show notifications since 8.0
     */
    private void RequestNoBatteryOptimization() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("DeOp", "attempting to un-optimize battery");
            String packageName = this.getApplicationContext().getPackageName();
            PowerManager pm = (PowerManager) this.getSystemService(this.POWER_SERVICE);
            if (pm.isIgnoringBatteryOptimizations(packageName)) {
                Log.d("DeOp", "Already ignoring optimization");
                return;
            } else {
                Log.d("DeOp", "Directing to settings");
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
                Calendar c = Calendar.getInstance();
                String sDate = c.get(Calendar.YEAR) + "-"
                        + c.get(Calendar.MONTH) + 1
                        + "-" + c.get(Calendar.DAY_OF_MONTH)
                        + " at " + c.get(Calendar.HOUR_OF_DAY)
                        + ":" + c.get(Calendar.MINUTE);
                backgroundwrite write = new backgroundwrite();
                write.execute("BatteryOptimized.txt", "Requested battery de-optimization at " + sDate + "\n", "false");
            }
        }
    }

    public void accept(View view){
        CheckBox over18 = (CheckBox) findViewById(R.id.over18);
        if(over18.isChecked()) {
            Calendar c = Calendar.getInstance();
            String sDate = c.get(Calendar.YEAR) + "-"
                    + c.get(Calendar.MONTH)+1
                    + "-" + c.get(Calendar.DAY_OF_MONTH)
                    + " at " + c.get(Calendar.HOUR_OF_DAY)
                    + ":" + c.get(Calendar.MINUTE);
            backgroundwrite write = new backgroundwrite();
            write.execute("AcceptedTerms.txt", "Confirmed terms at: " + sDate, "false");
            //TODO ship to server? Not really necessary.

            Intent myIntent = new Intent(getApplicationContext(), demographic.class);
            myIntent.putExtra("params0", userID); //Optional parameters
            startActivity(myIntent);
        }else {
            Toast.makeText(this, "Please confirm you are over 18", Toast.LENGTH_SHORT).show();
        }
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

/*
//OLD TERMS HERE
"We are Dr. Wenbing Zhao and Mr. Jonathan Gurary, professor and graduate student, in the Department of Electrical and Computer Engineering at Cleveland State University. We are asking you to participate in a research study. If you accept, you will complete some of the following tasks:\n\n" +
                "--Download and install this Android smartphone application.\n" +
                "--Fill out a survey with your age, gender, and estimated skill level with an Android device.  This is optional.\n" +
                "--Use the application to generate a standard password with letters, numbers, and special characters.\n" +
                "--Use the application to generate a unique type of password. This will be a kind of password we have developed and will not be available anywhere else.\n" +
                "--Remember the passwords you made and enter them again when we ask you. We might ask you to do this up to ten times total over the next 14 days. If you miss any of these requests, there is no penalty, and you can still finish the experiment.\n" +
                "--At the end of the 14 days, attempt to guess passwords which we made using hints that we provide you.\n" +
                "\n" +
                "We will only share group results, like average age, with others. We can't be sure that your results will reach us and only us, but your responses are optional and you can chose not to answer any of the questions.\n" +
                "\n" +
                "We will record all passwords you enter. These passwords will not be used to secure anything, they are just for research. For your security, please do not use a password which has ever or will ever be used to secure real information. For example don't use the same password you use for your email. Again, do not use a password that you have ever used or plan to use, or even one that's really similar.\n" +
                "\n" +
                "We might share examples of passwords generated in this experiment with others, but the examples will never be linked to you. We might also share group information about the passwords, like average length. We will record how long it takes you to enter a password, what parts of the screen you touch, what mistakes you make, and other things you do while you use the application. We might share some of that information too, like how much time it took you to enter a certain password, but we will never link the information to you. We will not record anything you do outside the application. We won't use your camera, microphone, fingerprint reader, or anything else not related to entering your password on the screen.\n" +
                "\n" +
                "Participation is completely voluntary and you can stop at any time without penalty. There is no consequence for not participating. There is no benefit, no payment, and no reimbursement for participating. You can uninstall the application from your device at any time.\n" +
                "\n" +
                "Any risks associated with this research do not exceed those of daily living. \n" +
                "\n" +
                "The study should take about 30 minutes of your time, not counting the guessing part at the end. It should take about 20 minutes to fill out the survey and set your passwords. It should take another 10 minutes to enter the passwords again when the application asks you to in 1-14 days. \n" +
                "\n" +
                "If you try to guess the passwords we made, at the end of this experiment, you can take as little or as long as you want. We anticipate you will take about 20 minutes.\n" +
                "\n" +
                "For further information regarding this research please contact us at NetSecCSU@gmail.com. You can also call Mr. Gurary at (440) 339-3132 or Dr. Zhao at (216) 523-7480.\n" +
                "\n" +
                "I understand that if I have any questions about my rights as a research subject, I can contact the Cleveland State University Institutional Review Board at (216) 687-3630.\n" +
                "\n" +
                "Please take a screenshot so that you have a copy of this form. If you agree to participate in our research study, please accept below."
 */
