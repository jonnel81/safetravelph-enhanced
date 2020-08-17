package ph.safetravel.app;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class TripHistory extends AppCompatActivity {
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private TripHistoryDBHelper db;
    private List<TripRecord> listTripRecords = new ArrayList<TripRecord>();
    private RecyclerView recyclerView;
    private TripRecordAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triphistory);

        db = new TripHistoryDBHelper(this);

        recyclerView = (RecyclerView) findViewById(R.id.rv_tripHistory);

        listTripRecords.addAll(db.allTripRecords());

        adapter = new TripRecordAdapter(this, listTripRecords);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);

        // list all trip records
        //List<TripRecord> tripRecords =  db.allTripRecords();

        //if(tripRecords !=null){
            //String [] itemNames = new String[tripRecords.size()];

            //for (int i = 0; i < tripRecords.size(); i++) {
            //    itemNames[i] = tripRecords.get(i).toString();
            //}

            //tripRecordList.addAll(db.allTripRecords());
            // display like string instances
            //list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, itemNames));
        //}

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
                        Toast.makeText(TripHistory.this, "Profile", Toast.LENGTH_SHORT).show();
                    }
                    case R.id.settings:
                    {
                        Toast.makeText(TripHistory.this, "Settings", Toast.LENGTH_SHORT).show();
                    }
                    case R.id.about:
                    {
                        //Intent intent1 = new Intent(TripHistory.this, About.class);
                        //startActivity(intent1);
                        Toast.makeText(TripHistory.this, "About", Toast.LENGTH_SHORT).show();
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
