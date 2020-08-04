package ph.safetravel.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import android.widget.FrameLayout;
import android.widget.ProgressBar;
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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
//import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
//import androidx.viewpager.widget.ViewPager;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import ph.safetravel.app.databinding.ActivityTripBinding;
import ph.safetravel.app.protos.Passenger;
import ph.safetravel.app.protos.Alert;

public class Trip extends AppCompatActivity implements OnMapReadyCallback, AdapterView.OnItemClickListener {
    SharedPreferences myPrefs;
    MqttAndroidClient client;
    MqttConnectOptions options;
    //Vibrator vibrator;
    String TAG="Mqtt";
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
    GoogleMap mMap;
    //LatLng origLatLng, destLatLng;
    //MarkerOptions markerOptions;
    private Toolbar toolbar;
    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;
    //private Marker mCurrentMarker;
    private ArrayList<Marker> mMarkerArrayList;
    Boolean brokerIsConnected = false;
    ProgressBar pgsBar;
    ActivityTripBinding bi;
    boolean isRotate = false;
    int feedsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Map Fragment
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        final SupportMapFragment mapFragment = new SupportMapFragment();
        ft.replace(R.id.mapFragTrip, mapFragment);
        ft.commit();
        mapFragment.getMapAsync(this);

        // Floating action bar
        bi = DataBindingUtil.setContentView(this, R.layout.activity_trip);
        ViewAnimation.init(bi.fabTripInfo);
        ViewAnimation.init(bi.fabTripFeeds);
        ViewAnimation.init(bi.fabTripAlert);
        ViewAnimation.init(bi.txtFeedsCount);

        // Get feeds count
        feedsCount=0;

