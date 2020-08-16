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


public class TripInfoFragment extends Fragment {
    SharedPreferences myPrefs;
    TextView txtOrigin, txtDestination;
    Spinner spinnerPurpose, spinnerMode;
    PlacesClient placesClient;
    Button closeButton;
    Button setTripInfoButton, clearTripInfoButton;
    ImageButton scanButton;
    public static TextView txtVehDetails;
    public static TextView txtVehId;
    String origin, destination, purpose, mode, vehicleId, vehicleDetails;
    String originLat, originLng, destinationLat, destinationLng;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_tripinfo, container, false);

        // Clear trip info
        clearTripInfoButton = (Button) view.findViewById(R.id.btnClear);
        View.OnClickListener cleartripinfoClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Clear the Trip Info?");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Clear shared preferences
                        myPrefs = getActivity().getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = myPrefs.edit();
                        editor.remove("origin");
                        editor.remove("originLat");
                        editor.remove("originLng");
                        editor.remove("destination");
                        editor.remove("destinationLat");
                        editor.remove("destinationLng");
                        editor.remove("purpose");
                        editor.remove("mode");
                        editor.remove("vehicleId");
                        editor.remove("vehicleDetails");
                        editor.apply();
                        txtOrigin.setText("");
                        txtDestination.setText("");
                        spinnerPurpose.setSelection(0);
                        spinnerMode.setSelection(0);
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
        clearTripInfoButton.setOnClickListener(cleartripinfoClickListener);

        // Set trip info
        setTripInfoButton = (Button) view.findViewById(R.id.btnSetTripInfo);
        View.OnClickListener tripinfoClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myPrefs = getActivity().getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = myPrefs.edit();

                // Get info
                origin = txtOrigin.getText().toString();
                destination = txtDestination.getText().toString();
                purpose = spinnerPurpose.getSelectedItem().toString();
                mode = spinnerMode.getSelectedItem().toString();
                vehicleId = txtVehId.getText().toString();
                vehicleDetails = txtVehDetails.getText().toString();

                if(origin.equals("") || destination.equals("") || purpose.equals("") || mode.equals("")) {
                    Toast.makeText(getContext(), "Please complete required fields.", Toast.LENGTH_SHORT).show();
                } else {
                    // Save to shared preferences
                    editor.putString("origin", origin);
                    editor.putString("originLat", originLat);
                    editor.putString("originLng", originLng);
                    editor.putString("destination", destination);
                    editor.putString("destinationLat", destinationLat);
                    editor.putString("destinationLng", destinationLng);
                    editor.putString("purpose", purpose);
                    editor.putString("mode", mode);
                    editor.putString("vehicleId", vehicleId);
                    editor.putString("vehicleDetails", vehicleDetails);
                    editor.apply();

                    // Get datetime
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                    String timeStamp = sdf.format(new Date());

                    // Save to database
                    TripHistoryDBHelper db = new TripHistoryDBHelper(getContext());

                    TripRecord tripRecord = new TripRecord(1, origin, originLat, originLng, destination, destinationLat, destinationLng, mode, purpose, vehicleId ,vehicleDetails, timeStamp);
                    db.addTripRecord(tripRecord);

                    Toast.makeText(getContext(), "Trip Info set.", Toast.LENGTH_SHORT).show();
                    //Toast.makeText(getContext(), "Trip Record saved.", Toast.LENGTH_SHORT).show();
                    //Toast.makeText(getContext(), tripRecord.getVehicleId(), Toast.LENGTH_LONG).show();
                }
            }
        };
        setTripInfoButton.setOnClickListener(tripinfoClickListener);

        // Scan results
        txtVehDetails = (TextView) view.findViewById(R.id.txtVehicleDetails);
        txtVehId = (TextView) view.findViewById(R.id.txtVehicleId);
        scanButton = (ImageButton) view.findViewById(R.id.btnScan);
        View.OnClickListener scanClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Scan QR code
                Intent intent = new Intent(getContext(), ScanTrip.class);
                startActivity(intent);
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
                ((Trip) getActivity()).restoreFab();
            }
        });

        // Places API
        String apiKey = getString(R.string.api_key);

        if (!Places.isInitialized()) {
            Places.initialize(getContext(), apiKey);
        }

        txtOrigin = view.findViewById(R.id.txtOrigin);
        txtDestination = view.findViewById(R.id.txtDest);

        // Create a new Places client instance.
        placesClient = Places.createClient(getContext());

        // Initialize the AutocompleteSupportFragment.
        final AutocompleteSupportFragment autocompleteFragmentOrig = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment_orig);

        final EditText etPlaceOrig = (EditText)autocompleteFragmentOrig.getView().findViewById(R.id.places_autocomplete_search_input);
        etPlaceOrig.setTextSize(14.0f);

        autocompleteFragmentOrig.setHint("Search Origin");
        autocompleteFragmentOrig.setCountry("PH");
        autocompleteFragmentOrig.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        autocompleteFragmentOrig.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                txtOrigin.setText(String.format(place.getName()));
                originLat = String.valueOf(place.getLatLng().latitude);
                originLng = String.valueOf(place.getLatLng().longitude);
                autocompleteFragmentOrig.getView().setVisibility(View.GONE);
                //Log.i("Places", "Place: " + place.getName() + ", " + place.getId());
            }

            @Override
            public void onError(Status status) {
                //Log.i("Places", "An error occurred: " + status);
            }
        });

        final AutocompleteSupportFragment autocompleteFragmentDest = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment_dest);

        final EditText etPlaceDest = (EditText)autocompleteFragmentDest.getView().findViewById(R.id.places_autocomplete_search_input);
        etPlaceDest.setTextSize(14.0f);

        autocompleteFragmentDest.setHint("Search Destination");
        autocompleteFragmentDest.setCountry("PH");
        autocompleteFragmentDest.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        autocompleteFragmentDest.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                txtDestination.setText(String.format(place.getName()));
                destinationLat = String.valueOf(place.getLatLng().latitude);
                destinationLng = String.valueOf(place.getLatLng().longitude);
                autocompleteFragmentDest.getView().setVisibility(View.GONE);
                //Log.i("Places", "Place: " + place.getName() + ", " + place.getId());
            }

            @Override
            public void onError(Status status) {
                //Log.i("Places", "An error occurred: " + status);
            }
        });

        // Hide placeautocomplete fragments
        autocompleteFragmentOrig.getView().setVisibility(View.GONE);
        autocompleteFragmentDest.getView().setVisibility(View.GONE);

        txtOrigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autocompleteFragmentOrig.getView().setVisibility(View.VISIBLE);
            }
        });

        txtDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autocompleteFragmentDest.getView().setVisibility(View.VISIBLE);
            }
        });

        // Purpose
        spinnerPurpose = view.findViewById(R.id.spinnerPurpose);
        String[] purpose = new String[]{
                "Trip Purpose",
                "Government work",
                "Private sector job",
                "Return to province",
                "Seek medical services",
                "Buy food and essential supplies",
                "Personal errand",
                "NGO/Religious activity",
                "School-related activity",
                "Others"
        };

        ArrayAdapter<String> purposeAdapter = new ArrayAdapter<String>(getActivity().getBaseContext(), R.layout.spinner_item, purpose) {
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
        purposeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPurpose.setAdapter(purposeAdapter);

        spinnerPurpose.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                // If user change the default selection
                // First item is disable and it is used for hint
                if (position > 0) {
                    // Notify the selected item text
                    //Toast.makeText(getContext(), "Selected : " + selectedItemText, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Mode
        spinnerMode = view.findViewById(R.id.spinnerMode);
        String[] mode = new String[]{
                "Trip Mode",
                "PUB",
                "Modern PUJ",
                "Traditional PUJ",
                "UV Express",
                "P2P",
                "Grab",
                "Ordinary Taxi",
                "Angkas",
                "Tricycle",
                "Walk",
                "Bike",
                "Company Service",
                "Others"
        };

        ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(getActivity().getBaseContext(), R.layout.spinner_item, mode) {
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
        modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMode.setAdapter(modeAdapter);

        spinnerMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        // Get shared preferences
        myPrefs = getActivity().getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);

        if(myPrefs.getString("origin",null)!=null) {
            txtOrigin.setText(myPrefs.getString("origin", null));
            originLat = myPrefs.getString("originLat", null);
            originLng = myPrefs.getString("originLng", null);
        }

        if(myPrefs.getString("destination",null)!=null) {
            txtDestination.setText(myPrefs.getString("destination",null));
            destinationLat = myPrefs.getString("destinationLat", null);
            destinationLng = myPrefs.getString("destinationLng", null);
        }

        if(myPrefs.getString("purpose",null)!=null) {
            int selectionPurpose= purposeAdapter.getPosition(myPrefs.getString("purpose",null));
            spinnerPurpose.setSelection(selectionPurpose);
        }

        if(myPrefs.getString("mode",null)!=null) {
            int selectionMode = modeAdapter.getPosition(myPrefs.getString("mode", null));
            spinnerMode.setSelection(selectionMode);
        }

        txtVehId.setText(myPrefs.getString("vehicleId",null));
        txtVehDetails.setText(myPrefs.getString("vehicleDetails",null));

        return view;

    } // onCreateView

}