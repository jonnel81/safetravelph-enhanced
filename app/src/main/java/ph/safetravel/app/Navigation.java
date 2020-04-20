package ph.safetravel.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class Navigation extends AppCompatActivity {
    SharedPreferences myPrefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);

        // go to info
        startActivity(new Intent(this, Info.class));

        // BottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_logout: {
                        // Clear shared preferences
                        myPrefs = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = myPrefs.edit();
                        editor.clear();
                        editor.apply();
                        // Go to main activity
                        Intent intent0 = new Intent(Navigation.this, MainActivity.class);
                        startActivity(intent0);
                        break;
                    }
                    case R.id.navigation_info: {
                        Intent intent1 = new Intent(Navigation.this, Report.class);
                        startActivity(intent1);
                        break;
                    }
                    case R.id.navigation_report: {
                        Intent intent2 = new Intent(Navigation.this, Report.class);
                        startActivity(intent2);
                        break;
                    }
                    case R.id.navigation_trip: {
                        Intent intent3 = new Intent(Navigation.this, Trip.class);
                        startActivity(intent3);
                        break;
                    }
                    case R.id.navigation_fleet: {
                        Intent intent4 = new Intent(Navigation.this, Fleet.class);
                        startActivity(intent4);
                        break;
                    }
                }
                return true;
            }
        });
    } // onCreate

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Navigation.this, Navigation.class);
        startActivity(intent);
    }

}
