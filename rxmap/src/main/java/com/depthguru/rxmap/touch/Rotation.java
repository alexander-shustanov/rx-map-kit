package com.depthguru.rxmap.touch;

/**
 * Created by kandalin on 31.03.17.
 */

public class Rotation {

    public static final float FULL_VALUE = 360.0f;

    private final Axis rotation;

    public Rotation() {
        rotation = new Axis(0f);
        rotation.setFullValue(FULL_VALUE);
    }

    public boolean computeRotation() {
        boolean inProcess = !isFinished();
        if (inProcess) {
            rotation.computeCurrentValue();
        }
        return inProcess;
    }

    public float getRotation() {
        return (float) rotation.getCurrentPosition();
    }

    public void add(float rotation) {
        this.rotation.scrollBy(rotation);
    }

    public void add(float rotation, int duration) {
        this.rotation.scrollBy(rotation, duration);
    }

    public void setRotation(float rotation) {
        this.rotation.setPosition(rotation);
    }

    public void stopRotate() {
        rotation.stopScroll();
    }

    public boolean isFinished() {
        return rotation.isEnd();
    }

}