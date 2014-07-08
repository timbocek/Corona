package com.tbocek.sunclock;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.text.format.Time;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;


public class ClockFaceActivity extends Activity {

    private static final double SEATTLE_LAT = 47.609722;
    private static final double SEATTLE_LONG = -122.333056;

    private TextView mTimeTextView;
    private TextView mSunriseTextView;
    private TextView mSunsetTextView;
    private DateTime mLastSunriseUpdateTime;

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
        DateTime currentTime = new DateTime();
        mLastSunriseUpdateTime = currentTime;
        SunriseTime sunriseTimeCalc = new SunriseTime(currentTime, SEATTLE_LAT, SEATTLE_LONG,
                SunriseTime.CIVIL_ZENITH);

        DateTime sunriseTime = sunriseTimeCalc.getSunriseTime();
        DateTime sunsetTime = sunriseTimeCalc.getSunsetTime();

        if (sunriseTime != null) {
            mSunriseTextView.setText(sunriseTime.toString("HH:mm:ss z"));
        } else {
            mSunriseTextView.setText("Enjoy your SAD :(");
        }

        if (sunsetTime != null) {
            mSunsetTextView.setText(sunriseTime.toString("HH:mm:ss z"));
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
                        DateTime currentTime = new DateTime();
                        mTimeTextView.setText(currentTime.toString("HH:mm:ss z"));
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
