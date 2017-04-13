package com.depthguru.rxmap.overlay.tiles;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Pair;

import com.depthguru.rxmap.Projection;
import com.depthguru.rxmap.TileSystem;
import com.depthguru.rxmap.rx.MapSchedulers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private final int cacheSize;

    private final TileCache tileCache;
    private final TileLoader loader;

    private final PublishSubject<MapTileState> loadedTiles = PublishSubject.create();

    public MapTileProviderArray(List<MapTileProviderModule> modules) {
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        int heightPixels = displayMetrics.heightPixels;
        int widthPixels = displayMetrics.widthPixels;
        int tileSize = TileSystem.getTileSize();

        int tilesPerWidth = (int) Math.ceil(Math.sqrt(2) * widthPixels / (double) tileSize) + 2;
        int tilesPerHeight = (int) Math.ceil(Math.sqrt(2) * heightPixels / (double) tileSize) + 2;
        cacheSize = tilesPerWidth * tilesPerHeight;

        tileCache = new TileCache(cacheSize);
        loader = new TileLoader(modules, loadedTiles);
    }

    @Override
    protected Observable<MapTileBatch> processTiles(Observable<Pair<Projection, Collection<MapTile>>> projectionWithTiles) {
        Observable<?> updateCache =
                loadedTiles
                        .buffer(100, TimeUnit.MILLISECONDS, 10)
                        .filter(list -> !list.isEmpty())
                        .onBackpressureBuffer()
                        .observeOn(MapSchedulers.tilesScheduler())
                        .doOnNext(mapTileStates -> {
                            for (MapTileState mapTileState : mapTileStates) {
                                tileCache.put(mapTileState.getMapTile(), mapTileState.getDrawable());
                            }
                        });

        projectionWithTiles = projectionWithTiles
                .observeOn(MapSchedulers.tilesScheduler());
        return
                combineLatest(
                        concat(just(null), updateCache),
                        projectionWithTiles,
                        (aVoid, projectionListPair) -> projectionListPair)
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
        loadedTiles.onCompleted();
        tileCache.detach();
    }
}