        // Trip Add Fab
        bi.fabTripAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRotate = ViewAnimation.rotateFab(v, !isRotate);
                if(isRotate){
                    ViewAnimation.showIn(bi.fabTripInfo);
                    ViewAnimation.showIn(bi.fabTripFeeds);
                    ViewAnimation.showIn(bi.fabTripAlert);
                    if(feedsCount!=0){
                        ViewAnimation.showIn(bi.txtFeedsCount);
                        if(feedsCount<=99) {
                            bi.txtFeedsCount.setText(String.valueOf(feedsCount));
                        } else {
                            bi.txtFeedsCount.setText("99+");
                        }
                    }
                }else{
                    ViewAnimation.showOut(bi.fabTripInfo);
                    ViewAnimation.showOut(bi.fabTripFeeds);
                    ViewAnimation.showOut(bi.fabTripAlert);
                    if(feedsCount!=0){
                        ViewAnimation.showOut(bi.txtFeedsCount);
                    }
                }
            }
        });

        // Trip Info Fab
        bi.fabTripInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide Fab
                if(isRotate){
                    bi.fabTripAdd.hide();
                    bi.fabTripInfo.hide();
                    bi.fabTripFeeds.hide();
                    bi.fabTripAlert.hide();
                    isRotate=true;
                } else{
                    bi.fabTripAdd.hide();
                }

                // Show fragment
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

                TripInfoFragment tripInfoFragment = new TripInfoFragment();

                FrameLayout layout = (FrameLayout) findViewById(R.id.container_frame);
                layout.setVisibility(View.VISIBLE);

                if (tripInfoFragment.isAdded()) {
                    ft.show(tripInfoFragment);
                } else {
                    ft.add(R.id.container_frame, tripInfoFragment);
                    ft.show(tripInfoFragment);
                }

                ft.commit();
            }
        });

        // Trip Alert Fab
        bi.fabTripAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide Fab
                if(isRotate){
                    bi.fabTripAdd.hide();
                    bi.fabTripInfo.hide();
                    bi.fabTripFeeds.hide();
                    bi.fabTripAlert.hide();
                    isRotate=true;
                } else{
                    bi.fabTripAdd.hide();
                }

                // Show fragment
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

                TripAlertFragment tripAlertFragment = new TripAlertFragment();

                FrameLayout layout = (FrameLayout) findViewById(R.id.container_frame);
                layout.setVisibility(View.VISIBLE);

                if (tripAlertFragment.isAdded()) {
                    ft.show(tripAlertFragment);
                } else {
                    ft.add(R.id.container_frame, tripAlertFragment);
                    ft.show(tripAlertFragment);
                }
                ft.commit();
            }
        });

        // Trip Feeds Fab
        bi.fabTripFeeds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide Fab
                if(isRotate){
                    bi.fabTripAdd.hide();
                    bi.fabTripInfo.hide();
                    bi.fabTripFeeds.hide();
                    bi.fabTripAlert.hide();
                    isRotate=true;
                } else{
                    bi.fabTripAdd.hide();
                }

                // Show fragment
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

                TripFeedsFragment tripFeedsFragment = new TripFeedsFragment();

                FrameLayout layout = (FrameLayout) findViewById(R.id.container_frame);
                layout.setVisibility(View.VISIBLE);

                if (tripFeedsFragment.isAdded()) {
                    ft.show(tripFeedsFragment);
                } else {
                    ft.add(R.id.container_frame, tripFeedsFragment);
                    ft.show(tripFeedsFragment);
                }
                ft.commit();
            }
        });

        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.main_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId()==R.id.settings)
                {
                    // do something
                }
                return false;
            }
        });

        // Progress bar
        pgsBar = findViewById(R.id.progressBarTrip);
        pgsBar.setVisibility(View.INVISIBLE);
        pgsBar.setScaleY(3f);

        // Drawer
        dl = findViewById(R.id.drawer_layout);
        t = new ActionBarDrawerToggle(this, dl, toolbar, R.string.Open, R.string.Close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // Hide Fab
                if(isRotate){
                    bi.fabTripAdd.hide();
                    bi.fabTripInfo.hide();
                    bi.fabTripFeeds.hide();
                    bi.fabTripAlert.hide();
                } else{
                    bi.fabTripAdd.hide();
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // Show Fab
                if(isRotate){
                    bi.fabTripAdd.show();
                    bi.fabTripInfo.show();
                    bi.fabTripFeeds.show();
                    bi.fabTripAlert.show();
                } else{
                    bi.fabTripAdd.show();
                }
            }
        };
        t.setDrawerIndicatorEnabled(true);
        dl.addDrawerListener(t);
        t.syncState();

        // Navigation
        nv = findViewById(R.id.nav_view);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch(id) {
                    case R.id.profile:
                    {
                        Toast.makeText(Trip.this, "My Profile", Toast.LENGTH_SHORT).show();
                    }
                    case R.id.settings:
                    {
                        Toast.makeText(Trip.this, "Settings", Toast.LENGTH_SHORT).show();
                    }
                    case R.id.about:
                    {
                        dl.closeDrawer(Gravity.LEFT);
                        Intent intent = new Intent(Trip.this, About.class);
                        startActivity(intent);
                    }
                }
                return false;
            }
        });

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

        //vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // Toogle Button
        tButton = findViewById(R.id.toggleTrip);
        final Button sendAlertButton = (Button) findViewById(R.id.btnSendAlert);
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
                    Toast.makeText(getApplicationContext(),"Connected", Toast.LENGTH_SHORT).show();
                    subscribeTopic("#");
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    exception.printStackTrace();
                    Toast.makeText(getApplicationContext(),"Connected Failed", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                //Log.i(TAG, "connection lost");
            }

            @Override
            public int hashCode() {
                return super.hashCode();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.i(TAG, "topic: " + topic + ", msg: " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                //Log.i(TAG, "msg delivered");
            }
        }); // Mqtt client callback

        // Bottom navigation
        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(3);
        menuItem.setChecked(true);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull final MenuItem menuItem) {
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
                                SharedPreferences.Editor editor = myPrefs  .edit();
                                editor.clear();
                                editor.apply();
                                closeApp();
                                startActivity(new Intent(Trip.this, MainActivity.class));
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                bottomNavigationView.setSelectedItemId(R.id.navigation_trip);
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.setTitle("Confirm");
                        alertDialog.setCancelable(false);
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.show();
                        break;
                    }
                    case R.id.navigation_data: {
                        // Dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(Trip.this);
                        builder.setMessage("Are you sure you want to exit Trip Tracking?");
                        builder.setCancelable(false);
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                closeApp();
                                startActivity(new Intent(Trip.this, Data.class));
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                bottomNavigationView.setSelectedItemId(R.id.navigation_trip);
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.setTitle("Confirm");
                        alertDialog.setCancelable(false);
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.show();
                        break;
                    }
                    case R.id.navigation_report: {
                        // Dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(Trip.this);
                        builder.setMessage("Are you sure you want to exit Trip Tracking?");
                        builder.setCancelable(false);
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                closeApp();
                                startActivity(new Intent(Trip.this, Report.class));
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                bottomNavigationView.setSelectedItemId(R.id.navigation_trip);
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.setTitle("Confirm");
                        alertDialog.setCancelable(false);
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.show();
                        break;
                    }
                    case R.id.navigation_trip: {
                        break;
                    }
                    case R.id.navigation_fleet: {
                        // Dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(Trip.this);
                        builder.setMessage("Are you sure you want to exit Trip Tracking?");
                        builder.setCancelable(false);
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                closeApp();
                                startActivity(new Intent(Trip.this, Fleet.class));
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                bottomNavigationView.setSelectedItemId(R.id.navigation_trip);
                             }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.setTitle("Confirm");
                        alertDialog.setCancelable(false);
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.show();
                        break;
                    }
                }
                return true;
            }
        });

    } // onCreate

    public void sendAlert() {
        CheckBox checkbox1 = (CheckBox) findViewById(R.id.checkBox1);
        CheckBox checkbox2 = (CheckBox) findViewById(R.id.checkBox2);
        CheckBox checkbox3 = (CheckBox) findViewById(R.id.checkBox3);
        CheckBox checkbox4 = (CheckBox) findViewById(R.id.checkBox4);
        CheckBox checkbox5 = (CheckBox) findViewById(R.id.checkBox5);
        CheckBox checkbox6 = (CheckBox) findViewById(R.id.checkBox6);
        CheckBox checkbox7 = (CheckBox) findViewById(R.id.checkBox7);
        CheckBox checkbox8 = (CheckBox) findViewById(R.id.checkBox8);
        CheckBox checkbox9 = (CheckBox) findViewById(R.id.checkBox9);
        CheckBox checkbox10 = (CheckBox) findViewById(R.id.checkBox10);
        CheckBox checkbox11 = (CheckBox) findViewById(R.id.checkBox11);

        StringBuilder alertString = new StringBuilder();
        if(checkbox1.isChecked()) {
            alertString.append(checkbox1.getText());
        }
        if(checkbox2.isChecked()) {
            alertString.append("," + checkbox2.getText());
        }
        if(checkbox3.isChecked()) {
            alertString.append("," + checkbox3.getText());
        }
        if(checkbox4.isChecked()) {
            alertString.append("," + checkbox4.getText());
        }
        if(checkbox5.isChecked()) {
            alertString.append("," + checkbox5.getText());
        }
        if(checkbox6.isChecked()) {
            alertString.append("," + checkbox6.getText());
        }
        if(checkbox7.isChecked()) {
            alertString.append("," + checkbox7.getText());
        }
        if(checkbox8.isChecked()) {
            alertString.append("," + checkbox8.getText());
        }
        if(checkbox9.isChecked()) {
            alertString.append("," + checkbox9.getText());
        }
        if(checkbox10.isChecked()) {
            alertString.append("," + checkbox10.getText());
        }
        if(checkbox11.isChecked()) {
            alertString.append("," + checkbox11.getText());
        }

        if (mLastLocation != null) {
            String lat = String.valueOf(mLastLocation.getLatitude());
            String lng = String.valueOf(mLastLocation.getLongitude());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
            String timeStamp = sdf.format(new Date());

            // Get shared preferences
            myPrefs = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);
            String username = myPrefs.getString("username", null);
            String androidId = myPrefs.getString("androidId", null);
            String userId = username;
            String description = alertString.toString();

            // Publish message
            publishAlert(alertMessage(androidId, lat, lng, timeStamp, userId, description));
            System.out.println(alertMessage(androidId, lat, lng, timeStamp, userId, description));
        }
        Toast.makeText(getApplicationContext(), "Alert sent.", Toast.LENGTH_SHORT).show();

    } // sendAlert

    public boolean connected() {
        if (brokerIsConnected) {
            return true;
        } else {
            return false;
        }
    }

    public void restoreFab() {
        // Show Fab
        if(isRotate){
            bi.fabTripAdd.show();
            bi.fabTripInfo.show();
            bi.fabTripFeeds.show();
            bi.fabTripAlert.show();
        } else{
            bi.fabTripAdd.show();
        }
    } // restoreFab


    // Drawerlayout menu item clicked
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // fetch the user selected value
        String item = parent.getItemAtPosition(position).toString();
        // create Toast with user selected value
        Toast.makeText(Trip.this, "Selected Item is: \t" + item, Toast.LENGTH_LONG).show();
        // set user selected value to the TextView

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
                if (mLastLocation != null) {
                    String lat = String.valueOf(location.getLatitude());
                    String lng = String.valueOf(location.getLongitude());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                    String timeStamp = sdf.format(new Date());

                    // Get shared preferences
                    myPrefs = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);
                    String username = myPrefs.getString("username",null);
                    String androidId = myPrefs.getString("androidId",null);
                    String userId = username;

                    String origin = myPrefs.getString("origin",null);
                    if (origin==null) {
                        origin = "None";
                    }
                    String destination = myPrefs.getString("origin",null);
                    if (destination==null) {
                        destination = "None";
                    }
                    String purpose = myPrefs.getString("purpose",null);
                    if (purpose==null) {
                        purpose = "None";
                    }
                    String mode = myPrefs.getString("mode",null);
                    if (mode==null) {
                        mode = "None";
                    }
                    String vehicleId = myPrefs.getString("vehicleId",null);
                    if (vehicleId==null) {
                        vehicleId = "None";
                    }
                    String vehicleDetails = myPrefs.getString("vehicleDetails",null);
                    if (vehicleDetails==null) {
                        vehicleDetails = "None";
                    }
                    // Publish message
                    publishMessage(passengerMessage(androidId, lat, lng, timeStamp, userId, origin, destination, purpose, mode, vehicleId, vehicleDetails));

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
    } // subscribeTopic

    public byte[] passengerMessage(String deviceId, String lat, String lng, String timestamp, String userId, String origin, String destination, String purpose, String mode, String vehicleId, String vehicleDetails) {
        Passenger passenger = Passenger.newBuilder()
                .setDeviceId(deviceId)
                .setLat(lat)
                .setLng(lng)
                .setTimestamp(timestamp)
                .setUserId(userId)
                .setOrig(origin)
                .setDest(destination)
                .setPurpose(purpose)
                .setMode(mode)
                .setVehicleId(vehicleId)
                .setVehicleDetails(vehicleDetails)
                .build();
        byte message[] = passenger.toByteArray();
        return message;
    } // passengerMessage

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
            e.printStackTrace();
        }
    } // publishMessage

    public byte[] alertMessage(String deviceId, String lat, String lng, String timestamp, String userId, String description) {
        Alert alert = Alert.newBuilder()
                .setDeviceId(deviceId)
                .setLat(lat)
                .setLng(lng)
                .setTimestamp(timestamp)
                .setUserId(userId)
                .setDescription(description)
                .build();
        byte message[] = alert.toByteArray();
        return message;
    } // alertMessage

    public void publishAlert(byte[] payload) {
        try {
            if (!client.isConnected()) {
                client.connect();
            }
            MqttMessage message = new MqttMessage();
            message.setPayload(payload);
            message.setQos(0);
            client.publish("alerts", message,null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    //
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    //
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    } // publishAlert

    public void connectBroker() {
        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(getApplicationContext(),"Connected", Toast.LENGTH_SHORT).show();
                    brokerIsConnected = true;
                    //vibrator.vibrate(500);
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    exception.printStackTrace();
                    Toast.makeText(getApplicationContext(),"Connect Failed", Toast.LENGTH_SHORT).show();
                    brokerIsConnected = false;
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    } // connectBroker

    public void disconnectBroker() {
        try {
            IMqttToken token = client.disconnect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(getApplicationContext(),"Disconnected", Toast.LENGTH_SHORT).show();
                    brokerIsConnected = false;
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    exception.printStackTrace();
                    Toast.makeText(getApplicationContext(),"Could Not Disconnect", Toast.LENGTH_SHORT).show();
                    brokerIsConnected = true;
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    } // disconnectBroker

    @Override
    public void onBackPressed() {

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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    AppConstants.LOCATION_REQUEST);
        } else {
            mMap.setMyLocationEnabled(true);
            mMap.setTrafficEnabled(true);
        }

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
