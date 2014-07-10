package com.tbocek.sunclock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import org.joda.time.DateTime;

/**
 * Created by tbocek on 7/8/14.
 */
public class ClockView extends View {
    private static final String TAG = "ClockView";

    private static final int TWILIGHT_COLOR = Color.parseColor("#71488A");
    private static final int NIGHT_COLOR = Color.parseColor("#373178");
    private static final int DAY_COLOR = Color.parseColor("#A6DBFF");

    private DateTime mTime  = new DateTime(2014, 1, 1, 16, 20);
    private DateTime mSunriseTime = new DateTime(2014, 1, 1, 5, 00);
    private DateTime mSunsetTime = new DateTime(2014, 1, 1, 19, 00);
    private DateTime mDawnTime = new DateTime(2014, 1, 1, 4, 30);
    private DateTime mDuskTime = new DateTime(2014, 1, 1, 19, 30);


    private Paint mHourHandPaint;
    private Paint mMinuteHandPaint;
    private Paint mSunriseSunsetHandPaint;
    private Paint mCirclePaint;
    private Paint mDayPaint;
    private Paint mNightPaint;

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
        mHourHandPaint.setStrokeWidth(4.0f);
        mHourHandPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        mMinuteHandPaint = new Paint();
        mMinuteHandPaint.setColor(Color.BLACK);
        mMinuteHandPaint.setStrokeWidth(2.0f);
        mMinuteHandPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        mSunriseSunsetHandPaint = new Paint();
        mSunriseSunsetHandPaint.setColor(TWILIGHT_COLOR);
        mSunriseSunsetHandPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        mNightPaint = new Paint();
        mNightPaint.setColor(NIGHT_COLOR);
        mNightPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        mDayPaint = new Paint();
        mDayPaint.setColor(DAY_COLOR);
        mDayPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

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

    public void setSunriseTimes(DateTime sunriseTime, DateTime dawnTime) {
        this.mSunriseTime = sunriseTime;
        this.mDawnTime = dawnTime;
        Log.i(TAG, "Dawn Time = " + dawnTime.toString("HH:mm:ss ZZ"));
        Log.i(TAG, "Sunrise Time = " + sunriseTime.toString("HH:mm:ss ZZ"));
        this.invalidate();
    }

    public DateTime getSunsetTime() {
        return mSunsetTime;
    }

    public void setSunsetTimes(DateTime sunsetTime, DateTime duskTime) {
        this.mSunsetTime = sunsetTime;
        this.mDuskTime = duskTime;

        Log.i(TAG, "Sunset Time = " + sunsetTime.toString("HH:mm:ss ZZ"));
        Log.i(TAG, "Dusk Time = " + duskTime.toString("HH:mm:ss ZZ"));
        this.invalidate();
    }

    public void onDraw(Canvas c) {
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();

        c.drawColor(Color.WHITE);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int r = Math.min(getWidth(), getHeight()) / 2 - (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_PX, 10, dm);

        // Draw arcs for the dusk sections.  Make them slightly larger than need be so that the
        // antialiasing on the day and night sections won't produce white line artifacts.
        drawArc(fractionOfDay(mDawnTime) - 0.01, fractionOfDay(mSunriseTime) + 0.01, centerX, centerY, r,
               mSunriseSunsetHandPaint, c);
        drawArc(fractionOfDay(mSunsetTime) - 0.01, fractionOfDay(mDuskTime) + 0.01, centerX, centerY, r,
                mSunriseSunsetHandPaint, c);
        drawArc(fractionOfDay(mSunriseTime), fractionOfDay(mSunsetTime), centerX, centerY, r,
                mDayPaint, c);
        drawArc(fractionOfDay(mDuskTime), 1 + fractionOfDay(mDawnTime), centerX, centerY, r,
                mNightPaint, c);

        // Draw dots on every hour
        for (int i = 0; i < 24; ++i) {
            Point dest = getPointOnCircle(((float)i) / 24, centerX, centerY, r);
            int dotRadius;
            if (i % 6 == 0) {
                dotRadius = 6;
            } else if (i % 2 == 0) {
                dotRadius = 4;
            } else {
                dotRadius = 2;
            }
            dotRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dotRadius, dm);

            c.drawCircle(dest.x, dest.y, dotRadius, mCirclePaint);
        }

        drawHand(fractionOfDay(mTime), centerX, centerY, (int)(r * 0.8), mHourHandPaint, c);
        drawHand(fractionOfHour(mTime), centerX, centerY, r, mMinuteHandPaint, c);
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

    private Point getPointOnCircle(double fraction, int centerX, int centerY, int r) {
        int x = centerX + (int) (r * Math.cos(Math.PI / 2 - fraction * 2 * Math.PI));
        int y = centerY - (int) (r * Math.sin(Math.PI / 2 - fraction * 2 * Math.PI));
        return new Point(x, y);

    }

    private void drawHand(double fraction, int centerX, int centerY, int r, Paint paint, Canvas c) {
        Point dest = getPointOnCircle(fraction, centerX, centerY, r);
        c.drawLine(centerX, centerY, dest.x, dest.y, paint);
    }

    private void drawArc(double fraction1, double fraction2, int centerX, int centerY, int r,
                         Paint paint, Canvas c) {
        c.drawArc(new RectF(centerX - r, centerY - r, centerX + r, centerY + r),
                360f * (float) fraction1 - 90, 360 * (float) (fraction2 - fraction1),
                true, paint);
    }
}
