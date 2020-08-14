package ph.safetravel.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;


public class FleetInfoFragment extends Fragment {
    SharedPreferences myPrefs;
    Button closeButton;
    Button setFleetInfoButton, clearFleetInfoButton;
    ImageButton scanButton;
    //String origin, destination, purpose, mode, vehicleId, vehicleDetails;
    String vehicleId, vehicleDetails;
    public static TextView txtVehDetails;
    public static TextView txtVehId;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_fleetinfo, container, false);

        // Clear fleet info
        clearFleetInfoButton = (Button) view.findViewById(R.id.btnClear);
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
                        //editor.remove("origin");
                        //editor.remove("destination");
                        //editor.remove("purpose");
                        //editor.remove("mode");
                        editor.remove("vehicleId");
                        editor.remove("vehicleDetails");
                        editor.apply();
                        //txtOrigin.setText("");
                        //txtDestination.setText("");
                        //spinnerPurpose.setSelection(0);
                        //spinnerMode.setSelection(0);
                        txtVehId.setText("");
                        txtVehDetails.setText("");
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
        clearFleetInfoButton.setOnClickListener(clearfleetinfoClickListener);

        // Set trip info
        setFleetInfoButton = (Button) view.findViewById(R.id.btnSetFleetInfo);
        View.OnClickListener fleetinfoClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myPrefs = getActivity().getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = myPrefs.edit();

                // Get info
                //origin = txtOrigin.getText().toString();
                //destination = txtDestination.getText().toString();
                //purpose = spinnerPurpose.getSelectedItem().toString();
                //mode = spinnerMode.getSelectedItem().toString();
                vehicleId = txtVehId.getText().toString();
                vehicleDetails = txtVehDetails.getText().toString();

                //if(origin.equals("") || destination.equals("") || purpose.equals("") || mode.equals("")) {
                //    Toast.makeText(getContext(), "Please complete required fields.", Toast.LENGTH_SHORT).show();
                //} else {
                // Save to shared preferences
                //    editor.putString("origin", origin);
                //    editor.putString("destination", destination);
                //    editor.putString("purpose", purpose);
                //    editor.putString("mode", mode);
                    editor.putString("vehicleId", vehicleId);
                    editor.putString("vehicleDetails", vehicleDetails);
                    editor.apply();
                Toast.makeText(getContext(), "Fleet Info set.", Toast.LENGTH_SHORT).show();

                    // Save to database
//                }
            }
        };
        setFleetInfoButton.setOnClickListener(fleetinfoClickListener);

        // Scan results
        txtVehDetails = (TextView) view.findViewById(R.id.txtVehicleDetails);
        txtVehId = (TextView) view.findViewById(R.id.txtVehicleId);
        scanButton = (ImageButton) view.findViewById(R.id.btnScan);
        View.OnClickListener scanClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), ScanFleet.class));
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

        txtVehId.setText(myPrefs.getString("vehicleId",null));
        txtVehDetails.setText(myPrefs.getString("vehicleDetails",null));

        return view;

    } // onCreateView

}