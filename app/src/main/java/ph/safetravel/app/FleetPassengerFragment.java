package ph.safetravel.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class FleetPassengerFragment extends Fragment {
    SharedPreferences myPrefs;
    Button closeButton;
    Button sendButton;
    Button setFleetPassengerButton, clearFleetPassengerButton;
    ImageButton scanButton;
    String userId, userDetails;
    public static TextView txtUserDetails;
    public static TextView txtUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_fleetpassenger, container, false);

        // Clear fleet passenger
        clearFleetPassengerButton = (Button) view.findViewById(R.id.btnClear);
        View.OnClickListener clearfleetinfoClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Clear the Fleet Info?");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Clear shared preferences
                        myPrefs = getActivity().getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = myPrefs.edit();
                        //editor.remove("route");
                        //editor.remove("vehicleId");
                        //editor.remove("vehicleDetails");
                        //editor.apply();
                        //txtVehId.setText("");
                        //txtVehDetails.setText("");
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.setTitle("Status");
                alertDialog.setCancelable(false);
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
            }
        };
        clearFleetPassengerButton.setOnClickListener(clearfleetinfoClickListener);

        // Send
        sendButton = (Button) view.findViewById(R.id.btnSend);
        View.OnClickListener sendClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int secs = 5; // Delay in seconds

                Utils.delay(secs, new Utils.DelayCallback() {
                    @Override
                    public void afterDelay() {
                        // Do something after delay
                        Toast.makeText(getContext(), "Sending message", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
        sendButton.setOnClickListener(sendClickListener);

        // Set passenger board
        //setFleetPassengerButton = (Button) view.findViewById(R.id.btnPassengerBoard);
        //View.OnClickListener fleetinfoClickListener = new View.OnClickListener() {
        //    @Override
        //    public void onClick(View view) {
        //        myPrefs = getActivity().getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);
        //        SharedPreferences.Editor editor = myPrefs.edit();
//
        //        // Get info
        //        userId = txtUserId.getText().toString();
        //        userDetails = txtUserDetails.getText().toString();
//
        //        if(userId.equals("") || userDetails.equals("")) {
        //            Toast.makeText(getContext(), "Please complete required fields.", Toast.LENGTH_SHORT).show();
        //        } else {
        //            // Save to shared preferences
        //            //editor.putString("vehicleId", vehicleId);
        //            //editor.putString("vehicleDetails", vehicleDetails);
        //            //editor.apply();
//
        //            // Get datetime
        //            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        //            sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        //            String timeStamp = sdf.format(new Date());
//
        //            // Save to database
        //            //FleetHistoryDBHelper db = new FleetHistoryDBHelper(getContext());
//
        //            //FleetRecord fleetRecord = new FleetRecord(1, route, "", "", vehicleId ,vehicleDetails, timeStamp);
        //            //db.addTripRecord(fleetRecord);
//
        //            Toast.makeText(getContext(), "Passenger Info set.", Toast.LENGTH_SHORT).show();
        //        }
        //    }
        //};
        //setFleetPassengerButton.setOnClickListener(fleetinfoClickListener);

        // Scan results
        txtUserDetails = (TextView) view.findViewById(R.id.txtUserDetails);
        txtUserId = (TextView) view.findViewById(R.id.txtUserId);
        scanButton = (ImageButton) view.findViewById(R.id.btnScan);
        View.OnClickListener scanClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), ScanPassenger.class));
            }
        };
        scanButton.setOnClickListener(scanClickListener);

        // Close button
        closeButton = (Button) view.findViewById(R.id.btnClose);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close fragment view
                container.removeView(view);
                container.setVisibility(View.GONE);

                // Restore Fab
                ((Fleet) getActivity()).restoreFleetFab();
            }
        });

        // Get shared preferences
        myPrefs = getActivity().getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);
        //txtUserId.setText(myPrefs.getString("vehicleId",""));
        //txtUserDetails.setText(myPrefs.getString("vehicleDetails",""));

        return view;

    } // onCreateView

}