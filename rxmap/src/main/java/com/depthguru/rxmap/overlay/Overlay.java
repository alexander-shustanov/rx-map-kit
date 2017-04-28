package com.depthguru.rxmap.overlay;

import com.depthguru.rxmap.Projection;
import com.depthguru.rxmap.rx.SingleItemBuffer;
import com.depthguru.rxmap.rx.ValveOperator;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;

import static rx.Observable.concat;
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
        Observable<Drawer> emptyDrawer = just(Drawer.EMPTY_DRAWER);

        Observable<Drawer> realDrawer = projectionObservable
                .lift(new ValveOperator<>(enabled,
                        projectionObservable_ ->
                                projectionObservable_.filter(projection -> projection.getDiscreteZoom() >= getMinZoom() && projection.getDiscreteZoom() <= getMaxZoom())
                                        .compose(this::setupProjectionSubscribe)
                                        .compose(dataProvider::fetch)
                                        .compose(this::postProcessData)
                                        .map(this::createDrawer),
                        projectionObservable_ -> projectionObservable_.first().map(projection -> Drawer.EMPTY_DRAWER),
                        true
                ));

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

    public boolean isEnabled() {
        return enabled.getValue();
    }

    public void setEnabled(boolean enabled) {
        this.enabled.onNext(enabled);
    }
}
