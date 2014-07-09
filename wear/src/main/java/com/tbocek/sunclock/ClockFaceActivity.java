package com.tbocek.sunclock;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Calendar;
import java.util.Locale;


public class ClockFaceActivity extends Activity {

    private static final double SEATTLE_LAT = 47.68;
    private static final double SEATTLE_LONG = -122.21;

    private ClockView mClockView;
    private DateTime mLastSunriseUpdateTime;

    private IntentFilter mTimeTickFilter;
    private IntentFilter mTimeChangedFilter;
    private IntentFilter mTimeZoneChangedFilter;

    private BroadcastReceiver mTimeUpdateReceiver;

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

    private void resetSunriseAndSunsetTimes() {
        DateTime currentTime = new DateTime(DateTimeZone.forID("America/Los_Angeles"));
        mLastSunriseUpdateTime = currentTime;
        Location location = new Location(SEATTLE_LAT, SEATTLE_LONG);
        SunriseSunsetCalculator calc = new SunriseSunsetCalculator(location, "America/Los_Angeles");

        Calendar sunriseTime = calc.getOfficialSunriseCalendarForDate(
                currentTime.toCalendar(Locale.US));
        Calendar sunsetTime = calc.getOfficialSunsetCalendarForDate(
                currentTime.toCalendar(Locale.US));

        if (sunriseTime != null) {
            mClockView.setSunriseTime(new DateTime(sunriseTime));
        }

        if (sunsetTime != null) {
            mClockView.setSunsetTime(new DateTime(sunsetTime));
        }
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
        DateTime currentTime = new DateTime(DateTimeZone.forID("America/Los_Angeles"));
        mClockView.setTime(currentTime);
        if (mLastSunriseUpdateTime == null ||
                mLastSunriseUpdateTime.getDayOfYear() != currentTime.getDayOfYear()) {
            resetSunriseAndSunsetTimes();
        }
    }
}
