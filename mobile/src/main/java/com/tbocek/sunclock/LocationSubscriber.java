package com.tbocek.sunclock;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

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
            new WearDataLayer(this).sendLocation(location.getLatitude(), location.getLongitude());
        }
    }
}
