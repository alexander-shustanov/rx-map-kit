package com.depthguru.rxmap.overlay.itemized;

import com.depthguru.rxmap.overlay.Drawer;
import com.depthguru.rxmap.overlay.Overlay;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * ItemizedOverlay
 * </p>
 * alexander.shustanov on 25.01.17.
 */
public class ItemizedOverlay<MarkerType, ItemType> extends Overlay<ItemsBatch<MarkerType, ItemType>> {

    private final boolean rotate;

    private PublishSubject<ItemType> tapItems = PublishSubject.create();

    public ItemizedOverlay(ItemizedDataProvider<MarkerType, ItemType> dataProvider) {
        this(dataProvider, false);
    }

    public ItemizedOverlay(ItemizedDataProvider<MarkerType, ItemType> dataProvider, boolean rotate) {
        super(dataProvider);
        this.rotate = rotate;
    }

    @Override
    protected Drawer createDrawer(ItemsBatch<MarkerType, ItemType> itemsBatch) {
        return new ItemizedOverlayDrawer<>(itemsBatch.getProjection(), itemsBatch, getMinZoom(), getMaxZoom(), rotate, tapItems);
    }

    @Override
    public int getMinZoom() {
        return 3;
    }

    public Observable<ItemType> getTapItemsObservable() {
        return tapItems;
    }
}
