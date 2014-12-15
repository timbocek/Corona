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


    public void onDraw(Canvas c) {

    }

    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        // Set up a radial gradient when we know the width and height of the view.
        mVignettePaint = new Paint();
        mVignettePaint.setShader(new RadialGradient(
                w / 2, h / 2, (float) (Math.max(w/2, h/2) * Math.sqrt(2)),
                Color.argb(0, 0, 0, 0), Color.argb(100, 0, 0, 0), Shader.TileMode.CLAMP));
    }

    public void setDimmed(boolean dimmed) {
        mDimmed = dimmed;
        if (!mHeld) invalidate();
    }
}
