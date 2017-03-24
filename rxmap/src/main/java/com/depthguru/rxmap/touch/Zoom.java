package com.depthguru.rxmap.touch;

/**
 * Zoom
 * </p>
 * alexander.shustanov on 24.01.17.
 */

public class Zoom {
    private final float MIN_ZOOM = 0.0f;
    private final float MAX_ZOOM = 18.5f;

    private final Axis z;
    private int ANIMATION_DURATION = 250;

    public Zoom(float initialZoom) {
        z = new Axis(initialZoom);
        z.clamp(MIN_ZOOM, MAX_ZOOM);
    }

    public float getCurrentZoom() {
        return (float) z.getCurrentPosition();
    }

    public int getDiscreteZoom() {
        return (int) Math.floor(z.getCurrentPosition());
    }

    public boolean computeZoomOffset() {
        boolean inProcess = !isFinished();
        if (inProcess) {
            z.computeCurrentValue();
        }
        return inProcess;
    }

    public void setZoom(float zoom) {
        z.setPosition(zoom);
    }

    public int add(float zoom) {
        int startZoom = getDiscreteZoom();
        z.setPosition((float) (z.getCurrentPosition() + zoom));
        int endZoom = getDiscreteZoom();
        return endZoom - startZoom;
    }

    public float getScaleFactor() {
        return (float) Math.pow(2, getCurrentZoom() - getDiscreteZoom());

    }

    public void zoomIn() {
        z.scrollBy(1f, ANIMATION_DURATION);
    }

    public void zoomOut() {
        z.scrollBy(-1f, ANIMATION_DURATION);
    }

    public boolean isFinished() {
        return z.isEnd();
    }
}
