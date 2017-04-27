package com.depthguru.rxmap.overlay.tiles;

import android.graphics.drawable.Drawable;

/**
 * MapTileProviderModule
 * </p>
 * alexander.shustanov on 15.12.16
 */
public abstract class MapTileProviderModule {

    public abstract Drawable process(MapTile mapTile);
}
