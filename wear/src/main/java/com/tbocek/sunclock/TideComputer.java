package com.tbocek.sunclock;

import android.util.Log;

import com.tideengine.BackEndTideComputer;
import com.tideengine.Coefficient;
import com.tideengine.Harmonic;
import com.tideengine.TideStation;
import com.tideengine.TideUtilities;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by tbocek on 7/15/14.
 */
public class TideComputer {
    private static final String TAG = "TideComputer";

    private static final int DELTA_MINUTES = 1;
    private static final int SAMPLES_IN_DERIVATIVE = 2;

    public enum ExtremaType {
        HIGH_TIDE,
        LOW_TIDE
    }

    private enum Trend {
        RISING,
        FALLING,
        NOT_DETERMINED, // Computation not finished.
        INCONSISTENT // Computation finished, trend wasn't consistent.
    }

    public static class TideExtreme {
        private final DateTime mTime;
        private final ExtremaType mType;

        private TideExtreme(DateTime time, ExtremaType type) {
            mTime = time;
            mType = type;
        }

        public DateTime getTime() {
            return mTime;
        }

        public ExtremaType getType() {
            return mType;
        }
    }

    TideStation mTideStation;
    List<Coefficient> mSpeedCoefficients;

    public TideComputer(TideStation tideStation) {
        mTideStation = tideStation;
        try {
            mSpeedCoefficients = BackEndTideComputer.buildSiteConstSpeed();
        } catch (Exception e) {
            throw new RuntimeException("Couldn't compute speed coefficients.", e);
        }
    }

    public List<TideExtreme> getExtrema(DateTime startingTime, int numberOfHours) {
        List<TideExtreme> ret = new ArrayList<TideExtreme>();

        logTideStation(mTideStation, mSpeedCoefficients);

        // Allocate an array of samples. We need
        double[] samples = new double[SAMPLES_IN_DERIVATIVE + 1];
        for (int i = 0; i < samples.length; ++i) {
            samples[i] = Float.NaN;
        }

        for (int minute = 0; minute < numberOfHours * 60; minute += DELTA_MINUTES) {
            DateTime t = startingTime.withSecondOfMinute(0).withMillisOfSecond(0).plusMinutes(minute);

            double waterHeight;
            try {
                waterHeight = TideUtilities.getWaterHeight(mTideStation, mSpeedCoefficients,
                        t.toGregorianCalendar());
            } catch (Exception e) {
                throw new RuntimeException("Could not compute tide level for t=" + t.toString(), e);
            }
            Log.d(TAG, "Water Height at " + t.toString() + " : " + Double.toString(waterHeight));

            addSample(samples, waterHeight);

            double derivative1 = firstDerivativeSign(samples, 0);
            double derivative2 = firstDerivativeSign(samples, 1);

            // Check whether three successive height computations are a local extreme by looking
            // for a sign change in a numeric estimate of the first derivative.
            if (derivative1 != Float.NaN && derivative2 != Float.NaN) {
                // Local maximum
                if (derivative1 > 0 && derivative2 < 0) {
                    Log.d(TAG, "HIGH!");
                    ret.add(new TideExtreme(t.minusMinutes(samples.length * DELTA_MINUTES / 2),
                            ExtremaType.HIGH_TIDE));
                }
                // Local minimum
                if (derivative1 < 0 && derivative2 > 0) {
                    Log.d(TAG, "LOW!");
                    ret.add(new TideExtreme(t.minusMinutes(samples.length * DELTA_MINUTES / 2),
                            ExtremaType.LOW_TIDE));
                }
            }
        }
        return ret;
    }

    public void addSample(double[] samples, double newSample) {
        for (int i = samples.length - 1; i > 0; --i) {
            samples[i] = samples[i - 1];
        }
        samples[0] = newSample;
    }

    /**
     * Computes the sign (but not the value) of the first derivative in the given samples starting
     * with sample i.
     * @param samples
     * @param i
     * @return
     */
    public double firstDerivativeSign(double[] samples, int i) {
        return samples[i + 1] - samples[i];
    }

    private void logTideStation(TideStation tideStation, List<Coefficient> speedCoefficients) {
        Log.d(TAG, "USING CONSTITUENTS:");
        for(Coefficient c : speedCoefficients) {
            Log.d(TAG, c.getName() + " : " + c.getValue());
        }
        Log.d(TAG, "USING HARMONICS:");
        for (Harmonic h: tideStation.getHarmonics()) {
            Log.d(TAG, h.getName() + " : " + h.getAmplitude() + " : " + h.getEpoch());
        }
    }
}
