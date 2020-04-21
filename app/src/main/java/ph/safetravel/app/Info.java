package ph.safetravel.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;

public class Info extends AppCompatActivity {
    SharedPreferences myPrefs;
    Context context;

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
                //int id = menuItem.getItemId();
                //if (id == R.id.navigation_logout) {
                //    AlertDialog.Builder builder = new AlertDialog.Builder(Info.this);
                //    builder.setMessage("Are you sure you want to logout?");
                //    builder.setCancelable(false);
                //    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                //        @Override
                //        public void onClick(DialogInterface dialog, int which) {
                //            dialog.dismiss();
                //            //startActivity(new Intent(view.getContext(), Report.class));
                //            //startActivity(new Intent(view.getContext(), Navigation.class));
                //        }
                //    });
                //    AlertDialog alertDialog = builder.create();
                //    alertDialog.setTitle("Status");
                //    alertDialog.setCancelable(false);
                //    alertDialog.setCanceledOnTouchOutside(false);
                //    alertDialog.show();
                //    }
                //return true;
                //}
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
//
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
//
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
