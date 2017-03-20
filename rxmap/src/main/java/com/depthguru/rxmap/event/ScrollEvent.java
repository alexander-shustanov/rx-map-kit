package com.depthguru.rxmap.event;

/**
 * Created by kandalin on 16.03.17.
 */

public class ScrollEvent {

    private final int x;
    private final int y;

    public ScrollEvent(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "ScrollEvent{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
