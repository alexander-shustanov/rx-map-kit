package com.depthguru.rxmap.overlay.tiles;

import android.graphics.drawable.Drawable;
import android.support.v4.util.Pair;

import com.depthguru.rxmap.Projection;
import com.depthguru.rxmap.rx.MapSchedulers;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.subjects.PublishSubject;

import static rx.Observable.combineLatest;
import static rx.Observable.concat;
import static rx.Observable.just;

/**
 * MapTileProviderArray
 * </p>
 * alexander.shustanov on 15.12.16
 */
public class MapTileProviderArray extends MapTileProviderBase {
    private final TileCache tileCache = new TileCache(30);
    private final TileLoader loader;

    private final PublishSubject<MapTileState> updates = PublishSubject.create();

    public MapTileProviderArray(List<MapTileProviderModule> modules) {
        loader = new TileLoader(modules, updates);
    }

    @Override
    protected Observable<MapTileBatch> processTiles(Observable<Pair<Projection, Collection<MapTile>>> listObservable) {
        Observable<?> updateCache =
                updates
                        .buffer(100, TimeUnit.MILLISECONDS, 10)
                        .filter(list -> !list.isEmpty())
                        .onBackpressureBuffer(1, null, () -> true)
                        .observeOn(MapSchedulers.tilesScheduler())
                        .doOnNext(mapTileStates -> {
                            for (MapTileState mapTileState : mapTileStates) {
                                tileCache.put(mapTileState.getMapTile(), mapTileState.getDrawable());
                            }
                        });

        return
                combineLatest(
                        concat(just(null), updateCache),
                        listObservable,
                        (aVoid, projectionListPair) -> projectionListPair)
                        .observeOn(MapSchedulers.tilesScheduler())
                        .map(projectionListPair -> {
                            Projection projection = projectionListPair.first;
                            Collection<MapTile> mapTiles = projectionListPair.second;
                            HashMap<MapTile, Drawable> tiles = new HashMap<>();
                            for (MapTile mapTile : mapTiles) {
                                Drawable drawable = tileCache.get(mapTile);
                                if (drawable != null) {
                                    tiles.put(mapTile, drawable);
                                } else {
                                    loader.schedule(new MapTileState(mapTile));
                                }
                            }
                            return new MapTileBatch(tiles, mapTiles, projection);
                        });
    }

    @Override
    protected void detach() {
        super.detach();
        loader.detach();
        updates.onCompleted();
        tileCache.detach();
    }
}
