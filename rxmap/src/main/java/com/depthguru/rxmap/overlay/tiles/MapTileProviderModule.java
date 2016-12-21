package com.depthguru.rxmap.overlay.tiles;

import rx.subjects.PublishSubject;

/**
 * MapTileProviderModule
 * </p>
 * alexander.shustanov on 15.12.16
 */
public abstract class MapTileProviderModule {
    public abstract Runnable process(MapTileState mapTileState, PublishSubject<MapTileState> loadSorter);
}
