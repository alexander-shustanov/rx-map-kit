package com.depthguru.rxmap.touch;

/**
 * Zoom
 * </p>
 * alexander.shustanov on 24.01.17.
 */

public class Zoom {

    private final Axis z;

    private float minZoom = 0f;
    private float maxZoom = 18f;
    private int ANIMATION_DURATION = 250;

    public Zoom(float initialZoom) {
        z = new Axis(initialZoom);
        z.setMinValue(minZoom);
        z.setMaxValue(maxZoom);
    }

    public float getMinZoom() {
        return minZoom;
    }

    public float getMaxZoom() {
        return maxZoom;
    }

    public void setMinZoom(float minZoom) {
        this.minZoom = minZoom;
        z.setMinValue(minZoom);
    }

    public void setMaxZoom(float maxZoom) {
        this.maxZoom = maxZoom;
        z.setMaxValue(maxZoom);
    }

    public float getCurrentZoom() {
        return (float) z.getCurrentPosition();
    }

    public int getDiscreteZoom() {
        return (int) z.getCurrentPosition();
    }

    public boolean computeZoomOffset() {
        boolean inProcess = !isFinished();
        if (inProcess) {
            z.computeCurrentValue();
        }
        return inProcess;
    }

    public int add(float zoom) {
        return setZoom((float) (z.getCurrentPosition() + zoom));
    }

    public int setZoom(float zoom) {
        int startZoom = getDiscreteZoom();
        z.setPosition(zoom);
        int endZoom = getDiscreteZoom();
        return endZoom - startZoom;
    }

    public float getScaleFactor() {
        return (float) Math.pow(2, getCurrentZoom() - getDiscreteZoom());

    }

    public void zoomTo(float zoom) {
        z.scrollBy(zoom - z.getCurrentPosition(), ANIMATION_DURATION);
    }

    public void zoomIn() {
        z.scrollBy(1f, ANIMATION_DURATION);
    }

    public void zoomOut() {
        z.scrollBy(-1f, ANIMATION_DURATION);
    }

    public void stopZoom() {
        z.stopScroll();
    }

    public boolean isFinished() {
        return z.isEnd();
    }
}
