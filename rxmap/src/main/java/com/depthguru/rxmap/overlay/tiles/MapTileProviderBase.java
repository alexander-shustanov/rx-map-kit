package com.depthguru.rxmap.overlay.tiles;

import android.util.Pair;

import com.depthguru.rxmap.Projection;
import com.depthguru.rxmap.overlay.OverlayDataProvider;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import rx.Observable;

/**
 * MapTileProviderBase
 * </p>
 * alexander.shustanov on 15.12.16
 */
public abstract class MapTileProviderBase extends OverlayDataProvider<MapTileBatch> {
    private final TilesInflater tilesInflater = new TilesInflater();

    @Override
    public Observable<MapTileBatch> fetch(Observable<Projection> projectionObservable) {
        return projectionObservable
                .throttleLast(500, TimeUnit.MILLISECONDS)
                .map(projection -> new Pair<>(projection, projection.visit(tilesInflater)))
                .compose(this::processTiles);
    }

    protected abstract Observable<MapTileBatch> processTiles(Observable<Pair<Projection, Collection<MapTile>>> listObservable);
}
