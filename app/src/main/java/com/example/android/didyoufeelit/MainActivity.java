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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Displays the perceived strength of a single earthquake event based on responses from people who
 * felt the earthquake.
 */
public class MainActivity extends AppCompatActivity {

    private LinearLayout mEarthquakeContainer;
    private TextView mEmptyState;
    private Button mRetry;
    private ProgressBar mProgressIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEarthquakeContainer = findViewById(R.id.earthquake_container);
        mEmptyState = findViewById(R.id.text_empty_state);
        mRetry = findViewById(R.id.btn_retry);
        mProgressIndicator = findViewById(R.id.progress_indicator);

        fetchEarthquakeData();

        mRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchEarthquakeData();
            }
        });
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
        protected void onPreExecute() {
            showProgressIndicator();
            hideEarthquakeData("");
            // hide retry button explicitly
            mRetry.setVisibility(View.GONE);
        }

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
            hideProgressIndicator();

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
        // Handle the empty or null earthquake
        if (earthquake != null) {
            TextView titleTextView = findViewById(R.id.title);
            titleTextView.setText(earthquake.title);

            TextView tsunamiTextView = findViewById(R.id.number_of_people);
            tsunamiTextView.setText(getString(R.string.num_people_felt_it, earthquake.numOfPeople));

            TextView magnitudeTextView = findViewById(R.id.perceived_magnitude);
            magnitudeTextView.setText(earthquake.perceivedStrength);

            showEarthquakeData();
        } else {
            hideEarthquakeData(getString(R.string.error_no_results_found));
        }
    }

    private void fetchEarthquakeData() {
        if (hasConnection()) {
            // Make network request to fetch the earthquake event data on a background thread
            // using a {@link AsyncTask} and then update the UI on main thread
            new FetchEarthquakeAsyncTask().execute(getEarthquakeUrl());
        } else {
            hideEarthquakeData(getString(R.string.error_no_internet_connection));
        }
    }

    private void showProgressIndicator() {
        mProgressIndicator.setVisibility(View.VISIBLE);
    }

    private void hideProgressIndicator() {
        mProgressIndicator.setVisibility(View.GONE);
    }

    private void showEarthquakeData() {
        mEarthquakeContainer.setVisibility(View.VISIBLE);
        mEmptyState.setVisibility(View.GONE);
        mRetry.setVisibility(View.GONE);
    }

    private void hideEarthquakeData(String message) {
        mEarthquakeContainer.setVisibility(View.GONE);

        mEmptyState.setVisibility(View.VISIBLE);
        mEmptyState.setText(message);

        mRetry.setVisibility(View.VISIBLE);
    }

    /**
     * Checks network connectivity.
     */
    private boolean hasConnection() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        } else {
            return false;
        }
    }
}
