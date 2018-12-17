package passhue.dev.gurary.passhue;

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

public class demographic extends AppCompatActivity {

    Spinner genderSpinner;
    Spinner skillSpinner;
    Spinner typeSpinner;
    Spinner timeSpinner;
    Spinner colorSpinner;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demographic);

        //for some reason, this portion never recieves a bad intent and crashes, this merits some further study
        userID = getIntent().getExtras().getString("params0");
        if(userID.length()<1){
            checkID();
        }
        setTitle("Demographics");

        //Initializes the spinners with the values stored in strings.xml
        genderSpinner = (Spinner) findViewById(R.id.genderSpinner);
        ArrayAdapter<CharSequence> gender = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        gender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(gender);

        skillSpinner = (Spinner) findViewById(R.id.skillSpinner);
        ArrayAdapter<CharSequence> skills = ArrayAdapter.createFromResource(this,
                R.array.skill_array, android.R.layout.simple_spinner_item);
        skills.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        skillSpinner.setAdapter(skills);

        typeSpinner = (Spinner) findViewById(R.id.typeSpinner);
        ArrayAdapter<CharSequence> types = ArrayAdapter.createFromResource(this,
                R.array.type_array, android.R.layout.simple_spinner_item);
        types.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(types);

        timeSpinner = (Spinner) findViewById(R.id.timeSpinner);
        ArrayAdapter<CharSequence> times = ArrayAdapter.createFromResource(this,
                R.array.time_array, android.R.layout.simple_spinner_item);
        times.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(times);

        colorSpinner = (Spinner) findViewById(R.id.colorSpinner);
        ArrayAdapter<CharSequence> colors = ArrayAdapter.createFromResource(this,
                R.array.color_array, android.R.layout.simple_spinner_item);
        times.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(colors);

    }

    public void accept (View view){
        EditText ageEditText = (EditText) findViewById((R.id.ageEditText));
        String age =        ageEditText.getText().toString();
        String gender =     genderSpinner.getSelectedItem().toString();
        String skill =      skillSpinner.getSelectedItem().toString();
        String type =       typeSpinner.getSelectedItem().toString();
        String time =       timeSpinner.getSelectedItem().toString();
        String color =      colorSpinner.getSelectedItem().toString();

        String toWrite = age + ", " + gender + ", " + skill + ", " +
                type + ", " + time + ", " + color;

        AESImplementation helper = new AESImplementation();

        backgroundwrite write = new backgroundwrite();
        write.execute("Demographics.txt", helper.encrypt(toWrite, helper.getKey()), "false");

        if(Integer.parseInt(age)>120){
            userID="PS"+userID;
        }
        BackgroundWorker serv = new BackgroundWorker(this);
        serv.execute("pushdemo", userID, helper.encrypt(toWrite, helper.getKey()));

        //TODO it's possible this is the cause of the empty intents sent to passwordsetup
        Intent myIntent = new Intent(getApplicationContext(), LoginPage.class);
        myIntent.putExtra("params0", ""); //Optional parameters
        startActivity(myIntent);
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
