package passhue.dev.gurary.passhue;

/**
 * Modified from internet code by Commander Fish on 5/24/2017.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class BackgroundWorker extends AsyncTask<String,Void,String> {

    Context context;
    AlertDialog alertDialog;

    BackgroundWorker (Context ctx) {
        context = ctx;
    }

    @Override
    protected String doInBackground(String... params) {
        //first Parameter decides what this thing does
        String type = params[0];
        //Hard code the url for the database like a real idiot (append the php code further down)
        //Don't worry, DNS should auto-resolve. If not, check the server machine's dyndns
        String login_url = "http://passworks.dyndns-remote.com";
        String result;
        //Check type, direct to appropriate method
        if(type.equals("login")) {
            result=login(login_url, params);
            return result;
        } else if(type.equals("fetch")){
            result=fetch(login_url, params);
            return result;
        } else if(type.equals("push")) {
            result=push(login_url, params);
            return result;
        }else if (type.equals("pushdemo")){
            result=pushdemo(login_url, params);
            return result;
        }
        return "Error reading type.";
    }

    //Pushes a line of data to the server
    //Column reference: userid, timestamps, passed, differences, rgbraw
    protected String push(String login_url, String[] params){
        login_url+="/push.php";
        //Everything in a try/catch because errors are for chumps
        try {
            //This has to be inside the try/catch
            URL url = new URL(login_url);

            //See column reference at top of method
            String col1 = params[1];
            String col2 = params[2];
            String col3=params[3];
            String col4=params[4];
            String col5=params[5];
      //      String col6=params[6];

            //Opens a connection to the specified URL and reserves input/output privileges
            //If just fetching data and privacy doesn't matter, GET can be used instead of POST
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);

            //Opens a container for the data to transmit to the server
            OutputStream outputStream = httpURLConnection.getOutputStream();
            //Sends text in UTF-8 format, should be UTF-16 and UTF-32 complaint too
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            //Generates a string from the data
            String post_data = URLEncoder.encode("userid","UTF-8")+"="+URLEncoder.encode(col1,"UTF-8")+"&"
                    +URLEncoder.encode("timestamp","UTF-8")+"="+URLEncoder.encode(col2,"UTF-8")+"&"
                    +URLEncoder.encode("passed","UTF-8")+"="+URLEncoder.encode(col3,"UTF-8")+"&"
                    +URLEncoder.encode("differences","UTF-8")+"="+URLEncoder.encode(col4,"UTF-8")+"&"
                    +URLEncoder.encode("rgbraw","UTF-8")+"="+URLEncoder.encode(col5,"UTF-8");
            //Writes the data to the output and ships it over the http url connection
            bufferedWriter.write(post_data);
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();

            //Opens a container to receive input from the server
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));
            String result="";
            String line="";
            while((line = bufferedReader.readLine())!= null) {
                result += line;
            }
            bufferedReader.close();
            inputStream.close();
            httpURLConnection.disconnect();
            return result;

            //Error reporting
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "push method or php error";
    }

    //Pushes a line of demographic data to the server
    //Column reference: userid, encrypted demographic info
    protected String pushdemo(String login_url, String[] params){
        login_url+="/pushdemo.php";
        //Everything in a try/catch because errors are for chumps
        try {
            //This has to be inside the try/catch
            URL url = new URL(login_url);

            //See column reference at top of method
            String col1 = params[1];
            String col2 = params[2];
            //      String col6=params[6];

            //Opens a connection to the specified URL and reserves input/output privileges
            //If just fetching data and privacy doesn't matter, GET can be used instead of POST
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);

            //Opens a container for the data to transmit to the server
            OutputStream outputStream = httpURLConnection.getOutputStream();
            //Sends text in UTF-8 format, should be UTF-16 and UTF-32 complaint too
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            //Generates a string from the data
            String post_data = URLEncoder.encode("userid","UTF-8")+"="+URLEncoder.encode(col1,"UTF-8")+"&"
                    +URLEncoder.encode("data","UTF-8")+"="+URLEncoder.encode(col2,"UTF-8")+"&";
            //Writes the data to the output and ships it over the http url connection
            bufferedWriter.write(post_data);
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();

            //Opens a container to receive input from the server
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));
            String result="";
            String line="";
            while((line = bufferedReader.readLine())!= null) {
                result += line;
            }
            bufferedReader.close();
            inputStream.close();
            httpURLConnection.disconnect();
            return result;

            //Error reporting
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "push method or php error";
    }

    //Fetches 2 pieces of data based on a key name
    //KEY COLUMN NAME, KEY, DATACOL1, DATACOL2
    protected String fetch(String login_url, String[] params){
        login_url+="/fetch.php";
        //Everything in a try/catch because errors are for chumps
        try {
            //This has to be inside the try/catch
            URL url = new URL(login_url);

            //Param1 is the column name, param2 is the search key
            String column = params[1];
            String key = params[2];
            String datatype1=params[3];
            String datatype2=params[4];

            //Opens a connection to the specified URL and reserves input/output privileges
            //If just fetching data and privacy doesn't matter, GET can be used instead of POST
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);

            //Opens a container for the data to transmit to the server
            OutputStream outputStream = httpURLConnection.getOutputStream();
            //Sends text in UTF-8 format, should be UTF-16 and UTF-32 complaint too
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            //Generates a string from the data
            String post_data = URLEncoder.encode(column,"UTF-8")+"="+URLEncoder.encode(key,"UTF-8")+"&"
                    +URLEncoder.encode("data1","UTF-8")+"="+URLEncoder.encode(datatype1,"UTF-8")+"&"
                    +URLEncoder.encode("data2","UTF-8")+"="+URLEncoder.encode(datatype2,"UTF-8");
            //Writes the data to the output and ships it over the http url connection
            bufferedWriter.write(post_data);
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();

            //Opens a container to receive input from the server
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));
            String result="";
            String line="";
            while((line = bufferedReader.readLine())!= null) {
                result += line;
            }
            bufferedReader.close();
            inputStream.close();
            httpURLConnection.disconnect();
            return result;

            //Error reporting
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "fetch method or php error";
    }

    //Checks 2 Strings against user_name and password columns
    //USERNAME, PASSWORD
    protected String login(String login_url, String[] params){
        login_url+="/login.php";
        //Everything in a try/catch because errors are for chumps
        try {
            //This has to be inside the try/catch
            URL url = new URL(login_url);

            //Username and password are the next two parameters of passed input
            String user_name = params[1];
            String password = params[2];

            //Opens a connection to the specified URL and reserves input/output privileges
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);

            //Opens a container for the data to transmit to the server
            OutputStream outputStream = httpURLConnection.getOutputStream();
            //Sends text in UTF-8 format, should be UTF-16 and UTF-32 complaint too
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            //Generates the string user_name=username&password=password in UTF-8 format
            String post_data = URLEncoder.encode("user_name","UTF-8")+"="+URLEncoder.encode(user_name,"UTF-8")+"&"
                    +URLEncoder.encode("password","UTF-8")+"="+URLEncoder.encode(password,"UTF-8");
            //writes the data to the output and ships it over the http url connection
            bufferedWriter.write(post_data);
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();

            //Opens a container to receive input from the server
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));
            String result="";
            String line="";
            while((line = bufferedReader.readLine())!= null) {
                result += line;
            }
            bufferedReader.close();
            inputStream.close();
            httpURLConnection.disconnect();
            return result;

            //Error reporting
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "login method or php error";
    }

    //Prepares a dialog box to pop up when the connection is done
    @Override
    protected void onPreExecute() {
    //    alertDialog = new AlertDialog.Builder(context).create();
        //   alertDialog.setTitle("Connection Status");
    }

    //Shows a dialog box with the results of the connection
    @Override
    protected void onPostExecute(String result) {
        Log.d("sending result", result);
      //     alertDialog.setMessage(result);
      //  alertDialog.show();
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}