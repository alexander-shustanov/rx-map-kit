package com.depthguru.rxmap.overlay.itemized;

import com.depthguru.rxmap.overlay.Drawer;
import com.depthguru.rxmap.overlay.Overlay;

/**
 * ItemizedOverlay
 * </p>
 * alexander.shustanov on 25.01.17.
 */
public class ItemizedOverlay<T,D> extends Overlay<ItemsBatch<T,D>> {

    private final boolean rotate;

    public ItemizedOverlay(ItemizedDataProvider<T,D> dataProvider) {
        this(dataProvider, false);
    }

    public ItemizedOverlay(ItemizedDataProvider<T,D> dataProvider, boolean rotate) {
        super(dataProvider);
        this.rotate = rotate;
    }

    @Override
    protected Drawer createDrawer(ItemsBatch<T,D> itemsBatch) {
        return new ItemizedOverlayDrawer<>(itemsBatch.getProjection(), itemsBatch, getMinZoom(), getMaxZoom(), rotate);
    }

    @Override
    public int getMinZoom() {
        return 3;
    }
}
