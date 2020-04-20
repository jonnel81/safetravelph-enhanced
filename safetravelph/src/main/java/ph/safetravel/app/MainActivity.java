package ph.safetravel.app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements AsyncResponse {
    EditText UserNameEt, PasswordEt;
    SharedPreferences myPrefs;
    AsyncResponse aR = MainActivity.this;
    BackgroundWorker backgroundWorker = new BackgroundWorker(MainActivity.this, aR);
    String username = "";
    String password = "";
    Context context;
    View view;
    AlertDialog.Builder builder;
    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UserNameEt = (EditText) findViewById(R.id.etUserName);
        PasswordEt = (EditText) findViewById(R.id.etPassword);
        view = (View) findViewById(android.R.id.content);

        myPrefs=getSharedPreferences("MYPREFS",Context.MODE_PRIVATE);
        String username = myPrefs.getString("username",null);
        String password = myPrefs.getString("password",null);

        // Check if shared preferences contains username and password then redirect to report activity
        if(username != null && password != null ){
            startActivity(new Intent(this,Report.class));
        }
    } // onCreate

    // Click login button
    public void OnLogin(View view) {
        username = UserNameEt.getText().toString();
        password = PasswordEt.getText().toString();
        String type = "login";

        backgroundWorker.execute(type,username, password);
    } // OnLogin

    @Override
    public void processFinish(String result) {
        // Login success
        if (result.equals("Login success")) {
            // Update shared preferences
            SharedPreferences.Editor editor = myPrefs.edit();
            editor.putString("username", username);
            editor.putString("password", password);
            editor.apply();

            builder = new AlertDialog.Builder(this);
            builder.setMessage("Success! You are logged in.");
            builder.setCancelable(false);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    startActivity(new Intent(view.getContext(), Report.class));
                }
            });
            alertDialog = builder.create();
            alertDialog.setTitle("Status");
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setOwnerActivity(this);
            alertDialog.show();
          } else{
            builder = new AlertDialog.Builder(this);
            builder.setMessage("Wrong username/password. Please try again.");
            builder.setCancelable(false);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    startActivity(new Intent(view.getContext(), MainActivity.class));
                }
            });
            alertDialog = builder.create();
            alertDialog.setTitle("Status");
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setOwnerActivity(this);
            alertDialog.show();
        }
    } // processFinish

    // Click register button
    public void OpenReg(View view) {
        // start register activity
        startActivity(new Intent(this,Register.class));
    } // OpenReg

}
