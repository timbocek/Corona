package com.tbocek.sunclock;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.WearableListenerService;
import com.tideengine.TideStation;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

/**
 * Created by tbocek on 11/14/14.
 */
public class DataLayerListenerService extends WearableListenerService {
    private static final String TAG = "DataLayerListenerService";

    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            DataItem item = event.getDataItem();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                    getApplicationContext());
            if (item.getUri().equals(WearDataDefs.LOCATION_DATA)) {
                DataMap data = DataMap.fromByteArray(item.getData());
                double latitude = data.getDouble(WearDataDefs.LATITUDE);
                double longitude = data.getDouble(WearDataDefs.LONGITUDE);

                prefs.edit()
                        .putFloat("lat", (float)latitude)
                        .putFloat("long", (float)longitude)
                        .commit();
            } else if (item.getUri().equals(WearDataDefs.FACE_CONFIGURATION)) {

            } else if (item.getUri().equals(WearDataDefs.TIDE_STATION)) {
                try {
                    byte b[] = item.getData();
                    EventData.instance(this).setTideStation(TideStation.deserialize(b));
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        }
    }
}
