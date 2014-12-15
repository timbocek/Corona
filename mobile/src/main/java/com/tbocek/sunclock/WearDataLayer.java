package com.tbocek.sunclock;

import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.tideengine.TideStation;

/**
 * Created by tbocek on 11/11/14.
 */
public class WearDataLayer {
    private static final boolean DEBUG_TOASTS = true;

    private GoogleApiClient mApiClient;
    private Context mContext;

    public WearDataLayer(Context context) {
        mApiClient = new GoogleApiClient.Builder(context).addApi(Wearable.API).build();
        mContext = context;
    }

    public void sendLocation(double latitude, double longitude) {
        PutDataMapRequest data = PutDataMapRequest.create(WearDataDefs.LOCATION_DATA);
        data.getDataMap().putDouble(WearDataDefs.LATITUDE, latitude);
        data.getDataMap().putDouble(WearDataDefs.LONGITUDE, longitude);

        Wearable.DataApi.putDataItem(mApiClient, data.asPutDataRequest());

        if (DEBUG_TOASTS) {
            Toast.makeText(
                    mContext,
                    "SENT LOCATION: " + Double.toString(latitude) + ", " +
                            Double.toString(longitude), Toast.LENGTH_LONG).show();
        }
    }

    public void sendFaceConfiguration(String background_data,
                                      String inner_ring_data,
                                      String outer_ring_data) {
        PutDataMapRequest data = PutDataMapRequest.create(WearDataDefs.FACE_CONFIGURATION);
        data.getDataMap().putString(WearDataDefs.FACE_BACKGROUND_DATA, background_data);
        data.getDataMap().putString(WearDataDefs.INNER_RING_DATA, inner_ring_data);
        data.getDataMap().putString(WearDataDefs.OUTER_RING_DATA, outer_ring_data);

        Wearable.DataApi.putDataItem(mApiClient, data.asPutDataRequest());
    }

    public void sendTideStation(TideStation tideStation) {

    }
}
