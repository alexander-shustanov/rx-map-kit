package com.depthguru.rxmap.rx;

import android.os.HandlerThread;
import android.support.annotation.NonNull;

import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;

/**
 * MapSchedulers
 * </p>
 * alexander.shustanov on 15.12.16
 */
public class MapSchedulers {

    private static Scheduler tilesBatchAssembleScheduler;
    private static Scheduler tilesScheduler;
    private static Scheduler overlayScheduler;

    public static Scheduler tilesScheduler() {
        if (tilesScheduler == null) {
            tilesScheduler = createHandlerScheduler(new HandlerThread("tilesScheduler"));
        }
        return tilesScheduler;
    }

    public static Scheduler overlayScheduler() {
        if (overlayScheduler == null) {
            overlayScheduler = createHandlerScheduler(new HandlerThread("overlayScheduler"));
        }
        return overlayScheduler;
    }

    public static Scheduler tilesBatchAssembleScheduler() {
        if (tilesBatchAssembleScheduler == null) {
            tilesBatchAssembleScheduler = createHandlerScheduler(new HandlerThread("tilesBatchAssembleScheduler"));
        }
        return tilesBatchAssembleScheduler;
    }

    @NonNull
    private static Scheduler createHandlerScheduler(HandlerThread name) {
        HandlerThread tilesHandlerThread = name;
        tilesHandlerThread.start();
        return AndroidSchedulers.from(tilesHandlerThread.getLooper());
    }
}
