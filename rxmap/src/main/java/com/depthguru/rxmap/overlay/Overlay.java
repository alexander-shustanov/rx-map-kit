package com.depthguru.rxmap.overlay;

import com.depthguru.rxmap.Projection;
import com.depthguru.rxmap.rx.SingleItemBuffer;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

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
                .filter(projection -> projection.getDiscreteZoom() >= getMinZoom() && projection.getDiscreteZoom() <= getMaxZoom())
                .compose(this::setupProjectionSubscribe)
                .compose(dataProvider::fetch)
                .compose(this::postProcessData)
                .map(this::createDrawer);
        return concat(emptyDrawer, realDrawer).compose(SingleItemBuffer.dropOldest()).observeOn(AndroidSchedulers.mainThread());
    }

    protected Observable<D> postProcessData(Observable<D> dataObservable) {
        return dataObservable;
    }

    protected Observable<Projection> setupProjectionSubscribe(Observable<Projection> projectionObservable) {
        return projectionObservable;
    }

    protected abstract Drawer createDrawer(D d);

    protected void detach() {
        dataProvider.detach();
    }

    public int getMinZoom() {
        return 0;
    }

    public int getMaxZoom() {
        return 23;
    }

}
