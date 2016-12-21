package com.depthguru.rxmap.touch;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

/**
 * TouchScroller
 * </p>
 * alexander.shustanov on 20.12.16
 */
public abstract class TouchScroller {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final VelocityTracker velocityTracker = VelocityTracker.obtain();
    private final Context context;
    private final double touchSlop;

    private MotionEvent prevEvent;
    private MotionEvent initialEvent;

    private State state;
    private Runnable detectLongPress = this::performLongPress;
    private Runnable rejectTap = this::rejectTap;

    protected TouchScroller(Context context) {
        this.context = context;
        touchSlop = (double) ViewConfiguration.get(context).getScaledTouchSlop();
    }


    public boolean onTouchEvent(MotionEvent event) {
        velocityTracker.addMovement(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                handleDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                handleMove(event);
                break;
            case MotionEvent.ACTION_UP:
                if (state == State.AWAIT_TO_TAP) {
                    onTap();
                } else if (state == State.SCROLL) {
                    velocityTracker.computeCurrentVelocity(1);
                    onFling(velocityTracker.getXVelocity(), velocityTracker.getYVelocity());
                }
                handler.removeCallbacks(detectLongPress);
                handler.removeCallbacks(rejectTap);
                break;
        }

        prevEvent = MotionEvent.obtain(event);
        return true;
    }

    private void handleDown(MotionEvent event) {
        state = State.AWAIT_TO_TAP;
        initialEvent = MotionEvent.obtain(event);
        handler.postDelayed(detectLongPress, 1000);
        handler.postDelayed(rejectTap, 500);
    }

    private void handleMove(MotionEvent event) {
        if (!state.isGesture) {
            if (isOvercomeSlop(initialEvent, event)) {
                state = State.SCROLL;
                handler.removeCallbacks(rejectTap);
                handler.removeCallbacks(detectLongPress);
            }
        } else {
            switch (state) {
                case SCROLL:
//                    case ROTATE:
                    performScroll(event);
                    break;
                case TILT:
                    break;
            }
        }
    }

    private void performScroll(MotionEvent event) {
        onScroll(prevEvent.getX() - event.getX(), prevEvent.getY() - event.getY());
    }

    private boolean isOvercomeSlop(MotionEvent a, MotionEvent b) {
        for (int i = 0; i < a.getPointerCount(); i++) {
            if (Math.abs(a.getX(i) - b.getX(i)) > touchSlop || Math.abs(a.getY(i) - b.getY(i)) > touchSlop) {
                return true;
            }
        }
        return false;
    }

    private void rejectTap() {
        if (state == State.AWAIT_TO_TAP) {
            state = State.AWAIT_TO_LONG_PRESS;
        }
    }

    private void performLongPress() {
        if (state == State.AWAIT_TO_LONG_PRESS && onLongTap()) {
            state = State.NONE;
        }
    }

    protected boolean onLongTap() {
        return false;
    }

    protected void onScroll(float dx, float dy) {
    }

    protected void onTap() {
    }

    protected void onFling(float xVelocity, float yVelocity) {
    }

    public void detach() {
        velocityTracker.recycle();
    }

    private enum State {
        NONE(false),
        AWAIT_TO_TAP(false),
        AWAIT_TO_LONG_PRESS(false),
        SCROLL(true),
        ROTATE(true),
        TILT(true);

        private final boolean isGesture;

        State(boolean isGesture) {
            this.isGesture = isGesture;
        }
    }
}
