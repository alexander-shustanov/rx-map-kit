package com.depthguru.rxmap;

import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

/**
 * Scroller
 * </p>
 * alexander.shustanov on 20.12.16
 */
public class Scroller {
    private static final Interpolator LINEAR = new LinearInterpolator();
    private static final Interpolator DECELERATE = new DecelerateInterpolator();

    private final float tension = 0.015f;

    private Axis x = new Axis();
    private Axis y = new Axis();

    public void scrollBy(float dx, float dy) {
        x.scrollBy(dx);
        y.scrollBy(dy);
    }

    public void fling(float xVelocity, float yVelocity) {
        if (xVelocity == 0 && yVelocity == 0) {
            return;
        }

        float velocity = (float) Math.hypot(xVelocity, yVelocity);
        int duration = (int) (velocity / tension);

//        float xVelocityRatio = Math.abs(xVelocity*xVelocity / (yVelocity*yVelocity + xVelocity*xVelocity));
//        float xTension = tension * xVelocityRatio;

        x.fling(xVelocity, duration, xVelocity / duration);
        y.fling(yVelocity, duration, yVelocity / duration);
    }

    private int computeDuration(float velocity) {
        return (int) (velocity / 0.02f);
    }

    public float getCurrX() {
        x.computeCurrentValue();
        return x.currentPosition;
    }

    public float getCurrY() {
        y.computeCurrentValue();
        return y.currentPosition;
    }

    public boolean computeScrollOffset() {
        return !x.isEnd() || !y.isEnd();
    }

    private class Axis {
        private Interpolator interpolator = LINEAR;

        private float startPosition;
        private float currentPosition;
        private float endPosition;

        private float velocity;
        private float tension;

        private long startTime;
        private int duration;

        public void scrollBy(float delta) {
            fixPosition();
            interpolator = LINEAR;
            duration = 20;
            endPosition = endPosition + delta;
        }

        private void fixPosition() {
            computeCurrentValue();
            this.velocity = 0;
            this.tension = 0;
            startTime = System.currentTimeMillis();
            startPosition = currentPosition;
        }

        public void computeCurrentValue() {
            if (currentPosition == endPosition) {
                return;
            }
            float normalTime = getNormalTime();
            if (normalTime == 1f) {
                currentPosition = endPosition;
                velocity = 0;
                tension = 0;
            } else {
                if (velocity != 0) {
                    float time = normalTime * duration;
                    currentPosition = startPosition + (tension * time / 2f - velocity) * time;
                } else {
                    currentPosition = normalTime * (endPosition - startPosition) + startPosition;
                }
            }
        }

        public float getNormalTime() {
            long now = System.currentTimeMillis();
            long delta = now - startTime;
            if (delta >= duration) {
                return 1f;
            }
            return interpolator.getInterpolation(((float) delta) / ((float) duration));
        }

        public boolean isEnd() {
            return currentPosition == endPosition;
        }

        public void fling(float velocity, int duration, float tension) {
            fixPosition();
            this.velocity = velocity;
            this.duration = duration;
            this.tension = tension;
            interpolator = LINEAR;
            endPosition = startPosition - velocity * duration / 2f;
        }
    }

}
