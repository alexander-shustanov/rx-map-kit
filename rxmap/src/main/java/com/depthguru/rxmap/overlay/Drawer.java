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

    private final Projection baseProjection;

    public Drawer(Projection baseProjection) {
        this.baseProjection = baseProjection;
    }

    public Projection baseProjection() {
        return baseProjection;
    }

    public abstract void draw(Canvas canvas, Projection projection);

    public boolean onTap(float x, float y, Projection projection) {
        return false;
    }
}
