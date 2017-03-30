package com.depthguru.rxmap.overlay.tiles;

import android.graphics.drawable.Drawable;

import com.depthguru.rxmap.TileSystem;

/**
 * Created by kandalin on 24.03.17.
 */

public class TileDrawable {

    private static final int TILE_SIZE = TileSystem.getTileSize();

    public final Drawable drawable;
    public final int startX, startY, endX, endY;

    public TileDrawable(MapTile mapTile, Drawable drawable, int projectionZoom) {
        this.drawable = drawable;

        int zoomDelta = projectionZoom - mapTile.getZoomLevel();
        double tileSize;
        if (zoomDelta >= 0) {
            tileSize = TILE_SIZE * (1 << zoomDelta);
        } else {
            tileSize = TILE_SIZE / (double) (1 << (-zoomDelta));
        }
        this.startX = (int) (mapTile.getX() * tileSize);
        this.startY = (int) (mapTile.getY() * tileSize);
        this.endX = startX + (int) tileSize;
        this.endY = startY + (int) tileSize;
    }
}
