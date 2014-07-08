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
    private Time mLastSunriseUpdateTime;

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
        Time now = new Time();
        mLastSunriseUpdateTime = now;
        now.setToNow();
        SunriseTime sunriseTimeCalc = new SunriseTime(now, SEATTLE_LAT, SEATTLE_LONG,
                SunriseTime.CIVIL_ZENITH);

        Time sunriseTime = sunriseTimeCalc.getSunriseTime();
        Time sunsetTime = sunriseTimeCalc.getSunsetTime();

        if (sunriseTime != null) {
            sunriseTime.switchTimezone(now.timezone);
            mSunriseTextView.setText(sunriseTime.format("%H:%M:%S"));
        } else {
            mSunriseTextView.setText("Enjoy your SAD :(");
        }

        if (sunsetTime != null) {
            sunsetTime.switchTimezone(now.timezone);
            mSunsetTextView.setText(sunriseTime.format("%H:%M:%S"));
        } else {
            mSunsetTextView.setText("Looks like the land of the midnight sun!");
        }

    }

    private Thread timerThread = new Thread() {
        @Override
        public void run() {
            while (true) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Time now = new Time();
                        now.setToNow();
                        mTimeTextView.setText(now.format("%H:%M:%S"));
                        if (mLastSunriseUpdateTime.yearDay != now.yearDay) {
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
