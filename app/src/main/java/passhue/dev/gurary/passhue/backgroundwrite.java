package passhue.dev.gurary.passhue;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import android.widget.Toast;

/**
 * Created by Commander Fish on 6/23/2017.
 * Writes data to external storage
 * String filename, String Body, Boolean append (as String)
 * NOTE: DON'T WRITE MULTIPLE THINGS AT THE SAME TIME OR THIS BABY MIGHT CRASH
 * IF YOU WANT TO WRITE IN PARALLEL, START MULTIPLE INSTANCES
 */

public class backgroundwrite extends AsyncTask<String,Void,String> {


    Context context;

    backgroundwrite () {
    //    context = ctx;
    }
    @Override
    protected String doInBackground(String... params) {
        boolean append;
        String result="";

        append = Boolean.valueOf(params[2]);
        WritetoSD(params[0],params[1], append);
        return result;
    }


    /*	Write data to a text file on the SD card in the PassHueData folder
 *  requires file name, contents as a String, and bool if appending or not
 */
    public void WritetoSD(String sFileName, String sBody, boolean append) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "PassHueData");
            if (!root.exists()) {
                root.mkdirs();
            }

            File gpxfile = new File(root, sFileName);
            FileWriter writer = new FileWriter(gpxfile, append);
            writer.append(sBody);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            //Error writing to storage
        }
    }

    public void deletefromSD(String sFileName){
        try{
            File root = new File(Environment.getExternalStorageDirectory(), "PassHueData");
            if (!root.exists()) {
                root.mkdirs();
            }

            File gpxfile = new File(root, sFileName);
            gpxfile.delete();

        }catch (Exception e) {
            e.printStackTrace();
           //error deleting old file
        }
    }

}