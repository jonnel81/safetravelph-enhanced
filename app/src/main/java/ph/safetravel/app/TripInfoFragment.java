package ph.safetravel.app;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class TripInfoFragment extends Fragment {
    AutoCompleteTextView origin, destination, purpose;
    ImageButton origPostButton, destPostButton, origDeleteButton, destDeleteButton;
    Spinner spinnerPurpose;
    private ArrayList<Marker> mMarkerArrayList;
    LatLng origLatLng, destLatLng;
    MarkerOptions markerOptions;
    GoogleMap mMap;
    private Marker mCurrentMarker;

    public TripInfoFragment() {
        // Required empty public constructor
    }
    /**
     * Create a new instance of this fragment
     * @return A new instance of fragment FirstFragment.
     */
    public static TripInfoFragment newInstance() {
        return new TripInfoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tripinfo, parent, false);

        // Post origin
        origPostButton = view.findViewById(R.id.btnOrigPost);
        View.OnClickListener origPostClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                origin = view.findViewById(R.id.atvOrig);
                String location = origin.getText().toString();
                if(location!=null && !location.equals("")){
                    new origGeocoderTask().execute(location);
                }
            }
        };
        origPostButton.setOnClickListener(origPostClickListener);

        // Delete origin
        origDeleteButton = view.findViewById(R.id.btnOrigDelete);
        View.OnClickListener origDeleteClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                origin = view.findViewById(R.id.atvOrig);
                origin.setText("");
                // Remove first marker, if existing
                if (mMarkerArrayList.size() > 0) {
                    for (int i = 0; i < mMarkerArrayList.size(); i++) {
                        Marker m = mMarkerArrayList.get(i);
                        if (m.getTitle().equals("orig")) {
                            Marker markerToRemove = mMarkerArrayList.get(i);
                            // remove the maker from list
                            mMarkerArrayList.remove(markerToRemove);
                            // remove the marker from the map
                            markerToRemove.remove();
                            break;
                        }
                    }
                }
            }
        };
        origDeleteButton.setOnClickListener(origDeleteClickListener);

        // Post destination
        destPostButton = view.findViewById(R.id.btnDestPost);
        View.OnClickListener destPostClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                destination = view.findViewById(R.id.atvDest);
                String location = destination.getText().toString();
                if(location!=null && !location.equals("")){
                    new destGeocoderTask().execute(location);
                }
            }
        };
        destPostButton.setOnClickListener(destPostClickListener);

        // Delete destination
        destDeleteButton = view.findViewById(R.id.btnDestDelete);
        View.OnClickListener destDeleteClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                destination = view.findViewById(R.id.atvDest);
                destination.setText("");
                // Remove second marker, if existing
                if (mMarkerArrayList.size() > 0) {
                    for (int i = 0; i < mMarkerArrayList.size(); i++) {
                        Marker m = mMarkerArrayList.get(i);
                        if (m.getTitle().equals("dest")) {
                            Marker markerToRemove = mMarkerArrayList.get(i);
                            // remove the maker from list
                            mMarkerArrayList.remove(markerToRemove);
                            // remove the marker from the map
                            markerToRemove.remove();
                            break;
                        }
                    }
                }
            }
        };
        destDeleteButton.setOnClickListener(destDeleteClickListener);

        // Get purpose
        //final AutoCompleteTextView purpose = (AutoCompleteTextView) findViewById(R.id.atvPurpose);
        //String[] purposeArray = getResources().getStringArray(R.array.purpose_array);
        //ArrayAdapter<String> arrayAdapter  = new ArrayAdapter<String>(this,
        //        android.R.layout.simple_spinner_item, purposeArray);
        //purpose.setThreshold(1); //will start working from first character
        //purpose.setEnabled(false);
        //purpose.setAdapter(arrayAdapter);
        //purpose.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(final View arg0) {
        //        purpose.showDropDown();
        //    }
        //});

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

        ArrayAdapter<String> adp = new ArrayAdapter<String>(getActivity().getBaseContext(), R.layout.spinner_item, purpose) {
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
        spinnerPurpose.setAdapter(adp);

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

        return view;

    } // onCreateView

    // An AsyncTask class for accessing the GeoCoding Web Service for origin
    private class origGeocoderTask extends AsyncTask<String, Void, List<Address>> {
        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getActivity().getBaseContext());
            List<Address> addresses = null;
            try {
                // Getting a maximum of 3 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 3);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            if(addresses==null || addresses.size()==0){
                Toast.makeText(getActivity().getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
            }
            // Remove first marker, if existing
            if (mMarkerArrayList.size() > 0) {
                for (int i = 0; i < mMarkerArrayList.size(); i++) {
                    Marker m = mMarkerArrayList.get(i);
                    if (m.getTitle().equals("orig")) {
                        Marker markerToRemove = mMarkerArrayList.get(i);
                        // remove the maker from list
                        mMarkerArrayList.remove(markerToRemove);
                        // remove the marker from the map
                        markerToRemove.remove();
                        break;
                    }
                }
            }
            // Add new marker on Google Map for matching address
            for(int i = 0; i < addresses.size(); i++){
                Address address = addresses.get(i);
                // Create an instance of GeoPoint, to display in Google Map
                origLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                String addressText = String.format("%s, %s, %s", address.getThoroughfare(), address.getSubLocality(), address.getLocality());
                // Add marker
                markerOptions = new MarkerOptions();
                markerOptions.position(origLatLng);
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.green_marker_big));
                markerOptions.title("orig");
                mCurrentMarker = mMap.addMarker(markerOptions);
                mMarkerArrayList.add(mCurrentMarker);
                origin.setText(origin.getText() + ", " + addressText);
                // Locate the origin
                if(i==0)
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(origLatLng));
            }
        }
    }

    // An AsyncTask class for accessing the GeoCoding Web Service for destination
    private class destGeocoderTask extends AsyncTask<String, Void, List<Address>> {
        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getActivity().getBaseContext());
            List<Address> addresses = null;
            try {
                // Getting a maximum of 3 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 3);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            if(addresses==null || addresses.size()==0){
                Toast.makeText(getActivity().getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
            }
            // Remove second marker, if existing
            if (mMarkerArrayList.size() > 0) {
                for (int i = 0; i < mMarkerArrayList.size(); i++) {
                    Marker m = mMarkerArrayList.get(i);
                    if (m.getTitle().equals("dest")) {
                        Marker markerToRemove = mMarkerArrayList.get(i);
                        // remove the maker from list
                        mMarkerArrayList.remove(markerToRemove);
                        // remove the marker from the map
                        markerToRemove.remove();
                        break;
                    }
                }
            }
            // Add new marker on Google Map for matching address
            for(int i = 0; i < addresses.size(); i++){
                Address address = addresses.get(i);
                // Creating an instance of GeoPoint, to display in Google Map
                destLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                String addressText = String.format("%s, %s, %s", address.getThoroughfare(), address.getSubLocality(), address.getLocality());
                // Add marker
                markerOptions = new MarkerOptions();
                markerOptions.position(destLatLng);
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.red_marker_big));
                markerOptions.title("dest");
                mCurrentMarker = mMap.addMarker(markerOptions);
                mMarkerArrayList.add(mCurrentMarker);
                destination.setText(destination.getText() + ", " + addressText);
                // Locate the destination
                if(i==0)
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(destLatLng));
            }
        }
    }

}