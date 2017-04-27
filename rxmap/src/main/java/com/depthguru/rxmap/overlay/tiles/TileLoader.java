package com.depthguru.rxmap.overlay.tiles;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import com.depthguru.rxmap.Projection;
import com.depthguru.rxmap.TileSystem;
import com.depthguru.rxmap.rx.MapSchedulers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import rx.Observable;

/**
 * TileLoader
 * </p>
 * alexander.shustanov on 19.12.16
 */
public class TileLoader {
    private final List<MapTileProviderModule> modules;
    private final TileCache tileCache;
    private final int cacheSize;


    /**
     * @param modules List of modules sorted by priority
     */
    public TileLoader(List<MapTileProviderModule> modules) {
        this.modules = modules;

        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        int heightPixels = displayMetrics.heightPixels;
        int widthPixels = displayMetrics.widthPixels;
        int tileSize = TileSystem.getTileSize();

        int tilesPerWidth = (int) Math.ceil(Math.sqrt(2) * widthPixels / (double) tileSize) + 2;
        int tilesPerHeight = (int) Math.ceil(Math.sqrt(2) * heightPixels / (double) tileSize) + 2;
        cacheSize = tilesPerWidth * tilesPerHeight;

        tileCache = new TileCache(cacheSize);
    }

    /**
     * Clear resources such as threads and subscriptions
     */
    public void detach() {

    }

    public Observable<TileDrawable> load(Collection<MapTile> mapTiles, Projection projection) {
        return Observable.<TileDrawable>create(subscriber -> {
            int discreteZoom = projection.getDiscreteZoom();

            List<MapTile> toLoad = new ArrayList<>();

            for (MapTile mapTile : mapTiles) {
                Drawable tile = tileCache.get(mapTile);
                if (subscriber.isUnsubscribed()) {
                    subscriber.onCompleted();
                    return;
                }
                if (tile != null) {
                    subscriber.onNext(new TileDrawable(mapTile, tile, discreteZoom));
                } else {
                    toLoad.add(mapTile);
                }
            }

            if (toLoad.isEmpty() || modules.isEmpty()) {
                subscriber.onCompleted();
                return;
            }

            Iterator<MapTile> mapTileIterator = toLoad.iterator();
            do {
                MapTile tile = mapTileIterator.next();
                Iterator<MapTileProviderModule> moduleIterator = modules.iterator();

                do {
                    Drawable drawable = moduleIterator.next().process(tile);
                    if (drawable != null) {
                        tileCache.put(tile, drawable);
                        subscriber.onNext(new TileDrawable(tile, drawable, discreteZoom));
                        break;
                    }
                } while (moduleIterator.hasNext() && !subscriber.isUnsubscribed());

            } while (mapTileIterator.hasNext() && !subscriber.isUnsubscribed());

            subscriber.onCompleted();
        }).subscribeOn(MapSchedulers.tilesScheduler());
    }
}
