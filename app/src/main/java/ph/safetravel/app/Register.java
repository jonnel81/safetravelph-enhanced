package ph.safetravel.app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
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
    EditText firstname, lastname, age, contactnumber, username, password, retypepassword;
    AsyncResponse aR = Register.this;
    BackgroundWorker backgroundWorker = new BackgroundWorker(Register.this, aR);
    AlertDialog.Builder builder;
    AlertDialog alertDialog;
    Spinner spinnerRole;
    Spinner spinnerQuestion1, spinnerQuestion2, spinnerQuestion3;
    EditText answer1, answer2, answer3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firstname = findViewById(R.id.etFirstName);
        lastname = findViewById(R.id.etLastName);
        age = findViewById(R.id.etAge);
        contactnumber = findViewById(R.id.etContactNumber);
        username = findViewById(R.id.etUserName);
        password = findViewById(R.id.etPassword);
        retypepassword = findViewById(R.id.etRetypePassword);
        answer1 = findViewById(R.id.etAnswer1);
        answer2 = findViewById(R.id.etAnswer2);
        answer3 = findViewById(R.id.etAnswer3);

        //==============================================================================
        spinnerRole = findViewById(R.id.spinnerRole);
        String[] roles = new String[]{
                "Select a role...",
                "Commuter",
                "Driver",
                "Conductor",
                "Driver-Operator"
        };

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

        //==============================================================================
        spinnerQuestion1 = findViewById(R.id.spinnerQuestion1);
        String[] question1 = new String[]{
                "Select a Question",
                "What is the first and last name of your first boyfriend or girlfriend?",
                "Which phone number do you remember most from your childhood?",
                "What was your favorite place to visit as a child?",
                "Who is your favorite actor, musician, or artist?",
                "What is the name of your favorite pet?",
                "In what city were you born?",
                "What high school did you attend?",
                "What is the name of your first school?",
                "What is your favorite movie?",
                "What is your mother's maiden name?",
                "What street did you grow up on?",
                "What was the make of your first car?",
                "When is your anniversary?",
                "What is your favorite color?",
                "What is your father's middle name?",
                "What is the name of your first grade teacher?",
                "What was your high school mascot?",
                "Which is your favorite web browser?"
        };

        ArrayAdapter<String> question1Adapter = new ArrayAdapter<String>(this,R.layout.spinner_item, question1) {
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
        question1Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerQuestion1.setAdapter(question1Adapter);

        spinnerQuestion1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        //==============================================================================
        spinnerQuestion2 = findViewById(R.id.spinnerQuestion2);
        String[] question2 = new String[]{
                "Select a Question",
                "What is the first and last name of your first boyfriend or girlfriend?",
                "Which phone number do you remember most from your childhood?",
                "What was your favorite place to visit as a child?",
                "Who is your favorite actor, musician, or artist?",
                "What is the name of your favorite pet?",
                "In what city were you born?",
                "What high school did you attend?",
                "What is the name of your first school?",
                "What is your favorite movie?",
                "What is your mother's maiden name?",
                "What street did you grow up on?",
                "What was the make of your first car?",
                "When is your anniversary?",
                "What is your favorite color?",
                "What is your father's middle name?",
                "What is the name of your first grade teacher?",
                "What was your high school mascot?",
                "Which is your favorite web browser?"
        };

        ArrayAdapter<String> question2Adapter = new ArrayAdapter<String>(this,R.layout.spinner_item, question2) {
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
        question2Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerQuestion2.setAdapter(question1Adapter);

        spinnerQuestion2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        //==============================================================================
        spinnerQuestion3 = findViewById(R.id.spinnerQuestion3);
        String[] question3 = new String[]{
                "Select a Question",
                "What is the first and last name of your first boyfriend or girlfriend?",
                "Which phone number do you remember most from your childhood?",
                "What was your favorite place to visit as a child?",
                "Who is your favorite actor, musician, or artist?",
                "What is the name of your favorite pet?",
                "In what city were you born?",
                "What high school did you attend?",
                "What is the name of your first school?",
                "What is your favorite movie?",
                "What is your mother's maiden name?",
                "What street did you grow up on?",
                "What was the make of your first car?",
                "When is your anniversary?",
                "What is your favorite color?",
                "What is your father's middle name?",
                "What is the name of your first grade teacher?",
                "What was your high school mascot?",
                "Which is your favorite web browser?"
        };

        ArrayAdapter<String> question3Adapter = new ArrayAdapter<String>(this,R.layout.spinner_item, question3) {
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
        question3Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerQuestion3.setAdapter(question1Adapter);

        spinnerQuestion3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        String str_contactnumber = contactnumber.getText().toString();
        String str_username = username.getText().toString();
        String str_password = password.getText().toString();
        String str_retypepassword = retypepassword.getText().toString();
        String str_role = spinnerRole.getSelectedItem().toString();
        String str_question1 = spinnerQuestion1.getSelectedItem().toString();
        String str_question2 = spinnerQuestion2.getSelectedItem().toString();
        String str_question3 = spinnerQuestion3.getSelectedItem().toString();
        String str_answer1 = answer1.getText().toString();
        String str_answer2 = answer2.getText().toString();
        String str_answer3 = answer3.getText().toString();
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
            alertDialog.setTitle("Register");
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        } else if(str_question1.equals(str_question2) || str_question1.equals(str_question3) || str_question2.equals(str_question3))  {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Register.this);
            builder.setMessage("Please select a different set of questions.");
            builder.setCancelable(false);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            androidx.appcompat.app.AlertDialog alertDialog = builder.create();
            alertDialog.setTitle("Register");
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        } else {
            if (isConnected()) {
                backgroundWorker.execute(type, str_firstname, str_lastname, str_age, str_contactnumber, str_username, str_password, str_role, str_question1, str_answer1, str_question2, str_answer2, str_question3, str_answer3);

            }else {
                Toast.makeText(Register.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            }
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
                    startActivity(new Intent(Register.this, MainActivity.class));
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
                    startActivity(new Intent(Register.this, Register.class));
                }
            });
            alertDialog = builder.create();
            alertDialog.setTitle("Register");
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setOwnerActivity(this);
            alertDialog.show();
        }
    } // processFinish

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

