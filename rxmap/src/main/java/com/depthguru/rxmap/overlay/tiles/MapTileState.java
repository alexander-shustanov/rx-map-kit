package com.depthguru.rxmap.overlay.tiles;

import android.graphics.drawable.Drawable;

/**
 * MapTileState
 * </p>
 * alexander.shustanov on 15.12.16
 */
public class MapTileState {
    private Drawable drawable;
    private MapTile mapTile;
    private int state = 0;

    public MapTileState(MapTile mapTile) {
        this.mapTile = mapTile;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public MapTile getMapTile() {
        return mapTile;
    }

    public int getState() {
        return state;
    }

    public void incState() {
        state++;
    }
}
