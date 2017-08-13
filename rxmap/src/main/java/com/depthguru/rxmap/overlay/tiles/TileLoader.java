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
import java.util.concurrent.atomic.AtomicLong;

import rx.Observable;
import rx.Producer;
import rx.Subscriber;
import rx.internal.operators.BackpressureUtils;

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
        cacheSize = tilesPerWidth * tilesPerHeight * 2;

        tileCache = new TileCache(cacheSize);
    }

    /**
     * Clear resources such as threads and subscriptions
     */
    public void detach() {

    }

    public Observable<TileDrawable> load(Collection<MapTile> mapTiles, Projection projection) {
        return Observable.<TileDrawable>unsafeCreate(subscriber ->
                subscriber.setProducer(new TilesProducer(subscriber, mapTiles, projection))
        ).subscribeOn(MapSchedulers.tilesScheduler());
    }

    private class TilesProducer extends AtomicLong implements Producer {

        private final int discreteZoom;
        private final Iterator<MapTile> mapTiles;
        private final Iterator<MapTile> toLoad;
        private final Projection projection;
        private Subscriber<? super TileDrawable> child;

        private TilesProducer(Subscriber<? super TileDrawable> child, Collection<MapTile> mapTiles, Projection projection) {
            this.child = child;
            List<MapTile> toLoad = new ArrayList<>();
            for (MapTile mapTile : mapTiles) {
                Drawable tile = tileCache.get(mapTile);
                if (tile == null) {
                    toLoad.add(mapTile);
                }
            }
            mapTiles.removeAll(toLoad);
            this.toLoad = toLoad.iterator();
            this.mapTiles = mapTiles.iterator();
            this.projection = projection;
            discreteZoom = projection.getDiscreteZoom();
        }

        @Override
        public void request(long requestedAmount) {
            if (get() == Long.MAX_VALUE) {
                // already started with fast-path
                return;
            }
            if (requestedAmount == Long.MAX_VALUE && compareAndSet(0L, Long.MAX_VALUE)) {
                // fast-path without backpressure
                fastPath();
            } else if (requestedAmount > 0L) {
                long c = BackpressureUtils.getAndAddRequest(this, requestedAmount);
                if (c == 0L) {
                    // backpressure is requested
                    slowPath(requestedAmount);
                }
            }
        }

        private void slowPath(long requestedAmount) {
            long emitted = 0L;
            final Subscriber<? super TileDrawable> childSubscriber = this.child;
            int discreteZoom = this.discreteZoom;

            for (; ; ) {
                while (emitted != requestedAmount && (mapTiles.hasNext() || toLoad.hasNext())) {
                    if (childSubscriber.isUnsubscribed()) {
                        return;
                    }

                    if (mapTiles.hasNext()) {
                        MapTile mapTile = mapTiles.next();
                        Drawable tile = tileCache.get(mapTile);
                        if (tile != null) {
                            childSubscriber.onNext(new TileDrawable(mapTile, tile, discreteZoom));
                            emitted++;
                        }
                    } else {
                        MapTile tile = toLoad.next();
                        Iterator<MapTileProviderModule> moduleIterator = modules.iterator();

                        do {
                            Drawable drawable = moduleIterator.next().process(tile);
                            if (drawable != null) {
                                tileCache.put(tile, drawable);
                                childSubscriber.onNext(new TileDrawable(tile, drawable, discreteZoom));
                                emitted++;
                                break;
                            }
                        } while (moduleIterator.hasNext() && !child.isUnsubscribed());
                    }
                }

                if (childSubscriber.isUnsubscribed()) {
                    return;
                }

                if (!mapTiles.hasNext() && !toLoad.hasNext()) {
                    childSubscriber.onCompleted();
                    return;
                }

                requestedAmount = get();

                if (requestedAmount == emitted) {
                    requestedAmount = addAndGet(-emitted);
                    if (requestedAmount == 0L) {
                        break;
                    }
                    emitted = 0L;
                }
            }
        }

        private void fastPath() {
            int discreteZoom = projection.getDiscreteZoom();

            while (mapTiles.hasNext()) {
                MapTile mapTile = mapTiles.next();
                Drawable tile = tileCache.get(mapTile);
                if (child.isUnsubscribed()) {
                    child.onCompleted();
                    return;
                }
                if (tile != null) {
                    child.onNext(new TileDrawable(mapTile, tile, discreteZoom));
                }
            }

            if (!toLoad.hasNext()) {
                child.onCompleted();
                return;
            }

            do {
                MapTile tile = toLoad.next();
                Iterator<MapTileProviderModule> moduleIterator = modules.iterator();

                do {
                    Drawable drawable = moduleIterator.next().process(tile);
                    if (drawable != null) {
                        tileCache.put(tile, drawable);
                        child.onNext(new TileDrawable(tile, drawable, discreteZoom));
                        break;
                    }
                } while (moduleIterator.hasNext() && !child.isUnsubscribed());

            } while (toLoad.hasNext() && !child.isUnsubscribed());

            child.onCompleted();
        }
    }
}
