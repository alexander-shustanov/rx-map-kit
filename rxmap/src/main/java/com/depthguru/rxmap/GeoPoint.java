package com.depthguru.rxmap;

import android.location.Location;

public class GeoPoint implements IGeoPoint, MapConstants {

    private int mLongitudeE6;
    private int mLatitudeE6;

    public GeoPoint(final int aLatitudeE6, final int aLongitudeE6) {
        this.mLatitudeE6 = aLatitudeE6;
        this.mLongitudeE6 = aLongitudeE6;
    }

    public GeoPoint(final double aLatitude, final double aLongitude) {
        this.mLatitudeE6 = (int) (aLatitude * 1E6);
        this.mLongitudeE6 = (int) (aLongitude * 1E6);
    }

    public GeoPoint(final Location aLocation) {
        this(aLocation.getLatitude(), aLocation.getLongitude());
    }

    public GeoPoint(final GeoPoint aGeopoint) {
        this.mLatitudeE6 = aGeopoint.mLatitudeE6;
        this.mLongitudeE6 = aGeopoint.mLongitudeE6;
    }


    public static GeoPoint fromCenterBetween(final GeoPoint geoPointA, final GeoPoint geoPointB) {
        return new GeoPoint((geoPointA.getLatitudeE6() + geoPointB.getLatitudeE6()) / 2,
                (geoPointA.getLongitudeE6() + geoPointB.getLongitudeE6()) / 2);
    }

    @Override
    public int getLongitudeE6() {
        return this.mLongitudeE6;
    }

    public void setLongitudeE6(final int aLongitudeE6) {
        this.mLongitudeE6 = aLongitudeE6;
    }

    @Override
    public int getLatitudeE6() {
        return this.mLatitudeE6;
    }

    public void setLatitudeE6(final int aLatitudeE6) {
        this.mLatitudeE6 = aLatitudeE6;
    }

    @Override
    public double getLongitude() {
        return this.mLongitudeE6 * 1E-6;
    }

    @Override
    public double getLatitude() {
        return this.mLatitudeE6 * 1E-6;
    }

    @Override
    public GeoPoint clone() {
        return new GeoPoint(this.mLatitudeE6, this.mLongitudeE6);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeoPoint geoPoint = (GeoPoint) o;

        if (mLongitudeE6 != geoPoint.mLongitudeE6) return false;
        return mLatitudeE6 == geoPoint.mLatitudeE6;

    }

    @Override
    public int hashCode() {
        int result = mLongitudeE6;
        result = 31 * result + mLatitudeE6;
        return result;
    }

    public int distanceTo(final IGeoPoint other) {

        final double a1 = DEG2RAD * this.mLatitudeE6 / 1E6;
        final double a2 = DEG2RAD * this.mLongitudeE6 / 1E6;
        final double b1 = DEG2RAD * other.getLatitudeE6() / 1E6;
        final double b2 = DEG2RAD * other.getLongitudeE6() / 1E6;

        final double cosa1 = Math.cos(a1);
        final double cosb1 = Math.cos(b1);

        final double t1 = cosa1 * Math.cos(a2) * cosb1 * Math.cos(b2);

        final double t2 = cosa1 * Math.sin(a2) * cosb1 * Math.sin(b2);

        final double t3 = Math.sin(a1) * Math.sin(b1);

        final double tt = Math.acos(t1 + t2 + t3);

        return (int) (RADIUS_EARTH_METERS * tt);
    }

    /**
     * @return bearing in degrees
     * @see <a href="http://groups.google.com/group/osmdroid/browse_thread/thread/d22c4efeb9188fe9/bc7f9b3111158dd">discussion</a>
     */
    public double bearingTo(final IGeoPoint other) {
        final double lat1 = Math.toRadians(this.mLatitudeE6 / 1E6);
        final double long1 = Math.toRadians(this.mLongitudeE6 / 1E6);
        final double lat2 = Math.toRadians(other.getLatitudeE6() / 1E6);
        final double long2 = Math.toRadians(other.getLongitudeE6() / 1E6);
        final double delta_long = long2 - long1;
        final double a = Math.sin(delta_long) * Math.cos(lat2);
        final double b = Math.cos(lat1) * Math.sin(lat2) -
                Math.sin(lat1) * Math.cos(lat2) * Math.cos(delta_long);
        final double bearing = Math.toDegrees(Math.atan2(a, b));
        final double bearing_normalized = (bearing + 360) % 360;
        return bearing_normalized;
    }

    public GeoPoint destinationPoint(final double aDistanceInMeters, final float aBearingInDegrees) {

        // convert distance to angular distance
        final double dist = aDistanceInMeters / RADIUS_EARTH_METERS;

        // convert bearing to radians
        final float brng = (float) (DEG2RAD * aBearingInDegrees);

        // get current location in radians
        final double lat1 = DEG2RAD * getLatitudeE6() / 1E6;
        final double lon1 = DEG2RAD * getLongitudeE6() / 1E6;

        final double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dist) + Math.cos(lat1)
                * Math.sin(dist) * Math.cos(brng));
        final double lon2 = lon1
                + Math.atan2(Math.sin(brng) * Math.sin(dist) * Math.cos(lat1), Math.cos(dist)
                - Math.sin(lat1) * Math.sin(lat2));

        final double lat2deg = lat2 / DEG2RAD;
        final double lon2deg = lon2 / DEG2RAD;

        return new GeoPoint(lat2deg, lon2deg);
    }
}
