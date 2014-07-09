package com.tbocek.sunclock;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.text.format.Time;
import android.widget.TextView;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.Calendar;
import java.util.Locale;


public class ClockFaceActivity extends Activity {

    private static final double SEATTLE_LAT = 47.68;
    private static final double SEATTLE_LONG = -122.21;

    private ClockView mClockView;
    private DateTime mLastSunriseUpdateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock_face);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);

        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

                mClockView = (ClockView) stub.findViewById(R.id.clock_view);
                resetSunriseAndSunsetTimes();
                timerThread.start();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            timerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    private Thread timerThread = new Thread() {
        @Override
        public void run() {
            while (true) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DateTime currentTime = new DateTime(DateTimeZone.forID("America/Los_Angeles"));
                        mClockView.setTime(currentTime);
                        if (mLastSunriseUpdateTime.getDayOfYear() != currentTime.getDayOfYear()) {
                            resetSunriseAndSunsetTimes();
                        }
                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    };
}
