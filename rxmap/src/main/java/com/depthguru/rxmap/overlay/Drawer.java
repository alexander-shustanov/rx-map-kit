package com.depthguru.rxmap.overlay;

import android.graphics.Canvas;

import com.depthguru.rxmap.Projection;

/**
 * Drawer
 * </p>
 * alexander.shustanov on 15.12.16
 */
public abstract class Drawer {
    public static final Drawer EMPTY_DRAWER = new Drawer(null) {
        @Override
        public void draw(Canvas canvas, Projection projection) {
        }
    };

    protected final Projection projection;

    public Drawer(Projection projection) {
        this.projection = projection;
    }

    public abstract void draw(Canvas canvas, Projection projection);
}
