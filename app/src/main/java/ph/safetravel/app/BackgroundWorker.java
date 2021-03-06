package ph.safetravel.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.nio.charset.StandardCharsets;

public class BackgroundWorker extends AsyncTask<String,String,String> {
    Context context;
    ProgressDialog progressDialog;

    public AsyncResponse aR;
    public BackgroundWorker(Context context, AsyncResponse aR) {
        this.context = context;
        //context=MainActivity.this
        this.aR = aR;
    }

    @Override
    protected String doInBackground(String... params) {
        String type = params[0];
        String login_url = "https://www.safetravel.ph/testapp/login.php";
        String register_url = "https://www.safetravel.ph/testapp/register.php";
        String report_url = "https://www.safetravel.ph/testapp/report.php";

        // login activity
        if(type.equals("login")) {
            try {
                String username = params[1];
                String password = params[2];
                URL url = new URL(login_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                String post_data = URLEncoder.encode("username","UTF-8")+"="+URLEncoder.encode(username,"UTF-8")+"&"
                        +URLEncoder.encode("password","UTF-8")+"="+URLEncoder.encode(password,"UTF-8");
                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1));
                String result="";
                String line="";
                while((line = bufferedReader.readLine())!=null){
                    result += line;
                }
                bufferedReader.close();
                httpURLConnection.disconnect();
                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } // for login

        // register activity
        if(type.equals("register")) {
            try {
                String firstname = params[1];
                String lastname = params[2];
                String age = params[3];
                String contactnumber = params[4];
                String username = params[5];
                String password = params[6];
                String role = params[7];
                String question1 = params[8];
                String answer1 = params[9];
                String question2 = params[10];
                String answer2 = params[11];
                String question3 = params[12];
                String answer3 = params[13];
                URL url = new URL(register_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                String post_data = URLEncoder.encode("firstname","UTF-8")+"="+URLEncoder.encode(firstname,"UTF-8")+"&"
                        +URLEncoder.encode("lastname","UTF-8")+"="+URLEncoder.encode(lastname,"UTF-8")+"&"
                        +URLEncoder.encode("age","UTF-8")+"="+URLEncoder.encode(age,"UTF-8")+"&"
                        +URLEncoder.encode("contactnumber","UTF-8")+"="+URLEncoder.encode(contactnumber,"UTF-8")+"&"
                        +URLEncoder.encode("username","UTF-8")+"="+URLEncoder.encode(username,"UTF-8")+"&"
                        +URLEncoder.encode("password","UTF-8")+"="+URLEncoder.encode(password,"UTF-8")+"&"
                        +URLEncoder.encode("role","UTF-8")+"="+URLEncoder.encode(role,"UTF-8")+"&"
                        +URLEncoder.encode("question1","UTF-8")+"="+URLEncoder.encode(question1,"UTF-8")+"&"
                        +URLEncoder.encode("answer1","UTF-8")+"="+URLEncoder.encode(answer1,"UTF-8")+"&"
                        +URLEncoder.encode("question2","UTF-8")+"="+URLEncoder.encode(question2,"UTF-8")+"&"
                        +URLEncoder.encode("answer2","UTF-8")+"="+URLEncoder.encode(answer2,"UTF-8")+"&"
                        +URLEncoder.encode("question3","UTF-8")+"="+URLEncoder.encode(question3,"UTF-8")+"&"
                        +URLEncoder.encode("answer3","UTF-8")+"="+URLEncoder.encode(answer3,"UTF-8");
                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1));
                String result="";
                String line="";
                while((line = bufferedReader.readLine())!=null){
                    result += line;
                }
                bufferedReader.close();
                httpURLConnection.disconnect();
                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } // for register

        // report activity
        if(type.equals("report")){
            try {
                String username = params[1];
                String street = params[2];
                String landmarks = params[3];
                String barangay = params[4];
                String city = params[5];
                String description = params[6];
                String lat = params[7];
                String lng = params[8];
                String image = params[9];
                URL url = new URL(report_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                String post_data = URLEncoder.encode("username","UTF-8")+"="+URLEncoder.encode(username,"UTF-8")+"&"
                        +URLEncoder.encode("street","UTF-8")+"="+URLEncoder.encode(street,"UTF-8")+"&"
                        +URLEncoder.encode("landmarks","UTF-8")+"="+URLEncoder.encode(landmarks,"UTF-8")+"&"
                        +URLEncoder.encode("barangay","UTF-8")+"="+URLEncoder.encode(barangay,"UTF-8")+"&"
                        +URLEncoder.encode("city","UTF-8")+"="+URLEncoder.encode(city,"UTF-8")+"&"
                        +URLEncoder.encode("description","UTF-8")+"="+URLEncoder.encode(description,"UTF-8")+"&"
                        +URLEncoder.encode("latitude","UTF-8")+"="+URLEncoder.encode(lat,"UTF-8")+"&"
                        +URLEncoder.encode("longitude","UTF-8")+"="+URLEncoder.encode(lng,"UTF-8")+"&"
                        +URLEncoder.encode("image","UTF-8")+"="+URLEncoder.encode(image,"UTF-8");
                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1));
                String result="";
                String line="";
                while((line = bufferedReader.readLine())!=null){
                    result += line;
                }
                bufferedReader.close();
                httpURLConnection.disconnect();
                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } // for report
        return null;
    } //doInBackground

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog=new ProgressDialog(context);
        progressDialog.setMessage("Working...");
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        aR.processFinish(result);
        progressDialog.dismiss();
    }

    @Override
    protected void onProgressUpdate(String... values) {
        //
        super.onProgressUpdate();
    }

}
