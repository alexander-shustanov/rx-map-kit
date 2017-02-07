package com.depthguru.rxmap.overlay.itemized;

import android.support.annotation.NonNull;

import com.depthguru.rxmap.BoundingBoxE6;
import com.depthguru.rxmap.Projection;
import com.depthguru.rxmap.overlay.OverlayDataProvider;
import com.depthguru.rxmap.rx.DistinctProjections;

import java.util.List;

import rx.Observable;

import static rx.Observable.switchOnNext;

/**
 * ItemizedDataProvider
 * </p>
 * alexander.shustanov on 25.01.17.
 */

public abstract class ItemizedDataProvider<T, D> extends OverlayDataProvider<ItemsBatch<T, D>> {
    private final IconProvider<T> iconProvider;

    protected ItemizedDataProvider(IconProvider<T> iconProvider) {
        this.iconProvider = iconProvider;
    }

    @Override
    public Observable<ItemsBatch<T, D>> fetch(Observable<Projection> projectionObservable) {

        Observable<Projection> distinctProjection = //Projections with different zooms and far away from each other
                projectionObservable.lift(DistinctProjections.get());

        Observable<Observable<ItemsBatch<T, D>>> batches =
                distinctProjection
                        .map(Projection::getBounds)
                        .map(this::expandBounds) //Expand bounds to receive data for bigger area
                        .map(this::fetchByBounds)
                        .zipWith(
                                distinctProjection,
                                (itemsObservable, projection) -> itemsObservable
                                        .scan((accumulator, newValue) -> {
                                            accumulator.addAll(newValue);
                                            return accumulator;
                                        }).map(items -> new ItemsBatch<>(items, projection))
                        );

        return switchOnNext(batches) //Receive updates about icons until new projection has been received
                .flatMap(iconProvider::fetchIcons);

    }

    @NonNull
    private BoundingBoxE6 expandBounds(BoundingBoxE6 boundingBoxE6) {
        int extraWidth = boundingBoxE6.getWidth() / 2;
        int extraHeight = boundingBoxE6.getHeight() / 2;
        return new BoundingBoxE6(
                boundingBoxE6.getLonWestE6() - extraWidth,
                boundingBoxE6.getLatNorthE6() + extraHeight,
                boundingBoxE6.getLonEastE6() + extraHeight,
                boundingBoxE6.getLatSouthE6() - extraHeight
        );
    }

    /**
     * Creates observable of @code{List<Item<T, D>>} items to show on map.
     * All items emitted by it will be shown for concrete projection.
     * @param boundingBoxE6
     * @return
     */
    @NonNull
    protected abstract Observable<List<Item<T, D>>> fetchByBounds(BoundingBoxE6 boundingBoxE6);
}