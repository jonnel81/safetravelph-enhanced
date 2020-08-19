package ph.safetravel.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.navigation.NavigationView;

public class Data extends AppCompatActivity {
    SharedPreferences myPrefs;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;

    public Data() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        // Tollbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.data_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Settings
                if(item.getItemId()==R.id.settings)
                {
                    //Intent intent = new Intent(Data.this, TripSettings.class);
                    //startActivity(intent);
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

        // Bottom Navigation
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                drawerLayout.closeDrawers();
                int id = menuItem.getItemId();
                switch(id) {
                    case R.id.profile:
                    {
                        Toast.makeText(Data.this, "Profile", Toast.LENGTH_SHORT).show();
                    }
                    case R.id.settings:
                    {
                        Toast.makeText(Data.this, "Settings", Toast.LENGTH_SHORT).show();
                    }
                    case R.id.about:
                    {
                        Toast.makeText(Data.this, "About", Toast.LENGTH_SHORT).show();
                        //dl.closeDrawer(Gravity.LEFT);
                        //Intent intent = new Intent(Data.this, About.class);
                        //startActivity(intent);
                    }
                }
                return false;
            }
        });

        // Get shared preferences
        myPrefs = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);
        String username = myPrefs.getString("username", null);

        // Display header values
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername =  headerView.findViewById(R.id.nav_header_textView);
        // Decrypt username
        try {
            String decrypted_username = AESUtils.decrypt(username);
            navUsername.setText(decrypted_username);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Bottom navigation
        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_logout: {
                        // Dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(Data.this);
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
                                startActivity(new Intent(Data.this, MainActivity.class));
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                bottomNavigationView.setSelectedItemId(R.id.navigation_data);
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

                        break;
                    }
                    case R.id.navigation_report: {
                        closeApp();
                        startActivity(new Intent(Data.this, Report.class));
                        break;
                    }
                    case R.id.navigation_trip: {
                        closeApp();
                        startActivity(new Intent(Data.this, Trip.class));
                        break;
                    }
                    case R.id.navigation_fleet: {
                        closeApp();
                        startActivity(new Intent(Data.this, Fleet.class));
                        break;
                    }
                }
                return true;
            }
        });

        // Get shared preferences
        myPrefs = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);
        username = myPrefs.getString("username", null);

        //View headerView = navigationView.getHeaderView(0);
        //TextView navUsername =  headerView.findViewById(R.id.nav_header_textView);
        //navUsername.setText(username);

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

    //@Override
    //public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    //    if(actionBarDrawerToggle.onOptionsItemSelected(item))
    //        return true;
//
    //    return super.onOptionsItemSelected(item);
    //}

    @Override
    public void onBackPressed() {

    } // onBackPressed


    public void closeApp(){
        this.finish();
    } // closeApp

}
