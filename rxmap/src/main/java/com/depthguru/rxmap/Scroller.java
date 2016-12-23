package com.depthguru.rxmap;

import android.graphics.PointF;
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

    private static final float TENSION = 0.012f;

    private Axis x = new Axis();
    private Axis y = new Axis();

    public Scroller() {
    }

    public void scrollBy(float dx, float dy) {
        x.scrollBy(dx);
        y.scrollBy(dy);
    }

    public void fling(float xVelocity, float yVelocity) {
        if (xVelocity == 0 && yVelocity == 0) {
            return;
        }

        float velocity = (float) Math.hypot(xVelocity, yVelocity);
        int duration = (int) (velocity / TENSION);

        x.fling(xVelocity, duration, xVelocity / duration);
        y.fling(yVelocity, duration, yVelocity / duration);
    }

    public int getCurrX() {
        return (int) x.currentPosition;
    }

    public int getCurrY() {
        return (int) y.currentPosition;
    }

    public boolean computeScrollOffset() {
        boolean inProcess = !isFinished();
        x.computeCurrentValue();
        y.computeCurrentValue();
        return inProcess;
    }

    public boolean isFinished() {
        return x.isEnd() && y.isEnd();
    }

    public void offsetBy(float offsetX, float offsetY) {
        this.x.offsetBy(offsetX);
        this.y.offsetBy(offsetY);
    }

    public void stopScroll() {
        x.stopScroll();
        y.stopScroll();
    }

    public void reconfigureWithZoomFactor(int zoomDiff, PointF pivot, int worldSize) {
        float scaleFactor = (float) Math.pow(2, zoomDiff);
        x.reconfigureWithZoomFactor(scaleFactor, pivot.x, worldSize);
        y.reconfigureWithZoomFactor(scaleFactor, pivot.y, worldSize);
    }

    private class Axis {
        private Interpolator interpolator = LINEAR;

        private double startPosition;
        private double currentPosition;
        private double endPosition;

        private float velocity;
        private float tension;

        private long startTime;
        private int duration;

        private boolean interrupted = false;

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
            if (interrupted) {
                interrupted = false;
                return false;
            }
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

        public void offsetBy(float offset) {
            startPosition += offset;
            endPosition += offset;
            currentPosition += offset;
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

            this.tension *= scaleFactor;
        }

        public void stopScroll() {
            fixPosition();
            duration = 0;
            endPosition = startPosition;
        }
    }

}
