package com.tbocek.sunclock;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.common.base.Joiner;
import com.tideengine.TideStation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;

    private static final String[] VALID_FRAGMENTS = new String[] {
        LocationPreferenceFragment.class.getName(),
        DisplayPreferenceFragment.class.getName()
    };

    @Override
    protected boolean isValidFragment(String fragmentName) {
        for (String fragName : VALID_FRAGMENTS) {
            if (fragName.equals(fragmentName)) return true;
        }
        return false;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_display);
        addPreferencesFromResource(R.xml.pref_locations);
    }

    /** {@inheritDoc} */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
        & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB;
    }

    /** {@inheritDoc} */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class LocationPreferenceFragment
            extends PreferenceFragment
            implements GoogleApiClient.ConnectionCallbacks,
                       GoogleApiClient.OnConnectionFailedListener {

        private static final String TAG = "GeneralPreferenceFragment";
        private WearDataLayer mDataLayer;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mDataLayer = new WearDataLayer(getActivity());
            addPreferencesFromResource(R.xml.pref_locations);

            findPreference("lookup_location").setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            lookupLocation();
                            return true;
                        }
                    });

            findPreference("tide_station").setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            selectTideStation();
                            return true;
                        }
            });

            findPreference("auto_location").setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            boolean auto = (Boolean) newValue;
                            enableManualLocationControls(!auto);

                            return true;
                        }
                    });

            findPreference("custom_latitude").setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            Float.parseFloat((String)newValue);
                            sendLocation();
                            return true;
                        }
                    }
            );

            findPreference("custom_longitude").setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            Float.parseFloat((String)newValue);
                            sendLocation();
                            return true;
                        }
                    }
            );

            enableManualLocationControls(
                    !getPreferenceManager().getSharedPreferences().getBoolean(
                            "auto_location", true));

            mApiClient = new GoogleApiClient.Builder(this.getActivity().getApplicationContext())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mConnectionInProgress = false;
            Intent intent = new Intent(getActivity().getApplicationContext(),
                    LocationSubscriber.class);
            mLocationPendingIntent = PendingIntent.getService(
                    getActivity().getApplicationContext(), 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

        }

        private void sendLocation() {
            SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
            mDataLayer.sendLocation(
                    Float.parseFloat(prefs.getString("custom_latitude", "0")),
                    Float.parseFloat(prefs.getString("custom_longitude", "0"))
            );
        }

        private void enableManualLocationControls(boolean enabled) {
            findPreference("custom_latitude").setEnabled(enabled);
            findPreference("custom_longitude").setEnabled(enabled);
            findPreference("lookup_location").setEnabled(enabled);
        }

        private void selectTideStation() {
            TideStationSelectorView view = new TideStationSelectorView(getActivity());

            Location loc = new Location("");
            SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
            loc.setLatitude(Float.parseFloat(prefs.getString("custom_latitude", "0")));
            loc.setLongitude(Float.parseFloat(prefs.getString("custom_longitude", "0")));
            view.setLocation(loc);

            final Dialog dlg = new AlertDialog.Builder(this.getActivity())
                    .setView(view).create();

            view.setOnTideStationSelected(
                    new TideStationSelectorView.OnTideStationSelectedListener() {
                @Override
                public void stationSelected(TideStationLibrary.StationStub station) {
                    dlg.dismiss();
                    TideStationLibrary.instance().loadSingleTideStation(
                            getActivity().getApplicationContext(), station.getName(),
                            new TideStationLibrary.SingleTideStationLoadedCallback() {
                                @Override
                                public void loaded(TideStation tideStation) {
                                    if (tideStation != null) {
                                        new WearDataLayer(getActivity().getApplicationContext()).sendTideStation(tideStation);
                                        Toast.makeText(getActivity().getApplicationContext(), "Tide Station Sent", Toast.LENGTH_LONG);
                                    }
                                }
                            });
                }
            });

            dlg.show();
        }

        private void lookupLocation() {
            if (!Geocoder.isPresent()) {
                Toast.makeText(getActivity(), getString(R.string.lookup_location_no_geocoder),
                               Toast.LENGTH_LONG).show();
                return;
            }

            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View view = inflater.inflate(R.layout.lookup_location, null);
            final EditTextPreference latPref =
                    (EditTextPreference) findPreference("custom_latitude");
            final EditTextPreference longPref =
                    (EditTextPreference) findPreference("custom_longitude");
            new AlertDialog.Builder(getActivity())
                    .setView(view)
                    .setTitle(R.string.lookup_location_title)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EditText entry =
                                    (EditText) view.findViewById(R.id.lookup_location_entry);
                            String locationName = entry.getText().toString();
                            Log.i(TAG, "Looking up location for " + locationName);
                            try {
                                List<Address> locations =
                                        new Geocoder(getActivity())
                                                .getFromLocationName(locationName, 1);
                                if (locations != null && !locations.isEmpty()) {
                                    Address location = locations.get(0);
                                    double lat = location.getLatitude();
                                    double longi = location.getLongitude();

                                    latPref.setText(Double.toString(lat));
                                    longPref.setText(Double.toString(longi));

                                    List<String> locationDesc = new ArrayList<String>();
                                    locationDesc.add(location.getSubLocality());
                                    locationDesc.add(location.getLocality());
                                    locationDesc.add(location.getAdminArea());
                                    locationDesc.add(location.getCountryCode());
                                    locationDesc.add("(" + Double.toString(lat));
                                    locationDesc.add(Double.toString(longi) + ")");

                                    Toast.makeText(
                                            getActivity(),
                                            "Location set to " + Joiner.on(", ").skipNulls()
                                                    .join(locationDesc),
                                            Toast.LENGTH_LONG).show();
                                    mDataLayer.sendLocation(lat, longi);

                                } else {
                                    Toast.makeText(
                                            getActivity(),
                                            getString(R.string.lookup_location_not_found),
                                            Toast.LENGTH_LONG).show();
                                }
                            } catch (IOException e) {
                                Toast.makeText(
                                        getActivity(),
                                        getString(R.string.lookup_location_no_network),
                                        Toast.LENGTH_LONG).show();
                            }
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }
                    )
                    .show();
        }


        /// LOCATION UPDATE STUFF

        /*
         * Store the PendingIntent used to send activity recognition events
         * back to the app
         */
        private PendingIntent mActivityRecognitionPendingIntent;
        // Store the current activity recognition client
        private GoogleApiClient mApiClient;
        private boolean mConnectionInProgress;
        PendingIntent mLocationPendingIntent;
        public enum REQUEST_TYPE {START, STOP}
        private REQUEST_TYPE mRequestType;

        @Override
        public void onConnected(Bundle bundle) {

            switch(mRequestType) {
                case START:
                    LocationServices.FusedLocationApi.requestLocationUpdates(
                            mApiClient,
                            LocationRequest.create().setPriority(LocationRequest.PRIORITY_NO_POWER)
                                    .setFastestInterval(10*60*100),
                            mLocationPendingIntent
                    );
                    break;
                case STOP:
                    LocationServices.FusedLocationApi.removeLocationUpdates(
                            mApiClient,
                            mLocationPendingIntent
                    );
                    break;
            }

            mConnectionInProgress = false;
            mApiClient.disconnect();
        }

        @Override
        public void onConnectionSuspended(int i) {

        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {

        }

        private boolean servicesConnected() {
            int resultCode =
                    GooglePlayServicesUtil.
                            isGooglePlayServicesAvailable(getActivity());
            return ConnectionResult.SUCCESS == resultCode;
            // TODO: handle errors as described by the android docs.
        }

        private void startLocationUpdates() {
            mRequestType = REQUEST_TYPE.START;
            if (!mConnectionInProgress) {
                mConnectionInProgress = true;
                mApiClient.connect();
            }
        }

        private void stopLocationUpdates() {
            mRequestType = REQUEST_TYPE.STOP;
            if (!mConnectionInProgress) {
                mConnectionInProgress = true;
                mApiClient.connect();
            }
        }
    } // END LOCATION PREF FRAGMENT

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DisplayPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_display);

            findPreference("watch_background_data").setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            sendDisplayPreferences();
                            return true;
                        }
                    }
            );

            findPreference("outer_ring_data").setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            sendDisplayPreferences();
                            return true;
                        }
                    }
            );

            findPreference("inner_ring_data").setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            sendDisplayPreferences();
                            return true;
                        }
                    }
            );
        }

        private void sendDisplayPreferences() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            new WearDataLayer(getActivity()).sendFaceConfiguration(
                    prefs.getString("watch_background_data", "Sun"),
                    prefs.getString("inner_ring_data", "Tides"),
                    prefs.getString("outer_ring_data", "Moon"));
        }
    }
}
