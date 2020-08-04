package ph.safetravel.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.UUID;


public class MainActivity extends AppCompatActivity implements AsyncResponse {
    EditText UserNameEt, PasswordEt;
    SharedPreferences myPrefs;
    AsyncResponse aR = MainActivity.this;
    BackgroundWorker backgroundWorker = new BackgroundWorker(MainActivity.this, aR);
    String username = "";
    String password = "";
    String androidId = "";
    View view;
    AlertDialog.Builder builder;
    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        UserNameEt = findViewById(R.id.etUserName);
        PasswordEt = findViewById(R.id.etPassword);
        view = findViewById(android.R.id.content);

        myPrefs=getSharedPreferences("MYPREFS",Context.MODE_PRIVATE);
        String username = myPrefs.getString("username",null);
        String password = myPrefs.getString("password",null);
        String androidId = myPrefs.getString("androidId",null);
        String vehicleId = myPrefs.getString("vehicleId",null);

        // Check if shared preferences contains username and password then redirect to dashboard activity
        if(username != null && password != null ){
            startActivity(new Intent(this, Data.class));
        }
    } // onCreate

    //@Override
    //public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    //    if(t.onOptionsItemSelected(item))
    //        return true;

    //    return super.onOptionsItemSelected(item);
    //}

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
            // AndroidID
            androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            // UUID
            //androidId = UUID.randomUUID().toString();
            editor.putString("androidId", androidId);
            Log.d("prefs", androidId);
            editor.apply();

            builder = new AlertDialog.Builder(this);
            builder.setMessage("Success! You are logged in.");
            builder.setCancelable(false);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    startActivity(new Intent(view.getContext(), Navigation.class));
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

    public void OpenReg(View view) {
        // start register activity
        startActivity(new Intent(this,Register.class));
    } // OpenReg

    @Override
    public void onBackPressed() {
        // Do nothing
    }
}
