package com.tbocek.sunclock;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.SurfaceHolder;

import com.mhuss.AstroLib.AstroDate;
import com.mhuss.AstroLib.Latitude;
import com.mhuss.AstroLib.Longitude;
import com.mhuss.AstroLib.ObsInfo;
import com.mhuss.AstroLib.RiseSet;
import com.mhuss.AstroLib.TimePair;
import com.tideengine.TideStation;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by tbocek on 12/15/14.
 */
public class SunClockFaceService extends CanvasWatchFaceService {
    private static final String TAG = "ClockView";

    private static final int TWILIGHT_COLOR = Color.parseColor("#5714E8");
    private static final int TWILIGHT_COLOR_2 = Color.parseColor("#070892");
    private static final int NIGHT_COLOR = Color.parseColor("#031669");
    private static final int DAY_COLOR = Color.parseColor("#7AB3FF");
    private static final int HAND_COLOR = Color.parseColor("#F7D910");
    private static final int DOT_COLOR = Color.parseColor("#F7ED9B");

    private static final int HAND_COLOR_DIMMED = Color.WHITE;
    private static final int DAY_COLOR_DIMMED = Color.DKGRAY;
    private static final int NIGHT_COLOR_DIMMED = Color.BLACK;

    public static final int LARGE_DOT_RADIUS = 6;
    public static final int MEDIUM_DOT_RADIUS = 3;
    public static final float SMALL_DOT_RADIUS = 1.5f;
    public static final int DOT_CENTER_EDGE_DISTANCE = 24;
    public static final int MOONRISE_EDGE_DISTANCE = 12;
    public static final int TIDE_EDGE_DISTANCE = 36;


    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {
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

        /* a time object */
        Time mTime;

        // Bitmap for caching the background, since it needs to be redrawn at most once a day.
        Bitmap mBackground;

        // Whether the background should be redrawn in the next draw loop.
        boolean mBackgroundNeedsUpdate;

        // Tracks the timestamp of the last data update.
        private long mLastDataUpdate;

        private boolean mAmbient;
        private boolean mBurnInProtection;
        private boolean mLowBitAmbient;

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                EventData.instance(SunClockFaceService.this).setTimeZone(
                        DateTimeZone.forID(intent.getStringExtra("time-zone")));
                mTime.setToNow();
                mBackgroundNeedsUpdate = true;
                invalidate();
            }
        };
        private boolean mRegisteredTimeZoneReceiver;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            mTime = new Time();
            mTime.setToNow();

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

            mBackgroundNeedsUpdate = true;

            EventData.instance(SunClockFaceService.this).setTimeZone(DateTimeZone.getDefault());
            EventData.instance(SunClockFaceService.this).loadFromPreferences();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT);
            if (mAmbient) {
                mBackgroundNeedsUpdate = true;
            }
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            int previousDate = mTime.monthDay;
            mTime.setToNow();
            if (mTime.monthDay != previousDate ||
                    mLastDataUpdate < EventData.instance(SunClockFaceService.this).getLastUpdate()) {
                mBackgroundNeedsUpdate = true;
                mLastDataUpdate = EventData.instance(SunClockFaceService.this).getLastUpdate();
            }
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            mAmbient = inAmbientMode;
            mBackgroundNeedsUpdate = true;
            invalidate();
        }

        int mWidth = 0;
        int mHeight = 0;

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            Log.i(TAG, "DRAW!");
            DisplayMetrics dm = SunClockFaceService.this.getResources().getDisplayMetrics();

            setUpDimensions(canvas.getWidth(), canvas.getHeight());

            int centerX = mWidth / 2;
            int centerY = mHeight / 2;
            int r = Math.min(mWidth, mHeight) / 2;
            int handLength =  r - dpToPx(DOT_CENTER_EDGE_DISTANCE);

            if (mBackgroundNeedsUpdate) {
                drawClockFaceBackground(new Canvas(mBackground));
            }

            // Copy the cached background to the clock face.
            canvas.drawBitmap(mBackground, 0, 0, null);

            // TODO: Test whether this behaves correctly for DST!
            DateTime dateTime = new DateTime(mTime.toMillis(true));

            drawHand(fractionOfDay(dateTime), centerX, centerY, (int)(handLength * 0.8),
                    mHourHandPaint, canvas);
            drawHand(fractionOfHour(dateTime), centerX, centerY, handLength,
                    mMinuteHandPaint, canvas);
        }

        private void setUpDimensions(int width, int height) {
            mWidth = width;
            mHeight = height;

            if (mVignettePaint == null) {
                mVignettePaint = new Paint();
                mVignettePaint.setShader(new RadialGradient(
                        mWidth / 2, mHeight / 2, (
                        float) (Math.max(mWidth / 2, mHeight / 2) * Math.sqrt(2)),
                        Color.argb(0, 0, 0, 0), Color.argb(100, 0, 0, 0), Shader.TileMode.CLAMP));
            }

            if (mBackground == null) {
                mBackground = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                registerReceiver();
            } else {
                unregisterReceiver();
            }
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            SunClockFaceService.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            SunClockFaceService.this.unregisterReceiver(mTimeZoneReceiver);
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
            int centerX = mWidth / 2;
            int centerY = mHeight / 2;

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
            p.moveTo(mWidth / 2, mHeight / 2);

            if (fraction1 < fraction2) {
                // Draw to any corner points we need to, going clockwise
                p.lineTo(startPoint.x, startPoint.y);
                if (fraction1 < 1.0 / 8 && fraction2 > 1.0 / 8) {
                    p.lineTo(mWidth, 0);
                }
                if (fraction1 < 3.0 / 8 && fraction2 > 3.0 / 8) {
                    p.lineTo(mWidth, mHeight);
                }
                if (fraction1 < 5.0 / 8 && fraction2 > 5.0 / 8) {
                    p.lineTo(0, mHeight);
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
                    p.lineTo(mWidth, mHeight);
                }

                if (fraction2 > 1.0 / 8 && fraction1 > 1.0 / 8) {
                    p.lineTo(mWidth, 0);
                }

                if (fraction1 < 7.0 / 8 && fraction2 < 7.0 / 8) {
                    p.lineTo(0, 0);
                }
                if (fraction1 < 5.0 / 8 && fraction2 < 5.0 / 8) {
                    p.lineTo(0, mHeight);
                }
                p.lineTo(startPoint.x, startPoint.y);
            }


            c.drawPath(p, paint);
        }

        private PointF getPointOnEdge(double fraction) {
            double theta = 2 * Math.PI * fraction;
            double w = this.mWidth;
            double h = this.mHeight;

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


        private int dpToPx(float dp) {
            DisplayMetrics dm = SunClockFaceService.this.getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);
        }

        private void drawClockFaceBackground(Canvas canvas) {
            DisplayMetrics dm = SunClockFaceService.this.getResources().getDisplayMetrics();
            EventData dat = EventData.instance(SunClockFaceService.this);

            int centerX = mWidth / 2;
            int centerY = mHeight / 2;
            int r = Math.min(mWidth, mHeight) / 2;

            // Draw arcs for the dusk sections.  Make them slightly larger than need be so that the
            // antialiasing on the day and night sections won't produce white line artifacts.
            fillArc(fractionOfDay(dat.getDawnTime()) - 0.01,
                    fractionOfDay(dat.getSunriseTime()) + 0.01,
                    mSunriseSunsetHandPaint, canvas);
            fillArc(fractionOfDay(dat.getSunsetTime()) - 0.01,
                    fractionOfDay(dat.getDuskTime()) + 0.01,
                    mSunriseSunsetHandPaint, canvas);

            fillArc(fractionOfDay(dat.getAstroDawnTime()) - 0.01, fractionOfDay(dat.getDawnTime()),
                    mAstroTwilightPaint, canvas);
            fillArc(fractionOfDay(dat.getDuskTime()),
                    Math.min(fractionOfDay(dat.getAstroDuskTime()) + .01, 1.0),
                    mAstroTwilightPaint, canvas);

            fillArc(fractionOfDay(dat.getSunriseTime()), fractionOfDay(dat.getSunsetTime()),
                    mDayPaint, canvas);
            fillArc(fractionOfDay(dat.getAstroDuskTime()), fractionOfDay(dat.getAstroDawnTime()),
                    mNightPaint, canvas);

            canvas.drawRect(0, 0, mWidth, mHeight, mVignettePaint);

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

                canvas.drawCircle(dest.x, dest.y, dotRadius, mCirclePaint);
            }

            //Draw moonrise and moonset
            drawArc(fractionOfDay(dat.getMoonriseTime()), fractionOfDay(dat.getMoonsetTime()),
                    r - dpToPx(MOONRISE_EDGE_DISTANCE), mMoonPaint, canvas);

            // Draw tides
            for (Pair<DateTime, DateTime> risingTide : dat.findRisingTides()) {
                drawArc(fractionOfDay(risingTide.first), fractionOfDay(risingTide.second),
                        r - dpToPx(TIDE_EDGE_DISTANCE), mMoonPaint, canvas);
            }

        }
    }
}
