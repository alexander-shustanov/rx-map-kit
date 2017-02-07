package com.depthguru.rxmap.rx;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func2;

/**
 * ProcessWithLast
 * </p>
 * alexander.shustanov on 22.12.16
 *
 * Enables to create result using last emitted value.
 *
 * <p>
 * current
 * |
 * |
 * |
 * func (current, last)
 * |              /\
 * |\             |
 * | \___________/
 * |
 * \/
 * </p>
 */
public class ProcessWithLast<T> implements Observable.Operator<T, T> {

    private final Func2<T, T, T> func;

    private ProcessWithLast(Func2<T, T, T> func) {
        this.func = func;
    }

    public static <T> ProcessWithLast<T> of(Func2<T, T, T> mapFunction) {
        return new ProcessWithLast<>(mapFunction);
    }

    @Override
    public Subscriber<? super T> call(Subscriber<? super T> child) {
        return new Subscriber<T>() {
            T last;

            @Override
            public void onCompleted() {
                child.onCompleted();
            }

            @Override
            public void onError(Throwable e) {
                child.onError(e);
            }

            @Override
            public void onNext(T current) {
                if (last == null) {
                    last = current;
                } else {
                    last = func.call(current, last);
                }
                child.onNext(last);
            }
        };
    }
}
