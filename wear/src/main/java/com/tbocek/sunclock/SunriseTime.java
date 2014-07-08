package com.tbocek.sunclock;

import android.text.format.Time;

/**
 * Class to compute sunrise and sunset times.
 *
 * Based on the algorithm given at:
 * http://williams.best.vwh.net/sunrise_sunset_algorithm.htm
 * Created by tbocek on 7/8/14.
 */
public class SunriseTime {
    public final static double OFFICIAL_ZENITH = 90.83;
    public final static double CIVIL_ZENITH = 96;
    public final static double NAUTICAL_ZENITH = 102;
    public final static double ASTRONOMICAL_ZENITH = 108;

    private final Time mCurrentTime;
    private final double mLatitude;
    private final double mLongitude;
    private final double mZenith;

    public SunriseTime(Time now, double latitude, double longitude, double zenith) {
        mCurrentTime = now;
        mLatitude = latitude;
        mLongitude = longitude;
        mZenith = zenith;
    }

    /**
     * Gets the sunrise time.
     * @return Time object containing the sunrise time, or null if the sun will not rise at the
     *     specified location.
     */
    public Time getSunriseTime() {
        return getSunriseOrSunsetTime(true);
    }

    /**
     * Gets the sunset time.
     * @return Time object containing the sunrise time, or null if the sun will not set at the
     *     specified location.
     */
    public Time getSunsetTime() {
        return getSunriseOrSunsetTime(false);
    }

    private Time getSunriseOrSunsetTime(boolean isSunrise) {
        // Convert the longitude to hour value and calculate an approximate time.
        double longitudeHour = mLongitude / 15;

        double approximateTime;
        if (isSunrise) {
            approximateTime = mCurrentTime.yearDay + ((6 - longitudeHour) / 24);
        } else {
            approximateTime = mCurrentTime.yearDay + ((18 - longitudeHour) / 24);
        }

        double meanAnomaly = (0.9856 * approximateTime) - 3.289;
        double trueLongitudeOfSun =
                meanAnomaly + (1.916 * sin(meanAnomaly)) +
                        (0.020 * sin(2 * meanAnomaly)) + 282.634;
        trueLongitudeOfSun = clampAngle(trueLongitudeOfSun);

        double rightAscension = arctan(0.91764 * tan(trueLongitudeOfSun));
        rightAscension = clampAngle(rightAscension);

        // Make sure the sun's right ascension is in the same quadrant as its true longitude.
        double lQuadrant  = (Math.floor(trueLongitudeOfSun / 90)) * 90;
        double raQuadrant = (Math.floor(rightAscension / 90)) * 90;
        rightAscension = rightAscension + (lQuadrant - raQuadrant);

        double rightAscensionHours = rightAscension / 15;

        double declinationSin = 0.39782 * sin(trueLongitudeOfSun);
        double declinationCos = cos(arcsin(declinationSin));

        // Calculate the cosine of the local hour angle, and use this value to check for whether
        // the sun actually rises or sets.
        double localHourAngleCos = (cos(mZenith) - (declinationSin * sin(mLatitude))) /
                (declinationCos * cos(mLatitude));

        if (localHourAngleCos > 1 || localHourAngleCos < -1) return null; // sun never rises

        // Finish calculating local hour angle and convert to hours.
        double localHourAngle = arccos(localHourAngleCos);
        if (isSunrise) {
            localHourAngle = 360 - localHourAngle;
        }

        // Calculate local mean time of rising/setting
        double localMeanTime = localHourAngle + rightAscensionHours - 0.06571 * approximateTime -
                6.622;

        double utcMeanTime = clampHour(localMeanTime - longitudeHour);

        int meanTimeInSeconds = (int) (utcMeanTime * 3600);
        int seconds = meanTimeInSeconds % 60;
        int minutes = (meanTimeInSeconds / 60) % 60;
        int hours = (meanTimeInSeconds) / 3600;

        Time result = new Time();
        result.set(seconds, minutes, hours, mCurrentTime.monthDay, mCurrentTime.month,
                mCurrentTime.year);
        return result;
    }

    /**
     * Ensures the given angle is in the range [0, 360)
     * @param angle The angle to clamp
     * @return An equivalent angle in the range [0, 360).
     */
    private double clampAngle(double angle) {
        while (angle >= 360)
            angle -= 360;
        while (angle < 0)
            angle += 360;
        return angle;
    }

    /**
     * Ensures the given hour is in the range [0, 24)
     * @param hour The hour to clamp
     * @return An equivalent angle in the range [0, 24).
     */
    private double clampHour(double hour) {
        while (hour >= 24)
            hour -= 24;
        while (hour < 0)
            hour += 24;
        return hour;
    }

    // Trig functions that work in degrees.

    private double sin(double angle) {
        return Math.sin(angle * Math.PI / 180.0);
    }

    private double cos(double angle) {
        return Math.cos(angle * Math.PI / 180.0);
    }

    private double tan(double angle) {
        return Math.tan(angle * Math.PI / 180.0);
    }

    private double arcsin(double tan) {
        return Math.asin(tan) * 180.0 / Math.PI;
    }

    private double arccos(double tan) {
        return Math.acos(tan) * 180.0 / Math.PI;
    }

    private double arctan(double tan) {
        return Math.atan(tan) * 180.0 / Math.PI;
    }
}
