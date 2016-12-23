package com.depthguru.rxmap.overlay;

import com.depthguru.rxmap.Projection;

import rx.Observable;

import static rx.Observable.concat;
import static rx.Observable.just;

/**
 * Overlay
 * </p>
 * alexander.shustanov on 15.12.16
 */
public abstract class Overlay<D> {

    private final OverlayDataProvider<D> dataProvider;

    protected Overlay(OverlayDataProvider<D> dataProvider) {
        this.dataProvider = dataProvider;
    }

    final Observable<Drawer> createDrawer(Observable<Projection> projectionObservable) {
        Observable<Drawer> emptyDrawer = just(Drawer.EMPTY_DRAWER);
        Observable<Drawer> realDrawer = projectionObservable
                .compose(this::setupProjectionSubscribe)
                .compose(dataProvider::fetch)
                .compose(this::dataPostPrecess)
                .map(this::createDrawer);
        return concat(emptyDrawer, realDrawer);
    }

    protected Observable<D> dataPostPrecess(Observable<D> dataObservable) {
        return dataObservable;
    }

    protected Observable<Projection> setupProjectionSubscribe(Observable<Projection> projectionObservable) {
        return projectionObservable;
    }

    protected abstract Drawer createDrawer(D d);

    protected void detach() {
        dataProvider.detach();
    }
}
