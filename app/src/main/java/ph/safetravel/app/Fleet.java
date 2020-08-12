package ph.safetravel.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import ph.safetravel.app.databinding.ActivityFleetBinding;
import ph.safetravel.app.protos.Vehicle;


public class Fleet extends AppCompatActivity implements OnMapReadyCallback  {
    SharedPreferences myPrefs;
    MqttAndroidClient client;
    MqttConnectOptions options;
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
    DBManager dbManager;
    // Sensor
    //private SensorManager sensorManager;
    //Sensor accelerometer;
    //SensorEventListener acListener;
    //Sensor gyroscope;
    Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    ImageButton boardButton, alightButton;
    int numPass;
    double speed=0.0, avgspeed=0.0, distance=0.0;
    TextView NumPassengers;
    TextView Speed, Distance, AverageSpeed;
    ProgressBar pgsBar;
    ActivityFleetBinding bi;
    boolean isRotate = false;
    int feedsCount;
    Polyline route;
    List<LatLng> routePoints = new ArrayList<LatLng>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fleet);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final MediaPlayer mp = new MediaPlayer();

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
                    bi.fabFleetFeeds.hide();
                    isRotate=true;
                } else{
                    bi.fabFleetAdd.hide();
                }

                // Show fragment
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

                FleetInfoFragment fleetInfoFragment = new FleetInfoFragment();

                FrameLayout layout = (FrameLayout) findViewById(R.id.container_frame);
                layout.setVisibility(View.VISIBLE);

                if (fleetInfoFragment.isAdded()) {
                    ft.show(fleetInfoFragment);
                } else {
                    ft.add(R.id.container_frame, fleetInfoFragment);
                    ft.show(fleetInfoFragment);
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
                    bi.fabFleetFeeds.hide();
                    isRotate=true;
                } else{
                    bi.fabFleetAdd.hide();
                }

                // Show fragment
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

                FleetFeedsFragment fleetFeedsFragment = new FleetFeedsFragment();

                FrameLayout layout = (FrameLayout) findViewById(R.id.container_frame);
                layout.setVisibility(View.VISIBLE);

                if (fleetFeedsFragment.isAdded()) {
                    ft.show(fleetFeedsFragment);
                } else {
                    ft.add(R.id.container_frame, fleetFeedsFragment);
                    ft.show(fleetFeedsFragment);
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
        pgsBar =  (ProgressBar) findViewById(R.id.progressBarFleet);
        pgsBar.setVisibility(View.INVISIBLE);
        pgsBar.setScaleY(3f);

        // Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.Open, R.string.Close) {
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
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // Navigation
        navigationView = (NavigationView)findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch(id) {
                    case R.id.profile:
                    {
                        Toast.makeText(Fleet.this, "My Profile", Toast.LENGTH_SHORT).show();
                    }
                    case R.id.settings:
                    {
                        Toast.makeText(Fleet.this, "Settings", Toast.LENGTH_SHORT).show();
                    }
                    case R.id.help:
                    {
                        Toast.makeText(Fleet.this, "Help", Toast.LENGTH_SHORT).show();
                    }
                    case R.id.about:
                    {
                        //drawerLayout.closeDrawer(Gravity.LEFT);
                        //Intent intent = new Intent(Fleet.this, About.class);
                        //startActivity(intent);
                    }
                }
                return false;
            }
        });

        // Displays
        NumPassengers = findViewById(R.id.txtNumPass);
        NumPassengers.setText(String.valueOf(numPass));
        Speed = findViewById(R.id.txtSpeedNum);
        AverageSpeed = findViewById(R.id.txtAvgSpeedNum);
        Distance =  findViewById(R.id.txtDistNum);

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
                    myPrefs = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);
                    String username = myPrefs.getString("username",null);
                    String androidId = myPrefs.getString("androidId",null);
                    String paxCode = username;
                    String vehCode = "None";
                    numPass=numPass+1;
                    NumPassengers.setText(String.valueOf(numPass));
                    publishMessage(vehicleMessage(androidId, lat, lng, timeStamp, paxCode, vehCode, true, false, numPass));
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
                    myPrefs = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);
                    String username = myPrefs.getString("username",null);
                    String androidId = myPrefs.getString("androidId",null);
                    String paxCode = username;
                    String vehCode = "None";
                    if (numPass != 0) numPass=numPass-1;
                    NumPassengers.setText(String.valueOf(numPass));
                    publishMessage(vehicleMessage(androidId, lat, lng, timeStamp, paxCode, vehCode, false, true, numPass));
                }
            }
        };
        alightButton.setOnClickListener(null);

        // Get shared preferences
        myPrefs = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);
        String username = myPrefs.getString("username", null);

        View headerView = navigationView.getHeaderView(0);
        TextView navUsername =  headerView.findViewById(R.id.nav_header_textView);
        navUsername.setText(username);

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

        // Open dbase
        //dbManager = new DBManager(this);
        //dbManager.open();

        // Map fragment
        //mMapFragmentFleet = MapFragment.newInstance();
        //FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        //fragmentTransaction.add(R.id.mapFragFleet, mMapFragmentFleet);
        //fragmentTransaction.commit();
        //mMapFragmentFleet.getMapAsync(this);

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
        tButton = findViewById(R.id.toggleFleet);
        tButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked && isGPS) {
                    connectBroker();
                    startLocationUpdates();
                    boardButton.setOnClickListener(boardClickListener);
                    alightButton.setOnClickListener(alightClickListener);
                    pgsBar.setVisibility(View.VISIBLE);
                } else {
                    stopLocationUpdates();
                    disconnectBroker();
                    boardButton.setOnClickListener(null);
                    alightButton.setOnClickListener(null);
                    pgsBar.setVisibility(View.INVISIBLE);
                    speed=0.0f;
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
                //Log.i(TAG, "connection lost");
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
        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(4);
        menuItem.setChecked(true);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_logout: {
                        // Dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(Fleet.this);
                        builder.setMessage("Are you sure you want to Logout?");
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
                                Intent intent0 = new Intent(Fleet.this, MainActivity.class);
                                startActivity(intent0);
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
                        alertDialog.setTitle("Status");
                        alertDialog.setCancelable(false);
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.show();
                        break;
                    }
                    case R.id.navigation_data: {
                        // Dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(Fleet.this);
                        builder.setMessage("Are you sure you want to exit Fleet?");
                        builder.setCancelable(false);
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                closeApp();
                                startActivity(new Intent(Fleet.this, Data.class));
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
                }
                return true;
            }
        });

    } // onCreate

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
            //List<LatLng> routePoints;
            //route = mMap.addPolyline(new PolylineOptions()
            //        .width(10)
            //        .color(Color.BLUE)
            //        .geodesic(true)
            //        //.add(mmla)
            //        .zIndex(100));
            //route.setPoints(routePoints);

            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);

                LatLng mCurrentPoint= new LatLng(location.getLatitude(),location.getLongitude());
                //Log.d("Route", mCurrentPoint.toString());
                routePoints.add(mCurrentPoint);
                Log.d("Route", routePoints.toString());

                if(mLastLocation != null) {
                    // Get loc1 and loc2
                    Location loc1 = mLastLocation;
                    Location loc2 = location;
                    distance = distance + computeDistance(loc1, loc2);
                    speed = computeSpeed(loc1, loc2);
                    avgspeed = (avgspeed + speed)/2;
                    Log.d("Loc/Speed/Dist", String.valueOf(loc1.getLatitude()) + ","
                            + String.valueOf(loc2.getLatitude()) + "," + String.valueOf(speed)+ ","
                            + String.valueOf(distance));
                }

                mLastLocation = location;  // update the last location

                if (mLastLocation != null) {

                    //mCurrentPoint= new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
                    //routePoints.add(mCurrentPoint);

                    //Polyline route = mMap.addPolyline(new PolylineOptions()
                    //        .width(10)
                    //        .color(Color.BLUE)
                    //        .geodesic(true)
                    //        .zIndex(100));
                    //route.setPoints(routePoints);

                    String lat = String.valueOf(location.getLatitude());
                    String lng = String.valueOf(location.getLongitude());

                    //Location loc1 = mLastLocation;
                    //Location loc2 = location;
                    //speed = avgspeed(loc1, loc2);
                    //speed = location.getSpeed()*3.6f;
                    //Speed.setText(String.valueOf(speed));

                    Speed.setText(String.format("%.2f", speed));
                    AverageSpeed.setText(String.format("%.2f", avgspeed));
                    Distance.setText(String.format("%.2f", distance));

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
                    publishMessage(vehicleMessage(androidId, lat, lng, timeStamp, userId, vehicleId, false, false, numPass));

                    // Place location marker
                    if (mCurrLocationMarker != null) {
                        mCurrLocationMarker.remove();
                    }
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("Current Position");
                    mMap.clear();
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

    static double computeDistance (Location loc1, Location loc2) {
        double R = 6371000;
        double la1 = loc1.getLatitude()* Math.PI/180;
        double la2 = loc2.getLatitude()* Math.PI/180;
        double lo1 = loc1.getLongitude()* Math.PI/180;
        double lo2 = loc2.getLongitude()* Math.PI/180;
        double tmp1 = Math.sin((la1-la2)/2)*Math.sin((la1-la2)/2) + Math.cos(la1)*Math.cos(la2) * Math.sin((lo1-lo2)/2) * Math.sin((lo1-lo2)/2);
        double tmp2 = Math.sqrt(tmp1);
        double d = Math.abs(2 * R * Math.asin(tmp2) * 100000) / 100000;

        return d;
    } // distance

    static double computeSpeed (Location loc1, Location loc2) {
        double s = computeDistance(loc1, loc2) / (loc2.getTime() - loc1.getTime());

        return s;
    } // computeSpeed

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

    public byte[] vehicleMessage(String deviceId, String lat, String lng, String timestamp, String userId, String vehicleId, boolean board, boolean alight, int numPass) {
        Vehicle vehicle = Vehicle.newBuilder()
                .setDeviceId(deviceId)
                .setLat(lat)
                .setLng(lng)
                .setTimestamp(timestamp)
                .setUserId(userId)
                .setVehicleId(vehicleId)
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
                    Toast.makeText(getApplicationContext(),"Disconnected", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getApplicationContext(),"Could Not Disconnect", Toast.LENGTH_SHORT).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    } // disconnectBroker

    @Override
    public void onBackPressed() {
;
    } // onBackPressed

    @Override
    protected void onResume() {
        super.onResume();
        if (isContinue) {
            startLocationUpdates();
        }
        //sensorManager.registerListener(acListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    } // onResume

    @Override
    protected void onPause() {
        super.onPause();
        //sensorManager.unregisterListener(acListener);
    } // onPause

    private void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    } // startLocationUpdates

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    } // stopLocationUpdates

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //mMap = googleMap;
        //LatLng mmla = new LatLng(14.6091, 121.0223);
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mmla, 10));
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

        //LatLng mmla = new LatLng(14.6091, 121.0223);

        // Initialize
        //List<LatLng> routePoints = new ArrayList<LatLng>();
        //routePoints.add(mmla);
        //route = mMap.addPolyline(new PolylineOptions()
        //        .width(10)
        //        .color(Color.BLUE)
        //        .geodesic(true)
        //        //.add(mmla)
        //        .zIndex(100));
        //route.setPoints(routePoints);

        LatLng mmla = new LatLng(14.6091, 121.0223);
        mMap.addMarker(new MarkerOptions().position(mmla).title("Current Position"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mmla, 10));
    } // onMapReady

    public void closeApp(){
        stopLocationUpdates();
        if(client.isConnected()) {
            disconnectBroker();
        }
        this.finish();
    } // closeApp

    public void restoreFleetFab() {
        // Show Fab
        if(isRotate){
            bi.fabFleetAdd.show();
            bi.fabFleetInfo.show();
            bi.fabFleetFeeds.show();
        } else{
            bi.fabFleetAdd.show();
        }
    } // restoreFab

    // Drawerlayout menu item clicked
    //@Override
    //public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    //    // fetch the user selected value
    //    String item = parent.getItemAtPosition(position).toString();
    //    // create Toast with user selected value
    //    Toast.makeText(Fleet.this, "Selected Item is: \t" + item, Toast.LENGTH_LONG).show();
    //    // set user selected value to the TextView
//
    //}

}
