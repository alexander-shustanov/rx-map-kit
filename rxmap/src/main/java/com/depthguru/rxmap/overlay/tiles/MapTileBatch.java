package com.depthguru.rxmap.overlay.tiles;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.depthguru.rxmap.util.MathUtils;
import com.depthguru.rxmap.Projection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * MapTileBatch
 * </p>
 * alexander.shustanov on 16.12.16
 */
public class MapTileBatch {
    private final Map<MapTile, Drawable> tiles;
    private final Projection projection;
    private List<MapTile> mapTiles;

    private Rect tilesRect = new Rect(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);

    public MapTileBatch(Map<MapTile, Drawable> tiles, Collection<MapTile> mapTiles, Projection projection) {
        this.tiles = tiles;
        this.mapTiles = new ArrayList<>(mapTiles);
        sortList();
        this.projection = projection;
        for (MapTile mapTile : mapTiles) {
            if (mapTile.getX() > tilesRect.right) {
                tilesRect.right = mapTile.getX();
            }
            if (mapTile.getX() < tilesRect.left) {
                tilesRect.left = mapTile.getX();
            }
            if (mapTile.getY() > tilesRect.top) {
                tilesRect.top = mapTile.getY();
            }
            if (mapTile.getY() < tilesRect.bottom) {
                tilesRect.bottom = mapTile.getY();
            }
        }
    }

    private void sortList() {
        Collections.sort(mapTiles, (tile1, tile2) -> tile1.getZoomLevel() - tile2.getZoomLevel());
    }

    public Collection<MapTile> getMapTiles() {
        return mapTiles;
    }

    public Map<MapTile, Drawable> getTiles() {
        return tiles;
    }

    public Drawable getTile(MapTile mapTile) {
        return tiles.get(mapTile);
    }

    public Projection getProjection() {
        return projection;
    }

    public MapTileBatch completeWith(MapTileBatch last) {

        final int zoom = projection.getDiscreteZoom();

        iterTiles:
        for (MapTile mapTile : last.getMapTiles()) {
            final int zoomDelta = zoom - mapTile.getZoomLevel();
            int tileX = mapTile.getX();
            int tileY = mapTile.getY();

            if (zoomDelta == 0) {
                continue;
            }

            if (zoomDelta > 0) {
                tileX = tileX << zoomDelta;
                tileY = tileY << zoomDelta;
            } else {
                tileX = tileX >> (-zoomDelta);
                tileY = tileY >> (-zoomDelta);
            }

            if (!MathUtils.around(tileX, tilesRect.left, tilesRect.right) || !MathUtils.around(tileY, tilesRect.bottom, tilesRect.top)) {
                continue;
            }

            if (zoomDelta <= 0) {
                MapTile currentZoomEquivalent = new MapTile(tileX, tileY, zoom);
                if (getTile(currentZoomEquivalent) != null) {
                    continue;
                }

                mapTiles.add(mapTile);
                tiles.put(mapTile, last.getTile(mapTile));
            } else {
                int range = 1 << zoomDelta;
                int xRange = Math.min(tileX + range, tilesRect.right) - tileX;
                int yRange = Math.min(tileY + range, tilesRect.top) - tileY;
                for (int i = 0; i <= xRange; i++) {
                    for (int j = 0; j <= yRange; j++) {
                        MapTile currentZoomEquivalent = new MapTile(tileX + i, tileY + j, zoom);
                        if (getTile(currentZoomEquivalent) == null) {
                            mapTiles.add(mapTile);
                            tiles.put(mapTile, last.getTile(mapTile));
                            continue iterTiles;
                        }
                    }
                }
            }
        }

        sortList();

        return this;
    }
}