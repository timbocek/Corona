package com.tbocek.sunclock;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

import com.tideengine.Harmonic;
import com.tideengine.TideStation;
import com.tideengine.TideUtilities;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by tbocek on 7/21/14.
 */
public class TideStationLibrary {

    public static class StationStub {
        private Location mLocation;
        private String mName;
        private float mDistance = Float.NaN;

        public StationStub() { }

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

    interface TideStationsFilterCallback {
        void stationsLoaded(List<StationStub> foundStations);
    }

    interface SingleTideStationLoadedCallback {
        void loaded(TideStation tideStation);
    }

    private static TideStationLibrary sInstance = null;

    public static TideStationLibrary instance() {
        if (sInstance == null) {
            sInstance = new TideStationLibrary();
        }
        return sInstance;
    }

    private boolean mStationsLoaded = false;
    private boolean mStationsLoading = false;

    private List<TideStationsLoadedCallback> mStationsLoadedCallbacks =
            new ArrayList<TideStationsLoadedCallback>();

    private List<TideStationsLoadedCallback> mLocationsComputedCallbacks =
            new ArrayList<TideStationsLoadedCallback>();

    private Location lastLocationUsed = null;

    private List<StationStub> mStations = new ArrayList<StationStub>();

    private TideStationLibrary() { }

    public synchronized void requestTideStations(
            Context context, TideStationsLoadedCallback onLoaded) {
        if (mStationsLoaded) {
            onLoaded.stationsLoaded(this, true);
        } else {
            mStationsLoadedCallbacks.add(onLoaded);
            ensureLoading(context);
        }
    }

    public synchronized void requestComputeDistances(
            Location location, TideStationsLoadedCallback onSuccess) {
        if (lastLocationUsed != null && lastLocationUsed.distanceTo(location) < 30) {
            onSuccess.stationsLoaded(this, true);
            return;
        }

        mLocationsComputedCallbacks.add(onSuccess);
        ensureComputingDistances(location);
    }

    public synchronized void requestFilter(
            float distance, String nameFragment, TideStationsFilterCallback cb) {
        if (mStationFilterTask != null) {
            mStationFilterTask.cancel(true);
        }
        mStationFilterTask = new StationFilterTask();
        StationFilterParams params = new StationFilterParams();
        params.distance = distance;
        params.nameFragment = nameFragment;
        params.filterCallback = cb;
        mStationFilterTask.execute(params);
    }

    public void loadSingleTideStation(Context context, String name, SingleTideStationLoadedCallback callback) {
        SingleTideStationLoadParams params = new SingleTideStationLoadParams();
        params.context = context;
        params.name = name;
        params.callback = callback;

        new SingleTideStationLoadTask().execute(params);
    }

    public List<StationStub> getAllStations() {
        return mStations;
    }

    private synchronized void ensureLoading(Context context) {
        if (mStations == null || mStations.isEmpty() && !mStationsLoading) {
            new LoadStationsTask().execute(context.getApplicationContext());
            mStationsLoading = true;
        }
    }


    private void ensureComputingDistances(Location location) {
        new ComputeDistancesTask().execute(location);
    }

    private class StationFilterParams {
        public String nameFragment;
        public float distance;
        public TideStationsFilterCallback filterCallback;
    }

    private class StationFilterResult {
        public List<StationStub> foundStations = new ArrayList<StationStub>();
        public TideStationsFilterCallback filterCallback;
    }

