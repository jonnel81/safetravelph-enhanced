package ph.safetravel.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;

import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.protobuf.CodedOutputStream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.Toolbar;
//import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import ph.safetravel.app.databinding.ActivityTripBinding;
import ph.safetravel.app.protos.Passenger;


public class Trip extends AppCompatActivity implements OnMapReadyCallback, AdapterView.OnItemClickListener {
    SharedPreferences myPrefs;
    View view;
    MqttAndroidClient client;
    MqttConnectOptions options;
    MqttClientPersistence clientPersistence;
    Vibrator vibrator;
    String topicStr = "test";
    String TAG="Mqtt";
    Context context;
    String MqttHost = "tcp://mqtt.safetravel.ph:8883";
    final String Username = "mqtt";
    final String Password = "mqtt";
    String clientId;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private boolean isGPS = false;
    private boolean isContinue = false;
    ToggleButton tButton;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    MapFragment mMapFragment;
    GoogleMap mMap;
    //AutoCompleteTextView origin, destination, purpose;
    //ImageButton origPostButton, destPostButton, origDeleteButton, destDeleteButton;
    //Spinner spinnerPurpose;
    //LatLng origLatLng, destLatLng;
    //MarkerOptions markerOptions;
    private Toolbar toolbar;
    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;
    //private Marker mCurrentMarker;
    private ArrayList<Marker> mMarkerArrayList;
    //Spinner spinnerPurpose;
    ProgressBar pgsBar;
    ActivityTripBinding bi;
    boolean isRotate = false;

