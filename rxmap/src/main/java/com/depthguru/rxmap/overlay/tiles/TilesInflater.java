package com.depthguru.rxmap.overlay.tiles;

import android.graphics.Point;

import com.depthguru.rxmap.Projection;
import com.depthguru.rxmap.TileSystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TilesInflater
 * </p>
 * alexander.shustanov on 15.12.16
 */
public class TilesInflater implements Projection.ProjectionVisitor<Collection<MapTile>> {
    private final Point upperLeft = new Point();
    private final Point lowerRight = new Point();

    @Override
    public Collection<MapTile> processProjectionBounds(int leftMercPx, int topMercPx, int rightMercPx, int bottomMercPx, int zoom) {
        Set<MapTile> tiles = new HashSet<>();
        TileSystem.PixelXYToTileXY(leftMercPx, topMercPx, upperLeft);
        upperLeft.offset(-1, -1);
        TileSystem.PixelXYToTileXY(rightMercPx, bottomMercPx, lowerRight);

        final int mapTileUpperBound = 1 << zoom;

        for (int y = upperLeft.y; y <= lowerRight.y; y++) {
            for (int x = upperLeft.x; x <= lowerRight.x; x++) {
                // Construct a MapTile to request from the tile provider.
                final int tileY = mod(y, mapTileUpperBound);
                final int tileX = mod(x, mapTileUpperBound);
                final MapTile tile = new MapTile(tileX, tileY, zoom);
                tiles.add(tile);
            }
        }

        return tiles;
    }

    public static int mod(int number, int modulus) {
        if (number > 0)
            return number % modulus;

        while (number < 0)
            number += modulus;

        return number;
    }
}
