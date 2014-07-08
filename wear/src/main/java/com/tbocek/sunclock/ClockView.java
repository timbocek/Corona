package com.tbocek.sunclock;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import org.joda.time.DateTime;

/**
 * Created by tbocek on 7/8/14.
 */
public class ClockView extends View {

    private DateTime mTime;
    private DateTime mSunriseTime;
    private DateTime mSunsetTime;

    public ClockView(Context context) {
        this(context, null, 0);
    }

    public ClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DateTime getTime() {
        return mTime;
    }

    public void setTime(DateTime mTime) {
        this.mTime = mTime;
    }

    public DateTime getSunriseTime() {
        return mSunriseTime;
    }

    public void setSunriseTime(DateTime mSunriseTime) {
        this.mSunriseTime = mSunriseTime;
    }

    public DateTime getSunsetTime() {
        return mSunsetTime;
    }

    public void setSunsetTime(DateTime mSunsetTime) {
        this.mSunsetTime = mSunsetTime;
    }

    public void onDraw(Canvas c) {
        double hourHandDegrees = ((double)mTime.getHourOfDay()) / 24.0;
        double minuteHandDegrees = ((double)mTime.getMinuteOfHour()) / 24.0;
    }

    private double fractionOfDay(DateTime t) {
        return ((double)t.getHourOfDay() + fractionOfHour) / 24.0;
    }

    private double fractionOfHour(DateTime t) {
        return ((double) t.getMinuteOfHour()) / 60.0;
    }
}
