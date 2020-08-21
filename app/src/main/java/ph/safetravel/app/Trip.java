package ph.safetravel.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
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
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
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

import ph.safetravel.app.databinding.ActivityTripBinding;
import ph.safetravel.app.protos.Passenger;
import ph.safetravel.app.protos.Alert;
import ph.safetravel.app.protos.Rating;

public class Trip extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {
    SharedPreferences myPrefs;
    MqttAndroidClient client;
    MqttConnectOptions options;
    //Vibrator vibrator;
    String TAG="Mqtt";
    String MqttHost = "tcp://mqtt.safetravel.ph:8883";
    final String MqttUsername = "mqtt";
    final String MqttPassword = "mqtt";
    String clientId;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private boolean isGPS = false;
    private boolean isContinue = false;
    ToggleButton startButton;
    ToggleButton tButton;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    GoogleMap mMap;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    Boolean brokerIsConnected = false;
    ProgressBar pgsBar;
    ActivityTripBinding bi;
    boolean isRotate = false;
    int feedsCount;
    double speed=0.0, distance=0.0;
    Polyline route;
    List<LatLng> routePoints = new ArrayList<LatLng>();
    List<Location> routeTracks = new ArrayList<Location>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

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
        ViewAnimation.init(bi.fabTripRating);
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
                    ViewAnimation.showIn(bi.fabTripRating);
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
                    ViewAnimation.showOut(bi.fabTripRating);
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
                    bi.fabTripRating.hide();
                    isRotate=true;
                } else{
                    bi.fabTripAdd.hide();
                }

                // Show fragment
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

                TripInfoFragment tripInfoFragment = new TripInfoFragment();

                FrameLayout layout = (FrameLayout) findViewById(R.id.Tripcontainer_frame);
                layout.setVisibility(View.VISIBLE);

                if (tripInfoFragment.isAdded()) {
                    ft.show(tripInfoFragment);
                } else {
                    ft.add(R.id.Tripcontainer_frame, tripInfoFragment);
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
                    bi.fabTripRating.hide();
                    isRotate=true;
                } else{
                    bi.fabTripAdd.hide();
                }

                // Show fragment
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

                TripAlertFragment tripAlertFragment = new TripAlertFragment();

                FrameLayout layout = (FrameLayout) findViewById(R.id.Tripcontainer_frame);
                layout.setVisibility(View.VISIBLE);

                if (tripAlertFragment.isAdded()) {
                    ft.show(tripAlertFragment);
                } else {
                    ft.add(R.id.Tripcontainer_frame, tripAlertFragment);
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
                    bi.fabTripRating.hide();
                    isRotate=true;
                } else{
                    bi.fabTripAdd.hide();
                }

                // Show fragment
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

                TripFeedsFragment tripFeedsFragment = new TripFeedsFragment();

                FrameLayout layout = (FrameLayout) findViewById(R.id.Tripcontainer_frame);
                layout.setVisibility(View.VISIBLE);

                if (tripFeedsFragment.isAdded()) {
                    ft.show(tripFeedsFragment);
                } else {
                    ft.add(R.id.Tripcontainer_frame, tripFeedsFragment);
                    ft.show(tripFeedsFragment);
                }
                ft.commit();
            }
        });

        // Trip Rating Fab
        bi.fabTripRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide Fab
                if(isRotate){
                    bi.fabTripAdd.hide();
                    bi.fabTripInfo.hide();
                    bi.fabTripFeeds.hide();
                    bi.fabTripAlert.hide();
                    bi.fabTripRating.hide();
                    isRotate=true;
                } else{
                    bi.fabTripAdd.hide();
                }

                // Show fragment
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

                TripRatingFragment tripRatingFragment = new TripRatingFragment();

                FrameLayout layout = (FrameLayout) findViewById(R.id.Tripcontainer_frame);
                layout.setVisibility(View.VISIBLE);

                if (tripRatingFragment.isAdded()) {
                    ft.show(tripRatingFragment);
                } else {
                    ft.add(R.id.Tripcontainer_frame, tripRatingFragment);
                    ft.show(tripRatingFragment);
                }
                ft.commit();
            }
        });

        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.trip_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Trip history
                if(item.getItemId()==R.id.triphistory)
                {
                    startActivity( new Intent(Trip.this, TripHistory.class));
                }
                // Trip settings
                if(item.getItemId()==R.id.settings)
                {
                    startActivity(new Intent(Trip.this, TripSettings.class));
                }
                return false;
            }
        });

        // Progress bar
        pgsBar = findViewById(R.id.progressBarTrip);
        pgsBar.setVisibility(View.INVISIBLE);
        pgsBar.setScaleY(3f);

        // Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        // for no shadow
        //drawerLayout.setScrimColor(Color.TRANSPARENT);
        //drawerLayout.setDrawerElevation(0);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.Open, R.string.Close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // Hide Fab
                if(isRotate){
                    bi.fabTripAdd.hide();
                    bi.fabTripInfo.hide();
                    bi.fabTripRating.hide();
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
                    bi.fabTripRating.hide();
                    bi.fabTripFeeds.show();
                    bi.fabTripAlert.show();
                } else{
                    bi.fabTripAdd.show();
                }
            }
        };
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // Navigation view
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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
                        startActivity(new Intent(Trip.this, About.class));
                    }
                    case R.id.share:
                    {
                        //Toast.makeText(Data.this, "Settings", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case R.id.logout:
                    {
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
                startActivity(new Intent(Trip.this, QRCode.class));
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

        //
        final Button sendAlertButton = (Button) findViewById(R.id.btnSendAlert);
        final Button sendRatingButton = (Button) findViewById(R.id.btnSendRating);

        // Toogle button
        startButton = findViewById(R.id.toggleTrip);
        startButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked && isGPS) {
                    // Start tracking
                    connectBroker();
                    startLocationUpdates();
                    pgsBar.setVisibility(View.VISIBLE);
                } else {
                    // Dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(Trip.this);
                    builder.setMessage("Are you sure you want to stop Tracking?");
                    builder.setCancelable(true);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            // Indicate 'Start Tracking'
                            startButton.setChecked(false);
                            // Stop tracking
                            stopLocationUpdates();
                            disconnectBroker();
                            pgsBar.setVisibility(View.INVISIBLE);
                            // Save GPS tracks
                            SaveTracks saveTracks  = new Trip.SaveTracks();
                            saveTracks.execute();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Indicate 'Stop Tracking'
                            startButton.setChecked(true);
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
        MenuItem menuItem = menu.getItem(2);
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
            public boolean onNavigationItemSelected(@NonNull final MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_board: {
                        // Dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(Trip.this);
                        builder.setMessage("Are you sure you want to exit Trip?");
                        builder.setCancelable(false);
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                closeApp();
                                startActivity(new Intent(Trip.this, Board.class));
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
                        builder.setMessage("Are you sure you want to exit Trip?");
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
                        builder.setMessage("Are you sure you want to exit Trip?");
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
                    case R.id.navigation_help: {
                        // Dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(Trip.this);
                        builder.setMessage("Are you sure you want to exit Trip?");
                        builder.setCancelable(false);
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                closeApp();
                                startActivity(new Intent(Trip.this, Help.class));
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

    // Send alerts from trip alert fragment
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
        // Alert description
        String description = alertString.toString();

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

            // Publish message
            publishAlert(alertMessage(deviceId, lat, lng, timeStamp, userId, description, vehicleId, vehicleDetails));
            Toast.makeText(getApplicationContext(), "Alert sent.", Toast.LENGTH_SHORT).show();
        }
    } // sendAlert

    // Send alerts from trip alert fragment
    public void sendRating() {
        SmileRating rating_vehiclecondition = (SmileRating) findViewById(R.id.rating_vehiclecondition);
        SmileRating rating_ridecomfort = (SmileRating) findViewById(R.id.rating_ridecomfort);
        SmileRating rating_serviceadequacy = (SmileRating) findViewById(R.id.rating_serviceadequacy);
        SmileRating rating_stopaccessibility = (SmileRating) findViewById(R.id.rating_stopaccessibility);
        SmileRating rating_infoprovision = (SmileRating) findViewById(R.id.rating_infoprovision);
        SmileRating rating_serviceavailability = (SmileRating) findViewById(R.id.rating_serviceavailability);
        SmileRating rating_routeconnectivity = (SmileRating) findViewById(R.id.rating_routeconnectivity);
        SmileRating rating_overall = (SmileRating) findViewById(R.id.rating_overall);

        StringBuilder ratingString = new StringBuilder();
        ratingString.append(rating_vehiclecondition.getRating());
        ratingString.append("," + rating_ridecomfort.getRating());
        ratingString.append("," + rating_serviceadequacy.getRating());
        ratingString.append("," + rating_stopaccessibility.getRating());
        ratingString.append("," + rating_infoprovision.getRating());
        ratingString.append("," + rating_serviceavailability.getRating());
        ratingString.append("," + rating_routeconnectivity.getRating());
        ratingString.append("," + rating_overall.getRating());
        // Rating description
        String description = ratingString.toString();

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

            // Publish message
            publishRating(ratingMessage(deviceId, lat, lng, timeStamp, userId, description, vehicleId, vehicleDetails));
            Toast.makeText(getApplicationContext(), "Rating sent.", Toast.LENGTH_SHORT).show();
        }
    } // sendRating

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
            bi.fabTripRating.show();
        } else{
            bi.fabTripAdd.show();
        }
    } // restoreFab

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

                routeTracks.add(location);
                LatLng mCurrentPoint= new LatLng(location.getLatitude(),location.getLongitude());
                routePoints.add(mCurrentPoint);
                //Log.d("Route", routePoints.toString());

                if(mLastLocation != null) {
                    Location prevLocation = mLastLocation;
                    Location currLocation = location;
                    distance = distance + computeDistance(prevLocation, currLocation)/1000;
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

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                    String timeStamp = sdf.format(new Date());

                    //String lat = String.valueOf(location.getLatitude());
                    //String lng = String.valueOf(location.getLongitude());
                    //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    //sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                    //String timeStamp = sdf.format(new Date());

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

                    String origin = myPrefs.getString("origin","");
                    String destination = myPrefs.getString("origin","");
                    String purpose = myPrefs.getString("purpose","");
                    String mode = myPrefs.getString("mode","");
                    String vehicleId = myPrefs.getString("vehicleId","");
                    String vehicleDetails = myPrefs.getString("vehicleDetails","");

                    // Publish message
                    publishMessage(passengerMessage(deviceId, lat, lng, timeStamp, userId, origin, destination, purpose, mode, vehicleId, vehicleDetails));

                    // Place location marker
                    if (mCurrLocationMarker != null) {
                        mCurrLocationMarker.remove();
                    }
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    // Get shared preferences
                    myPrefs = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);

                    // Origin marker
                    String originLat = myPrefs.getString("originLat","");
                    String originLng = myPrefs.getString("originLng","");

                    if (!originLat.equals("") && !originLat.equals("")) {
                        LatLng originPoint = new LatLng(Double.parseDouble(originLat), Double.parseDouble(originLng));
                        BitmapDescriptor iconOrigin = BitmapDescriptorFactory.fromResource(R.drawable.ic_flagorigin);
                        MarkerOptions markerOptions = new MarkerOptions().position(originPoint)
                                .icon(BitmapDescriptorFactory
                                        .defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                .title("Origin");
                        mMap.addMarker(markerOptions);
                    }

                    // Destination marker
                    String destinationLat = myPrefs.getString("destinationLat","");
                    String destinationLng = myPrefs.getString("destinationLng","");

                    if (!destinationLat.equals("") && !destinationLng.equals("")) {
                        LatLng destinationPoint = new LatLng(Double.parseDouble(destinationLat), Double.parseDouble(destinationLng));
                        BitmapDescriptor iconDestination = BitmapDescriptorFactory.fromResource(R.drawable.ic_flagorigin);
                        MarkerOptions markerOptions = new MarkerOptions().position(destinationPoint)
                                .icon(BitmapDescriptorFactory
                                        .defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                .title("Destination");
                        mMap.addMarker(markerOptions);
                    }

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

    public byte[] alertMessage(String deviceId, String lat, String lng, String timestamp, String userId, String description, String vehicleId, String vehicleDetails) {
        Alert alert = Alert.newBuilder()
                .setDeviceId(deviceId)
                .setLat(lat)
                .setLng(lng)
                .setTimestamp(timestamp)
                .setUserId(userId)
                .setDescription(description)
                .setVehicleId(vehicleId)
                .setVehicleDetails(vehicleDetails)
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

    public byte[] ratingMessage(String deviceId, String lat, String lng, String timestamp, String userId, String description, String vehicleId, String vehicleDetails) {
        Rating rating = Rating.newBuilder()
                .setDeviceId(deviceId)
                .setLat(lat)
                .setLng(lng)
                .setTimestamp(timestamp)
                .setUserId(userId)
                .setDescription(description)
                .setVehicleId(vehicleId)
                .setVehicleDetails(vehicleDetails)
                .build();
        byte message[] = rating.toByteArray();
        return message;
    } // ratingMessage

    public void publishRating(byte[] payload) {
        try {
            if (!client.isConnected()) {
                client.connect();
            }
            MqttMessage message = new MqttMessage();
            message.setPayload(payload);
            message.setQos(0);
            client.publish("ratings", message,null, new IMqttActionListener() {
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
    } // publishRating

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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

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

            // Create file
            String filename =  Calendar.getInstance().getTime().toString();
            File gpxfile = new File(mypath, filename +".gpx");
            // Gpx filename, e.g. [userId]-tracks-2020-08-16 14_09_33.gpx
            //File gpxfile = new File(mypath, userId + "-tracks-" + timeStamp +".gpx");

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

}
