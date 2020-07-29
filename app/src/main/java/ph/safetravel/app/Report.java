package ph.safetravel.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.navigation.NavigationView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Report extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerDragListener, AsyncResponse {
    EditText street, landmarks, barangay, city, description, lat, lng;
    MapFragment mMapFragment;
    SharedPreferences myPrefs;
    GoogleMap mMap;
    View view;
    AsyncResponse aR = Report.this;
    BackgroundWorker backgroundWorker = new BackgroundWorker(Report.this, aR);
    AlertDialog.Builder builder;
    AlertDialog alertDialog;
    Button buttonReport;
    Button takePhotoButton, pickPhotoButton;
    ImageView imageView;
    public final String APP_TAG = "CameraTest";
    public final String photoFileName = "photo.jpg";
    String base64String="";
    Location mLastLocation;
    Marker mCurrLocationMarker;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private boolean isContinue = false;
    private boolean isGPS = false;
    private Toolbar toolbar;
    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        street = findViewById(R.id.etStreet);
        landmarks = findViewById(R.id.etLandmarks);
        barangay = findViewById(R.id.etBarangay);
        city = findViewById(R.id.etCity);
        description = findViewById(R.id.etDescription);
        lat = findViewById(R.id.etLat);
        lng = findViewById(R.id.etLng);
        view = findViewById(android.R.id.content);

        // Tollbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);

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
                    case R.id.profile:
                    {
                        Toast.makeText(Report.this, "Profile", Toast.LENGTH_SHORT).show();
                    }
                    case R.id.settings:
                    {
                        Toast.makeText(Report.this, "Settings", Toast.LENGTH_SHORT).show();
                    }
                    case R.id.about:
                    {
                        dl.closeDrawer(Gravity.LEFT);
                        Intent intent = new Intent(Report.this, About.class);
                        startActivity(intent);
                    }
                }
                return false;
            }
        });

        // Avoid Uri issues
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        // Taking a photo
        takePhotoButton = findViewById(R.id.btnTakePhoto);
        pickPhotoButton = findViewById(R.id.btnPickPhoto);
        imageView = findViewById(R.id.imageviewPhoto);
        takePhotoButton.setEnabled(false);
        pickPhotoButton.setEnabled(false);

        // Get permission for camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, AppConstants.CAMERA_REQUEST);
        } else {
            takePhotoButton.setEnabled(true);
            pickPhotoButton.setEnabled(true);
        }

        // Disable report button
        buttonReport = findViewById(R.id.btnReport);
        buttonReport.setEnabled(false);

        // Watcher for complete inputs
        street.addTextChangedListener(watcher);
        landmarks.addTextChangedListener(watcher);
        barangay.addTextChangedListener(watcher);
        city.addTextChangedListener(watcher);
        description.addTextChangedListener(watcher);

        // FuseLocationProviderClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Location request for GPS
        locationRequest= LocationRequest.create();
        //locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(120 * 1000); // 2 minutes
        locationRequest.setFastestInterval(60 * 1000); // 1 minute

        // Map fragment
        mMapFragment = MapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.map, mMapFragment);
        fragmentTransaction.commit();
        mMapFragment.getMapAsync(this);

        new GpsUtils(this).turnGPSOn(new GpsUtils.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                // turn on GPS
                isGPS = isGPSEnable;
            }
        });

        // BottomNavigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_logout: {
                        // Dialog
                        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Report.this);
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
                                startActivity(new Intent(Report.this, MainActivity.class));
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                startActivity(new Intent(Report.this, Report.class));
                            }
                        });
                        androidx.appcompat.app.AlertDialog alertDialog = builder.create();
                        alertDialog.setTitle("Status");
                        alertDialog.setCancelable(false);
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.show();

                        break;
                    }
                    case R.id.navigation_data: {
                        startActivity(new Intent(Report.this, Data.class));
                        break;
                    }
                    case R.id.navigation_report: {

                        break;
                    }
                    case R.id.navigation_trip: {
                        startActivity(new Intent(Report.this, Trip.class));
                        break;
                    }
                    case R.id.navigation_fleet: {
                        startActivity(new Intent(Report.this, Fleet.class));
                        break;
                    }
                }
                return true;
            }
        });

    } // onCreate

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
                        mFusedLocationClient.getLastLocation().addOnSuccessListener(Report.this, new OnSuccessListener<Location>() {
                            @SuppressLint("DefaultLocale")
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    // Place location marker
                                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                    MarkerOptions markerOptions = new MarkerOptions();
                                    markerOptions.position(latLng);
                                    markerOptions.title("Marker Position");
                                    mMap.clear();
                                    mCurrLocationMarker = mMap.addMarker(markerOptions);
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                                    lat.setText(String.format ("%.9f", location.getLatitude()));
                                    lng.setText(String.format ("%.9f", location.getLongitude()));
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
            case AppConstants.CAMERA_REQUEST: {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                            && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                            takePhotoButton.setEnabled(true);
                            pickPhotoButton.setEnabled(true);
                    }
                break;
            }
        }
    } // onRequestPermissionsResult

    public void onTakePhoto(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getPhotoFileUri(photoFileName));
        startActivityForResult(intent, AppConstants.TAKE_PHOTO_REQUEST);
    } // onTakePhoto

    @SuppressLint("IntentReset")
    public void onPickPhoto(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Photo"),AppConstants.PICK_PHOTO_REQUEST);
    } // onPickPhoto

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == AppConstants.TAKE_PHOTO_REQUEST) {
                Uri takenPhotoUri = getPhotoFileUri(photoFileName);
                // Display the image, all images in Android are by default Bitmap images
                Bitmap takenImage = BitmapFactory.decodeFile(takenPhotoUri.getPath());
                imageView.setImageBitmap(takenImage);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(takenImage, 640, 480, true);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                byte[] imageBytes = bytes.toByteArray();
                base64String = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
                System.out.println(base64String);
            }
        }
        if (resultCode == RESULT_OK) {
            if(requestCode == AppConstants.PICK_PHOTO_REQUEST && data != null && data.getData() != null) {
                Uri uri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    ImageView imageView = findViewById(R.id.imageviewPhoto);
                    imageView.setImageBitmap(bitmap);
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 640, 480, true);
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    byte[] imageBytes = bytes.toByteArray();
                    base64String = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
                    System.out.println(base64String);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (resultCode == RESULT_OK) {
            if (requestCode == AppConstants.GPS_REQUEST) {
                // flag maintain before get location
                isGPS = true;
                // Get current location
                FusedLocationProviderClient mFusedLocationClient =  LocationServices.getFusedLocationProviderClient(this);
                Task<Location> locationTask = mFusedLocationClient.getLastLocation();
                locationTask.addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location
                        if (location != null) {
                            // Place location marker
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(latLng);
                            markerOptions.title("Marker Position");
                            mMap.clear();
                            mCurrLocationMarker = mMap.addMarker(markerOptions);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                            //lat.setText(String.format ("%.9f", location.getLatitude()));
                            //lng.setText(String.format ("%.9f", location.getLongitude()));

                            // Get current city address
                            Geocoder geocoder = new Geocoder(Report.this, Locale.getDefault());
                            List<Address> addresses = null;
                            try {
                                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                String addressString = addresses.get(0).getAddressLine(0);
                                String cityString = addresses.get(0).getLocality();
                                Toast.makeText(getApplicationContext(), "Current location: " + cityString, Toast.LENGTH_SHORT).show();
                                city.setText(cityString);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }
    } // onActivityResult

    // Returns the File for a photo stored on disk given the fileName
    public Uri getPhotoFileUri(String fileName) {
        // get the directory path
        // SD card -> Pictures -> CameraTest (custom folder)
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), APP_TAG);
        // Create the storage directory if it does not exist
        if(!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            //System.out.println("failed to create the directory");
        }
        // path of SD Card -> Pictures -> CameraTest -> photo.jpg
        return Uri.fromFile(new File(mediaStorageDir.getPath() + File.separator + fileName));
    } // getPhotoFileUri

    private final TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (street.getText().toString().length()==0 || landmarks.getText().toString().length()==0 ||
                    barangay.getText().toString().length()==0 || city.getText().toString().length()==0 ||
                    description.getText().toString().length()==0) {
                buttonReport.setEnabled(false);
            } else {
                buttonReport.setEnabled(true);
            }
        }
    }; // TextWatcher

    public void OnReport(View view) {
        // Get user id from shared preferences
        myPrefs=getSharedPreferences("MYPREFS",Context.MODE_PRIVATE);
        String username = myPrefs.getString("username",null);

        String str_street = street.getText().toString();
        String str_landmarks = landmarks.getText().toString();
        String str_barangay = barangay.getText().toString();
        String str_city = city.getText().toString();
        String str_description = description.getText().toString();
        String str_lat = lat.getText().toString();
        String str_lng = lng.getText().toString();
        String type = "report";
        String str_image = base64String;

        backgroundWorker.execute(type, username, str_street, str_landmarks, str_barangay, str_city, str_description, str_lat, str_lng, str_image);
    } // onReport

    @Override
    public void processFinish(String result) {
        // Report success
        if(result.equals("Report success")) {
            builder = new AlertDialog.Builder(this);
            builder.setMessage("Report uploaded. Thank you!");
            builder.setCancelable(false);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                    startActivity(new Intent(view.getContext(), Report.class));
                }
            });
            alertDialog = builder.create();
            alertDialog.setTitle("Status");
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setOwnerActivity(this);
            alertDialog.show();
        } else {
            builder = new AlertDialog.Builder(this);
            builder.setMessage("Report error. Please try again.");
            builder.setCancelable(false);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                    startActivity(new Intent(view.getContext(), Report.class));
                }
            });
            alertDialog = builder.create();
            alertDialog.setTitle("Status");
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setOwnerActivity(this);
            alertDialog.show();
        }
    } // processFinish

    public void OnLogout(View view) {
        // Clear shared preferences
        myPrefs = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = myPrefs.edit();
        editor.clear();
        editor.apply();
        startActivity(new Intent(this, MainActivity.class));
    } // onLogout

    @Override
    public void onBackPressed() {

    } // onBackPressed

    @SuppressLint("DefaultLocale")
    @Override
    public void onMapClick(LatLng point) {
        //lat.setText(String.format ("%.9f", point.latitude));
        //lng.setText(String.format ("%.9f", point.longitude));
        mMap.clear();
        mMap.addMarker(new MarkerOptions()
                .position(point)
                .draggable(true)
                .title("Marker location"));
    } // onMapClick

    @SuppressLint("DefaultLocale")
    @Override
    public void onMapLongClick(LatLng point) {
        //lat.setText(String.format ("%.9f", point.latitude));
        //lng.setText(String.format ("%.9f", point.longitude));
        mMap.clear();
        mMap.addMarker(new MarkerOptions()
                .position(point)
                .draggable(true)
                .title("Marker Position"));
    } // onMapLongClick

    @SuppressLint("DefaultLocale")
    @Override
    public void onMarkerDragStart(Marker marker) {
        LatLng position=marker.getPosition();
        //lat.setText(String.format ("%.9f", position.latitude));
        //lng.setText(String.format ("%.9f", position.longitude));
    } // onMarkerDragStart

    @SuppressLint("DefaultLocale")
    @Override
    public void onMarkerDrag(Marker marker) {
        LatLng position=marker.getPosition();
        //lat.setText(String.format ("%.9f", position.latitude));
        //lng.setText(String.format ("%.9f", position.longitude));
    } //onMarkerDrag

    @SuppressLint("DefaultLocale")
    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng position=marker.getPosition();
        //lat.setText(String.format ("%.9f", position.latitude));
        //lng.setText(String.format ("%.9f", position.longitude));
    } // onMarkerDragEnd

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
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            }
        }
    }; // locationCallback

    @SuppressLint("DefaultLocale")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMapLongClickListener(this);

        // Get permission for location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    AppConstants.LOCATION_REQUEST);
        } else {
            mMap.setMyLocationEnabled(true);
        }

        if (isGPS){
            // Get current location
            FusedLocationProviderClient mFusedLocationClient =  LocationServices.getFusedLocationProviderClient(this);
            Task<Location> locationTask = mFusedLocationClient.getLastLocation();
            locationTask.addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location
                    if (location != null) {
                        // Place location marker
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.title("Marker Position");
                        mMap.clear();
                        mCurrLocationMarker = mMap.addMarker(markerOptions);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                        // Get current city address
                        Geocoder geocoder = new Geocoder(Report.this, Locale.getDefault());
                        List<Address> addresses = null;
                        try {
                            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            String streetString = addresses.get(0).getThoroughfare();
                            String brgyString = addresses.get(0).getSubLocality();
                            String cityString = addresses.get(0).getLocality();
                            Toast.makeText(getApplicationContext(), "Current location: " + cityString, Toast.LENGTH_SHORT).show();
                            if(streetString!=null) {
                                street.setText(streetString);
                            }
                            if(brgyString!=null) {
                                barangay.setText(brgyString);
                            }
                            if(cityString!=null) {
                                city.setText(cityString);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } else {
            // Add a marker in Metro Manila and move the camera, if there is no GPS
            LatLng mmla = new LatLng(14.6091, 121.0223);
            mMap.addMarker(new MarkerOptions().position(mmla).title("Marker Position"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mmla, 10));
            //lat.setText(String.format ("%.9f", mmla.latitude));
            //lng.setText(String.format ("%.9f", mmla.longitude));
        }
    } // onMapReady

}
