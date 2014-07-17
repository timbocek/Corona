package com.tbocek.sunclock;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


public class ClockFaceActivity extends Activity {

    private static final double SEATTLE_LAT = 47.68;
    private static final double SEATTLE_LONG = -122.21;

    private ClockView mClockView;
    private DateTime mLastSunriseUpdateTime;

    private IntentFilter mTimeTickFilter;
    private IntentFilter mTimeChangedFilter;
    private IntentFilter mTimeZoneChangedFilter;

    private BroadcastReceiver mTimeUpdateReceiver;

    private TideComputer mTideComputer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock_face);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);

        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mClockView = (ClockView) stub.findViewById(R.id.clock_view);
                updateTime();
            }
        });
        initReceivers();

        AsyncTask<Void, Void, Void> loadTidetableTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    String location = "Seattle, Washington";
                    BackEndTideComputer.setVerbose(true);
                    BackEndTideComputer.connect();
                    TideStation ts = BackEndTideComputer.findTideStation(
                            location, DateTime.now().getYear());
                    mTideComputer = new TideComputer(ts);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                resetSunriseAndSunsetTimes();
            }
        };
        loadTidetableTask.execute();

    }
    @Override
    protected void onStart() {
        super.onStart();

        // attach the time receiver to listen for time ticks.
        this.registerReceiver(mTimeUpdateReceiver, mTimeTickFilter);
        this.registerReceiver(mTimeUpdateReceiver, mTimeChangedFilter);
        this.registerReceiver(mTimeUpdateReceiver, mTimeZoneChangedFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Traditionally this is done in the onPause.
        // But since we are going to still get time updates
        // when paused, we can unregister the receiver
        this.unregisterReceiver(mTimeUpdateReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTime();
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
        int secondsInDay = (int) ((60 * 60 * 24) * dayFraction);
        int secondsInMinute = secondsInDay % 60;
        int minutes = (secondsInDay / 60) % 60;
        int hours = secondsInDay / (60 * 60);

        return new DateTime(
                t.getYear(), t.getMonthOfYear(), t.getDayOfMonth(), hours, minutes,
                secondsInMinute).plusMillis(t.getZone().getOffset(t.toInstant()));
    }

    private void resetSunriseAndSunsetTimes() {
        DateTime currentTime = new DateTime(DateTimeZone.forID("America/Los_Angeles"));
        mLastSunriseUpdateTime = currentTime;
        ObsInfo observerInfo = new ObsInfo(new Latitude(SEATTLE_LAT), new Longitude(SEATTLE_LONG),
                currentTime.getZone().getOffset(currentTime.toInstant()) / 3600);

        Pair<DateTime, DateTime> sunTimes = computeTimes(observerInfo, currentTime, RiseSet.SUN);
        Pair<DateTime, DateTime> duskTimes = computeTimes(observerInfo, currentTime,
                RiseSet.CIVIL_TWI);
        Pair<DateTime, DateTime> astroDuskTimes =
                computeTimes(observerInfo, currentTime, RiseSet.ASTRONOMICAL_TWI);

        mClockView.setHeld(true);

        if (sunTimes.first != null) {
            mClockView.setSunriseTimes(sunTimes.first, duskTimes.first, astroDuskTimes.first);
        }

        if (sunTimes.second != null) {
            mClockView.setSunsetTimes(sunTimes.second, duskTimes.second, astroDuskTimes.second);
        }

        Pair<DateTime, DateTime> moonTimes = computeTimes(observerInfo, currentTime, RiseSet.MOON);

        if (moonTimes.first != null && moonTimes.second != null) {
            mClockView.setMoonriseTime(moonTimes.first);
            mClockView.setMoonsetTime(moonTimes.second);
        }

        // Find the tides
        ArrayList<DateTime> lowTides = new ArrayList<DateTime>();
        ArrayList<DateTime> highTides = new ArrayList<DateTime>();
        if (mTideComputer != null) {
            for (TideComputer.TideExtreme tide : mTideComputer.getExtrema(currentTime, 36)) {
                if (tide.getType() == TideComputer.ExtremaType.HIGH_TIDE) {
                    highTides.add(tide.getTime());
                } else {
                    lowTides.add(tide.getTime());
                }
            }
            mClockView.setTides(lowTides, highTides);
        }

        mClockView.setHeld(false);
    }

    private void initReceivers() {
        // create the intent filter for the time tick action
        mTimeTickFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
        mTimeChangedFilter = new IntentFilter(Intent.ACTION_TIME_CHANGED);
        mTimeZoneChangedFilter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);

        // create the receiver
        mTimeUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
                    updateTime();
                }
            }
        };
    }

    private void updateTime() {
        if (mClockView == null) return;
        mClockView.setDimmed(isDimmed());
        DateTime currentTime = new DateTime(DateTimeZone.forID("America/Los_Angeles"));
        mClockView.setTime(currentTime);
        if (mLastSunriseUpdateTime == null ||
                mLastSunriseUpdateTime.getDayOfYear() != currentTime.getDayOfYear()) {
            resetSunriseAndSunsetTimes();
        }
    }

    private void setDimmed() {
        // set the dimmed flag
        this.getSharedPreferences("com.tbocek.sunclock.prefs", MODE_PRIVATE).edit()
                .putBoolean("dimmed", true).commit();
    }

    private void setBright() {
        this.getSharedPreferences("com.tbocek.sunclock.prefs", MODE_PRIVATE).edit()
                .putBoolean("dimmed", false).commit();
    }

    private boolean isDimmed() {
        return this.getSharedPreferences("com.tbocek.sunclock.prefs", MODE_PRIVATE)
                .getBoolean("dimmed", false);
    }
}
