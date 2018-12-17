package passhue.dev.gurary.passhue;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class endexperiment extends AppCompatActivity {

    Spinner createSpinner;
    Spinner loginSpinner;
    Spinner preferSpinner;
    String userID;
    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_endexperiment);

        checkID();
        setTitle("Thanks for Participating");

        //Initializes the spinners with the values stored in strings.xml
        createSpinner = (Spinner) findViewById(R.id.creationSpinner);
        ArrayAdapter<CharSequence> create = ArrayAdapter.createFromResource(this,
                R.array.difficulty_array, android.R.layout.simple_spinner_item);
        create.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        createSpinner.setAdapter(create);

        loginSpinner = (Spinner) findViewById(R.id.loginSpinner);
        ArrayAdapter<CharSequence> login = ArrayAdapter.createFromResource(this,
                R.array.difficulty_array, android.R.layout.simple_spinner_item);
        login.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        loginSpinner.setAdapter(login);

        preferSpinner = (Spinner) findViewById(R.id.preferSpinner);
        ArrayAdapter<CharSequence> prefer = ArrayAdapter.createFromResource(this,
                R.array.prefer_array, android.R.layout.simple_spinner_item);
        prefer.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        preferSpinner.setAdapter(prefer);

        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Thank you for your comments.");
    }

    public void submit (View view){
        EditText commentsText = (EditText) findViewById((R.id.share));
        String comments =        commentsText.getText().toString();
        String create =     createSpinner.getSelectedItem().toString();
        String login =      loginSpinner.getSelectedItem().toString();
        String prefer =       preferSpinner.getSelectedItem().toString();

        String toWrite = "survey: " + create + ", " +  login + ", " + prefer + ", comments: " + comments;

        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, "/PassHueData/EndExperiment.txt");
        if (!file.exists()) {
            AESImplementation helper = new AESImplementation();

            //TODO verify encrypted write works for all devices.
            backgroundwrite write = new backgroundwrite();
            write.execute("EndExperiment.txt", helper.encrypt(toWrite, helper.getKey()), "false");

            BackgroundWorker serv = new BackgroundWorker(this);
            serv.execute("pushdemo", userID, helper.encrypt(toWrite, helper.getKey()));

            alertDialog.setMessage("That's it, you finished the experiment. If you'd like to play a PassHue guessing game (~20 minutes), click below, otherwise feel free to uninstall the application. "+
            "If you don't have time now, and you want to play later, just close the application and start it back up whenever you're ready. Thank you so much for helping out!");
            alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Sure, I'll play!", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent myIntent = new Intent(getApplicationContext(), Guesser.class);
                            myIntent.putExtra("params0", userID); //Optional parameters
                            startActivity(myIntent);
                        }
                    }
            );
            alertDialog.show();
        }else{
            alertDialog.setMessage("We appreciate the enthusiasm, but we already got your comments! That's it for this experiment. Remember to leave the app installed," +
                    "and we'll send you a notification if there's another experiment you can help with. Thanks again for helping out!");
            alertDialog.show();
        }

        return;

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
