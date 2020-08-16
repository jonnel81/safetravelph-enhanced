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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class FleetInfoFragment extends Fragment {
    SharedPreferences myPrefs;
    Button closeButton;
    Button setFleetInfoButton, clearFleetInfoButton;
    ImageButton scanButton;
    Spinner spinnerRoute;
    String route;
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
                        editor.remove("route");
                        editor.remove("vehicleId");
                        editor.remove("vehicleDetails");
                        editor.apply();
                        spinnerRoute.setSelection(0);
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
                route = spinnerRoute.getSelectedItem().toString();
                vehicleId = txtVehId.getText().toString();
                vehicleDetails = txtVehDetails.getText().toString();

                if(route.equals("") || vehicleId.equals("") || vehicleDetails.equals("")) {
                    Toast.makeText(getContext(), "Please complete required fields.", Toast.LENGTH_SHORT).show();
                } else {
                    // Save to shared preferences
                    editor.putString("route", route);
                    editor.putString("vehicleId", vehicleId);
                    editor.putString("vehicleDetails", vehicleDetails);
                    editor.apply();

                    // Get datetime
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                    String timeStamp = sdf.format(new Date());

                    // Save to database
                    FleetHistoryDBHelper db = new FleetHistoryDBHelper(getContext());

                    FleetRecord fleetRecord = new FleetRecord(1, route, "", "", vehicleId ,vehicleDetails, timeStamp);
                    db.addTripRecord(fleetRecord);

                    Toast.makeText(getContext(), "Fleet Info set.", Toast.LENGTH_SHORT).show();
                }
            }
        };
        setFleetInfoButton.setOnClickListener(fleetinfoClickListener);

        // Route
        spinnerRoute = view.findViewById(R.id.spinnerRoute);
        String[] route = new String[]{
                "Route",
                "Monumento - Balagtas via McArthur",
                "Monumento - PITX via R10, R1",
                "Monumento - Valenzuela Gateway Complex via NLEX",
                "North EDSA - Fairview via Quirino Highway",
                "Quezon Avenue - Angat via Commonwealth",
                "Quezon Avenue - EDSA Taft",
                "Quezon Avenue - Montalban via Commonwealth",
                "Cubao (Araneta Bus Station) - Montalban via Aurora Blvd, JP Rizal",
                "Cubao (Araneta Bus Station) - Antipolo via Aurora Blvd, Marcos Highway",
                "Cubao (Araneta Bus Station) - Doroteo Jose via Aurora Blvd,  Magsaysay Blvd",
                "Gilmore - Taytay via Ortigas Avenue",
                "Kalentong - Pasig via Shaw Blvd",
                "Buendia - Bonifacio Global City via Buendia Ave, Kalayaan Flyover",
                "Ayala - Alabang via Bicutan, Sucat",
                "Ayala - Biñan via Bicutan, Sucat, Alabang",
                "Ayala - FTI",
                "Monumento - EDSA Taft via Rizal Ave.",
                "PITX - NAIA",
                "North EDSA - Bonifacio Global City via Luzon Ave. flyover, C-5",
                "Monumento - Meycauyan via NLEX (Meycauyan Exit)",
                "Monumento - SJDM via NLEX (Marilao Exit)",
                "Monumento - Angat via NLEX (Bocaue Exit)",
                "PITX - Sucat via Dr. Arcadio Santos Ave.",
                "PITX - Alabang via CAVITEX, Alabang-Zapote Rd",
                "Bonifacio Global City - Alabang via SLEX, Bicutan, Sucat",
                "PITX - Naic via CAVITEX, Antero Soriano Highway",
                "PITX - Trece Martires via CAVITEX, Antero Soriano Highway, Tanza-Trece Martires Rd",
                "PITX - Dasmariñas via CAVITEX, Emilio Aguinaldo Highway",
                "PITX - Biñan via CAVITEX, Emilio Aguinaldo Highway, Governor's Drive",
                "Others"
        };

        ArrayAdapter<String> routeAdapter = new ArrayAdapter<String>(getActivity().getBaseContext(), R.layout.spinner_item, route) {
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
        routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoute.setAdapter(routeAdapter);

        spinnerRoute.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        if(myPrefs.getString("route",null)!=null) {
            int selectionMode = routeAdapter.getPosition(myPrefs.getString("route", null));
            spinnerRoute.setSelection(selectionMode);
        }

        txtVehId.setText(myPrefs.getString("vehicleId",""));
        txtVehDetails.setText(myPrefs.getString("vehicleDetails",""));

        return view;

    } // onCreateView

}