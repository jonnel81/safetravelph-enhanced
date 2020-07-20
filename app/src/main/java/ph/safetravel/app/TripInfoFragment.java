package ph.safetravel.app;

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


public class TripInfoFragment extends Fragment {
    TextView origin, destination, purpose;
    Spinner spinnerPurpose, spinnerMode;
    private ArrayAdapter<String> adapterOrig;
    PlacesClient placesClient;
    Button closeButton;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_tripinfo, container, false);

        origin = view.findViewById(R.id.txtOrigin);
        destination = view.findViewById(R.id.txtDest);

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
        //String apiKey = getString(R.string.api_key);
        String apiKey = getString(R.string.google_maps_key);

        if (!Places.isInitialized()) {
            Places.initialize(getContext(), apiKey);
        }

        // Create a new Places client instance.
        placesClient = Places.createClient(getContext());

        // Initialize the AutocompleteSupportFragment.
        final AutocompleteSupportFragment autocompleteFragmentOrig = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment_orig);

        //((EditText)autocompleteFragment.getView().findViewById(R.id.places_autocomplete_search_input)).setTextSize(14.0f);
        final EditText etPlaceOrig = (EditText)autocompleteFragmentOrig.getView().findViewById(R.id.places_autocomplete_search_input);
        etPlaceOrig.setTextSize(14.0f);

        autocompleteFragmentOrig.setHint("Search Origin");
        autocompleteFragmentOrig.setCountry("PH");
        autocompleteFragmentOrig.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        autocompleteFragmentOrig.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                origin.setText(String.format(place.getName()));
                //Log.i("Places", "Place: " + place.getName() + ", " + place.getId());
            }

            @Override
            public void onError(Status status) {
                //Log.i("Places", "An error occurred: " + status);
            }
        });

        AutocompleteSupportFragment autocompleteFragmentDest = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment_dest);

        //((EditText)autocompleteFragmentDest.getView().findViewById(R.id.places_autocomplete_search_input)).setTextSize(14.0f);
        final EditText etPlaceDest = (EditText)autocompleteFragmentDest.getView().findViewById(R.id.places_autocomplete_search_input);
        etPlaceDest.setTextSize(14.0f);

        autocompleteFragmentDest.setHint("Search Destination");
        autocompleteFragmentDest.setCountry("PH");
        autocompleteFragmentDest.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        autocompleteFragmentDest.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                destination.setText(String.format(place.getName()));
                Log.i("Places", "Place: " + place.getName() + ", " + place.getId());
            }

            @Override
            public void onError(Status status) {
                Log.i("Places", "An error occurred: " + status);
            }
        });

        //mGoogleApiClient = new GoogleApiClient.Builder(getContext())
        //        .enableAutoManage(getActivity(), (GoogleApiClient.OnConnectionFailedListener) this)
        //        .addApi(Places.GEO_DATA_API)
        //        .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
        //        .build();

        //mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
        //        .addConnectionCallbacks(this)
        //        .addOnConnectionFailedListener(this)
        //        .addConnectionCallbacks(this)
        //        //.addApi(LocationServices.API)
        //        .addApi(Places.GEO_DATA_API)
        //        .build();

        //AutoCompleteTextView origText = view.findViewById(R.id.atvOrig);
        //adapterOrig = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line);
        //origText.setAdapter(adapterOrig);

        /*
        origText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();

                //mMap is a GoogleMap object. Alternatively, you can initialize a
                //LatLngBounds object directly through its constructor

                //FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                //SupportMapFragment supportMapFragment = (SupportMapFragment) fragmentManager
                //        .findFragmentById(R.id.mapFragTrip);
                //mMap = supportMapFragment.getMap();


                ((SupportMapFragment)  getActivity().getSupportFragmentManager().findFragmentById(R.id.mapFragTrip)).getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        mMap  = googleMap;
                    }
                });

                LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                Log.e("TEST", bounds.toString());

                AutocompleteFilter filter = new AutocompleteFilter.Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_NONE)
                    .build();

                PendingResult<AutocompletePredictionBuffer> autocompleteResult =
                    Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient, query, bounds, filter);

                autocompleteResult.setResultCallback(new ResultCallback<AutocompletePredictionBuffer>() {
                    @Override
                    public void onResult(@NonNull AutocompletePredictionBuffer autocompletePredictions) {
                        for (int i = 0; i < autocompletePredictions.getCount(); i++) {
                            adapterOrig.add(autocompletePredictions.get(0).getFullText(null).toString());
                        }
                        autocompletePredictions.release();
                        adapterOrig.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {
                //

            }

        });*/

        //-----------------------------------------------------------
        spinnerPurpose = view.findViewById(R.id.spinnerPurpose);
        String[] purpose = new String[]{
                "Select a purpose...",
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

        //-----------------------------------------------------------
        spinnerMode = view.findViewById(R.id.spinnerMode);
        String[] mode = new String[]{
                "Select a mode...",
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