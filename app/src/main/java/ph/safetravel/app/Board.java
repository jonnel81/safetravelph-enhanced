package ph.safetravel.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.navigation.NavigationView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class Board extends AppCompatActivity {
    SharedPreferences myPrefs;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.board_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId()==R.id.settings)
                {
                    //Do something
                }
                return false;
            }
        });

        // Navigation Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.Open, R.string.Close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        //  Navigation view
        navigationView = findViewById(R.id.nav_view);
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
                        startActivity(new Intent(Board.this, About.class));
                        break;
                    }
                    case R.id.share:
                    {
                        //Toast.makeText(Data.this, "Settings", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case R.id.logout:
                    {
                        // Dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(Board.this);
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
                                startActivity(new Intent(Board.this, MainActivity.class));
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
                startActivity(new Intent(Board.this, QRCode.class));
            }
        });

        // Bottom navigation
        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(0);
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
                        break;
                    }
                    case R.id.navigation_report: {
                        closeApp();
                        startActivity(new Intent(Board.this, Report.class));
                        break;
                    }
                    case R.id.navigation_trip: {
                        closeApp();
                        startActivity(new Intent(Board.this, Trip.class));
                        break;
                    }
                    case R.id.navigation_fleet: {
                        closeApp();
                        startActivity(new Intent(Board.this, Fleet.class));
                        break;
                    }
                    case R.id.navigation_help: {
                        closeApp();
                        startActivity(new Intent(Board.this, Help.class));
                        break;
                    }
                }
                return true;
            }
        });


        //WebView mWebView1, mWebView2, mWebView3, mWebView4, mWebView5, mWebView6;
        //mWebView1 = (WebView) findViewById(R.id.webview1);
        //mWebView2 = (WebView) findViewById(R.id.webview2);
        //mWebView3 = (WebView) findViewById(R.id.webview3);
        //mWebView4 = (WebView) findViewById(R.id.webview4);
        //mWebView5 = (WebView) findViewById(R.id.webview5);
        //mWebView6 = (WebView) findViewById(R.id.webview6);
//
        //// Enable Javascript
        //WebSettings webSettings1 = mWebView1.getSettings();
        //webSettings1.setJavaScriptEnabled(true);
//
        //WebSettings webSettings2 = mWebView2.getSettings();
        //webSettings2.setJavaScriptEnabled(true);
//
        //WebSettings webSettings3 = mWebView3.getSettings();
        //webSettings3.setJavaScriptEnabled(true);
//
        //WebSettings webSettings4 = mWebView4.getSettings();
        //webSettings4.setJavaScriptEnabled(true);
//
        //WebSettings webSettings5 = mWebView5.getSettings();
        //webSettings5.setJavaScriptEnabled(true);
//
        //WebSettings webSettings6 = mWebView6.getSettings();
        //webSettings6.setJavaScriptEnabled(true);
//
        //mWebView1.loadUrl("http://beta.html5test.com/");
        //mWebView1.loadUrl("https://safetravel.ph/dashboard/piechart.html");
        //mWebView1.loadUrl("https://safetravel.ph/dashboard/barchart.html");
        //mWebView3.loadUrl("https://safetravel.ph/dashboard/gaugechart.html");
        //mWebView1.loadUrl("https://safetravel.ph/");

    } // onCreate

    @Override
    public void onBackPressed() {

    } // onBackPressed


    public void closeApp(){
        this.finish();
    } // closeApp

}
