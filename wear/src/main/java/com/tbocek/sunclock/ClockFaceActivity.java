package com.tbocek.sunclock;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.text.format.Time;
import android.widget.TextView;


public class ClockFaceActivity extends Activity {

    private static final double SEATTLE_LAT = 47.609722;
    private static final double SEATTLE_LONG = -122.333056;
    private TextView mTimeTextView;
    private TextView mSunriseTextView;
    private TextView mSunsetTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock_face);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);

        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTimeTextView = (TextView) findViewById(R.id.watch_face_time_text);
                mSunriseTextView = (TextView) findViewById(R.id.watch_face_sunrise_text);
                mSunsetTextView = (TextView) findViewById(R.id.watch_face_sunset_text);
            }
        });
    }

    @Override
    protected void onResume() {
        Time now = new Time();
        now.setToNow();
        SunriseTime sunriseTimeCalc = new SunriseTime(now, SEATTLE_LAT, SEATTLE_LONG,
                SunriseTime.CIVIL_ZENITH);

        Time sunriseTime = sunriseTimeCalc.getSunriseTime();
        Time sunsetTime = sunriseTimeCalc.getSunsetTime();

        sunriseTime.switchTimezone(now.timezone);
        sunsetTime.switchTimezone(now.timezone);

        mSunriseTextView.setText(sunriseTime.format("%H:%M:%S"));
        mSunsetTextView.setText(sunriseTime.format("%H:%M:%S"));
    }
}
