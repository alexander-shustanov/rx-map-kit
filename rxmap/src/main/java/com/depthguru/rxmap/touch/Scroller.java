package com.depthguru.rxmap.touch;

import com.depthguru.rxmap.TileSystem;

/**
 * Scroller
 * </p>
 * alexander.shustanov on 20.12.16
 */
public class Scroller {
    private static final float TENSION = 0.012f;

    private final Axis x;
    private final Axis y;

    public Scroller(int x, int y, int zoom) {
        this.x = new Axis(x);
        this.y = new Axis(y);
        setZoom(zoom);
    }

    public void scrollTo(int x, int y) {
        this.x.setPosition(x);
        this.y.setPosition(y);
    }

    public void scrollBy(float dx, float dy) {
        x.scrollBy(dx);
        y.scrollBy(dy);
    }

    public void scrollBy(int dx, int dy, int duration) {
        x.scrollBy(dx, duration);
        y.scrollBy(dy, duration);
    }

    public boolean isFlinging() {
        return x.isFlinging() || y.isFlinging();
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
        return (int) x.getCurrentPosition();
    }

    public int getCurrY() {
        return (int) y.getCurrentPosition();
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

    public void reconfigureWithZoomFactor(int zoomDiff, float pivotX, float pivotY) {
        float scaleFactor = (float) Math.pow(2, zoomDiff);
        x.reconfigureWithZoomFactor(scaleFactor, pivotX);
        y.reconfigureWithZoomFactor(scaleFactor, pivotY);
    }

    public void setZoom(int zoom) {
        final int worldSize = TileSystem.MapSize(zoom);
        x.setFullValue(worldSize);
        y.setFullValue(worldSize);
    }
}
