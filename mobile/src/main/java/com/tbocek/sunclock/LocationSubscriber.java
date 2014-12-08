package com.tbocek.sunclock;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import com.google.android.gms.location.LocationClient;

/**
 * Created by tbocek on 11/12/14.
 */
public class LocationSubscriber extends IntentService {

    public LocationSubscriber() {
        super("LocationSubscriber");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Location location = intent.getParcelableExtra(LocationClient.KEY_LOCATION_CHANGED);
        if (location != null) {
            SharedPreferences.Editor prefEditor =
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
            prefEditor.putString("custom_latitude", Double.toString(location.getLatitude()));
            prefEditor.putString("custom_longitude", Double.toString(location.getLongitude()));
            new WearDataLayer(this).sendLocation(location.getLatitude(), location.getLongitude());
        }
    }
}