    @SuppressLint({"ServiceCast", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle  savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_trip);

        //currentFragment = LoginFragment.getInstance("Rohit");
        //getSupportFragmentManager()
        //        .beginTransaction()
        //        .add(R.id.fragmentHolder, currentFragment, "LOGIN_TAG")
        //        .commit();

        // Floating action bar
        bi = DataBindingUtil.setContentView(this, R.layout.activity_trip);
        ViewAnimation.init(bi.fabTripInfo);
        ViewAnimation.init(bi.fabTripFeeds);

        bi.fabTripAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRotate = ViewAnimation.rotateFab(v, !isRotate);
                if(isRotate){
                    ViewAnimation.showIn(bi.fabTripInfo);
                    ViewAnimation.showIn(bi.fabTripFeeds);
                }else{
                    ViewAnimation.showOut(bi.fabTripInfo);
                    ViewAnimation.showOut(bi.fabTripFeeds);
                }
            }
        });

        bi.fabTripInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Trip.this, "Info", Toast.LENGTH_SHORT).show();
                //Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });

        bi.fabTripFeeds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Trip.this, "Feeds", Toast.LENGTH_SHORT).show();
            }
        });

        // Tollbar
        toolbar = (Toolbar) findViewById(R.id.toolbarTrip);
        toolbar.inflateMenu(R.menu.main_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId()==R.id.scan) {
                    startActivity(new Intent(getApplicationContext(), BarcodeReader.class));
                }
                if(item.getItemId()==R.id.settings)
                {
                    // do something
                }
                return false;
            }
        });

        //setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);

        // Progress bar
        pgsBar =  (ProgressBar) findViewById(R.id.progressBarTrip);
        pgsBar.setVisibility(View.INVISIBLE);
        pgsBar.setScaleY(3f);

        // Drawer
        dl = findViewById(R.id.drawer_layout);
        t = new ActionBarDrawerToggle(this, dl, toolbar, R.string.Open, R.string.Close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //actions upon opening slider
                //presently nothing
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                //actions upon closing slider
                //presently nothing
            }
        };
        t.setDrawerIndicatorEnabled(true);
        dl.addDrawerListener(t);
        t.syncState();

        // Navigation
        nv = (NavigationView)findViewById(R.id.nav_view);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch(id) {
                    case R.id.myprofile:
                    {
                        Toast.makeText(Trip.this, "My Profile", Toast.LENGTH_SHORT).show();
                    }
                    case R.id.settings:
                    {
                        Toast.makeText(Trip.this, "Settings", Toast.LENGTH_SHORT).show();
                    }
                    case R.id.editprofile:
                    {
                        Toast.makeText(Trip.this, "Edit Profile", Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }
        });

        // Menu
        //toolbar.inflateMenu(R.menu.main_menu);
        //toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
        //    @Override
        //    public boolean onMenuItemClick(MenuItem item) {
        //    if(item.getItemId()==R.id.item1) {
        //        startActivity(new Intent(getApplicationContext(), BarcodeReader.class));
        //    }
        //    if(item.getItemId()==R.id.item2)
        //    {
        //        // do something
        //    }
        //    return false;
        //    }
        //});

        // Post origin
        /*origPostButton = findViewById(R.id.btnOrigPost);
        OnClickListener origPostClickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                origin = findViewById(R.id.atvOrig);
                String location = origin.getText().toString();
                if(location!=null && !location.equals("")){
                    new origGeocoderTask().execute(location);
                }
            }
        };
        origPostButton.setOnClickListener(origPostClickListener);

        // Delete origin
        origDeleteButton = findViewById(R.id.btnOrigDelete);
        OnClickListener origDeleteClickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                origin = findViewById(R.id.atvOrig);
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
        destPostButton = findViewById(R.id.btnDestPost);
        OnClickListener destPostClickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                destination = findViewById(R.id.atvDest);
                String location = destination.getText().toString();
                if(location!=null && !location.equals("")){
                    new destGeocoderTask().execute(location);
                }
            }
        };
        destPostButton.setOnClickListener(destPostClickListener);

        // Delete destination
        destDeleteButton = findViewById(R.id.btnDestDelete);
        OnClickListener destDeleteClickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                destination = findViewById(R.id.atvDest);
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
        */

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

        /*
        spinnerPurpose = findViewById(R.id.spinnerPurpose);
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

        ArrayAdapter<String> adp = new ArrayAdapter<String>(this,R.layout.spinner_item, purpose) {
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
        */

        // Map fragment
        mMapFragment = MapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.mapFragTrip, mMapFragment);
        fragmentTransaction.commit();
        mMapFragment.getMapAsync(this);
        // FuseLocationProviderClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Location request for GPS
        locationRequest= LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5 * 1000);
        locationRequest.setFastestInterval(1 * 1000);

        // Enable GPS
        new GpsUtils(this).turnGPSOn(new GpsUtils.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                // turn on GPS
                isGPS = isGPSEnable;
            }
        });

        // Get permission for location
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    AppConstants.LOCATION_REQUEST);
        }

        //view = findViewById(android.R.id.content);
        //subText = (EditText) findViewById(R.id.txtMessage);
        //vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // Toogle Button
        tButton = findViewById(R.id.toggleTrip);
        tButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked && isGPS) {
                    connectBroker();
                    startLocationUpdates();
                    pgsBar.setVisibility(View.VISIBLE);
                } else {
                    stopLocationUpdates();
                    disconnectBroker();
                    pgsBar.setVisibility(View.INVISIBLE);
                }
            }
        });

        // Mqtt host
        clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), MqttHost, clientId);
        options = new MqttConnectOptions();
        options.setUserName(Username);
        options.setPassword(Password.toCharArray());

        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Toast.makeText(getApplicationContext(),"Connected", Toast.LENGTH_SHORT).show();
                    subscribeTopic("#");
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(getApplicationContext(),"Connected Failed", Toast.LENGTH_SHORT).show();
                    exception.printStackTrace();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.i(TAG, "connection lost");
            }

            @Override
            public int hashCode() {
                return super.hashCode();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                //Log.i(TAG, "topic: " + topic + ", msg: " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                //Log.i(TAG, "msg delivered");
            }
        }); // mqtt client callback

        // Bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(3);
        menuItem.setChecked(true);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_logout: {
                        // Dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(Trip.this);
                        builder.setMessage("Are you sure you want to logout?");
                        builder.setCancelable(false);
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Clear shared preferences
                                myPrefs = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = myPrefs.edit();
                                editor.clear();
                                editor.apply();
                                closeApp();
                                // Go to main activity
                                Intent intent0 = new Intent(Trip.this, MainActivity.class);
                                startActivity(intent0);
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent3 = new Intent(Trip.this, Trip.class);
                                startActivity(intent3);
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.setTitle("Status");
                        alertDialog.setCancelable(false);
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.show();
                        break;
                    }
                    case R.id.navigation_info: {
                        closeApp();
                        Intent intent1 = new Intent(Trip.this, Report.class);
                        startActivity(intent1);
                        break;
                    }
                    case R.id.navigation_report: {
                        closeApp();
                        Intent intent2 = new Intent(Trip.this, Report.class);
                        startActivity(intent2);
                        break;
                    }
                    case R.id.navigation_trip: {
                        break;
                    }
                    case R.id.navigation_fleet: {
                        closeApp();
                        Intent intent4 = new Intent(Trip.this, Fleet.class);
                        startActivity(intent4);
                        break;
                    }
                }
                return true;
            }
        });
    } // onCreate

    /*
    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new TripInfoFragment(), "Test");
        adapter.addFrag(new TripInfoFragment(), "Info");
        adapter.addFrag(new TripInfoFragment(), "Feeds");
        viewPager.setAdapter(adapter);
    }
    */

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // fetch the user selected value
        String item = parent.getItemAtPosition(position).toString();
        // create Toast with user selected value
        Toast.makeText(Trip.this, "Selected Item is: \t" + item, Toast.LENGTH_LONG).show();
        // set user selected value to the TextView
        //tvDisplay.setText(item);
    }

    /*
    // An AsyncTask class for accessing the GeoCoding Web Service for origin
    private class origGeocoderTask extends AsyncTask<String, Void, List<Address>> {
        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getBaseContext());
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
                Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
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
            Geocoder geocoder = new Geocoder(getBaseContext());
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
                Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
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
    */

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AppConstants.LOCATION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    if (isContinue) {
                        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    } else {
                        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @SuppressLint("DefaultLocale")
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    //LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                } else {
                                    mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                                }
                            }
                        });
                    }
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    } // onRequestPermissionsResult

    LocationCallback locationCallback = new LocationCallback() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                //Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                mLastLocation = location;
                //sendTrack();
                if (mLastLocation != null) {
                    String lat = String.valueOf(location.getLatitude());
                    String lng = String.valueOf(location.getLongitude());
                    //double lat = location.getLatitude();
                    //double lng = location.getLongitude();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                    String timeStamp = sdf.format(new Date());
                    myPrefs = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);
                    String username = myPrefs.getString("username",null);
                    String androidId = myPrefs.getString("androidId",null);
                    String userId = username;
                    //String vehicleId = myPrefs.getString("vehicleId",null);
                    //String vehCode = vehicleId;
                    String vehicleId = "None";
                    // Publish message
                    publishMessage(passengerMessage(androidId, lat, lng, timeStamp, userId, vehicleId));

                    // Place location marker
                    if (mCurrLocationMarker != null) {
                        mCurrLocationMarker.remove();
                    }
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("Marker Position");
                    mMap.clear();
                    mCurrLocationMarker = mMap.addMarker(markerOptions);
                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));

                    //publishMessage(clientId.toString()+","+lat+"_"+lng+"_"+timeStamp);
                    //JSONObject personTrack = new JSONObject();
                    //try {
                    //    personTrack.put("clientID", clientId);
                    //    personTrack.put("lat", lat);
                    //    personTrack.put("lng", lng);
                    //    personTrack.put("timeStamp", timeStamp);
                    //    publishMessage(personTrack.toString());

                    //} catch (JSONException e) {
                    //    e.printStackTrace();
                    //}
                }
            }
        }
    }; // locationCallback

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == AppConstants.GPS_REQUEST) {
                // flag maintain before get location
                isGPS = true;
            }
        }
    } // onActivityResult

    public void subscribeTopic(String topic) {
        try {
            client.subscribe(topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    //Log.i(TAG, "subscribed succeed");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    //Log.i(TAG, "subscribed failed");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public byte[] passengerMessage(String deviceId, String lat, String lng, String timestamp, String userId, String vehId) {
        Passenger passenger = Passenger.newBuilder()
                .setDeviceId(deviceId)
                .setLat(lat)
                .setLng(lng)
                .setTimestamp(timestamp)
                .setUserId(userId)
                .setVehicleId(vehId)
                .build();
        byte message[] = passenger.toByteArray();
        return message;
        //String message = passenger.toString();
        //Log.i("pb", message);
    }

    public void publishMessage(byte[] payload) {
        try {
            if (!client.isConnected()) {
                client.connect();
            }
            MqttMessage message = new MqttMessage();
            //message.setPayload(payload.getBytes());
            //write to disk
            //try {
            //    // Write the new address book back to disk.
            //    FileOutputStream output = openFileOutput("test.pb", context.MODE_PRIVATE);
            //    DataOutputStream dos = new DataOutputStream(output);
            //    dos.write(payload);
            //    dos.close();
            //    output.close();
            //    Log.d("Pb","File written");
            //} catch (FileNotFoundException e) {
            //    e.printStackTrace();
            //} catch (IOException e) {
            //    e.printStackTrace();
            //}
            message.setPayload(payload);
            message.setQos(0);
            client.publish("passengers", message,null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    //Log.i (TAG, "publish succeed! ") ;
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    //Log.i(TAG, "publish failed!") ;
                }
            });
        } catch (MqttException e) {
            //Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    public void connectBroker() {
        // Connect to Mqtt broker
        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(getApplicationContext(),"Connected", Toast.LENGTH_SHORT).show();
                    //vibrator.vibrate(500);
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    exception.printStackTrace();
                    Toast.makeText(getApplicationContext(),"Connect Failed", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    } // connectBroker

    public void disconnectBroker() {
        // Disconnect Mqtt broker
        try {
            IMqttToken token = client.disconnect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Toast.makeText(getApplicationContext(),"Disconnected", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(getApplicationContext(),"Could Not Disconnect", Toast.LENGTH_SHORT).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    } // disconnectBroker

    @Override
    public void onBackPressed() {
        // Go to report activity
        startActivity(new Intent(this, Report.class));
    } // onBackPressed

    @Override
    protected void onResume() {
        super.onResume();
        if (isContinue) {
            startLocationUpdates();
        }
    } // onResume

    private void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    } // startLocationUpdates

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    } // stopLocationUpdates

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        UiSettings mapUiSettings = mMap.getUiSettings();
        mapUiSettings.setZoomControlsEnabled(true);
        mapUiSettings.setMapToolbarEnabled(true);
        mapUiSettings.setCompassEnabled(true);
        mapUiSettings.setIndoorLevelPickerEnabled(true);
        mapUiSettings.setMyLocationButtonEnabled(true);

        // Get permission for location
        //if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        //        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        //    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
        //            AppConstants.LOCATION_REQUEST);
        //} else {
        //    mMap.setMyLocationEnabled(true);
        //    mMap.setTrafficEnabled(true);
        //}

        // Initialize marker array
        mMarkerArrayList = new ArrayList<>();

        LatLng mmla = new LatLng(14.6091, 121.0223);
        //mMap.addMarker(new MarkerOptions().position(mmla).title("Marker Position"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mmla, 11));
    } // onMapReady

    public void closeApp(){
        stopLocationUpdates();
        if(client.isConnected()) {
            disconnectBroker();
        }
        this.finish();
    } // closeApp

}
