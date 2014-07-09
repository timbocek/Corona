package com.tbocek.sunclock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import org.joda.time.DateTime;

/**
 * Created by tbocek on 7/8/14.
 */
public class ClockView extends View {

    private DateTime mTime  = new DateTime(2014, 1, 1, 16, 20);
    private DateTime mSunriseTime = new DateTime(2014, 1, 1, 5, 00);
    private DateTime mSunsetTime = new DateTime(2014, 1, 1, 19, 00);

    private Paint mHourHandPaint;
    private Paint mMinuteHandPaint;
    private Paint mSunriseSunsetHandPaint;
    private Paint mCirclePaint;

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
        mHourHandPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        mMinuteHandPaint = new Paint();
        mMinuteHandPaint.setColor(Color.BLACK);
        mMinuteHandPaint.setStrokeWidth(1.0f);
        mMinuteHandPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        mSunriseSunsetHandPaint = new Paint();
        mSunriseSunsetHandPaint.setColor(Color.RED);
        mSunriseSunsetHandPaint.setStrokeWidth(1.0f);
        mSunriseSunsetHandPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        mCirclePaint = new Paint();
        mCirclePaint.setColor(Color.GRAY);
        mCirclePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
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
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();

        c.drawColor(Color.WHITE);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int r = Math.min(getWidth(), getHeight()) / 2 - (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_PX, 5, dm);

        // Draw dots on every hour
        for (int i = 0; i < 12; ++i) {

            float fraction = ((float)i) / 12;

            int destX = centerX + (int) (r * Math.cos(Math.PI / 2 - fraction * 2 * Math.PI));
            int destY = centerY - (int) (r * Math.sin(Math.PI / 2 - fraction * 2 * Math.PI));
            int dotRadius = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_PX, (i % 3 == 0) ? 4 : 2, dm);

            c.drawCircle(destX, destY, dotRadius, mCirclePaint);
        }

        drawHand(fractionOfDay(mTime), centerX, centerY, r, mHourHandPaint, c);
        drawHand(fractionOfHour(mTime), centerX, centerY, r, mMinuteHandPaint, c);
        drawHand(fractionOfDay(mSunriseTime), centerX, centerY, r, mSunriseSunsetHandPaint, c);
        drawHand(fractionOfDay(mSunsetTime), centerX, centerY, r, mSunriseSunsetHandPaint, c);

    }

    private double fractionOfDay(DateTime t) {
        if (t == null) return 0;
        return ((double)t.getHourOfDay() + fractionOfHour(t)) / 24.0;
    }

    private double fractionOfHour(DateTime t) {
        if (t == null) return 0;
        return ((double) t.getMinuteOfHour() + fractionOfMinute(t)) / 60.0;
    }

    private double fractionOfMinute(DateTime t) {
        if (t == null) return 0;
        return ((double) t.getSecondOfMinute()) / 60.0;
    }

    private void drawHand(double fraction, int centerX, int centerY, int r, Paint paint, Canvas c) {
        int destX = centerX + (int) (r * Math.cos(Math.PI / 2 - fraction * 2 * Math.PI));
        int destY = centerY - (int) (r * Math.sin(Math.PI / 2 - fraction * 2 * Math.PI));

        c.drawLine(centerX, centerY, destX, destY, paint);
    }
}