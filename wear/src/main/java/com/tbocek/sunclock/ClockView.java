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

    private static final int TWILIGHT_COLOR = Color.parseColor("#6302E0");
    private static final int NIGHT_COLOR = Color.parseColor("#0325D1");
    private static final int DAY_COLOR = Color.parseColor("#0476FC");
    private static final int HAND_COLOR = Color.parseColor("#F7D910");
    private static final int DOT_COLOR = Color.parseColor("#F7ED9B");
    public static final int LARGE_DOT_RADIUS = 6;
    public static final int MEDIUM_DOT_RADIUS = 3;
    public static final float SMALL_DOT_RADIUS = 1.5f;
    public static final int DOT_CENTER_EDGE_DISTANCE = 24;
    public static final int MOONRISE_EDGE_DISTANCE = 16;
    public static final int TIDE_EDGE_DISTANCE = 32;

    private DateTime mTime  = new DateTime(2014, 1, 1, 16, 20);
    private DateTime mSunriseTime = new DateTime(2014, 1, 1, 5, 00);
    private DateTime mSunsetTime = new DateTime(2014, 1, 1, 19, 00);
    private DateTime mDawnTime = new DateTime(2014, 1, 1, 4, 30);
    private DateTime mDuskTime = new DateTime(2014, 1, 1, 19, 30);
    private DateTime mMoonriseTime = new DateTime(2014, 1, 1, 12, 00);
    private DateTime mMoonsetTime = new DateTime(2014, 1, 1, 23, 00);

    private Paint mHourHandPaint;
    private Paint mMinuteHandPaint;
    private Paint mSunriseSunsetHandPaint;
    private Paint mCirclePaint;
    private Paint mDayPaint;
    private Paint mNightPaint;
    private Paint mMoonPaint;

    public ClockView(Context context) {
        this(context, null, 0);
    }

    public ClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mHourHandPaint = new Paint();
        mHourHandPaint.setColor(HAND_COLOR);
        mHourHandPaint.setStrokeWidth(4.0f);
        mHourHandPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        mMinuteHandPaint = new Paint();
        mMinuteHandPaint.setColor(HAND_COLOR);
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
        mCirclePaint.setColor(DOT_COLOR);
        mCirclePaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        mMoonPaint = new Paint();
        mMoonPaint.setColor(DOT_COLOR);
        mMoonPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mMoonPaint.setStyle(Paint.Style.STROKE);
        mMoonPaint.setStrokeWidth(2.0f);
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

    public DateTime getMoonriseTime() {
        return mMoonriseTime;
    }

    public void setMoonriseTime(DateTime moonriseTime) {
        mMoonriseTime = moonriseTime;
        this.invalidate();
    }

    public DateTime getMoonsetTime() {
        return mMoonsetTime;
    }

    public void setMoonsetTime(DateTime moonsetTime) {
        mMoonsetTime = moonsetTime;
        this.invalidate();
    }

    private int dpToPx(float dp) {
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);
    }

    public void onDraw(Canvas c) {
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();

        c.drawColor(Color.WHITE);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int r = Math.min(getWidth(), getHeight()) / 2;

        // Draw arcs for the dusk sections.  Make them slightly larger than need be so that the
        // antialiasing on the day and night sections won't produce white line artifacts.
        fillArc(fractionOfDay(mDawnTime) - 0.01, fractionOfDay(mSunriseTime) + 0.01, centerX, centerY, 2 * r,
                mSunriseSunsetHandPaint, c);
        fillArc(fractionOfDay(mSunsetTime) - 0.01, fractionOfDay(mDuskTime) + 0.01, centerX, centerY, 2 * r,
                mSunriseSunsetHandPaint, c);
        fillArc(fractionOfDay(mSunriseTime), fractionOfDay(mSunsetTime), centerX, centerY, 2 * r,
                mDayPaint, c);
        fillArc(fractionOfDay(mDuskTime), 1 + fractionOfDay(mDawnTime), centerX, centerY, 2 * r,
                mNightPaint, c);

        // Draw dots on every hour
        for (int i = 0; i < 24; ++i) {
            Point dest = getPointOnCircle(((float)i) / 24, centerX, centerY,
                    r - dpToPx(DOT_CENTER_EDGE_DISTANCE));
            int dotRadius;
            if (i % 6 == 0) {
                dotRadius = dpToPx(LARGE_DOT_RADIUS);
            } else if (i % 2 == 0) {
                dotRadius = dpToPx(MEDIUM_DOT_RADIUS);
            } else {
                dotRadius = dpToPx(SMALL_DOT_RADIUS);
            }
            dotRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dotRadius, dm);

            c.drawCircle(dest.x, dest.y, dotRadius, mCirclePaint);
        }

        // Draw moonrise and moonset
        drawArc(fractionOfDay(mMoonriseTime), fractionOfDay(mMoonsetTime), centerX, centerY,
                r - dpToPx(MOONRISE_EDGE_DISTANCE), mMoonPaint, c);

        int handLength =  r - dpToPx(DOT_CENTER_EDGE_DISTANCE);
        drawHand(fractionOfDay(mTime), centerX, centerY, (int)(handLength * 0.8), mHourHandPaint, c);
        drawHand(fractionOfHour(mTime), centerX, centerY, handLength, mMinuteHandPaint, c);
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
        if (fraction1 > fraction2) {
            fraction2 += 1.0;
        }

        Path p = new Path();
        p.addArc(new RectF(centerX - r, centerY - r, centerX + r, centerY + r),
                360f * (float) fraction1 - 90, 360 * (float) (fraction2 - fraction1));

        c.drawPath(p, paint);
    }

    private void fillArc(double fraction1, double fraction2, int centerX, int centerY, int r,
                         Paint paint, Canvas c) {
        c.drawArc(new RectF(centerX - r, centerY - r, centerX + r, centerY + r),
                360f * (float) fraction1 - 90, 360 * (float) (fraction2 - fraction1),
                true, paint);
    }
}
