package com.depthguru.rxmap.overlay.tiles;

import com.depthguru.rxmap.rx.MapSchedulers;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import rx.Observer;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * TileLoader
 * </p>
 * alexander.shustanov on 19.12.16
 */
public class TileLoader extends Thread {
    private final CompositeSubscription subscription = new CompositeSubscription();
    private final BlockingQueue<MapTileState> queue = new LinkedBlockingQueue<>(4);
    private final PublishSubject<MapTileState> loadResultConveyor = PublishSubject.create();
    private final List<MapTileProviderModule> modules;
    private final ExecutorService executor;

    private boolean inProcess = true;

    /**
     * @param modules List of modules sorted by priority
     * @param successLoaded observer observing success loadede tiles
     */
    public TileLoader(List<MapTileProviderModule> modules, Observer<MapTileState> successLoaded) {
        this.modules = modules;
        executor = new ThreadPoolExecutor(2, 2, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<>(20));

        Subscription subscription = loadResultConveyor
                .filter(mapTileState -> mapTileState.getDrawable() != null)
                .doOnNext(mapTileState -> {
                    Iterator<MapTileState> iterator = queue.iterator();
                    while (iterator.hasNext()) {
                        MapTileState next = iterator.next();
                        if(next.getMapTile().equals(mapTileState.getMapTile())) {
                            iterator.remove();
                        }
                    }
                })
                .subscribe(successLoaded);
        this.subscription.add(subscription);

        subscription = loadResultConveyor
                .filter(mapTileState -> mapTileState.getDrawable() == null)
                .onBackpressureLatest()
                .observeOn(MapSchedulers.tilesScheduler())
                .subscribe(this::schedule);
        this.subscription.add(subscription);

        start();
    }

    /**
     * Schedules tile loading
     * @param mapTileState
     */
    public void schedule(MapTileState mapTileState) {
        queue.offer(mapTileState);
    }

    @Override
    public void run() {
        while (inProcess) {
            MapTileState mapTileState;
            try {
                mapTileState = queue.take();
            } catch (InterruptedException e) {
                return;
            }

            int state = mapTileState.getState();
            if (modules.size() > state) {
                MapTileProviderModule module = modules.get(state);
                try {
                    executor.submit(module.process(mapTileState, loadResultConveyor));
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * Clear resources such as threads and subscriptions
     */
    public void detach() {
        loadResultConveyor.onCompleted();
        subscription.unsubscribe();
        inProcess = false;
        interrupt();
    }
}
