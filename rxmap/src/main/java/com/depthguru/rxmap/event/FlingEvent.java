package com.depthguru.rxmap.event;

/**
 * Created by kandalin on 16.03.17.
 */

public class FlingEvent {

    public final float xVelocity;
    public final float yVelocity;

    public FlingEvent(float xVelocity, float yVelocity) {
        this.xVelocity = xVelocity;
        this.yVelocity = yVelocity;
    }

    @Override
    public String toString() {
        return "FlingEvent{" +
                "xVelocity=" + xVelocity +
                ", yVelocity=" + yVelocity +
                '}';
    }
}
