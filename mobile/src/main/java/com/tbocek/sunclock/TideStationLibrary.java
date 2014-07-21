package com.tbocek.sunclock;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.tideengine.BackEndTideComputer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tbocek on 7/21/14.
 */
public class TideStationLibrary {
    public static class StationStub {
        private Location mLocation;
        private String mName;
        private float mDistance;

        public StationStub(Location location, String name) {
            mLocation = location;
            mName = name;
        }

        public Location getLocation() {
            return mLocation;
        }

        public void setLocation(Location location) {
            mLocation = location;
        }

        public String getName() {
            return mName;
        }

        public void setName(String name) {
            mName = name;
        }

        public float getDistance() {
            return mDistance;
        }

        public void setDistance(float distance) {
            mDistance = distance;
        }
    }

    private static final String TAG = "TideStationLibrary";

    interface TideStationsLoadedCallback {
        void stationsLoaded(TideStationLibrary stations, boolean loadSuccess);
    }

    private static TideStationLibrary sInstance = null;

    public static TideStationLibrary instance() {
        if (sInstance == null) {
            sInstance = new TideStationLibrary();
        }
        return sInstance;
    }

    private boolean mStationsLoaded = false;
    private List<TideStationsLoadedCallback> mStationsLoadedRunnables =
            new ArrayList<TideStationsLoadedCallback>();
    private List<StationStub> mStations = new ArrayList<StationStub>();

    private TideStationLibrary() { }

    public synchronized void requestTideStations(TideStationsLoadedCallback onLoaded) {
        if (mStationsLoaded) {
            onLoaded.stationsLoaded(this, true);
        } else {
            mStationsLoadedRunnables.add(onLoaded);
            ensureLoading();
        }
    }

    public synchronized void requestComputeDistances(Location location, Runnable onSuccess) {

    }

    public synchronized void requestFilter() {

    }

    private synchronized void ensureLoading() {
        if (mLoadStationsTask.getStatus() != AsyncTask.Status.RUNNING) {
            mLoadStationsTask.execute();
        }
    }

    private class StationFilterParams {
        public String nameFragment;
        public float distance;
    }

    private class StationFilterTask extends AsyncTask<>

    private AsyncTask<Void, Void, Boolean> mLoadStationsTask = new AsyncTask<Void, Void, Boolean>() {
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                BackEndTideComputer.connect();
            } catch (Exception e) {
                Log.e(TAG, "Loading tide stations failed.", e);
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean succeeded) {
            synchronized (this) {
                for (TideStationsLoadedCallback r : mStationsLoadedRunnables) {
                    r.stationsLoaded(TideStationLibrary.this, succeeded);
                }
            }
        }
    };
}
