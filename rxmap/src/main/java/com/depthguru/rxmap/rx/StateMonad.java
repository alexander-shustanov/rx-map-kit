package com.depthguru.rxmap.rx;

import android.util.Pair;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func2;


public class StateMonad<T, R, S> implements Observable.Operator<R, T> {

    private final Func2<T, S, Pair<S, R>> func;
    private final S initialState;

    private StateMonad(Func2<T, S, Pair<S, R>> func, S initialState) {
        this.func = func;
        this.initialState = initialState;
    }

    public static <T, R, S> StateMonad<T, R, S> create(Func2<T, S, Pair<S, R>> func, S initialState) {
        return new StateMonad<>(func, initialState);
    }

    public static <T> StateMonad<T, T, T> create(Func2<T, T, T> func) {
        return new StateMonad<>((t, state) -> {
            if (state == null) {
                return new Pair<T, T>(t, t);
            }
            T val = func.call(t, state);
            return new Pair<>(val, val);
        }, null);
    }

    @Override
    public Subscriber<? super T> call(Subscriber<? super R> subscriber) {
        return new StateSubscriber<>(subscriber, func, initialState);
    }

    private static class StateSubscriber<R, T, S> extends Subscriber<T> {

        private Subscriber<? super R> child;
        private Func2<T, S, Pair<S, R>> func;
        private S state;

        public StateSubscriber(Subscriber<? super R> child, Func2<T, S, Pair<S, R>> func, S initialState) {
            this.child = child;
            this.func = func;
            this.state = initialState;

            child.add(this);
        }

        @Override
        public void onCompleted() {
            child.onCompleted();
        }

        @Override
        public void onError(Throwable e) {
            child.onError(e);
        }

        @Override
        public void onNext(T t) {
            Pair<S, R> ret = func.call(t, state);
            state = ret.first;
            child.onNext(ret.second);
        }
    }
}
