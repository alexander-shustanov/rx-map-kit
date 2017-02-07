package com.depthguru.rxmap.rx;

import com.depthguru.rxmap.GeoPoint;
import com.depthguru.rxmap.Projection;

import rx.Observable;
import rx.Subscriber;

/**
 * alexander.shustanov on 26.01.17.
 * Returns only {@link Projection}s, that are father then half screen from each other, or that have different zoom.
 */

public class DistinctProjections implements Observable.Operator<Projection, Projection> {

    private static final DistinctProjections INSTANCE = new DistinctProjections();

    public static DistinctProjections get() {
        return INSTANCE;
    }

    @Override
    public Subscriber<? super Projection> call(Subscriber<? super Projection> child) {
        return new Subscriber<Projection>() {
            Projection previousProjection;

            @Override
            public void onCompleted() {
                child.onCompleted();
            }

            @Override
            public void onError(Throwable e) {
                child.onError(e);
            }

            @Override
            public void onNext(Projection projection) {
                if(previousProjection == null) {
                    previousProjection = projection;
                    child.onNext(projection);
                } else {
                    if(previousProjection.getDiscreteZoom() != projection.getDiscreteZoom()) {
                        previousProjection = projection;
                        child.onNext(projection);
                        return;
                    }

                    GeoPoint newCenter = projection.getCenter();
                    GeoPoint prevCenter = previousProjection.getCenter();

                    int deltaLat = Math.abs(newCenter.getLatitudeE6() - prevCenter.getLatitudeE6());
                    if (deltaLat > previousProjection.getBounds().getHeight()) {
                        previousProjection = projection;
                        child.onNext(projection);
                        return;
                    }

                    int deltaLon = Math.abs(newCenter.getLongitudeE6() - prevCenter.getLongitudeE6());
                    if (deltaLon > previousProjection.getBounds().getWidth()) {
                        previousProjection = projection;
                        child.onNext(projection);
                    }
                }

            }
        };
    }
}
