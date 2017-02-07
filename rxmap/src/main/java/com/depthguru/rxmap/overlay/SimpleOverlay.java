package com.depthguru.rxmap.overlay;

import com.depthguru.rxmap.Projection;

import rx.Observable;

/**
 * SimpleOverlay
 * </p>
 * alexander.shustanov on 16.12.16
 */
public abstract class SimpleOverlay extends Overlay<Projection> {
    public SimpleOverlay() {
        super(new ProjectionOverlayDataProvider());
    }

    private static class ProjectionOverlayDataProvider extends OverlayDataProvider<Projection> {
        @Override
        public Observable<Projection> fetch(Observable<Projection> projectionObservable) {
            return projectionObservable;
        }
    }
}
