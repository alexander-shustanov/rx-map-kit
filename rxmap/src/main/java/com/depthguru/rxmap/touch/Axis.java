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

    private double minValue = -Double.MAX_VALUE;
    private double maxValue = Double.MAX_VALUE;

    private double fullValue = Double.MAX_VALUE;

    Axis(float initialPosition) {
        startPosition = initialPosition;
        currentPosition = initialPosition;
        endPosition = initialPosition;
        checkFullValue();
    }

    Axis(float initialPosition, double minValue, double maxValue) {
        this(initialPosition);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    void scrollBy(float delta) {
        scrollBy(delta, 20);
    }

    void scrollBy(float delta, int duration) {
        fixPosition();
        interpolator = LINEAR;
        this.duration = duration;
        endPosition = clamp(startPosition + delta);
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

        checkFullValue();
    }

    void setFullValue(double fullValue) {
        this.fullValue = fullValue;
    }

    private void checkFullValue() {
        if (currentPosition >= fullValue) {
            currentPosition -= fullValue;
            startPosition -= fullValue;
            endPosition -= fullValue;
        } else if (currentPosition < 0) {
            currentPosition += fullValue;
            startPosition += fullValue;
            endPosition += fullValue;
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
        return currentPosition == endPosition;
    }

    boolean isFlinging() {
        return velocity != 0;
    }

    void fling(float velocity, int duration, float tension) {
        fixPosition();
        endPosition = clamp(startPosition - velocity * duration / 2f);
        this.velocity = velocity;
        this.duration = (int) ((startPosition - endPosition) / velocity * 2f);
        this.tension = tension;
        interpolator = LINEAR;
    }

    void offsetBy(float offset) {
        startPosition = clamp(startPosition + offset);
        endPosition = clamp(endPosition + offset);
        currentPosition = clamp(currentPosition + offset);
        checkFullValue();
    }

    void reconfigureWithZoomFactor(float scaleFactor, float pivot) {
        this.startPosition += pivot;
        this.endPosition += pivot;
        this.currentPosition += pivot;

        this.startPosition *= scaleFactor;
        this.endPosition *= scaleFactor;
        this.currentPosition *= scaleFactor;

        this.startPosition -= pivot;
        this.endPosition -= pivot;
        this.currentPosition -= pivot;

        checkFullValue();

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

    private double clamp(double value) {
        if (value < minValue) {
            return minValue;
        }
        if (value > maxValue) {
            return maxValue;
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
        checkFullValue();
    }
}
