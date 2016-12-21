package com.depthguru.rxmap.rx;

import android.os.HandlerThread;
import android.os.Looper;

import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;

/**
 * MapSchedulers
 * </p>
 * alexander.shustanov on 15.12.16
 */
public class MapSchedulers {

    private static Scheduler tilesScheduler;
    private static Looper tileLooper;

    public static Scheduler tilesScheduler() {
        if (tilesScheduler == null) {
            HandlerThread tilesHandlerThread = new HandlerThread("tilesScheduler");
            tilesHandlerThread.start();
            tileLooper = tilesHandlerThread.getLooper();
            tilesScheduler = AndroidSchedulers.from(tileLooper);
        }
        return tilesScheduler;
    }

    public static Looper getTileLooper() {
        if (tileLooper == null) {
            tilesScheduler();
        }
        return tileLooper;
    }
}
