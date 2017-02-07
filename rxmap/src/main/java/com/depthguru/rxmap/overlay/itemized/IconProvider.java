package com.depthguru.rxmap.overlay.itemized;

import rx.Observable;

/**
 * IconProvider
 * </p>
 * alexander.shustanov on 26.01.17.
 */

public abstract class IconProvider<T> {
    protected abstract <D> Observable<ItemsBatch<T, D>> fetchIcons(ItemsBatch<T, D> batchWithoutIcons);
}
