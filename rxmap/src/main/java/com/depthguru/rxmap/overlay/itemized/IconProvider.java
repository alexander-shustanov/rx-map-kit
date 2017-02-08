package com.depthguru.rxmap.overlay.itemized;

import android.graphics.drawable.Drawable;

import java.util.List;
import java.util.Map;

import rx.Observable;

/**
 * IconProvider
 * </p>
 * alexander.shustanov on 26.01.17.
 */

public abstract class IconProvider<T> {
    protected abstract Observable<Map<T, Drawable>> fetchIcons(List<T> types);
}
