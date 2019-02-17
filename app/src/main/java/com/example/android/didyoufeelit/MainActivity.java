/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.didyoufeelit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Displays the perceived strength of a single earthquake event based on responses from people who
 * felt the earthquake.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Make network request to fetch the earthquake event data on a background thread
        // using a {@link AsyncTask} and then update the UI on main thread
        new FetchEarthquakeAsyncTask().execute(getEarthquakeUrl());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * {@link AsyncTask} to perform network request on a background thread, and then
     * update the UI with the first earthquake in response.
     */
    private class FetchEarthquakeAsyncTask extends AsyncTask<String, Void, Event> {

        @Override
        protected Event doInBackground(String... urls) {
            if (urls.length == 0) {
                return null;
            }

            // Perform the HTTP request for earthquake data and process the response.
            return Utils.fetchEarthquakeData(urls[0]);
        }

        @Override
        protected void onPostExecute(Event event) {
            // If event data is null then exit early
            if (event == null) {
                return;
            }

            // Update the information displayed to the user.
            updateUi(event);
        }
    }

    private String getEarthquakeUrl() {
        final String USGS_BASE_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query?";
        final String USGS_FORMAT = "format";
        final String USGS_START_TIME = "starttime";
        final String USGS_END_TIME = "endtime";
        final String USGS_MIN_FELT = "minfelt";
        final String USGS_MIN_MAG = "minmagnitude";

        String format = "geojson";
        String startDate = "2019-01-01";
        String endDate = "2019-05-02";

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String minFelt = pref.getString(
                getString(R.string.pref_key_min_felt), getString(R.string.pref_default_min_felt));
        String minMagnitude = pref.getString(
                getString(R.string.pref_key_min_mag), getString(R.string.pref_default_min_mag));

        Uri.Builder uriBuilder = Uri.parse(USGS_BASE_URL).buildUpon()
                .appendQueryParameter(USGS_FORMAT, format)
                .appendQueryParameter(USGS_START_TIME, startDate)
                .appendQueryParameter(USGS_END_TIME, endDate)
                .appendQueryParameter(USGS_MIN_FELT, minFelt)
                .appendQueryParameter(USGS_MIN_MAG, minMagnitude);

        return uriBuilder.toString();
    }

    /**
     * Update the UI with the given earthquake information.
     */
    private void updateUi(Event earthquake) {
        TextView titleTextView = findViewById(R.id.title);
        titleTextView.setText(earthquake.title);

        TextView tsunamiTextView = findViewById(R.id.number_of_people);
        tsunamiTextView.setText(getString(R.string.num_people_felt_it, earthquake.numOfPeople));

        TextView magnitudeTextView = findViewById(R.id.perceived_magnitude);
        magnitudeTextView.setText(earthquake.perceivedStrength);
    }
}
