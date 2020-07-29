package ph.safetravel.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.navigation.NavigationView;

public class Info extends AppCompatActivity {
    EditText UserNameEt, PasswordEt;
    SharedPreferences myPrefs;
    private Toolbar toolbar;
    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        // Tollbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

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
                        //Toast.makeText(MainActivity.this, "My Profile", Toast.LENGTH_SHORT).show();
                    }
                    case R.id.settings:
                    {
                        //Toast.makeText(MainActivity.this, "Settings", Toast.LENGTH_SHORT).show();
                    }
                    case R.id.about:
                    {
                        Intent intent1 = new Intent(Info.this, Info.class);
                        startActivity(intent1);
                        //Toast.makeText(MainActivity.this, "Edit Profile", Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }
        });

        // BottomNavigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull final MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_logout: {
                        // Dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(Info.this);
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
                                Intent intent0 = new Intent(Info.this, MainActivity.class);
                                startActivity(intent0);
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent1 = new Intent(Info.this, Info.class);
                                startActivity(intent1);
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

                        break;
                    }
                    case R.id.navigation_report: {
                        Intent intent2 = new Intent(Info.this, Report.class);
                        startActivity(intent2);
                        break;
                    }
                    case R.id.navigation_trip: {
                        Intent intent3 = new Intent(Info.this, Trip.class);
                        startActivity(intent3);
                        break;
                    }
                    case R.id.navigation_fleet: {
                        Intent intent4 = new Intent(Info.this, Fleet.class);
                        startActivity(intent4);
                        break;
                    }
                }
                return true;
            }
        });
    } // OnCreate

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(t.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Do nothing
    }
}
