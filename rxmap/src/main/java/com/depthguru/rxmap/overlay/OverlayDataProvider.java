package com.depthguru.rxmap.overlay;

import com.depthguru.rxmap.Projection;

import rx.Observable;

/**
 * OverlayDataProvider
 * </p>
 * alexander.shustanov on 16.12.16
 */
public abstract class OverlayDataProvider<D> {
    public abstract Observable<D> fetch(Observable<Projection> projectionObservable);

    protected void detach() {}
}
