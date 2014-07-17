package com.tbocek.sunclock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tbocek on 7/8/14.
 */
public class ClockView extends View {
    private static final String TAG = "ClockView";

    private static final int TWILIGHT_COLOR = Color.parseColor("#5714E8");
    private static final int TWILIGHT_COLOR_2 = Color.parseColor("#070892");
    private static final int NIGHT_COLOR = Color.parseColor("#031669");
    private static final int DAY_COLOR = Color.parseColor("#7AB3FF");
    private static final int HAND_COLOR = Color.parseColor("#F7D910");
    private static final int DOT_COLOR = Color.parseColor("#F7ED9B");

    private static final int HAND_COLOR_DIMMED = Color.YELLOW;
    private static final int DAY_COLOR_DIMMED = Color.LTGRAY;
    private static final int NIGHT_COLOR_DIMMED = Color.BLUE;

    public static final int LARGE_DOT_RADIUS = 6;
    public static final int MEDIUM_DOT_RADIUS = 3;
    public static final float SMALL_DOT_RADIUS = 1.5f;
    public static final int DOT_CENTER_EDGE_DISTANCE = 24;
    public static final int MOONRISE_EDGE_DISTANCE = 12;
    public static final int TIDE_EDGE_DISTANCE = 36;

    private DateTime mTime  = new DateTime(2014, 1, 1, 16, 20);
    private DateTime mSunriseTime = new DateTime(2014, 1, 1, 5, 00);
    private DateTime mSunsetTime = new DateTime(2014, 1, 1, 19, 00);
    private DateTime mDawnTime = new DateTime(2014, 1, 1, 4, 30);
    private DateTime mDuskTime = new DateTime(2014, 1, 1, 19, 30);
    private DateTime mAstroDawnTime =new DateTime(2014, 1, 1, 3, 00);
    private DateTime mAstroDuskTime = new DateTime(2014, 1, 1, 21, 00);
    private DateTime mMoonriseTime = new DateTime(2014, 1, 1, 12, 00);
    private DateTime mMoonsetTime = new DateTime(2014, 1, 1, 23, 00);

    private List<DateTime> mLowTides;
    private List<DateTime> mHighTides;

    private Paint mHourHandPaint;
    private Paint mMinuteHandPaint;
    private Paint mSunriseSunsetHandPaint;
    private Paint mCirclePaint;
    private Paint mDayPaint;
    private Paint mNightPaint;
    private Paint mMoonPaint;
    private Paint mAstroTwilightPaint;
    private Paint mVignettePaint;

    private Paint mHourHandPaintDimmed;
    private Paint mMinuteHandPaintDimmed;
    private Paint mDayPaintDimmed;
    private Paint mNightPaintDimmed;
    private Paint mCirclePaintDimmed;
    private Paint mMoonPaintDimmed;

    private boolean mHeld = false;
    private boolean mDimmed = false;

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

        mAstroTwilightPaint = new Paint();
        mAstroTwilightPaint.setColor(TWILIGHT_COLOR_2);
        mAstroTwilightPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

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

        mHourHandPaintDimmed = new Paint();
        mHourHandPaintDimmed.setColor(HAND_COLOR_DIMMED);
        mHourHandPaintDimmed.setStrokeWidth(4.0f);

        mMinuteHandPaintDimmed = new Paint();
        mMinuteHandPaintDimmed.setColor(HAND_COLOR_DIMMED);
        mMinuteHandPaintDimmed.setStrokeWidth(2.0f);

        mDayPaintDimmed = new Paint();
        mDayPaintDimmed.setColor(DAY_COLOR_DIMMED);

        mNightPaintDimmed = new Paint();
        mNightPaintDimmed.setColor(NIGHT_COLOR_DIMMED);

        mCirclePaintDimmed = new Paint();
        mCirclePaintDimmed.setColor(HAND_COLOR_DIMMED);

