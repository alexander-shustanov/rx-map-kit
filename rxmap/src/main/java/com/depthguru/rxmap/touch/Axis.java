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

public class Axis {
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

    public Axis(float initialPosition) {

        startPosition = initialPosition;
        currentPosition = initialPosition;
        endPosition = initialPosition;

        resetClamp();
    }

    public void scrollBy(float delta) {
        scrollBy(delta, 20);
    }

    public void scrollBy(float delta, int duration) {
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

    public void computeCurrentValue() {
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

    public float getNormalTime() {
        long now = AnimationUtils.currentAnimationTimeMillis();
        long delta = now - startTime;
        if (delta >= duration) {
            return 1f;
        }
        return interpolator.getInterpolation(((float) delta) / ((float) duration));
    }

    public boolean isEnd() {
        if (interrupted) {
            interrupted = false;
            return false;
        }
        return currentPosition == endPosition;
    }

    public void fling(float velocity, int duration, float tension) {
        fixPosition();
        endPosition = clamp(startPosition - velocity * duration / 2f);
        this.velocity = velocity;
        this.duration = (int) ((startPosition - endPosition)/velocity*2f);
        this.tension = tension;
        interpolator = LINEAR;
    }

    public void offsetBy(float offset) {
        startPosition = clamp(startPosition + offset);
        endPosition = clamp(endPosition + offset);
        currentPosition = clamp(currentPosition + offset);
        interrupted = true;
    }

    public void reconfigureWithZoomFactor(float scaleFactor, float pivot, int worldSize) {
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

    public void stopScroll() {
        fixPosition();
        duration = 0;
        endPosition = startPosition;
    }

    public double getCurrentPosition() {
        return currentPosition;
    }

    public void clamp(double left, double right) {
        this.left = left;
        this.right = right;
    }

    public void resetClamp() {
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

    public void setPosition(float pos) {
        tension = 0.0;
        velocity = 0.0;
        startTime = AnimationUtils.currentAnimationTimeMillis();
        startPosition = clamp(pos);
        endPosition = clamp(pos);
        currentPosition = clamp(pos);
    }
}
