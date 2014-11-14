package com.tbocek.sunclock;

import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

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
}