        mMoonPaintDimmed = new Paint();
        mMoonPaintDimmed.setColor(HAND_COLOR_DIMMED);
        mMoonPaintDimmed.setStyle(Paint.Style.STROKE);
        mMoonPaintDimmed.setStrokeWidth(2.0f);
    }

    public DateTime getTime() {
        return mTime;
    }

    public void setTime(DateTime mTime) {
        this.mTime = mTime;

        if (!mHeld) this.invalidate();
    }

    public DateTime getSunriseTime() {
        return mSunriseTime;
    }

    public void setSunriseTimes(DateTime sunriseTime, DateTime dawnTime, DateTime astroDawnTime) {
        this.mSunriseTime = sunriseTime;
        this.mDawnTime = dawnTime;
        this.mAstroDawnTime = astroDawnTime;
        Log.i(TAG, "Astronomical Dawn Time = " + astroDawnTime.toString("HH:mm:ss ZZ"));
        Log.i(TAG, "Civil Dawn Time = " + dawnTime.toString("HH:mm:ss ZZ"));
        Log.i(TAG, "Sunrise Time = " + sunriseTime.toString("HH:mm:ss ZZ"));
        if (!mHeld) this.invalidate();
    }

    public DateTime getSunsetTime() {
        return mSunsetTime;
    }

    public void setSunsetTimes(DateTime sunsetTime, DateTime duskTime, DateTime astroDuskTime) {
        this.mSunsetTime = sunsetTime;
        this.mDuskTime = duskTime;
        this.mAstroDuskTime = astroDuskTime;

        Log.i(TAG, "Sunset Time = " + sunsetTime.toString("HH:mm:ss ZZ"));
        Log.i(TAG, "Civil Dusk Time = " + duskTime.toString("HH:mm:ss ZZ"));
        Log.i(TAG, "Astronomical Dusk Time = " + astroDuskTime.toString("HH:mm:ss ZZ"));
        if (!mHeld) this.invalidate();
    }

    public DateTime getMoonriseTime() {
        return mMoonriseTime;
    }

    public void setMoonriseTime(DateTime moonriseTime) {
        mMoonriseTime = moonriseTime;
        if (!mHeld) this.invalidate();
    }

    public DateTime getMoonsetTime() {
        return mMoonsetTime;
    }

    public void setMoonsetTime(DateTime moonsetTime) {
        mMoonsetTime = moonsetTime;
        if (!mHeld) this.invalidate();
    }

    public void setTides(List<DateTime> lowTides, List<DateTime> highTides) {
        mLowTides = lowTides;
        mHighTides = highTides;
        if (!mHeld) this.invalidate();
    }

    public void setHeld(boolean held) {
        mHeld = held;
        if (!mHeld) this.invalidate();
    }

    private int dpToPx(float dp) {
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);
    }

    public void onDraw(Canvas c) {
        Log.i(TAG, "DRAW!");
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();

        c.drawColor(Color.WHITE);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int r = Math.min(getWidth(), getHeight()) / 2;

        // Draw arcs for the dusk sections.  Make them slightly larger than need be so that the
        // antialiasing on the day and night sections won't produce white line artifacts.
        if (!mDimmed) {
            fillArc(fractionOfDay(mDawnTime) - 0.01, fractionOfDay(mSunriseTime) + 0.01,
                    mSunriseSunsetHandPaint, c);
            fillArc(fractionOfDay(mSunsetTime) - 0.01, fractionOfDay(mDuskTime) + 0.01,
                    mSunriseSunsetHandPaint, c);

            fillArc(fractionOfDay(mAstroDawnTime) - 0.01, fractionOfDay(mDawnTime),
                    mAstroTwilightPaint, c);
            fillArc(fractionOfDay(mDuskTime), Math.min(fractionOfDay(mAstroDuskTime) + .01, 1.0),
                    mAstroTwilightPaint, c);

            fillArc(fractionOfDay(mSunriseTime), fractionOfDay(mSunsetTime),
                    mDayPaint, c);
            fillArc(fractionOfDay(mAstroDuskTime), fractionOfDay(mAstroDawnTime),
                    mNightPaint, c);

            c.drawRect(0, 0, getWidth(), getHeight(), mVignettePaint);
        } else {
            fillArc(fractionOfDay(mSunsetTime) - 0.01, fractionOfDay(mSunriseTime) + 0.01,
                    mNightPaintDimmed, c);
            fillArc(fractionOfDay(mSunriseTime), fractionOfDay(mSunsetTime), mDayPaintDimmed, c);
        }

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

            c.drawCircle(dest.x, dest.y, dotRadius, mDimmed ? mCirclePaintDimmed : mCirclePaint);
        }

        Paint moonPaint = mDimmed ? mMoonPaintDimmed : mMoonPaint;
        //Draw moonrise and moonset
        drawArc(fractionOfDay(mMoonriseTime), fractionOfDay(mMoonsetTime),
                 r - dpToPx(MOONRISE_EDGE_DISTANCE), moonPaint, c);

        // Draw tides
        for (Pair<DateTime, DateTime> risingTide : findRisingTides()) {
            drawArc(fractionOfDay(risingTide.first), fractionOfDay(risingTide.second),
                    r - dpToPx(TIDE_EDGE_DISTANCE), moonPaint, c);
        }

        int handLength =  r - dpToPx(DOT_CENTER_EDGE_DISTANCE);
        drawHand(fractionOfDay(mTime), centerX, centerY, (int)(handLength * 0.8),
                mDimmed ? mHourHandPaintDimmed : mHourHandPaint, c);
        drawHand(fractionOfHour(mTime), centerX, centerY, handLength,
                mDimmed ? mMinuteHandPaintDimmed : mMinuteHandPaint, c);
    }

    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        // Set up a radial gradient when we know the width and height of the view.
        mVignettePaint = new Paint();
        mVignettePaint.setShader(new RadialGradient(
                w / 2, h / 2, (float) (Math.max(w/2, h/2) * Math.sqrt(2)),
                Color.argb(0, 0, 0, 0), Color.argb(100, 0, 0, 0), Shader.TileMode.CLAMP));
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

    private void drawArc(double fraction1, double fraction2, int r, Paint paint, Canvas c) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        if (fraction1 > fraction2) {
            fraction2 += 1.0;
        }

        Path p = new Path();
        p.addArc(new RectF(centerX - r, centerY - r, centerX + r, centerY + r),
                360f * (float) fraction1 - 90, 360 * (float) (fraction2 - fraction1));

        c.drawPath(p, paint);
    }

    private void fillArc(double fraction1, double fraction2, Paint paint, Canvas c) {
        PointF startPoint = getPointOnEdge(fraction1);
        PointF endPoint = getPointOnEdge(fraction2);

        Path p = new Path();
        p.moveTo(getWidth() / 2, getHeight() / 2);

        if (fraction1 < fraction2) {
            // Draw to any corner points we need to, going clockwise
            p.lineTo(startPoint.x, startPoint.y);
            if (fraction1 < 1.0 / 8 && fraction2 > 1.0 / 8) {
                p.lineTo(getWidth(), 0);
            }
            if (fraction1 < 3.0 / 8 && fraction2 > 3.0 / 8) {
                p.lineTo(getWidth(), getHeight());
            }
            if (fraction1 < 5.0 / 8 && fraction2 > 5.0 / 8) {
                p.lineTo(0, getHeight());
            }
            if (fraction1 < 7.0 / 8 && fraction2 > 7.0 / 8) {
                p.lineTo(0, 0);
            }
            p.lineTo(endPoint.x, endPoint.y);
        } else {
            // Go counterclockwise.
            p.lineTo(endPoint.x, endPoint.y);

            // In order to determine the order and conditions here, we have to consider that,
            // if we are drawing counter-clockwise, the most drawing happens if both points are
            // on either side of 180 degrees.
            if (fraction2 > 3.0 / 8 && fraction1 > 3.0 / 8) {
                p.lineTo(getWidth(), getHeight());
            }

            if (fraction2 > 1.0 / 8 && fraction1 > 1.0 / 8) {
                p.lineTo(getWidth(), 0);
            }

            if (fraction2 > 7.0 / 8 && fraction1 < 7.0 / 8) {
                p.lineTo(0, 0);
            }
            if (fraction2 > 5.0 / 8 && fraction1 < 5.0 / 8) {
                p.lineTo(0, getHeight());
            }
            p.lineTo(startPoint.x, startPoint.y);
        }


        c.drawPath(p, paint);
    }

    private PointF getPointOnEdge(double fraction) {
        double theta = 2 * Math.PI * fraction;
        double w = this.getWidth();
        double h = this.getHeight();

        if (fraction < 0 || fraction > 1) {
            throw new IllegalArgumentException("Bad fraction: " + Double.toString(fraction) +
                    " must be between 0 and 1.");
        }

        if (fraction < 1.0/8) {
            return new PointF ((float)(w + h * Math.tan(theta)) / 2, 0);
        } else if (fraction < 3.0/8) {
            return new PointF((float) w, (float) ((h - w * Math.tan(Math.PI / 2 - theta)) / 2));
        } else if (fraction < 5.0/8) {
            return new PointF((float) ((w - h * Math.tan(Math.PI - theta)) / 2), (float) h);
        } else if (fraction < 7.0/8) {
            return new PointF (0, (float)(h + w * Math.tan(3 * Math.PI / 2 - theta)) / 2);
        } else {
            return new PointF ((float)(w - h * Math.tan(2 * Math.PI - theta)) / 2, 0);
        }
    }

    private List<Pair<DateTime, DateTime>> findRisingTides() {
        List<Pair<DateTime, DateTime>> risingTides = new ArrayList<Pair<DateTime, DateTime>>();

        if (mLowTides == null || mHighTides == null ||
                mLowTides.isEmpty() || mHighTides.isEmpty()) return risingTides;

        int currentLow = 0;
        int currentHigh = 0;
        if (mLowTides.get(0).isAfter(mHighTides.get(0).toInstant())) {
            currentHigh += 1;
        }

        while (currentLow < mLowTides.size() && currentHigh < mHighTides.size() &&
                mLowTides.get(currentLow).minusDays(1).isBefore(mLowTides.get(0)) &&
                mHighTides.get(currentHigh).minusDays(1).isBefore(mLowTides.get(0))) {
            risingTides.add(new Pair<DateTime, DateTime>(
                    mLowTides.get(currentLow), mHighTides.get(currentHigh)));

            currentLow++;
            currentHigh++;
        }
        return risingTides;
    }

    public void setDimmed(boolean dimmed) {
        mDimmed = dimmed;
        if (!mHeld) invalidate();
    }
}
