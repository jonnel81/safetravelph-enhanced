package ph.safetravel.app;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class FleetHistory extends AppCompatActivity {
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private FleetHistoryDBHelper db;
    private List<FleetRecord> listFleetRecords = new ArrayList<FleetRecord>();
    private RecyclerView recyclerView;
    private FleetRecordAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fleethistory);

        db = new FleetHistoryDBHelper(this);
        recyclerView = (RecyclerView) findViewById(R.id.rv_tripHistory);
        listFleetRecords.addAll(db.allFleetRecords());

        adapter = new FleetRecordAdapter(this, listFleetRecords);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        //recyclerView.addItemDecoration(decoration);
        recyclerView.setAdapter(adapter);

        // Tollbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

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
                        Toast.makeText(FleetHistory.this, "Profile", Toast.LENGTH_SHORT).show();
                    }
                    case R.id.settings:
                    {
                        Toast.makeText(FleetHistory.this, "Settings", Toast.LENGTH_SHORT).show();
                    }
                    case R.id.about:
                    {
                        //Intent intent1 = new Intent(TripHistory.this, About.class);
                        //startActivity(intent1);
                        Toast.makeText(FleetHistory.this, "About", Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }
        });

    } // OnCreate

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

}
