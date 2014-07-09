package com.tbocek.sunclock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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

    private Paint mHourHandPaint;
    private Paint mMinuteHandPaint;
    private Paint mSunriseSunsetHandPaint;

    public ClockView(Context context) {
        this(context, null, 0);
    }

    public ClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mHourHandPaint = new Paint();
        mHourHandPaint.setColor(Color.BLACK);
        mHourHandPaint.setStrokeWidth(2.0f);

        mMinuteHandPaint = new Paint();
        mMinuteHandPaint.setColor(Color.BLACK);
        mMinuteHandPaint.setStrokeWidth(1.0f);

        mSunriseSunsetHandPaint = new Paint();
        mSunriseSunsetHandPaint.setColor(Color.RED);
        mSunriseSunsetHandPaint.setStrokeWidth(1.0f);
    }

    public DateTime getTime() {
        return mTime;
    }

    public void setTime(DateTime mTime) {
        this.mTime = mTime;
        this.invalidate();
    }

    public DateTime getSunriseTime() {
        return mSunriseTime;
    }

    public void setSunriseTime(DateTime mSunriseTime) {
        this.mSunriseTime = mSunriseTime;
        this.invalidate();
    }

    public DateTime getSunsetTime() {
        return mSunsetTime;
    }

    public void setSunsetTime(DateTime mSunsetTime) {
        this.mSunsetTime = mSunsetTime;
        this.invalidate();
    }

    public void onDraw(Canvas c) {
        c.drawColor(Color.WHITE);

        int centerX = c.getWidth() / 2;
        int centerY = c.getHeight() / 2;
        int r = Math.min(c.getWidth(), c.getHeight());

        drawHand(fractionOfDay(mTime), centerX, centerY, r, mHourHandPaint, c);
        drawHand(fractionOfHour(mTime), centerX, centerY, r, mMinuteHandPaint, c);
        drawHand(fractionOfDay(mSunriseTime), centerX, centerY, r, mSunriseSunsetHandPaint, c);
        drawHand(fractionOfDay(mSunsetTime), centerX, centerY, r, mSunriseSunsetHandPaint, c);
    }

    private double fractionOfDay(DateTime t) {
        return ((double)t.getHourOfDay() + fractionOfHour(t)) / 24.0;
    }

    private double fractionOfHour(DateTime t) {
        return ((double) t.getMinuteOfHour()) / 60.0;
    }

    private void drawHand(double fraction, int centerX, int centerY, int r, Paint paint, Canvas c) {
        int destX = centerX + r * (int) Math.cos(Math.PI / 2 - fraction * 2 * Math.PI);
        int destY = centerY + r * (int) Math.sin(Math.PI / 2 - fraction * 2 * Math.PI);

        c.drawLine(centerX, centerY, destX, destY, paint);
    }
}
