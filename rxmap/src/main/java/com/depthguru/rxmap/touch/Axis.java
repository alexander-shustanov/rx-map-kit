package com.depthguru.rxmap.touch;

import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

/**
 * Axis
 * </p>
 * alexander.shustanov on 24.01.17.
 */

class Axis {
    private static final Interpolator LINEAR = new LinearInterpolator();
    private static final Interpolator DECELERATE = new DecelerateInterpolator();

    private Interpolator interpolator = LINEAR;

    private double startPosition;
    private double currentPosition;
    private double endPosition;

    private double velocity;
    private double tension;

    private long startTime;
    private int duration;

    private boolean interrupted = false;
    private double left;
    private double right;

    Axis(float initialPosition) {

        startPosition = initialPosition;
        currentPosition = initialPosition;
        endPosition = initialPosition;

        resetClamp();
    }

    void scrollBy(float delta) {
        scrollBy(delta, 20);
    }

    void scrollBy(float delta, int duration) {
        fixPosition();
        interpolator = LINEAR;
        this.duration = duration;
        endPosition = clamp(endPosition + delta);
    }

    private void fixPosition() {
        computeCurrentValue();
        tension = 0.0;
        velocity = 0.0;
        startTime = AnimationUtils.currentAnimationTimeMillis();
        startPosition = currentPosition;
    }

    void computeCurrentValue() {
        if (currentPosition == endPosition) {
            return;
        }
        float normalTime = getNormalTime();
        if (normalTime == 1f) {
            currentPosition = endPosition;
            velocity = 0.0;
            tension = 0.0;
        } else {
            if (velocity != 0) {
                double time = normalTime * duration;
                currentPosition = (startPosition + (tension * time / 2.0 - velocity) * time);
            } else {
                currentPosition = (normalTime * (endPosition - startPosition) + startPosition);
            }
        }
    }

    private float getNormalTime() {
        long now = AnimationUtils.currentAnimationTimeMillis();
        long delta = now - startTime;
        if (delta >= duration) {
            return 1f;
        }
        return interpolator.getInterpolation(((float) delta) / ((float) duration));
    }

    boolean isEnd() {
        if (interrupted) {
            interrupted = false;
            return false;
        }
        return currentPosition == endPosition;
    }

    boolean isFlinging() {
        return velocity != 0;
    }

    void fling(float velocity, int duration, float tension) {
        fixPosition();
        endPosition = clamp(startPosition - velocity * duration / 2f);
        this.velocity = velocity;
        this.duration = (int) ((startPosition - endPosition)/velocity*2f);
        this.tension = tension;
        interpolator = LINEAR;
    }

    void offsetBy(float offset) {
        startPosition = clamp(startPosition + offset);
        endPosition = clamp(endPosition + offset);
        currentPosition = clamp(currentPosition + offset);
        interrupted = true;
    }

    void reconfigureWithZoomFactor(float scaleFactor, float pivot, int worldSize) {
        this.startPosition += pivot;
        this.endPosition += pivot;
        this.currentPosition += pivot;

        this.startPosition *= scaleFactor;
        this.endPosition *= scaleFactor;
        this.currentPosition *= scaleFactor;

        this.startPosition -= pivot;
        this.endPosition -= pivot;
        this.currentPosition -= pivot;

        this.startPosition %= worldSize;
        this.endPosition %= worldSize;
        this.currentPosition %= worldSize;

        this.startPosition = clamp(this.startPosition);
        this.endPosition = clamp(this.endPosition);
        this.currentPosition = clamp(this.currentPosition);

        this.tension *= scaleFactor;
    }

    void stopScroll() {
        fixPosition();
        duration = 0;
        endPosition = startPosition;
    }

    double getCurrentPosition() {
        return currentPosition;
    }

    void clamp(double left, double right) {
        this.left = left;
        this.right = right;
    }

    private void resetClamp() {
        this.left = -Double.MAX_VALUE;
        this.right = Double.MAX_VALUE;
    }

    private double clamp(double value) {
        if (value < left) {
            return left;
        }
        if (value > right) {
            return right;
        }
        return value;
    }

    void setPosition(float pos) {
        tension = 0.0;
        velocity = 0.0;
        startTime = AnimationUtils.currentAnimationTimeMillis();
        startPosition = clamp(pos);
        endPosition = clamp(pos);
        currentPosition = clamp(pos);
    }
}
