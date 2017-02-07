package com.depthguru.rxmap.overlay.itemized;

import com.depthguru.rxmap.overlay.Drawer;
import com.depthguru.rxmap.overlay.Overlay;

/**
 * ItemizedOverlay
 * </p>
 * alexander.shustanov on 25.01.17.
 */
public class ItemizedOverlay<T,D> extends Overlay<ItemsBatch<T,D>> {

    public ItemizedOverlay(ItemizedDataProvider<T,D> dataProvider) {
        super(dataProvider);
    }

    @Override
    protected Drawer createDrawer(ItemsBatch<T,D> itemsBatch) {
        return new ItemizedOverlayDrawer<T, D>(itemsBatch.getProjection(), itemsBatch, getMinZoom(), getMaxZoom());
    }

    @Override
    public int getMinZoom() {
        return 3;
    }
}
