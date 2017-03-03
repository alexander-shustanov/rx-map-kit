package com.depthguru.rxmap.overlay.tiles;

import com.depthguru.rxmap.rx.MapSchedulers;

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
    private final LinkedBlockingQueue<Runnable> workQueue;
    private final Object monitor = new Object();

    private boolean inProcess = true;

    /**
     * @param modules               List of modules sorted by priority
     * @param successLoadedObserver observer observing success loadede tiles
     */
    public TileLoader(List<MapTileProviderModule> modules, Observer<MapTileState> successLoadedObserver) {
        this.modules = modules;
        workQueue = new LinkedBlockingQueue<>(4);
        executor = new ThreadPoolExecutor(2, 2, 5, TimeUnit.SECONDS, workQueue);

        Subscription subscription = loadResultConveyor
                .filter(mapTileState -> mapTileState.getDrawable() != null || modules.size() <= mapTileState.getState())
                .subscribe(successLoadedObserver);
        this.subscription.add(subscription);

        subscription = loadResultConveyor
                .filter(mapTileState -> mapTileState.getDrawable() == null && modules.size() > mapTileState.getState())
                .onBackpressureDrop(mapTileState -> {
                    mapTileState.incState();
                    loadResultConveyor.onNext(mapTileState);
                })
                .observeOn(MapSchedulers.tilesScheduler())
                .subscribe(mapTileState -> {
                    if (!schedule(mapTileState)) {
                        mapTileState.skip();
                        loadResultConveyor.onNext(mapTileState);
                    }
                });
        this.subscription.add(subscription);

        start();
    }

    /**
     * Schedules tile loading
     *
     * @param mapTileState
     */
    public boolean schedule(MapTileState mapTileState) {
        return queue.offer(mapTileState);
    }

    @Override
    public void run() {
        while (inProcess) {
            MapTileState mapTileState;
            try {
                while (queue.size() > 4) {
                    mapTileState = queue.take();
                    mapTileState.skip();
                    loadResultConveyor.onNext(mapTileState);
                }
                mapTileState = queue.take();

            } catch (InterruptedException e) {
                return;
            }

            synchronized (monitor) {
                while (workQueue.remainingCapacity() == 0) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }

            int state = mapTileState.getState();
            if (modules.size() > state) {
                MapTileProviderModule module = modules.get(state);
                executor.submit(module.process(mapTileState, loadResultConveyor, monitor));
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
