package com.tbocek.sunclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.util.Pair;

import com.mhuss.AstroLib.AstroDate;
import com.mhuss.AstroLib.Latitude;
import com.mhuss.AstroLib.Longitude;
import com.mhuss.AstroLib.ObsInfo;
import com.mhuss.AstroLib.RiseSet;
import com.mhuss.AstroLib.TimePair;
import com.tideengine.BackEndTideComputer;
import com.tideengine.TideStation;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tbocek on 12/16/14.
 */
public class EventData {
    private static final String TAG = "EventData";

    public static EventData instance(Context context) {
        if (sInstance == null)
            sInstance = new EventData(context);
        return sInstance;
    }

    private static EventData sInstance;

    // Default to Seattle, WA
    private static final float DEFAULT_LAT_FOR_TESTING = 47.61f;
    private static final float DEFAULT_LONG_FOR_TESTING = -122.33f;

    private EventData(Context context) {
        mContext = context.getApplicationContext();
        // TODO: Make the following asynchronous
        InputStream is = context.getResources().openRawResource(R.raw.constituents);
        try {
            BackEndTideComputer.connect(is);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    private Context mContext;

    private DateTime mSunriseTime = new DateTime(2014, 1, 1, 5, 00);
    private DateTime mSunsetTime = new DateTime(2014, 1, 1, 19, 00);

    private DateTime mDawnTime = new DateTime(2014, 1, 1, 4, 30);
    private DateTime mDuskTime = new DateTime(2014, 1, 1, 19, 30);
    private DateTime mAstroDawnTime =new DateTime(2014, 1, 1, 3, 00);
    private DateTime mAstroDuskTime = new DateTime(2014, 1, 1, 21, 00);
    private DateTime mMoonriseTime = new DateTime(2014, 1, 1, 12, 00);
    private DateTime mMoonsetTime = new DateTime(2014, 1, 1, 23, 00);

    private TideComputer mTideComputer;
    private List<DateTime> mLowTides;
    private List<DateTime> mHighTides;

    private Latitude mLatitude;
    private Longitude mLongitude;

    private long mLastUpdate = 0;

    private DateTimeZone mTimeZone;

    public long getLastUpdate() {
        return mLastUpdate;
    }

    public void setLocation(double latitude, double longitude) {
        mLatitude = new Latitude(latitude);
        mLongitude = new Longitude(longitude);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        prefs.edit()
                .putFloat("latitude", (float) latitude)
                .putFloat("longitude", (float) longitude)
                .commit();

        resetSunriseAndSunsetTimes();
        mLastUpdate = new Time().toMillis(false);
    }

    public void setTideStation(TideStation tideStation) {
        mLastUpdate = new Time().toMillis(false);
    }

    public void setTimeZone(DateTimeZone timeZone) {
        Log.i(TAG, "Time Zone = " + timeZone.getID());
        mTimeZone = timeZone;
        resetSunriseAndSunsetTimes();
        mLastUpdate = new Time().toMillis(false);
    }

    public void loadFromPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mLatitude = new Latitude(prefs.getFloat("latitude", DEFAULT_LAT_FOR_TESTING));
        mLongitude = new Longitude(prefs.getFloat("longitude", DEFAULT_LONG_FOR_TESTING));

        resetSunriseAndSunsetTimes();
        mLastUpdate = new Time().toMillis(false);
    }

    public List<DateTime> getHighTides() {
        return mHighTides;
    }

    public List<DateTime> getLowTides() {
        return mLowTides;
    }

    public DateTime getMoonsetTime() {
        return mMoonsetTime;
    }

    public DateTime getMoonriseTime() {
        return mMoonriseTime;
    }

    public DateTime getAstroDuskTime() {
        return mAstroDuskTime;
    }

    public DateTime getAstroDawnTime() {
        return mAstroDawnTime;
    }

    public DateTime getDuskTime() {
        return mDuskTime;
    }

    public DateTime getDawnTime() {
        return mDawnTime;
    }

    public DateTime getSunsetTime() {
        return mSunsetTime;
    }

    public DateTime getSunriseTime() {
        return mSunriseTime;
    }


    public List<Pair<DateTime, DateTime>> findRisingTides() {
        List<Pair<DateTime, DateTime>> risingTides = new ArrayList<Pair<DateTime, DateTime>>();

        if (mLowTides == null || mHighTides == null ||
                mLowTides.isEmpty() || mHighTides.isEmpty()) return risingTides;

        int currentLow = 0;
        int currentHigh = 0;
        if (mLowTides.get(0).isAfter(mHighTides.get(0).toInstant())) {
            currentHigh += 1;
        }

        while (currentLow < mLowTides.size() && currentHigh < mHighTides.size() &&
                mLowTides.get(currentLow).minusDays(1).isBefore(mLowTides.get(0)) &&
                mHighTides.get(currentHigh).minusDays(1).isBefore(mLowTides.get(0))) {
            risingTides.add(new Pair<DateTime, DateTime>(
                    mLowTides.get(currentLow), mHighTides.get(currentHigh)));

            currentLow++;
            currentHigh++;
        }
        return risingTides;
    }

    private void resetSunriseAndSunsetTimes() {
        if (mTimeZone == null || mLatitude == null || mLongitude == null) return;

        DateTime currentTime = DateTime.now(mTimeZone);
        ObsInfo observerInfo = new ObsInfo(mLatitude, mLongitude,
                currentTime.getZone().getOffset(currentTime.toInstant()) / 3600);

        Pair<DateTime, DateTime> sunTimes = computeTimes(observerInfo, currentTime, RiseSet.SUN);
        mSunriseTime = sunTimes.first;
        mSunsetTime = sunTimes.second;
        Log.i(TAG, String.format("Sunrise=%s ; Sunset=%s",
                mSunriseTime.toString(), mSunsetTime.toString()));

        Pair<DateTime, DateTime> duskTimes = computeTimes(observerInfo, currentTime,
                RiseSet.CIVIL_TWI);
        mDawnTime = duskTimes.first;
        mDuskTime = duskTimes.second;
        Log.i(TAG, String.format("Dawn=%s ; Dusk=%s",
                mDawnTime.toString(), mDuskTime.toString()));

        Pair<DateTime, DateTime> astroDuskTimes =
                computeTimes(observerInfo, currentTime, RiseSet.ASTRONOMICAL_TWI);
        mAstroDawnTime = astroDuskTimes.first;
        mAstroDuskTime = astroDuskTimes.second;

        Pair<DateTime, DateTime> moonTimes = computeTimes(observerInfo, currentTime, RiseSet.MOON);
        mMoonriseTime = moonTimes.first;
        mMoonsetTime = moonTimes.second;

        // Find the tides
        mLowTides = new ArrayList<DateTime>();
        mHighTides = new ArrayList<DateTime>();
        if (mTideComputer != null) {
            for (TideComputer.TideExtreme tide : mTideComputer.getExtrema(currentTime, 36)) {
                if (tide.getType() == TideComputer.ExtremaType.HIGH_TIDE) {
                    mHighTides.add(tide.getTime());
                } else {
                    mLowTides.add(tide.getTime());
                }
            }
        }
    }

    private Pair<DateTime, DateTime> computeTimes(
            ObsInfo observerInfo, DateTime currentTime, int riseSetType) {
        double julianDay = new AstroDate(currentTime.getDayOfMonth(), currentTime.getMonthOfYear(),
                currentTime.getYear()).jd();
        RiseSet calc = new RiseSet();
        TimePair timePair = calc.getTimes(riseSetType, julianDay, observerInfo);

        return new Pair<DateTime, DateTime> (
                changeDayFraction(currentTime, timePair.a),
                changeDayFraction(currentTime, timePair.b));
    }

    private DateTime changeDayFraction(DateTime t, double dayFraction) {
        while (dayFraction < 0) {
            t = t.minusDays(1);
            dayFraction += 1;
        }
        int secondsInDay = (int) ((60 * 60 * 24) * dayFraction);
        int secondsInMinute = secondsInDay % 60;
        int minutes = (secondsInDay / 60) % 60;
        int hours = secondsInDay / (60 * 60);

        return new DateTime(
                t.getYear(), t.getMonthOfYear(), t.getDayOfMonth(), hours, minutes,
                secondsInMinute).plusMillis(t.getZone().getOffset(t.toInstant()));
    }

}
