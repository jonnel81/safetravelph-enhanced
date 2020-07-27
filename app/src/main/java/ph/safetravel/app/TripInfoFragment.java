package ph.safetravel.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

//import android.app.Fragment;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TripInfoFragment extends Fragment {
    TextView txtOrigin, txtDestination;
    Spinner spinnerPurpose, spinnerMode;
    private ArrayAdapter<String> adapterOrig;
    PlacesClient placesClient;
    Button closeButton;
    ImageButton scanButton;
    public static TextView txtVehDetails;
    public static TextView txtVehId;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_tripinfo, container, false);

        // Scan results
        txtVehDetails = (TextView) view.findViewById(R.id.txtVehicleDetails);
        txtVehId = (TextView) view.findViewById(R.id.txtVehicleId);
        scanButton = (ImageButton) view.findViewById(R.id.btnScan);
        View.OnClickListener scanClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Scan QR code
                Intent intent = new Intent(getContext(), Scan.class);
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
        //String apiKey = getString(R.string.google_maps_key);

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
        autocompleteFragmentOrig.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        autocompleteFragmentOrig.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                txtOrigin.setText(String.format(place.getName()));
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
        autocompleteFragmentDest.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        autocompleteFragmentDest.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                txtDestination.setText(String.format(place.getName()));
                autocompleteFragmentDest.getView().setVisibility(View.GONE);
                Log.i("Places", "Place: " + place.getName() + ", " + place.getId());
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
                    //Toast.makeText(getApplicationContext(), "Selected : " + selectedItemText, Toast.LENGTH_SHORT).show();
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

        return view;

    } // onCreateView

}