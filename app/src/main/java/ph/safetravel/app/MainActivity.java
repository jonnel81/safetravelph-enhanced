package ph.safetravel.app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements AsyncResponse {
    EditText UserNameEt, PasswordEt;
    SharedPreferences myPrefs;
    AsyncResponse aR = MainActivity.this;
    BackgroundWorker backgroundWorker = new BackgroundWorker(MainActivity.this, aR);
    String username = "";
    String password = "";
    String androidId = "";
    View view;
    ImageView imgPassword;
    private boolean isShowPassword = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        UserNameEt = findViewById(R.id.etUserName);
        PasswordEt = findViewById(R.id.etPassword);
        view = findViewById(android.R.id.content);

        imgPassword = findViewById(R.id.imagePassword);
        imgPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShowPassword) {
                    PasswordEt.setTransformationMethod(new PasswordTransformationMethod());
                    imgPassword.setImageResource(R.drawable.ic_eyeshow);
                    isShowPassword = false;
                }else{
                    PasswordEt.setTransformationMethod(null);
                    imgPassword.setImageResource(R.drawable.ic_eyehide);
                    isShowPassword = true;
                }
            }
        });

        myPrefs=getSharedPreferences("MYPREFS",Context.MODE_PRIVATE);
        String username = myPrefs.getString("username",null);
        String password = myPrefs.getString("password",null);

        // Check if shared preferences contains username and password then redirect to board activity
        if(username != null && password != null ){
            startActivity(new Intent(MainActivity.this, Board.class));
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

        if (isConnected()) {
            backgroundWorker.execute(type, username, password);
        }else {
            Toast.makeText(MainActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
        }
    } // OnLogin

    @Override
    public void processFinish(String result) {
        String firstname="", lastname="", role="", age="", contactnumber="", datereg="";
        // Login success
        //if (result.equals("Login success")) {
        if (!result.equals("Login failed")) {
            // Get user details
            //Log.d("result", result);
            try {
                JSONObject jObj = new JSONObject(result);
                //Log.d("json", result);

                String usernameString = jObj.getString("username");
                firstname = jObj.getString("firstname");
                lastname = jObj.getString("lastname");
                role = jObj.getString("role");
                age = jObj.getString("age");
                contactnumber = jObj.getString("contactnumber");
                datereg = jObj.getString("datereg");

            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            // Update shared preferences
            SharedPreferences.Editor editor = myPrefs.edit();

            // Encrypt username and password before storing in shared preferences
            try {
                String encrypted_username = AESUtils.encrypt(username);
                editor.putString("username", encrypted_username);
                String encrypted_password = AESUtils.encrypt(password);
                editor.putString("password", encrypted_password);
            } catch (Exception e) {
                e.printStackTrace();
            }
            editor.putString("firstname", firstname);
            editor.putString("lastname", lastname);
            editor.putString("role", role);
            editor.putString("age", age);
            editor.putString("contactnumber", contactnumber);
            editor.putString("datereg", datereg);

            // Set AndroidId
            androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            //---Alternative is UUID
            //androidId = UUID.randomUUID().toString();
            // Encrypt androidId
            try {
                String encrypted_androidId = AESUtils.encrypt(androidId);
                editor.putString("androidId", encrypted_androidId);
            } catch (Exception e) {
                e.printStackTrace();
            }
            editor.apply();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Hello, " + firstname + ".  Have a safe travel!");
            builder.setCancelable(false);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    startActivity(new Intent(MainActivity.this, Board.class));

                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.setTitle("Login");
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setOwnerActivity(this);
            alertDialog.show();

        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Wrong username/password. Please try again.");
            builder.setCancelable(false);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    startActivity(new Intent(view.getContext(), MainActivity.class));
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.setTitle("Login");
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setOwnerActivity(this);
            alertDialog.show();
        }
    } // processFinish

    public void OpenReg(View view) {
        startActivity(new Intent(MainActivity.this, Register.class));
    } // OpenReg

    @Override
    public void onBackPressed() {
        // Do nothing
    }

    // Check if there's internet
    public boolean isConnected() {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
        return connected;
    }
}
