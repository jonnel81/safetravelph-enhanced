package ph.safetravel.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class FleetSettings extends AppCompatActivity {
    SharedPreferences myPrefs;
    private Toolbar toolbar;
    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new SettingsFleetFragment()).commit();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close
                finish();
            }
        });

        // Drawer
        //dl = findViewById(R.id.drawer_layout);
        //t = new ActionBarDrawerToggle(this, dl, toolbar, R.string.Open, R.string.Close) {
        //    @Override
        //    public void onDrawerOpened(View drawerView) {
        //        super.onDrawerOpened(drawerView);
        //    }
//
        //    @Override
        //    public void onDrawerClosed(View drawerView) {
        //        super.onDrawerClosed(drawerView);
        //    }
        //};
        //t.setDrawerIndicatorEnabled(true);
        //dl.addDrawerListener(t);
        //t.syncState();

    } // OnCreate

    //@Override
    //public boolean onOptionsItemSelected(MenuItem item) {
    //    switch (item.getItemId()) {
    //        case android.R.id.home:
    //            return true;
    //        default:
    //            finish();
    //            return super.onOptionsItemSelected(item);
    //    }
    //}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

    } // onBackPressed

}
