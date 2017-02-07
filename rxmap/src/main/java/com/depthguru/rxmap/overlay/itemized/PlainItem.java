package com.depthguru.rxmap.overlay.itemized;

import com.depthguru.rxmap.GeoPoint;

/**
 * alexander.shustanov on 03.02.17.
 */

public class PlainItem<T> implements Item<T, Void> {
    private final T type;
    private final GeoPoint coordinate;

    public PlainItem(T type, GeoPoint coordinate) {
        this.type = type;
        this.coordinate = coordinate;
    }

    @Override
    public T getType() {
        return type;
    }

    @Override
    public GeoPoint getCoordinate() {
        return coordinate;
    }

    @Override
    public Void getData() {
        return null;
    }
}
