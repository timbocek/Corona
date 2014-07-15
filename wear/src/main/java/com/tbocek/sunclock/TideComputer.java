package com.tbocek.sunclock;

import com.tideengine.BackEndTideComputer;
import com.tideengine.Coefficient;
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
    public enum ExtremaType {
        HIGH_TIDE,
        LOW_TIDE
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

        boolean wasRising;
        double waterHeightTMinus1 = Float.NaN;
        double waterHeightTMinus2 = Float.NaN;

        for (int minute = 0; minute < numberOfHours * 60; ++minute) {
            DateTime t = startingTime.plusMinutes(minute);

            double waterHeight;
            try {
                waterHeight = TideUtilities.getWaterHeight(mTideStation, mSpeedCoefficients,
                        t.toGregorianCalendar());
            } catch (Exception e) {
                throw new RuntimeException("Could not compute tide level for t=" + t.toString(), e);
            }

            // Check whether three successive height computations are a local extreme.
            if (waterHeightTMinus1 != Float.NaN && waterHeightTMinus2 != Float.NaN) {
                // Local maximum
                if (waterHeightTMinus2 < waterHeightTMinus1 && waterHeight < waterHeightTMinus1) {
                    ret.add(new TideExtreme(t.minusMinutes(1), ExtremaType.HIGH_TIDE));
                }
                // Local minimum
                if (waterHeightTMinus2 > waterHeightTMinus1 && waterHeight > waterHeightTMinus1) {
                    ret.add(new TideExtreme(t.minusMinutes(1), ExtremaType.LOW_TIDE));
                }
            }

            waterHeightTMinus2 = waterHeightTMinus1;
            waterHeightTMinus1 = waterHeight;
        }
        return ret;
    }
}