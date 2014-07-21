package com.tbocek.sunclock;

import android.os.AsyncTask;
import android.util.Log;

import com.tideengine.BackEndTideComputer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tbocek on 7/21/14.
 */
public class TideStationLibrary {
    private static final String TAG = "TideStationLibrary";

    interface TideStationsLoadedRunnable {
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
    private List<TideStationsLoadedRunnable> mStationsLoadedRunnables =
            new ArrayList<TideStationsLoadedRunnable>();

    private TideStationLibrary() { }

    public synchronized void requestTideStations(TideStationsLoadedRunnable onLoaded) {
        if (mStationsLoaded) {
            onLoaded.stationsLoaded(this, true);
        } else {
            mStationsLoadedRunnables.add(onLoaded);
            ensureLoading();
        }
    }

    private synchronized void ensureLoading() {
        if (mLoadStationsTask.getStatus() != AsyncTask.Status.RUNNING) {
            mLoadStationsTask.execute();
        }
    }

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
                for (TideStationsLoadedRunnable r : mStationsLoadedRunnables) {
                    r.stationsLoaded(TideStationLibrary.this, succeeded);
                }
            }
        }
    };
}
