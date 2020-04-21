package ph.safetravel.app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.view.View;
import android.widget.TextView;

public class Register extends AppCompatActivity implements AsyncResponse {
    EditText firstname, lastname, age, username, password;
    View view;
    AsyncResponse aR = Register.this;
    BackgroundWorker backgroundWorker = new BackgroundWorker(Register.this, aR);
    Context context;
    AlertDialog.Builder builder;
    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        firstname = findViewById(R.id.etFirstName);
        lastname = findViewById(R.id.etLastName);
        age = findViewById(R.id.etAge);
        username = findViewById(R.id.etUserName);
        password = findViewById(R.id.etPassword);
        view = findViewById(android.R.id.content);

        // Agree checkbox
        TextView link = findViewById(R.id.chkRegister);
        link.setText(
                Html.fromHtml("I agree with the <a href='https://safetravel.ph/testapp/termsandconditions.html'>Terms and Conditions</a>."));
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

    // -- not implemented ---
    //public void ShowMessageDialog(String str, final View view){
    //    builder = new AlertDialog.Builder(this);
    //    builder.setMessage(str);
    //    builder.setCancelable(false);
    //    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    //        @Override
    //        public void onClick(DialogInterface dialog, int which) {
    //            dialog.dismiss();
    //            Intent intent=new Intent(view.getContext(),Register.class);
    //            startActivity(intent);
    //        }
    //    });
    //    alertDialog = builder.create();
    //    alertDialog.setTitle("Status");
    //    alertDialog.setCancelable(false);
    //    alertDialog.setCanceledOnTouchOutside(false);
    //    alertDialog.setOwnerActivity(this);
    //    alertDialog.show();
    //} // ShowMessageDialog

    public void OnReg(View view) {
        String str_firstname = firstname.getText().toString();
        String str_lastname = lastname.getText().toString();
        String str_age = age.getText().toString();
        String str_username = username.getText().toString();
        String str_password = password.getText().toString();
        String type = "register";

        backgroundWorker.execute(type, str_firstname, str_lastname, str_age, str_username, str_password);
    } // OnReg

    @Override
    public void processFinish(String result) {
        // Register success
        if(result.equals("Register success")) {
            builder = new AlertDialog.Builder(this);
            builder.setMessage("Registration completed. You may login now.");
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

