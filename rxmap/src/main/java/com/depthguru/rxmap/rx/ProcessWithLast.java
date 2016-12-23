package com.depthguru.rxmap.rx;

import rx.Observable;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

import static rx.Observable.concat;
import static rx.Observable.just;
import static rx.Observable.zip;

/**
 * ProcessWithLast
 * </p>
 * alexander.shustanov on 22.12.16
 */
public class ProcessWithLast<T> implements Observable.Transformer<T, T> {

    private final Func2<T, T, T> mapFunction;

    private ProcessWithLast(Func2<T, T, T> mapFunction) {
        this.mapFunction = mapFunction;
    }

    public static <T> ProcessWithLast<T> of(Func2<T, T, T> mapFunction) {
        return new ProcessWithLast<>(mapFunction);
    }

    @Override
    public Observable<T> call(Observable<T> observable) {
        Observable<T> current = observable;
        PublishSubject<T> last = PublishSubject.create();
        return zip(current, concat(just(null), last), mapFunction)
                .doOnNext(last::onNext)
                .doOnUnsubscribe(last::onCompleted);
    }
}
