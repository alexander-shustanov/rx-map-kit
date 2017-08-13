package com.depthguru.rxmap.overlay.tiles;

import android.util.Pair;

import com.depthguru.rxmap.Projection;
import com.depthguru.rxmap.rx.MapSchedulers;
import com.depthguru.rxmap.rx.SingleItemBuffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Functions;

/**
 * MapTileProviderArray
 * </p>
 * alexander.shustanov on 15.12.16
 */
public class MapTileProviderArray extends MapTileProviderBase {
    private final TileLoader loader;


    public MapTileProviderArray(List<MapTileProviderModule> modules) {
        loader = new TileLoader(modules);
    }

    @Override
    protected Observable<MapTileBatch> processTiles(Observable<Pair<Projection, Collection<MapTile>>> projectionWithTiles) {
        return
                projectionWithTiles
                        .observeOn(MapSchedulers.tilesBatchAssembleScheduler())
                        .switchMap(projectionCollectionPair ->
                                loader
                                        .load(new ArrayList<>(projectionCollectionPair.second), projectionCollectionPair.first)
                                        .observeOn(MapSchedulers.tilesBatchAssembleScheduler())
                                        .scan(new ArrayList<TileDrawable>(), (tileDrawables, tileDrawable) -> {
                                            tileDrawables.add(tileDrawable);
                                            return tileDrawables;
                                        })
                                        .map(ArrayList::new)
                                        .throttleLast(300, TimeUnit.MILLISECONDS)
                                        .filter(tileDrawables -> !tileDrawables.isEmpty())
                                        .map(tileDrawables -> new MapTileBatch(tileDrawables, projectionCollectionPair.second, projectionCollectionPair.first))
                                        .compose(SingleItemBuffer.dropOldest())
                                        .observeOn(MapSchedulers.tilesBatchAssembleScheduler())
                        );
    }

    @Override
    protected void detach() {
        super.detach();
        loader.detach();
    }
}
