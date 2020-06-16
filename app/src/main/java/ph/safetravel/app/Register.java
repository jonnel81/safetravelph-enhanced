package ph.safetravel.app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Register extends AppCompatActivity implements AsyncResponse {
    EditText firstname, lastname, age, username, password, retypepassword;
    View view;
    AsyncResponse aR = Register.this;
    BackgroundWorker backgroundWorker = new BackgroundWorker(Register.this, aR);
    Context context;
    AlertDialog.Builder builder;
    AlertDialog alertDialog;
    Spinner spinnerRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        firstname = findViewById(R.id.etFirstName);
        lastname = findViewById(R.id.etLastName);
        age = findViewById(R.id.etAge);
        username = findViewById(R.id.etUserName);
        password = findViewById(R.id.etPassword);
        retypepassword = findViewById(R.id.etRetypePassword);
        view = findViewById(android.R.id.content);

        spinnerRole = findViewById(R.id.spinnerRole);
        String[] roles = new String[]{
                "Select a role...",
                "Commuter",
                "Driver"
        };
        //final List<String> rolesList = new ArrayList<>(Arrays.asList(roles));

        ArrayAdapter<String> adp = new ArrayAdapter<String>(this,R.layout.spinner_item, roles) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                } else {
                    return true;
                }
            }
            @Override
            public View getDropDownView (int position, View convertView,
                                         ViewGroup parent){
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);               }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adp);

        spinnerRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
              @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                // If user change the default selection
                // First item is disable and it is used for hint
                if (position > 0) {
                    // Notify the selected item text
                    //Toast.makeText(getApplicationContext(), "Selected : " + selectedItemText, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                  // Do nothing
            }
        });

        // Agree checkbox
        TextView link = findViewById(R.id.chkRegister);
        link.setText(
                Html.fromHtml("Agree with <a href='https://safetravel.ph/testapp/termsandconditions.html'>Terms and Conditions</a>."));
        link.setMovementMethod(LinkMovementMethod.getInstance());

        // Disable register button
        Button buttonReg = findViewById(R.id.btnReg);
        buttonReg.setEnabled(false);

        // Listener of agree checkbox
        CheckBox checkAgree = findViewById(R.id.chkRegister);
        checkAgree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(buttonView.isChecked()){
                    findViewById(R.id.btnReg).setEnabled(true);
                 }
                else {
                    findViewById(R.id.btnReg).setEnabled(false);
                }
            }
        });

    } // onCreate

    public void OnReg(View view) {
        String str_firstname = firstname.getText().toString();
        String str_lastname = lastname.getText().toString();
        String str_age = age.getText().toString();
        String str_username = username.getText().toString();
        String str_password = password.getText().toString();
        String str_retypepassword = retypepassword.getText().toString();
        String str_role = spinnerRole.getSelectedItem().toString();
        String type = "register";

        // Check inputs
        if(!str_password.equals(str_retypepassword)) {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Register.this);
            builder.setMessage("Passwords do not match.");
            builder.setCancelable(false);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            androidx.appcompat.app.AlertDialog alertDialog = builder.create();
            alertDialog.setTitle("Status");
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        } else {
            backgroundWorker.execute(type, str_firstname, str_lastname, str_age, str_username, str_password, str_role);
        }

    } // OnReg

    @Override
    public void processFinish(String result) {
        // Register success
        if(result.equals("Register success")) {
            builder = new AlertDialog.Builder(this);
            builder.setMessage("Registration completed. You may log in now.");
            builder.setCancelable(false);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                    startActivity(new Intent(view.getContext(), MainActivity.class));
                }
            });
            alertDialog = builder.create();
            alertDialog.setTitle("Status");
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setOwnerActivity(this);
            alertDialog.show();
        } else {
            builder = new AlertDialog.Builder(this);
            builder.setMessage("Registration failed. Please try again.");
            builder.setCancelable(false);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                    startActivity(new Intent(view.getContext(), Register.class));
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

}

