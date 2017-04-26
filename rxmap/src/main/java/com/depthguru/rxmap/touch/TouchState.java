package com.depthguru.rxmap.touch;

import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.VelocityTracker;

/**
 * TouchState
 * </p>
 * alexander.shustanov on 21.12.16
 */
@SuppressWarnings("WeakerAccess")
public class TouchState {
    private static final double LOG2 = Math.log(2);

    private final VelocityTracker velocityTracker = VelocityTracker.obtain();
    private final PointF initialPosition = new PointF();
    private final PointF scrollPosition = new PointF();
    private final float touchSlop;
    private final float flingVelocitySlop;
    private float fingersDistance;
    private float initialRotation;
    private State state = State.NONE;

    public TouchState(float touchSlop, float flingVelocitySlop) {
        this.touchSlop = touchSlop;
        this.flingVelocitySlop = flingVelocitySlop;
    }

    public boolean canPerformTap() {
        return state == State.AWAIT_TO_TAP;
    }

    public boolean canPerformFling() {
        return state == State.SCROLL;
    }

    public void onDown(MotionEvent event) {
        initialPosition.set(event.getX(), event.getY());
        state = State.AWAIT_TO_TAP;
    }

    public boolean isInMove() {
        return state != State.AWAIT_TO_TAP && state != State.AWAIT_TO_LONG_PRESS && state != State.NONE;
    }

    private boolean isOvercomeSlop(PointF a, MotionEvent b) {
        return Math.abs(a.x - b.getX()) > touchSlop || Math.abs(a.y - b.getY()) > touchSlop;
    }

    public boolean tryStartScroll(MotionEvent event) {
        if (isOvercomeSlop(initialPosition, event)) {
            state = State.SCROLL;
            return true;
        }
        return false;
    }

    public void rejectTap() {
        if (state == State.AWAIT_TO_TAP) {
            state = State.AWAIT_TO_LONG_PRESS;
        }
    }

    public boolean canPerformLongPress() {
        return state == State.AWAIT_TO_LONG_PRESS;
    }

    public void longTapPerformed() {
        state = State.NONE;
    }

    public boolean inScroll() {
        return state == State.SCROLL || state == State.ZOOM || state == State.ROTATE;
    }

    public void savePrevEvent(MotionEvent event) {
        computeScrollPosition(event, scrollPosition);
        if (event.getPointerCount() > 1) {
            fingersDistance = computeFingerDistance(event);
        }
    }

    private float computeFingerDistance(MotionEvent event) {
        return (float) Math.hypot(event.getX(0) - event.getX(1), event.getY(0) - event.getY(1));
    }

    private void computeScrollPosition(MotionEvent event, PointF sc) {
        int index = Integer.MAX_VALUE;
        if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
            index = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        }
        int pointersCount = Math.min(event.getPointerCount(), index > 1 ? 2 : 3);

        sc.x = 0;
        sc.y = 0;
        for (int i = 0; i < pointersCount; i++) {
            if (i == index) {
                continue;
            }
            sc.x += event.getX(i);
            sc.y += event.getY(i);
        }
        if (index < pointersCount) {
            pointersCount--;
        }
        sc.x /= pointersCount;
        sc.y /= pointersCount;
    }

    public void computeScroll(MotionEvent event, PointF reuse) {
        computeScrollPosition(event, reuse);
        reuse.negate();
        reuse.x += scrollPosition.x;
        reuse.y += scrollPosition.y;
    }

    public void trackVelocity(MotionEvent event) {
        velocityTracker.addMovement(event);
    }

    public void computeVelocity(PointF reuse) {
        velocityTracker.computeCurrentVelocity(1);
        reuse.set(velocityTracker.getXVelocity(), velocityTracker.getYVelocity());
    }

    public boolean doFling() {
        float velocity = (float) Math.sqrt(
                velocityTracker.getXVelocity() * velocityTracker.getXVelocity()
                        + velocityTracker.getYVelocity() * velocityTracker.getYVelocity()
        );
        return velocity >= flingVelocitySlop;
    }

    public void detach() {
        velocityTracker.recycle();
    }

    public boolean inZoomOrRotate() {
        return state == State.ZOOM || state == State.ROTATE;
    }

    public void computeCenterPivot(MotionEvent event, PointF reuse) {
        reuse.x = (event.getX(0) + event.getX(1)) / 2f;
        reuse.y = (event.getY(0) + event.getY(1)) / 2f;
    }

    public float computeZoom(MotionEvent event) {
        return (float) (Math.log(computeFingerDistance(event) / fingersDistance) / LOG2);
    }

    public float computeRotation(MotionEvent event) {
        float rotation = rotation(event);
        float delta = rotation - initialRotation;
        initialRotation += delta;
        return delta;
    }

    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    public void onPointerDown(MotionEvent event) {
        state = State.ZOOM;
        initialRotation = rotation(event);
    }

    public void onPointerUp(MotionEvent event) {
        if(event.getPointerCount() == 2) {
            state = State.SCROLL;
        }
    }


    public enum State {
        NONE,
        AWAIT_TO_TAP,
        AWAIT_TO_LONG_PRESS,
        SCROLL,
        ZOOM,
        ROTATE,
        TILT
    }
}