    private class StationFilterTask
            extends AsyncTask<StationFilterParams, Void, StationFilterResult> {

        @Override
        protected StationFilterResult doInBackground(StationFilterParams... params) {
            StationFilterParams filterParams = params[0];
            StationFilterResult result = new StationFilterResult();
            result.filterCallback = filterParams.filterCallback;

            String nameFragmentLower = filterParams.nameFragment.toLowerCase();
            Log.i(TAG, String.format("FILTERING %d STATIONS", mStations.size()));
            for (StationStub station: mStations) {
                Log.i(TAG, String.format("Station %s %.2f km",
                        station.getName(), station.getDistance() / 1000));
                if (station.getDistance() > filterParams.distance) {
                    break;
                }

                if (station.getName().toLowerCase().contains(nameFragmentLower)) {
                    result.foundStations.add(station);
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(StationFilterResult result) {
            result.filterCallback.stationsLoaded(result.foundStations);
        }
    }
    private StationFilterTask mStationFilterTask;


    private class LoadStationsTask extends AsyncTask<Context, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Context... params) {
            try {
                Log.i(TAG, "START TIDE STATION PARSE");
                InputStream in = ((Context)params[0]).getResources().openRawResource(R.raw.stations);
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(in, null);
                mStations.addAll(getTideStationStubs(parser));
                Log.i(TAG, "END TIDE STATION PARSE");
            } catch (Exception e) {
                Log.e(TAG, "Loading tide stations failed.", e);
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean succeeded) {
            synchronized (this) {
                for (TideStationsLoadedCallback r : mStationsLoadedCallbacks) {
                    r.stationsLoaded(TideStationLibrary.this, succeeded);
                }
                mStationsLoadedCallbacks.clear();
                mStationsLoaded = true;
                mStationsLoading = false;
            }
        }
    };

    private class ComputeDistancesTask extends AsyncTask<Location, Void, Void> {

        @Override
        protected Void doInBackground(Location... params) {
            Location location = params[0];

            for (StationStub station: mStations) {
                if (station.getLocation() != null)
                    station.setDistance(station.getLocation().distanceTo(location));
            }

            Collections.sort(mStations, new Comparator<StationStub>() {

                @Override
                public int compare(StationStub lhs, StationStub rhs) {
                    return Float.compare(lhs.getDistance(), rhs.getDistance());
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            synchronized (this) {
                for (TideStationsLoadedCallback r : mLocationsComputedCallbacks) {
                    r.stationsLoaded(TideStationLibrary.this, true);
                }
                mLocationsComputedCallbacks.clear();
            }
        }
    };

    private static class SingleTideStationLoadParams {
        public SingleTideStationLoadedCallback callback;
        public String name;
        public Context context;
    }

    private static class SingleTideStationLoadResult {
        public SingleTideStationLoadedCallback callback;
        TideStation result;
    }

    private static class SingleTideStationLoadTask extends AsyncTask<SingleTideStationLoadParams, Void, SingleTideStationLoadResult> {

        @Override
        protected SingleTideStationLoadResult doInBackground(SingleTideStationLoadParams... params) {
            SingleTideStationLoadResult result = new SingleTideStationLoadResult();
            result.callback = params[0].callback;

            try {
                Log.i(TAG, "START TIDE STATION PARSE");
                InputStream in = params[0].context.getResources().openRawResource(R.raw.stations);
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(in, null);
                result.result = getSingleTideStation(parser, params[0].name);
                Log.i(TAG, "END TIDE STATION PARSE");
            } catch (Exception e) {
                Log.e(TAG, "Loading tide stations failed.", e);
            }

            return result;
        }

        @Override
        protected void onPostExecute(SingleTideStationLoadResult result) {
            if (result.result != null) {
                result.callback.loaded(result.result);
            }
        }
    }

    private static List<StationStub> getTideStationStubs(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<StationStub> stations = new ArrayList<StationStub>();

        StationStub currentStation = null;

        // Consume events up to the Stations tag
        while (parser.next() != XmlPullParser.START_TAG && parser.getName() != "stations") { }

        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.getEventType() == XmlPullParser.START_TAG) {
                String name = parser.getName();
                if (name.equals("station")) {
                    currentStation = new StationStub();
                    stations.add(currentStation);
                    currentStation.setName(parser.getAttributeValue(null, "name"));
                } else if (name.startsWith("p")) {  // Only tag that matches is "position"
                    Location l = new Location("");
                    l.setLatitude(Double.parseDouble(parser.getAttributeValue(null, "latitude")));
                    l.setLongitude(Double.parseDouble(parser.getAttributeValue(null, "longitude")));
                    if (currentStation != null)
                        currentStation.setLocation(l);
                    skip(parser, 1);  // We have the data we need, skip rest of the station tag
                } else {
                    skip(parser);
                }
            }
        }
        return stations;
    }

    private static TideStation getSingleTideStation(XmlPullParser parser, String name) throws XmlPullParserException, IOException {

        // Consume events up to the Stations tag
        while (parser.next() != XmlPullParser.START_TAG && parser.getName() != "stations") { }

        TideStation station = null;
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.getEventType() == XmlPullParser.START_TAG) {
                if (parser.getName().equals("station")) {
                    if (parser.getAttributeValue(null, "name") == name) {
                        station = new TideStation();
                        station.setFullName(parser.getAttributeValue(null, "name"));
                        // TODO!!!
                    } else {
                        skip(parser);
                    }
                } else if (station != null) {
                    if (name.equals("position")) {
                        station.setLatitude(Double.parseDouble(parser.getAttributeValue(null, "latitude")));
                        station.setLongitude(Double.parseDouble(parser.getAttributeValue(null, "longitude")));
                    } else if (name.equals("base-height")) {
                        station.setBaseHeight(Double.parseDouble(parser.getAttributeValue(null, "value")));
                        station.setUnit(parser.getAttributeValue(null, "unit"));
                    } else if (name.equals("time-zone")) {
                        station.setTimeOffset(parser.getAttributeValue(null, "offset"));
                        station.setTimeZone(parser.getAttributeValue(null, "name"));
                    } else if (name.equals("harmonic-coeff")) {
                        String coeffName = parser.getAttributeValue(null, "name");
                        double amplitude = Double.parseDouble(parser.getAttributeValue(null, "amplitude"));
                        double epoch = Double.parseDouble(parser.getAttributeValue(null, "epoch")) * TideUtilities.COEFF_FOR_EPOCH;
                        station.getHarmonics().add(new Harmonic(coeffName, amplitude, epoch));
                    }
                }
            } else if (parser.getEventType() == XmlPullParser.END_TAG &&
                    parser.getName().equals("station") &&
                    station != null) {
                return station;
            }
        }

        return null;
    }

    private static void skip(XmlPullParser parser, int upLevels) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1 + upLevels;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        skip(parser, 0);
    }
}
