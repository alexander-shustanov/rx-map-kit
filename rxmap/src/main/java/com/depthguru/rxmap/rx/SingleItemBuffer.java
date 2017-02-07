package com.depthguru.rxmap.rx;


import rx.BackpressureOverflow;
import rx.Observable;

/**
 * alexander.shustanov on 30.01.17.
 */

public class SingleItemBuffer<T> implements Observable.Transformer<T, T> {

    private static BackpressureOverflow.Strategy DROP_OLDEST_STRATEGY = () -> true;
    private static BackpressureOverflow.Strategy DROP_NEWEST_STRATEGY = () -> false;

    private final BackpressureOverflow.Strategy strategy;

    private static final SingleItemBuffer DROP_OLDEST = new SingleItemBuffer(DROP_OLDEST_STRATEGY);
    private static final SingleItemBuffer DROP_NEWEST = new SingleItemBuffer(DROP_NEWEST_STRATEGY);

    public static <T> SingleItemBuffer<T> dropOldest() {
        return DROP_OLDEST;
    }

    public static <T> SingleItemBuffer<T> dropNewest() {
        return DROP_NEWEST;
    }

    private SingleItemBuffer(BackpressureOverflow.Strategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public Observable<T> call(Observable<T> tObservable) {

        return tObservable.onBackpressureBuffer(1, null, strategy);
    }
}
