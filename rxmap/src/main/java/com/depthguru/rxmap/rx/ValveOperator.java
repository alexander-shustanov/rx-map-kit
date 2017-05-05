package com.depthguru.rxmap.rx;

import rx.Observable;
import rx.Subscriber;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.SerialSubscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by kandalin on 28.04.17.
 */

public class ValveOperator<R, T> implements Observable.Operator<R, T> {
    private final Observable.Transformer<T, R> then_, else_;
    private final Observable<Boolean> valveObservable;
    private final boolean initial;

    public ValveOperator(Observable<Boolean> valveObservable, Observable.Transformer<T, R> then_, Observable.Transformer<T, R> else_, boolean initial) {
        this.then_ = then_;
        this.else_ = else_;
        this.valveObservable = valveObservable;
        this.initial = initial;
    }

    @Override
    public Subscriber<? super T> call(Subscriber<? super R> child) {
        Subscriber<T> subscriber = new Subscriber<T>() {

            private final Object monitor = new Object();
            private final BehaviorSubject<T> items = BehaviorSubject.create();
            private SerialSubscription subscription = new SerialSubscription();
            private boolean state = initial;

            @Override
            public void onStart() {
                super.onStart();
                add(Subscriptions.create(items::onCompleted));
                add(valveObservable
                        .distinctUntilChanged()
                        .subscribe(newState -> {
                            synchronized (monitor) {
                                if (state != newState) {
                                    state = newState;
                                    setupBranch();
                                }
                            }
                        }, this::onError, this::unsubscribe));

                setupBranch();
                add(subscription);
            }

            private synchronized void setupBranch() {
                if (state) {
                    subscription.set(items.compose(then_).subscribe(child::onNext, child::onError, () -> {}));
                } else {
                    subscription.set(items.compose(else_).subscribe(child::onNext, child::onError, () -> {}));
                }
            }

            @Override
            public void onCompleted() {
                items.onCompleted();
                unsubscribe();
            }

            @Override
            public void onError(Throwable e) {
                items.onError(e);
            }

            @Override
            public void onNext(T t) {
                items.onNext(t);
            }
        };
        child.add(subscriber);
        return subscriber;
    }
}