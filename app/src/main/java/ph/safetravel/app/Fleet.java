package ph.safetravel.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.facebook.FacebookSdk;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.navigation.NavigationView;
import com.hsalf.smilerating.SmileRating;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
//import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
//import androidx.viewpager.widget.ViewPager;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import ph.safetravel.app.databinding.ActivityFleetBinding;
import ph.safetravel.app.protos.Vehicle;
import ph.safetravel.app.protos.Boarding;


public class Fleet extends AppCompatActivity implements OnMapReadyCallback  {
    SharedPreferences myPrefs;
    MqttAndroidClient client;
    MqttConnectOptions options;
    String MqttHost = "tcp://mqtt.safetravel.ph:8883";
    final String MqttUsername = "mqtt";
    final String MqttPassword = "mqtt";
    String clientId;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private boolean isGPS = false;
    private boolean isContinue = false;
    Boolean brokerIsConnected = false;
    ToggleButton startButton;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    GoogleMap mMap;
    DBManager dbManager;
    Context context;
    //---Sensors
    //private SensorManager sensorManager;
    //Sensor accelerometer;
    //Sensor gyroscope;
    //SensorEventListener acListener;
    Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    ImageButton boardButton, alightButton;
    int numPass, numBoard, numAlight;
    double speed=0.0, avespeed=0.0, maxspeed=0.0, traveldist=0.0, traveltime=0.0;
    TextView NumPassengers;
    TextView NumBoarding, NumAlighting;
    TextView Speed, TravelDist, AveSpeed, MaxSpeed, TravelTime;
    ProgressBar pgsBar;
    ActivityFleetBinding bi;
    boolean isRotate = false;
    int feedsCount;
    Polyline route;
    List<LatLng> routePoints = new ArrayList<LatLng>();
    List<Location> routeTracks = new ArrayList<Location>();
    final MediaPlayer mp = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fleet);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());

        // Map Fragment
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        final SupportMapFragment mapFragment = new SupportMapFragment();
        ft.replace(R.id.mapFragFleet, mapFragment);
        ft.commit();
        mapFragment.getMapAsync(this);

        // Floating action bar
        bi = DataBindingUtil.setContentView(this, R.layout.activity_fleet);
        ViewAnimation.init(bi.fabFleetInfo);
        ViewAnimation.init(bi.fabFleetBoarding);
        ViewAnimation.init(bi.fabFleetAlighting);
        ViewAnimation.init(bi.fabFleetFeeds);
        ViewAnimation.init(bi.txtFeedsCount);

        // Get feeds count
        feedsCount=0;

        // Fleet Add Fab
        bi.fabFleetAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRotate = ViewAnimation.rotateFab(v, !isRotate);
                if(isRotate){
                    ViewAnimation.showIn(bi.fabFleetInfo);
                    ViewAnimation.showIn(bi.fabFleetBoarding);
                    ViewAnimation.showIn(bi.fabFleetAlighting);
                    ViewAnimation.showIn(bi.fabFleetFeeds);
                    if(feedsCount!=0){
                        ViewAnimation.showIn(bi.txtFeedsCount);
                        if(feedsCount<=99) {
                            bi.txtFeedsCount.setText(String.valueOf(feedsCount));
                        } else {
                            bi.txtFeedsCount.setText("99+");
                        }
                    }
                }else{
                    ViewAnimation.showOut(bi.fabFleetInfo);
                    ViewAnimation.showOut(bi.fabFleetBoarding);
                    ViewAnimation.showOut(bi.fabFleetAlighting);
                    ViewAnimation.showOut(bi.fabFleetFeeds);
                    if(feedsCount!=0){
                        ViewAnimation.showOut(bi.txtFeedsCount);
                    }
                }
            }
        });

        // Fleet Info Fab
        bi.fabFleetInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide Fab
                if(isRotate){
                    bi.fabFleetAdd.hide();
                    bi.fabFleetInfo.hide();
                    bi.fabFleetBoarding.hide();
                    bi.fabFleetAlighting.hide();
                    bi.fabFleetFeeds.hide();
                    isRotate=true;
                } else{
                    bi.fabFleetAdd.hide();
                }
                // Show fragment
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

                FleetInfoFragment fleetInfoFragment = new FleetInfoFragment();

                FrameLayout layout = (FrameLayout) findViewById(R.id.Fleetcontainer_frame);
                layout.setVisibility(View.VISIBLE);

                if (fleetInfoFragment.isAdded()) {
                    ft.show(fleetInfoFragment);
                } else {
                    ft.add(R.id.Fleetcontainer_frame, fleetInfoFragment);
                    ft.show(fleetInfoFragment);
                }
                ft.commit();
            }
        });

        // Fleet Boarding Fab
        bi.fabFleetBoarding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide Fab
                if(isRotate){
                    bi.fabFleetAdd.hide();
                    bi.fabFleetInfo.hide();
                    bi.fabFleetBoarding.hide();
                    bi.fabFleetAlighting.hide();
                    bi.fabFleetFeeds.hide();
                    isRotate=true;
                } else{
                    bi.fabFleetAdd.hide();
                }
                // Show fragment
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

                FleetBoardingFragment fleetBoardingFragment = new FleetBoardingFragment();

                FrameLayout layout = (FrameLayout) findViewById(R.id.Fleetcontainer_frame);
                layout.setVisibility(View.VISIBLE);

                if (fleetBoardingFragment.isAdded()) {
                    ft.show(fleetBoardingFragment);
                } else {
                    ft.add(R.id.Fleetcontainer_frame, fleetBoardingFragment);
                    ft.show(fleetBoardingFragment);
                }
                ft.commit();
            }
        });

        // Fleet Alighting Fab
        bi.fabFleetAlighting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide Fab
                if(isRotate){
                    bi.fabFleetAdd.hide();
                    bi.fabFleetInfo.hide();
                    bi.fabFleetBoarding.hide();
                    bi.fabFleetAlighting.hide();
                    bi.fabFleetFeeds.hide();
                    isRotate=true;
                } else{
                    bi.fabFleetAdd.hide();
                }
                // Show fragment
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

                FleetAlightingFragment fleetAlightingFragment = new FleetAlightingFragment();

                FrameLayout layout = (FrameLayout) findViewById(R.id.Fleetcontainer_frame);
                layout.setVisibility(View.VISIBLE);

                if (fleetAlightingFragment.isAdded()) {
                    ft.show(fleetAlightingFragment);
                } else {
                    ft.add(R.id.Fleetcontainer_frame, fleetAlightingFragment);
                    ft.show(fleetAlightingFragment);
                }
                ft.commit();
            }
        });

        // Fleet Feeds Fab
        bi.fabFleetFeeds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide Fab
                if(isRotate){
                    bi.fabFleetAdd.hide();
                    bi.fabFleetInfo.hide();
                    bi.fabFleetBoarding.hide();
                    bi.fabFleetAlighting.hide();
                    bi.fabFleetFeeds.hide();
                    isRotate=true;
                } else{
                    bi.fabFleetAdd.hide();
                }
                // Show fragment
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

                FleetFeedsFragment fleetFeedsFragment = new FleetFeedsFragment();

                FrameLayout layout = (FrameLayout) findViewById(R.id.Fleetcontainer_frame);
                layout.setVisibility(View.VISIBLE);

                if (fleetFeedsFragment.isAdded()) {
                    ft.show(fleetFeedsFragment);
                } else {
                    ft.add(R.id.Fleetcontainer_frame, fleetFeedsFragment);
                    ft.show(fleetFeedsFragment);
                }
                ft.commit();
            }
        });

        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.fleet_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                // Fleet history
                if(item.getItemId()==R.id.fleethistory)
                {
                    startActivity(new Intent(Fleet.this, FleetHistory.class));
                }
                // Fleet settings
                if(item.getItemId()==R.id.settings)
                {
                    startActivity(new Intent(Fleet.this, FleetSettings.class));
                }
                return false;
            }
        });

        // Progress bar
        pgsBar =  (ProgressBar) findViewById(R.id.progressBarFleet);
        pgsBar.setVisibility(View.INVISIBLE);
        pgsBar.setScaleY(3f);

        // Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.Open, R.string.Close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // Hide Fab
                if(isRotate){
                    bi.fabFleetAdd.hide();
                    bi.fabFleetInfo.hide();
                    bi.fabFleetBoarding.hide();
                    bi.fabFleetAlighting.hide();
                    bi.fabFleetFeeds.hide();
                } else{
                    bi.fabFleetAdd.hide();
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // Show Fab
                if(isRotate){
                    bi.fabFleetAdd.show();
                    bi.fabFleetInfo.show();
                    bi.fabFleetBoarding.show();
                    bi.fabFleetAlighting.show();
                    bi.fabFleetFeeds.show();
                } else{
                    bi.fabFleetAdd.show();
                }
            }
        };
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // Navigation view
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        //navigationView.setNavigationItemSelectedListener(this);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                drawerLayout.closeDrawers();
                int id = item.getItemId();
                switch(id) {
                    case R.id.profile:
                    {
                        //Toast.makeText(Data.this, "Profile", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case R.id.settings:
                    {
                        //Toast.makeText(Data.this, "Settings", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case R.id.help:
                    {
                        //Toast.makeText(Data.this, "Settings", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case R.id.feedback:
                    {
                        //Toast.makeText(Data.this, "Settings", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case R.id.about:
                    {
                        startActivity(new Intent(Fleet.this, About.class));
                    }
                    case R.id.share:
                    {
                        ////Toast.makeText(Data.this, "Settings", Toast.LENGTH_SHORT).show();
                        //int secs = 2; // Delay in seconds
//
                        //Utils.delay(secs, new Utils.DelayCallback() {
                        //    @Override
                        //    public void afterDelay() {
                        //        // Do something after delay
                        //        shareScreenShot();
                        //    }
                        //});
                        break;
                    }
                    case R.id.logout:
                    {
                        // Dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(Fleet.this);
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
                                startActivity(new Intent(Fleet.this, MainActivity.class));
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.setTitle("Logout");
                        alertDialog.setCancelable(false);
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.show();
                        break;
                    }
                }
                return true;
            }
        });

        // Get shared preferences
        myPrefs = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);
        String username = myPrefs.getString("username", "");

        // Display username on header
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername =  headerView.findViewById(R.id.nav_header_textView);
        String nav_username = username;

        // Decrypt username
        try {
            String decrypted_username = AESUtils.decrypt(username);
            nav_username = decrypted_username;
            navUsername.setText(decrypted_username);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Display QR code button on header
        ImageButton showQRCode = headerView.findViewById(R.id.nav_header_imageButtonQRCode);
        showQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
                startActivity(new Intent(Fleet.this, QRCode.class));
            }
        });

        // Display text values
        NumPassengers = findViewById(R.id.txtNumPass);
        NumPassengers.setText(String.valueOf(numPass));

        NumBoarding = findViewById(R.id.txtNumBoard);
        NumBoarding.setText(String.valueOf(numBoard));

        NumAlighting= findViewById(R.id.txtNumAlight);
        NumAlighting.setText(String.valueOf(numAlight));

        Speed = findViewById(R.id.txtSpeedNum);
        //Speed.setText(String.valueOf(speed));
        Speed.setText("0.00");

        AveSpeed = findViewById(R.id.txtAveSpeedNum);
        //AveSpeed.setText(String.valueOf(avespeed));
        AveSpeed.setText("0.00");

        MaxSpeed = findViewById(R.id.txtMaxSpeedNum);
        //MaxSpeed.setText(String.valueOf(maxspeed));
        MaxSpeed.setText("0.00");

        TravelDist = findViewById(R.id.txtTravelDistNum);
        //TravelDist.setText(String.valueOf(traveldist));
        TravelDist.setText("0.00");

        TravelTime = findViewById(R.id.txtTravelTimeNum);
        //TravelTime.setText(String.valueOf(traveltime));
        TravelTime.setText("0.00");

        // Board button
        boardButton = findViewById(R.id.btnBoard);
        final View.OnClickListener boardClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mp.isPlaying())
                {
                    mp.stop();
                }
                try {
                    mp.reset();
                    AssetFileDescriptor afd;
                    afd = getAssets().openFd("beep-high.mp3");
                    mp.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
                    mp.prepare();
                    mp.start();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (mLastLocation != null) {
                    String lat = String.valueOf(mLastLocation.getLatitude());
                    String lng = String.valueOf(mLastLocation.getLongitude());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                    String timeStamp = sdf.format(new Date());

                    // Get shared preferences
                    myPrefs = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);
                    String username = myPrefs.getString("username","");
                    String userId = "";
                    try {
                        String decrypted_username = AESUtils.decrypt(username);
                        userId = decrypted_username;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String androidId = myPrefs.getString("androidId","");
                    String deviceId = "";
                    try {
                        String decrypted_androidId = AESUtils.decrypt(androidId);
                        deviceId = decrypted_androidId;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // vehicleId and vehicleDetails
                    String vehicleId = myPrefs.getString("vehicleId","");
                    String vehicleDetails = myPrefs.getString("vehicleDetails","");

                    // Publish message
                    numPass=numPass+1;
                    NumPassengers.setText(String.valueOf(numPass));
                    publishMessage(vehicleMessage(deviceId, lat, lng, timeStamp, userId, vehicleId, vehicleDetails, true, false, numPass));
                }
            }
        };
        boardButton.setOnClickListener(null);

        // Alight button
        alightButton = findViewById(R.id.btnAlight);
        final View.OnClickListener alightClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mp.isPlaying())
                {
                    mp.stop();
                }
                try {
                    mp.reset();
                    AssetFileDescriptor afd;
                    afd = getAssets().openFd("beep-low.mp3");
                    mp.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
                    mp.prepare();
                    mp.start();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (mLastLocation != null) {
                    String lat = String.valueOf(mLastLocation.getLatitude());
                    String lng = String.valueOf(mLastLocation.getLongitude());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                    String timeStamp = sdf.format(new Date());

                    // Get shared preferences
                    myPrefs = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);
                    // userId
                    String username = myPrefs.getString("username","");
                    String userId = "";
                    try {
                        String decrypted_username = AESUtils.decrypt(username);
                        userId = decrypted_username;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // deviceId
                    String androidId = myPrefs.getString("androidId","");
                    String deviceId = "";
                    try {
                        String decrypted_androidId = AESUtils.decrypt(androidId);
                        deviceId = decrypted_androidId;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // vehicleId and vehicleDetails
                    String vehicleId = myPrefs.getString("vehicleId","");
                    String vehicleDetails = myPrefs.getString("vehicleDetails","");

                    // Publish message
                    if (numPass != 0) numPass=numPass-1;
                    NumPassengers.setText(String.valueOf(numPass));
                    publishMessage(vehicleMessage(deviceId, lat, lng, timeStamp, userId, vehicleId, vehicleDetails, false, true, numPass));
                }
            }
        };
        alightButton.setOnClickListener(null);

        //  Sensors
        //sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // Accelerometer
        //accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //acListener = new SensorEventListener() {
        //    @Override
        //    public void onSensorChanged(SensorEvent sensorEvent) {
        //        Log.d("Accelerometer","X: " + sensorEvent.values[0] + " Y: " + sensorEvent.values[1] + " Z: " + sensorEvent.values[2]);
        //    }

        //    @Override
        //    public void onAccuracyChanged(Sensor sensor, int i) {
        //    }
        //};
        //sensorManager.registerListener(acListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        //gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // FuseLocationProviderClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Location request for GPS
        locationRequest= LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1 * 1000);
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

        // Toogle button
        startButton = findViewById(R.id.toggleFleet);
        startButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, final boolean isChecked) {
                if(isChecked && isGPS) {
                    // Start tracking
                    connectBroker();
                    startLocationUpdates();
                    boardButton.setOnClickListener(boardClickListener);
                    alightButton.setOnClickListener(alightClickListener);
                    pgsBar.setVisibility(View.VISIBLE);
                } else {
                    // Dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(Fleet.this);
                    builder.setMessage("Are you sure you want to stop Tracking?");
                    builder.setCancelable(true);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            startButton.setChecked(false);  // Indicate 'Start Tracking'
                            // Stop tracking
                            stopLocationUpdates();
                            disconnectBroker();
                            boardButton.setOnClickListener(null);
                            alightButton.setOnClickListener(null);
                            pgsBar.setVisibility(View.INVISIBLE);
                            // Reset speed
                            Speed.setText("0.00");
                            // Save GPS tracks
                            SaveTracks saveTracks = new SaveTracks();
                            saveTracks.execute();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startButton.setChecked(true); // Indicate 'Stop Tracking'
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.setTitle("Confirm");
                    alertDialog.setCancelable(false);
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.show();
                }
            }
        });

        // Mqtt host
        clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), MqttHost, clientId);
        options = new MqttConnectOptions();
        options.setUserName(MqttUsername);
        options.setPassword(MqttPassword.toCharArray());

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
                //Log.i(TAG, "connection lost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                //Log.i("MQTT", "topic: " + topic + ", msg: " + new String(message.getPayload()));
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

        // Get shared preferences
        myPrefs = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);
        String role = myPrefs.getString("role", "");

        if(role.equals("Driver") || role.equals("Conductor") || role.equals("Driver-Operator")) {
            // Disable Trip item
            MenuItem item = menu.findItem(R.id.navigation_trip);
            Drawable resIcon = getResources().getDrawable(R.drawable.ic_navigate);
            resIcon.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
            item.setEnabled(false); // any text will be automatically disabled
            item.setIcon(resIcon);
            item.getIcon().setAlpha(130);
        }

        if(role.equals("Commuter")) {
            // Disable Fleet item
            MenuItem item = menu.findItem(R.id.navigation_fleet);
            Drawable resIcon = getResources().getDrawable(R.drawable.ic_pub);
            resIcon.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
            item.setEnabled(false); // any text will be automatically disabled
            item.setIcon(resIcon);
            item.getIcon().setAlpha(130);
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_board: {
                        // Dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(Fleet.this);
                        builder.setMessage("Are you sure you want to exit Fleet?");
                        builder.setCancelable(false);
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                closeApp();
                                startActivity(new Intent(Fleet.this, Board.class));
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                bottomNavigationView.setSelectedItemId(R.id.navigation_fleet);
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(Fleet.this);
                        builder.setMessage("Are you sure you want to exit Fleet?");
                        builder.setCancelable(false);
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                closeApp();
                                startActivity(new Intent(Fleet.this, Report.class));
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                bottomNavigationView.setSelectedItemId(R.id.navigation_fleet);
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
                        // Dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(Fleet.this);
                        builder.setMessage("Are you sure you want to exit Fleet?");
                        builder.setCancelable(false);
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                closeApp();
                                startActivity(new Intent(Fleet.this, Trip.class));
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                bottomNavigationView.setSelectedItemId(R.id.navigation_fleet);
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.setTitle("Confirm");
                        alertDialog.setCancelable(false);
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.show();
                        break;
                    }
                    case R.id.navigation_fleet: {
                        break;
                    }
                    case R.id.navigation_help: {
                        // Dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(Fleet.this);
                        builder.setMessage("Are you sure you want to exit Fleet?");
                        builder.setCancelable(false);
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                closeApp();
                                startActivity(new Intent(Fleet.this, Help.class));
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                bottomNavigationView.setSelectedItemId(R.id.navigation_fleet);
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    //@Override
    //public boolean onNavigationItemSelected(MenuItem item) {
    //    drawerLayout.closeDrawers();
    //    int id = item.getItemId();
    //    switch(id) {
    //        case R.id.profile:
    //        {
    //            break;
    //            //Toast.makeText(Fleet.this, "My Profile", Toast.LENGTH_SHORT).show();
    //        }
    //        case R.id.settings:
    //        {
    //            break;
    //            //Toast.makeText(Fleet.this, "Settings", Toast.LENGTH_SHORT).show();
    //        }
    //        case R.id.help:
    //        {
    //            break;
    //            //Toast.makeText(Fleet.this, "Help", Toast.LENGTH_SHORT).show();
    //        }
    //        case R.id.about:
    //        {
    //            break;
    //            //drawerLayout.closeDrawer(Gravity.LEFT);
    //            //Intent intent = new Intent(Fleet.this, About.class);
    //            //startActivity(intent);
    //        }
    //    }
    //    //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    //    //drawer.closeDrawer(GravityCompat.START);
    //    //return true;
    //    //this.drawerLayout.closeDrawer(GravityCompat.START);
    //    return true;
    //}

    //@Override
    //public void onSensorChanged(SensorEvent sensorEvent){
    //    Log.d("Accelerometer","X: " + sensorEvent.values[0] + " Y: " + sensorEvent.values[1] + " Z: " + sensorEvent.values[2]);
    //}

    //@Override
    //public void onAccuracyChanged(Sensor sensor, int accuracy) {

    //}

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
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);

                routeTracks.add(location);
                LatLng mCurrentPoint= new LatLng(location.getLatitude(),location.getLongitude());
                routePoints.add(mCurrentPoint);
                //Log.d("Route", routePoints.toString());

                if(mLastLocation != null) {
                    Location prevLocation = mLastLocation;
                    Location currLocation = location;
                    traveldist = traveldist + computeDistance(prevLocation, currLocation)/1000;
                    //speed = computeSpeed(loc1, loc2);
                    speed=location.getSpeed()*3600/1000;
                    //Log.d("Loc/Speed/Dist", String.valueOf(loc1.getLatitude()) + "," + String.valueOf(loc2.getLatitude()) + "," + String.valueOf(speed)+ ","
                    //        + String.valueOf(distance));
                }

                mLastLocation = location;  // update the last location

                if (mLastLocation != null) {
                    // Clear the map
                    mMap.clear();

                    String lat = String.valueOf(location.getLatitude());
                    String lng = String.valueOf(location.getLongitude());

                    Speed.setText(String.format("%.2f", speed));
                    TravelDist.setText(String.format("%.2f", traveldist));

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                    String timeStamp = sdf.format(new Date());

                    // Get shared preferences
                    myPrefs = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);
                    String username = myPrefs.getString("username","");
                    String userId = "";
                    try {
                        String decrypted_username = AESUtils.decrypt(username);
                        userId = decrypted_username;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    String androidId = myPrefs.getString("androidId","");
                    String deviceId = "";
                    try {
                        String decrypted_androidId = AESUtils.decrypt(androidId);
                        deviceId = decrypted_androidId;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    String vehicleId = myPrefs.getString("vehicleId","");
                    String vehicleDetails = myPrefs.getString("vehicleDetails","");

                    // Publish message
                    publishMessage(vehicleMessage(deviceId, lat, lng, timeStamp, userId, vehicleId, vehicleDetails, false, false, numPass));

                    // Place location marker
                    if (mCurrLocationMarker != null) {
                        mCurrLocationMarker.remove();
                    }
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    // Current location marker
                    MarkerOptions markerOptions = new MarkerOptions().position(latLng)
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            .title("Current Position");
                    mCurrLocationMarker = mMap.addMarker(markerOptions);

                    // Draw route
                    route = mMap.addPolyline(new PolylineOptions()
                            .width(10)
                            .color(Color.BLUE)
                            .geodesic(true)
                            .zIndex(100));
                    route.setPoints(routePoints);
                }
            }
        }
    }; // locationCallback

    static double computeDistance(Location loc1, Location loc2) {
        double R = 6371000;
        double la1 = loc1.getLatitude()* Math.PI/180;
        double la2 = loc2.getLatitude()* Math.PI/180;
        double lo1 = loc1.getLongitude()* Math.PI/180;
        double lo2 = loc2.getLongitude()* Math.PI/180;
        double tmp1 = Math.sin((la1-la2)/2)*Math.sin((la1-la2)/2) + Math.cos(la1)*Math.cos(la2) * Math.sin((lo1-lo2)/2) * Math.sin((lo1-lo2)/2);
        double tmp2 = Math.sqrt(tmp1);
        double d = Math.abs(2 * R * Math.asin(tmp2) * 100000) / 100000;

        return d;
    } // computeDistance

    static double computeSpeed(Location loc1, Location loc2) {
        double s = computeDistance(loc1, loc2) / (loc2.getTime() - loc1.getTime());

        return s;
    } // computeSpeed

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == AppConstants.GPS_REQUEST) {
                isGPS = true;  // flag maintain before get location
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

    public byte[] vehicleMessage(String deviceId, String lat, String lng, String timestamp, String userId, String vehicleId, String vehicleDetails, boolean board, boolean alight, int numPass) {
        Vehicle vehicle = Vehicle.newBuilder()
                .setDeviceId(deviceId)
                .setLat(lat)
                .setLng(lng)
                .setTimestamp(timestamp)
                .setUserId(userId)
                .setVehicleId(vehicleId)
                .setVehicleDetails(vehicleDetails)
                .setBoard(board)
                .setAlight(alight)
                .setNumPass(numPass)
                .build();
        byte message[] = vehicle.toByteArray();
        return message;
    }

    public void publishMessage(byte[] payload) {
        try {
            if (!client.isConnected()) {
                client.connect();
            }
            MqttMessage message = new MqttMessage();
            message.setPayload(payload);
            message.setQos(0);
            client.publish("vehicles", message,null, new IMqttActionListener() {
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
    } // publishMessage

    public byte[] boardingMessage(String deviceId, String lat, String lng, String timestamp, String userId, String vehicleId, String vehicleDetails, String passengerId, String passengerDetails) {
        protos.Boarding boarding = protos.Boarding.newBuilder()
                .setDeviceId(deviceId)
                .setLat(lat)
                .setLng(lng)
                .setTimestamp(timestamp)
                .setUserId(userId)
                .setVehicleId(vehicleId)
                .setVehicleDetails(vehicleDetails)
                .setPassengerId(passengerId)
                .setPassengerDetails(passengerDetails)
                .build();
        byte message[] = boarding.toByteArray();
        return message;
    } // boardingMessage

    public void publishBoarding(byte[] payload) {
        try {
            if (!client.isConnected()) {
                client.connect();
            }
            MqttMessage message = new MqttMessage();
            message.setPayload(payload);
            message.setQos(0);
            client.publish("boardings", message,null, new IMqttActionListener() {
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
    } // publishBoarding

    public void connectBroker() {
        // Connect to Mqtt broker
        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(Fleet.this,"Connected", Toast.LENGTH_SHORT).show();
                    brokerIsConnected = true;
                    //vibrator.vibrate(500);
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    exception.printStackTrace();
                    Toast.makeText(Fleet.this,"Connect Failed", Toast.LENGTH_SHORT).show();
                    brokerIsConnected = false;
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
                    Toast.makeText(Fleet.this,"Disconnected", Toast.LENGTH_SHORT).show();
                    brokerIsConnected = false;
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(Fleet.this,"Could Not Disconnect", Toast.LENGTH_SHORT).show();
                    brokerIsConnected = true;
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    } // disconnectBroker

    @Override
    protected void onResume() {
        super.onResume();
        if (isContinue) {
            startLocationUpdates();
        }
    } // onResume

    @Override
    protected void onPause() {
        super.onPause();
    } // onPause

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

        // Initialize
        LatLng mmla = new LatLng(14.6091, 121.0223);
        MarkerOptions markerOptions = new MarkerOptions().position(mmla)
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .title("Current Position");
        mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mmla, 10));
    } // onMapReady

    public void closeApp(){
        stopLocationUpdates();
        if(client.isConnected()) {
            disconnectBroker();
        }
        this.finish();
    } // closeApp

    // Send boarding from fleet boarding fragment
    public void sendBoarding() {
        TextView txtBoardingPassengerId = findViewById(R.id.txtBoardingPassengerId);
        TextView txtBoardingPassengerDetails = findViewById(R.id.txtBoardingPassengerDetails);
        String commuterId = txtBoardingPassengerId.getText().toString();
        String commuterDetails = txtBoardingPassengerDetails.getText().toString();

        ImageView imageBoardingStatus = findViewById(R.id.imageBoardingStatus);
        TextView txtBoardingStatus = findViewById(R.id.txtBoardingStatus);

        if (mLastLocation != null) {
            String lat = String.valueOf(mLastLocation.getLatitude());
            String lng = String.valueOf(mLastLocation.getLongitude());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
            String timeStamp = sdf.format(new Date());

            // Get shared preferences
            myPrefs = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);
            String username = myPrefs.getString("username","");
            String userId = "";
            try {
                String decrypted_username = AESUtils.decrypt(username);
                userId = decrypted_username;
            } catch (Exception e) {
                e.printStackTrace();
            }

            String androidId = myPrefs.getString("androidId","");
            String deviceId = "";
            try {
                String decrypted_androidId = AESUtils.decrypt(androidId);
                deviceId = decrypted_androidId;
            } catch (Exception e) {
                e.printStackTrace();
            }

            String vehicleId = myPrefs.getString("vehicleId","");
            String vehicleDetails = myPrefs.getString("vehicleDetails","");

            if(!vehicleId.equals("") && !vehicleDetails.equals("") && !commuterId.equals("") && !commuterDetails.equals("")) {
                // Publish message
                publishBoarding(boardingMessage(deviceId, lat, lng, timeStamp, userId, vehicleId, vehicleDetails, commuterId, commuterDetails));
                Toast.makeText(Fleet.this, "Boarding sent.", Toast.LENGTH_SHORT).show();
                imageBoardingStatus.setVisibility(View.VISIBLE);
                imageBoardingStatus.setImageResource(R.drawable.sign_correct);
                txtBoardingStatus.setVisibility(View.VISIBLE);
                txtBoardingStatus.setText("Boarding successful.");
                numPass = numPass + 1;
                NumPassengers.setText(String.valueOf(numPass));
                numBoard = numBoard + 1;
                NumBoarding.setText(String.valueOf(numBoard));
            } else {
                Toast.makeText(Fleet.this, "Boarding failed.", Toast.LENGTH_SHORT).show();
                imageBoardingStatus.setVisibility(View.VISIBLE);
                imageBoardingStatus.setImageResource(R.drawable.sign_wrong);
                txtBoardingStatus.setVisibility(View.VISIBLE);
                txtBoardingStatus.setText("Boarding failed.");
            }

        }
    } // sendBoarding

    public boolean connected() {
        if (brokerIsConnected) {
            return true;
        } else {
            return false;
        }
    } // connected

    public void restoreFleetFab() {
        // Show Fab
        if(isRotate){
            bi.fabFleetAdd.show();
            bi.fabFleetInfo.show();
            bi.fabFleetBoarding.show();
            bi.fabFleetAlighting.show();
            bi.fabFleetFeeds.show();
        } else{
            bi.fabFleetAdd.show();
        }
    } // restoreFab

    public void clearSharedPreferences(){
        myPrefs = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = myPrefs.edit();
        editor.clear();
        editor.apply();
    } // clearSharedPreferences

    public static boolean generateGpx(File file, String name, List<Location> points) {
        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"MapSource 6.15.5\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\"><trk>\n";
        name = "<name>" + name + "</name><trkseg>\n";
        String segments = "";
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

        for (Location location : points) {
            segments += "<trkpt lat=\"" + location.getLatitude() + "\" lon=\"" + location.getLongitude() + "\"><time>" + df.format(new Date(location.getTime())) + "</time></trkpt>\n";
        }
        String footer = "</trkseg></trk></gpx>";

        try {
            FileWriter writer = new FileWriter(file, false);
            writer.append(header);
            writer.append(name);
            writer.append(segments);
            writer.append(footer);
            writer.flush();
            writer.close();
            Log.e("generateGpx", "Gpx file saved");
            return true;
        } catch (IOException e) {
            Log.e("generateGpx", "Error Writing Path",e);
            return false;
        }
    } // generateGpx

    private class SaveTracks extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            // your background code here. Don't touch any UI components
            ContextWrapper cw = new ContextWrapper(getBaseContext());
            File mypath = cw.getDir("tracks", Context.MODE_PRIVATE);
            try {
                if (!mypath.exists()) {
                    mypath.createNewFile();
                    mypath.mkdir();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Get shared preferences
            myPrefs = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);
            String username = myPrefs.getString("username","");
            String userId = "";
            try {
                String decrypted_username = AESUtils.decrypt(username);
                userId = decrypted_username;
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Datetime stamp
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
            String timeStamp = sdf.format(new Date());

            // Gpx filename, e.g. [userId]-tracks-2020-08-16 14_09_33.gpx
            File gpxfile = new File(mypath, userId + "-tracks-" + timeStamp +".gpx");

            if(generateGpx(gpxfile, "tracks-" + timeStamp, routeTracks))
                return true;
            else
                return false;
        }

        protected void onPostExecute(Boolean result) {
            //This is run on the UI thread so you can do as you wish here
            if(result) {
                Toast.makeText(getApplicationContext(), "Tracks saved.", Toast.LENGTH_SHORT).show();
                routePoints.clear();
                routeTracks.clear();
            } else
                Toast.makeText(getApplicationContext(), "Error saving tracks.", Toast.LENGTH_SHORT).show();
        }
    } // SaveTracks

    public void shareScreenShot(){
        View rootView = getWindow().getDecorView().getRootView();
        rootView.setDrawingCacheEnabled(true);

        Bitmap bitmap = Bitmap.createBitmap(rootView.getDrawingCache());
        rootView.setDrawingCacheEnabled(false);

        // create directory
        //ContextWrapper cw = new ContextWrapper(getBaseContext());
        //File mypath = cw.getDir("pics", Context.MODE_PRIVATE);
        //try {
        //    if (!mypath.exists()) {
        //        mypath.createNewFile();
        //        mypath.mkdir();
        //    }
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}

        File path = Environment.getExternalStorageDirectory();
        File dir = new File(path + "/storage/emulated/safetravelph");
        boolean success= true;
        if(!dir.exists()){
            Toast.makeText(this, "Directory does not exist, create it", Toast.LENGTH_SHORT).show();
        }
        if(success){
            dir.mkdirs();
            Toast.makeText(this, "Directory created ", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Failed to create directory", Toast.LENGTH_SHORT).show();
        }

        // Create file
        String imagename =   Calendar.getInstance().getTime().toString()+".jpeg";
        File filePath = new File(path, imagename);
        File fileScreenshot = new File(String.valueOf(filePath));

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(fileScreenshot);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            Toast.makeText(Fleet.this, "Image Save in DCN", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Share content
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setAction(Intent.ACTION_SEND);
            Uri contentUri = FileProvider.getUriForFile(this, "com.your.package.fileProvider",fileScreenshot );
            intent.setDataAndType(contentUri, "image/jpeg");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_STREAM, contentUri);
            intent.setType("image/*");
            startActivity(Intent.createChooser(intent,"Share Image Via"));
        } catch (ActivityNotFoundException anfe) {
            Toast.makeText(this, "No activity found to open this attachment.", Toast.LENGTH_LONG).show();
        }
    }

}
