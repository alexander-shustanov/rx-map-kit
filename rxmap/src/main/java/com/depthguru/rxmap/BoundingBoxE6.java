package com.depthguru.rxmap;

/**
 * BoundingBoxE6
 * </p>
 * alexander.shustanov on 15.12.16
 */
public class BoundingBoxE6 {

    protected final int latNorthE6;
    protected final int latSouthE6;
    protected final int lonEastE6;
    protected final int lonWestE6;

    public BoundingBoxE6(GeoPoint northWest, GeoPoint southEast) {
        this.latNorthE6 = northWest.getLatitudeE6();
        this.latSouthE6 = southEast.getLatitudeE6();
        this.lonWestE6 = northWest.getLongitudeE6();
        this.lonEastE6 = southEast.getLongitudeE6();
    }

    public BoundingBoxE6(int lonWestE6, int latNorthE6, int lonEastE6, int latSouthE6) {
        this.lonWestE6 = lonWestE6;
        this.latNorthE6 = latNorthE6;
        this.lonEastE6 = lonEastE6;
        this.latSouthE6 = latSouthE6;
    }

    public int getLonWestE6() {
        return lonWestE6;
    }

    public int getLonEastE6() {
        return lonEastE6;
    }

    public int getLatNorthE6() {
        return latNorthE6;
    }

    public int getLatSouthE6() {
        return latSouthE6;
    }

    public int getWidth() {
        int left = getLonWestE6();
        int right = getLonEastE6();
        if (left > right) {
            return (int) (180 * 1E6 - left + right);
        }
        return right - left;

    }

    public int getHeight() {
        int top = getLatNorthE6();
        int bottom = getLatSouthE6();
        if (top < bottom) {
            return (int) (90 * 1E6 - bottom + top);
        }
        return top - bottom;
    }

    public GeoPoint getCenter() {
        return new GeoPoint(
                (latNorthE6 + latSouthE6) / 2,
                (lonEastE6 + lonWestE6) / 2
        );
    }

    public boolean contains(GeoPoint geoPoint) {
        boolean containsByLat;
        if(latNorthE6 > latSouthE6) {
            containsByLat = latNorthE6 > geoPoint.getLatitudeE6() && geoPoint.getLatitudeE6() > latSouthE6;
        } else {
            containsByLat = latNorthE6 > geoPoint.getLatitudeE6() || geoPoint.getLatitudeE6() > latSouthE6;
        }
        if(!containsByLat) {
            return false;
        }
        if(lonEastE6 > lonWestE6) {
            return lonEastE6 > geoPoint.getLongitudeE6() && geoPoint.getLongitudeE6() > lonWestE6;
        } else {
            return lonEastE6 > geoPoint.getLongitudeE6() || geoPoint.getLongitudeE6() > lonWestE6;
        }
    }
}
