package ph.safetravel.app;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

public class SettingsFleetFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String s) {
        addPreferencesFromResource(R.xml.fleet_preferences);
        // implement your settings here
    }
}