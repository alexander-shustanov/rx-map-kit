package com.depthguru.rxmap.overlay;

import android.graphics.Canvas;

import com.depthguru.rxmap.Projection;
import com.depthguru.rxmap.RxMapView;
import com.depthguru.rxmap.rx.MapSchedulers;
import com.depthguru.rxmap.rx.SingleItemBuffer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

import static rx.Observable.combineLatest;

/**
 * OverlayManager
 * </p>
 * alexander.shustanov on 15.12.16
 */
public class OverlayManager {
    private final LinkedHashSet<Overlay> overlays = new LinkedHashSet<>();
    private Subscription drawersSubscription = new CompositeSubscription();
    private List<Drawer> drawers = new ArrayList<>();
    private Observable<Projection> projectionObservable;
    private RxMapView mapView;

    public OverlayManager(Observable<Projection> projectionObservable, RxMapView mapView) {
        this.projectionObservable = projectionObservable;
        this.mapView = mapView;
        add(new BackgroundOverlay());
    }

    public boolean add(Overlay overlay) {
        if (overlays.add(overlay)) {
            resubscribe();
            return true;
        }
        return false;
    }

    private void resubscribe() {
        drawersSubscription.unsubscribe();
        List<Observable<Drawer>> drawerObservables = new ArrayList<>();
        Observable<Projection> projectionObservable = this.projectionObservable
                .compose(SingleItemBuffer.dropOldest())
                .observeOn(MapSchedulers.overlayScheduler());
        for (Overlay overlay : overlays) {
            drawerObservables.add(projectionObservable
                    .compose(overlay::createDrawer));
        }
        drawersSubscription = combineLatest(drawerObservables, args -> args)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(objects -> {
                    drawers.clear();
                    for (Object drawer : objects) {
                        drawers.add((Drawer) drawer);
                    }
                    mapView.invalidate();
                });
    }

    public void draw(Canvas canvas, Projection projection) {
        for (Drawer drawer : drawers) {
            drawer.draw(canvas, projection);
        }
    }

    public boolean remove(Overlay overlay) {
        if (overlays.remove(overlay)) {
            overlay.detach();
            return true;
        }
        return false;
    }

    public int size() {
        return drawers.size();
    }

    public void detach() {
        for (Overlay overlay : overlays) {
            overlay.detach();
        }
        overlays.clear();
        drawersSubscription.unsubscribe();
    }
}
