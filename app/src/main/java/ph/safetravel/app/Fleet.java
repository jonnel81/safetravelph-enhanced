package ph.safetravel.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;

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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import ph.safetravel.app.protos.Vehicle;


public class Fleet extends FragmentActivity implements OnMapReadyCallback  {
    SharedPreferences myPrefs;
    View view;
    MqttAndroidClient client;
    MqttConnectOptions options;
    MqttClientPersistence clientPersistence;
    Vibrator vibrator;
    String mqttTopic = "";
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
    MapFragment mMapFragment;
    GoogleMap mMap;
    DBManager dbManager;
    private SensorManager sensorManager;
    Sensor accelerometer;
    SensorEventListener acListener;
    Sensor gyroscope;
    Toolbar toolbar;
    ImageButton boardButton, alightButton;
    int numPass;
    TextView NumPassengers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fleet);

        NumPassengers = findViewById(R.id.txtNumPass);
        NumPassengers.setText(String.valueOf(numPass));

        // App Tollbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Fleet Tracking");
        toolbar.inflateMenu(R.menu.main_menu);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId()==R.id.item1) {
                    startActivity(new Intent(getApplicationContext(), BarcodeReader.class));
                }
                if(item.getItemId()==R.id.item2)
                {
                    // do something
                }
                return false;
            }
        });

        // Board button
        boardButton = findViewById(R.id.btnBoard);
        final OnClickListener boardClickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
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
        final OnClickListener alightClickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
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
        mMapFragment = MapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.mapFragFleet, mMapFragment);
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

        view = findViewById(android.R.id.content);
        //subText = (EditText) findViewById(R.id.txtMessage);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        // BottomNavigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(4);
        menuItem.setChecked(true);

        // Toogle Button
        tButton = findViewById(R.id.toggleFleet);
        tButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked && isGPS) {
                    connectBroker();
                    startLocationUpdates();
                    boardButton.setOnClickListener(boardClickListener);
                    alightButton.setOnClickListener(alightClickListener);
                } else {
                    stopLocationUpdates();
                    disconnectBroker();
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
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.i(TAG, "topic: " + topic + ", msg: " + new String(message.getPayload()));
                //subText.setText(new String(message.getPayload()));
                //vibrator.vibrate(500);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i(TAG, "msg delivered");
            }
        }); // mqtt client callback

        // BottomNavigation
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_logout: {
                        // Dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(Fleet.this);
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
                                // Go to main activity
                                Intent intent0 = new Intent(Fleet.this, MainActivity.class);
                                startActivity(intent0);
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

                        break;
                    }
                    case R.id.navigation_info: {
                        Intent intent1 = new Intent(Fleet.this, Info.class);
                        startActivity(intent1);
                        break;
                    }
                    case R.id.navigation_report: {
                        Intent intent2 = new Intent(Fleet.this, Report.class);
                        startActivity(intent2);
                        break;
                    }
                    case R.id.navigation_trip: {
                        Intent intent3 = new Intent(Fleet.this, Trip.class);
                        startActivity(intent3);
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
        @SuppressLint("DefaultLocale")
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                mLastLocation = location;
                //sendTrack();
                if (mLastLocation != null) {
                    String lat = String.valueOf(location.getLatitude());
                    String lng = String.valueOf(location.getLongitude());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                    String timeStamp = sdf.format(new Date());
                    myPrefs = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);
                    String username = myPrefs.getString("username",null);
                    String androidId = myPrefs.getString("androidId",null);
                    String paxCode = username;
                    //String vehicleId = myPrefs.getString("vehicleId",null);
                    //String vehCode = vehicleId;
                    String vehCode = "None";
                    // Publish message
                    publishMessage(vehicleMessage(androidId, lat, lng, timeStamp, paxCode, vehCode, false, false, numPass));
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

    //public void sendBoard

    public void subscribeTopic(String topic) {
        try {
            client.subscribe(topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "subscribed succeed");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "subscribed failed");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public byte[] vehicleMessage(String deviceId, String lat, String lng, String timestamp, String paxCode, String vehCode, boolean board, boolean alight, int numPass) {
        Vehicle vehicle = Vehicle.newBuilder()
                .setDeviceId(deviceId)
                .setLat(lat)
                .setLng(lng)
                .setTimestamp(timestamp)
                .setPaxCode(paxCode)
                .setVehCode(vehCode)
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
                    Log.i (TAG, "publish succeed! ") ;
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "publish failed!") ;
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, e.toString());
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
        // Go to trip activity
        startActivity(new Intent(this, Trip.class));
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
        mMap = googleMap;
        LatLng mmla = new LatLng(14.6091, 121.0223);
        //mMap.addMarker(new MarkerOptions().position(mmla).title("Marker Position"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mmla, 10));
    }
}
