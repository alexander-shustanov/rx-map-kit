package com.depthguru.rxmap.touch;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * TouchScroller
 * </p>
 * alexander.shustanov on 20.12.16
 */
public abstract class TouchScroller {
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final float touchSlop;
    private final float flingVelocitySlop;

    private final PointF reuse = new PointF();

    private final TouchState state;
    private final Runnable detectLongPress = this::performLongPress;
    private final Runnable rejectTap;

    private PointF tapToken;

    protected TouchScroller(Context context) {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        flingVelocitySlop = viewConfiguration.getScaledMinimumFlingVelocity() * Resources.getSystem().getDisplayMetrics().density / 1000f;
        touchSlop = viewConfiguration.getScaledTouchSlop();
        state = new TouchState(touchSlop, flingVelocitySlop);
        rejectTap = state::rejectTap;
    }


    public boolean onTouchEvent(MotionEvent event) {
        state.trackVelocity(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                handleActionDown(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                handlerPointerDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                handleActionMove(event);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                handlePointerUp(event);
                break;
            case MotionEvent.ACTION_UP:
                handleActionUp(event);
                break;
        }

        state.savePrevEvent(event);
        return true;
    }

    private void handlePointerUp(MotionEvent event) {
        state.onPointerUp(event);
    }

    private void handlerPointerDown(MotionEvent event) {
        state.onPointerDown(event);
    }

    private void handleActionUp(MotionEvent event) {
        if (state.canPerformTap()) {
            if(tapToken == null) {
                tapToken = new PointF(event.getX(), event.getY());
                postTap(event);
            } else {
                if (Math.abs(tapToken.x - event.getX()) < touchSlop*2 && Math.abs(tapToken.y - event.getY()) < touchSlop*2) {
                    handler.removeCallbacks(null, tapToken);
                    performDoubleTap(event);
                } else {
                    tapToken.set(event.getX(), event.getY());
                    postTap(event);
                }
            }
        } else if (state.canPerformFling()) {
            state.computeVelocity(reuse);
            if (state.doFling()) {
                onFling(reuse.x, reuse.y);
            }
        }
        handler.removeCallbacks(detectLongPress);
        handler.removeCallbacks(rejectTap);
    }

    private void performDoubleTap(MotionEvent event) {
        tapToken = null;
        onDoubleTap(event.getX(), event.getY());
    }

    private void postTap(MotionEvent event) {
        handler.postAtTime(() -> performTap(event.getX(), event.getY()), tapToken, SystemClock.uptimeMillis() + 500);
    }

    private void handleActionDown(MotionEvent event) {
        state.onDown(event);
        handler.postDelayed(detectLongPress, 1000);
        handler.postDelayed(rejectTap, 500);
        onStartMotion();
    }

    private void handleActionMove(MotionEvent event) {
        if (!state.isInMove()) {
            if (state.tryStartScroll(event)) {
                handler.removeCallbacks(rejectTap);
                handler.removeCallbacks(detectLongPress);
            }
        } else {
            if (state.inScroll()) {
                state.computeScroll(event, reuse);
                performScroll(reuse);
            }
            if (state.inZoom()) {
                float zoom = state.computeZoom(event, reuse);
                performZoom(zoom, reuse);
            }
        }
    }

    private void performZoom(float zoom, PointF pivot) {
        onZoom(zoom, pivot);
    }

    protected void onStartMotion() {
    }

    protected void onZoom(float zoom, PointF pivot) {
    }

    private void performScroll(PointF scrollOffset) {
        onScroll(scrollOffset.x, scrollOffset.y);
    }

    private void performLongPress() {
        if (state.canPerformLongPress() && onLongTap()) {
            state.longTapPerformed();
        }
    }

    protected void onDoubleTap(float x, float y) {}

    protected boolean onLongTap() {
        return false;
    }

    protected void onScroll(float dx, float dy) {
    }

    private void performTap(float x, float y) {
        onTap(x, y);
        if(!handler.hasMessages(0, tapToken)) {
            tapToken = null;
        }
    }

    protected void onTap(float x, float y) {
    }

    protected void onFling(float xVelocity, float yVelocity) {
    }

    public void detach() {
        state.detach();
    }
}
