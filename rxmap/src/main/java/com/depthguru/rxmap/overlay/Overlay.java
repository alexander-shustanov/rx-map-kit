package com.depthguru.rxmap.overlay;

import android.util.Pair;

import com.depthguru.rxmap.Projection;
import com.depthguru.rxmap.rx.SingleItemBuffer;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;

import static rx.Observable.just;

/**
 * Overlay
 * </p>
 * alexander.shustanov on 15.12.16
 */
public abstract class Overlay<D> {

    private final OverlayDataProvider<D> dataProvider;

    private final BehaviorSubject<Boolean> enabled = BehaviorSubject.create(true);

    protected Overlay(OverlayDataProvider<D> dataProvider) {
        this.dataProvider = dataProvider;
    }

    final Observable<Drawer> createDrawer(Observable<Projection> projectionObservable) {
        Observable<Projection> storeLastProjection = projectionObservable.replay(1).autoConnect();

        return enabled
                .distinctUntilChanged()
                .withLatestFrom(just(storeLastProjection), Pair::new)
                .switchMap(booleanProjectionPair -> {
                    if (booleanProjectionPair.first) {
                        return booleanProjectionPair.second
                                .filter(projection -> projection.getDiscreteZoom() >= getMinZoom() && projection.getDiscreteZoom() <= getMaxZoom())
                                .compose(this::setupProjectionSubscribe)
                                .compose(dataProvider::fetch)
                                .compose(this::postProcessData)
                                .map(this::createDrawer);
                    } else {
                        return just(Drawer.EMPTY_DRAWER);
                    }
                })
                .compose(SingleItemBuffer.dropOldest())
                .observeOn(AndroidSchedulers.mainThread())
                .startWith(Drawer.EMPTY_DRAWER);
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
        enabled.onCompleted();
    }

    public int getMinZoom() {
        return 0;
    }

    public int getMaxZoom() {
        return 23;
    }

    public boolean isEnabled() {
        return enabled.getValue();
    }

    public void setEnabled(boolean enabled) {
        this.enabled.onNext(enabled);
    }
}
