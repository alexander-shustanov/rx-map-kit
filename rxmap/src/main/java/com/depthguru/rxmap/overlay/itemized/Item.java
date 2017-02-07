package com.depthguru.rxmap.overlay.itemized;

import com.depthguru.rxmap.GeoPoint;

/**
 * alexander.shustanov on 02.02.17.
 */

public interface Item<T, D> {
    T getType();

    GeoPoint getCoordinate();

    D getData();
}
