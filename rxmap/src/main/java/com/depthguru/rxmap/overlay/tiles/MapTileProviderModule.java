package com.depthguru.rxmap.overlay.tiles;

import rx.Observer;

/**
 * MapTileProviderModule
 * </p>
 * alexander.shustanov on 15.12.16
 */
public abstract class MapTileProviderModule {

    final Runnable process(MapTileState mapTileState, Observer<MapTileState> loadSorter, Object monitor) {
        LoadTask task = createTask(mapTileState, loadSorter);
        task.monitor = monitor;
        return task;
    }

    protected abstract LoadTask createTask(MapTileState mapTileState, Observer<MapTileState> loadSorter);


    public static abstract class LoadTask implements Runnable {
        private Object monitor;

        @Override
        public void run() {
            try {
                load();
            } finally {
                synchronized (monitor) {
                    monitor.notifyAll();
                }
            }
        }

        protected abstract void load();
    }
}
