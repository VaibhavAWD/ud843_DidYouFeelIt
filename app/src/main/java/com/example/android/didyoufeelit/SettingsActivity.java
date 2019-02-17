package com.example.android.didyoufeelit;

import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public static class EarthquakePreferenceFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_earthquake);

            Preference minFeltPref = findPreference(getString(R.string.pref_key_min_felt));
            bindPreferenceSummaryToValue(minFeltPref);

            Preference minMagnitudePref = findPreference(getString(R.string.pref_key_min_mag));
            bindPreferenceSummaryToValue(minMagnitudePref);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = (String) value;

            if (preference instanceof EditTextPreference) {
                if (TextUtils.isEmpty(stringValue)) {
                    if (preference.getKey().equals(getString(R.string.pref_default_min_mag))) {
                        preference.setSummary(getString(R.string.pref_default_min_mag));
                    } else if (preference.getKey().equals(getString(R.string.pref_default_min_felt))) {
                        preference.setSummary(getString(R.string.pref_default_min_felt));
                    }
                } else {
                    preference.setSummary(stringValue);
                }
            }

            return true;
        }

        private void bindPreferenceSummaryToValue(Preference preference) {
            preference.setOnPreferenceChangeListener(this);

            SharedPreferences pref =
                    PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            String stringValue = pref.getString(preference.getKey(), "");

            onPreferenceChange(preference, stringValue);
        }
    }
}
