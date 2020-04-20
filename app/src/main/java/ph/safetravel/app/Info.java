package ph.safetravel.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;

public class Info extends AppCompatActivity {
    SharedPreferences myPrefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        // BottomNavigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);

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
                        Intent intent0 = new Intent(Info.this, MainActivity.class);
                        startActivity(intent0);
                        break;
                    }
                    case R.id.navigation_info: {

                        break;
                    }
                    case R.id.navigation_report: {
                        Intent intent1 = new Intent(Info.this, Report.class);
                        startActivity(intent1);
                        break;
                    }
                    case R.id.navigation_trip: {
                        Intent intent2 = new Intent(Info.this, Trip.class);
                        startActivity(intent2);
                        break;
                    }
                    case R.id.navigation_fleet: {

                        break;
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Do nothing
    }
}
