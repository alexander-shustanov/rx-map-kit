package com.depthguru.rxmap.touch;

import android.content.Context;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * TouchScroller
 * </p>
 * alexander.shustanov on 20.12.16
 */
public abstract class TouchScroller {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Context context;

    private final float touchSlop;

    private final PointF reuse = new PointF();

    private final TouchState state;
    private final Runnable detectLongPress = this::performLongPress;
    private final Runnable rejectTap;

    protected TouchScroller(Context context) {
        this.context = context;
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        state = new TouchState(touchSlop);
        rejectTap = state::rejectTap;
    }


    public boolean onTouchEvent(MotionEvent event) {
        state.trackVelocity(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                handleActionDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                handleActionMove(event);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                System.out.println("sadf");
                break;
            case MotionEvent.ACTION_UP:
                handleActionUp();
                break;
        }

        state.savePrevEvent(event);
        return true;
    }

    private void handleActionUp() {
        if (state.canPerformTap()) {
            onTap();
        } else if (state.canPerformFling()) {
            state.computeVelocity(reuse);
            onFling(reuse.x, reuse.y);
        }
        handler.removeCallbacks(detectLongPress);
        handler.removeCallbacks(rejectTap);
    }

    private void handleActionDown(MotionEvent event) {
        state.onDown(event);
        handler.postDelayed(detectLongPress, 1000);
        handler.postDelayed(rejectTap, 500);
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
//            switch (state) {
//                case SCROLL:
////                    case ROTATE:
//                    performScroll(event);
//                    break;
//                case TILT:
//                    break;
//            }
        }
    }

    private void performScroll(PointF scrollOffset) {
        onScroll(scrollOffset.x, scrollOffset.y);
    }

    private void performLongPress() {
        if (state.canPerformLongPress() && onLongTap()) {
            state.longTapPerformed();
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
        state.detach();
    }
}
