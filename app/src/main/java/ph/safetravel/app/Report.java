package ph.safetravel.app;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
//import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Report extends FragmentActivity
        implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerDragListener,
        AsyncResponse {
    EditText street, landmarks, barangay, city, description, lat, lng;
    SharedPreferences sp;
    MapFragment mMapFragment;
    SharedPreferences myPrefs;
    private GoogleMap mMap;
    View view;
    AsyncResponse aR = Report.this;
    BackgroundWorker backgroundWorker = new BackgroundWorker(Report.this, aR);
    Context context;
    AlertDialog.Builder builder;
    AlertDialog alertDialog;
    Button buttonReport;
    Button takePhotoButton;
    ImageView imageView;
    public final String APP_TAG = "Testapp";
    public final String photoFileName = "photo.jpg";
    String base64String="";
    LocationManager lm;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    FusedLocationProviderClient mFusedLocationClient;
    Boolean isGPS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        street = (EditText) findViewById(R.id.etStreet);
        landmarks = (EditText) findViewById(R.id.etLandmarks);
        barangay = (EditText) findViewById(R.id.etBarangay);
        city = (EditText) findViewById(R.id.etCity);
        description = (EditText) findViewById(R.id.etDescription);
        lat = (EditText) findViewById(R.id.etLat);
        lng = (EditText) findViewById(R.id.etLng);
        view = (View) findViewById(android.R.id.content);

        // Avoid Uri issues
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        // Taking a photo
        takePhotoButton = (Button) findViewById(R.id.btnTakePhoto);
        imageView = (ImageView) findViewById(R.id.imageview);
        takePhotoButton.setEnabled(false);

        // Get permission for camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        } else {
            takePhotoButton.setEnabled(true);
        }

        // Disable report button
        buttonReport = (Button) findViewById(R.id.btnReport);
        buttonReport.setEnabled(false);

        // Watcher for complete inputs
        street.addTextChangedListener(watcher);
        landmarks.addTextChangedListener(watcher);
        barangay.addTextChangedListener(watcher);
        city.addTextChangedListener(watcher);
        description.addTextChangedListener(watcher);

        // FuseLocationProviderClient
        //mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Map fragment
        mMapFragment = MapFragment.newInstance();
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
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

    } // onCreate

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //
            }
        }
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                //
            }
        }
    } // onRequestPermissionsResult

    public void onTakePhoto(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getPhotoFileUri(photoFileName));
        startActivityForResult(intent, 100);
    } // onTakePhoto

    public void onPickPhoto(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        //intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),200);
    } // onPickPhoto


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
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
        if (requestCode == 200 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                ImageView imageView = findViewById(R.id.imageview);
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
        if (requestCode == 500) {
            if (resultCode == RESULT_OK) {
                isGPS = true; // flag maintain before get location
            }
        }
    } // onActivityResult

    // Returns the File for a photo stored on disk given the fileName
    public Uri getPhotoFileUri(String fileName) {
        // get the directory path, SD card => Pictures => CameraTest (custom folder)
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
        // Go back to main activity
        startActivity(new Intent(this, MainActivity.class));
    } // onLogout

    @Override
    public void onBackPressed() {
        // Do nothing
    } // onBackPressed

    @SuppressLint("DefaultLocale")
    @Override
    public void onMapClick(LatLng point) {
        double latVal = point.latitude;
        double lngVal = point.longitude;
        // Display position values
        lat.setText(String.format ("%.9f", latVal));
        lng.setText(String.format ("%.9f", lngVal));
        // Clear the marker
        mMap.clear();
        // Add a marker to the current position
        mMap.addMarker(new MarkerOptions()
                .position(point)
                .draggable(true)
                .title("Current location"));
    } // onMapClick

    @SuppressLint("DefaultLocale")
    @Override
    public void onMapLongClick(LatLng point) {
        double latVal = point.latitude;
        double lngVal = point.longitude;
        // Display position values
        lat.setText(String.format ("%.9f", latVal));
        lng.setText(String.format ("%.9f", lngVal));
        // Clear the markers
        mMap.clear();
        // Add a marker to the current position
        mMap.addMarker(new MarkerOptions()
                .position(point)
                .draggable(true)
                .title("Current location"));
    } // onMapLongClick

    @SuppressLint("DefaultLocale")
    @Override
    public void onMarkerDragStart(Marker marker) {
        LatLng position=marker.getPosition();
        // Display position values
        double latVal = position.latitude;
        double lngVal = position.longitude;
        lat.setText(String.format ("%.9f", latVal));
        lng.setText(String.format ("%.9f", lngVal));
    } // onMarkerDragStart

    @SuppressLint("DefaultLocale")
    @Override
    public void onMarkerDrag(Marker marker) {
        LatLng position=marker.getPosition();
        // Display position values
        double latVal = position.latitude;
        double lngVal = position.longitude;
        lat.setText(String.format ("%.9f", latVal));
        lng.setText(String.format ("%.9f", lngVal));
    } //onMarkerDrag

    @SuppressLint("DefaultLocale")
    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng position=marker.getPosition();
        // Display position values
        double latVal = position.latitude;
        double lngVal = position.longitude;
        lat.setText(String.format ("%.9f", latVal));
        lng.setText(String.format ("%.9f", lngVal));
    } // onMarkerDragEnd

    LocationCallback mLocationCallback = new LocationCallback() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                mLastLocation = location;
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
                }
                // Place current location marker
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("Current Position");
                mMap.clear();
                mCurrLocationMarker = mMap.addMarker(markerOptions);
                //move map camera
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                // Display position values
                double latVal = latLng.latitude;
                double lngVal = latLng.longitude;
                lat.setText(String.format ("%.9f", latVal));
                lng.setText(String.format ("%.9f", lngVal));
            }
        }
    }; // mLocationCallback

    @SuppressLint("DefaultLocale")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMapLongClickListener(this);
        //mMap.setMyLocationEnabled(true);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(300000); // five minute interval
        mLocationRequest.setFastestInterval(300000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        // FuseLocationProviderClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Get permission for location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.INTERNET}, 100);
        //if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        //        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            //mMap.setMyLocationEnabled(true);
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mMap.setMyLocationEnabled(true);
            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        // Get current location
        //LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final boolean gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (gpsEnabled) {
            Location location = (Location) lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();

            // Get current location
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                String addressString = addresses.get(0).getAddressLine(0);
                String cityString = addresses.get(0).getLocality();
                Toast.makeText(getApplicationContext(), "Current location: " + cityString, Toast.LENGTH_LONG).show();
                city.setText(cityString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Add a marker in Metro Manila and move the camera, if there is no GPS
            LatLng mmla = new LatLng(14.6091, 121.0223);
            mMap.addMarker(new MarkerOptions().position(mmla).title("Current Position"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mmla, 15));
        }
    } // onMapReady

}
