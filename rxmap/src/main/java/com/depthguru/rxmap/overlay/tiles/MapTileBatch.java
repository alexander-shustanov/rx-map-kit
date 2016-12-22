package com.depthguru.rxmap.overlay.tiles;

import android.graphics.drawable.Drawable;

import com.depthguru.rxmap.Projection;

import java.util.Collection;
import java.util.Map;

/**
 * MapTileBatch
 * </p>
 * alexander.shustanov on 16.12.16
 */
public class MapTileBatch {
    private final Map<MapTile, Drawable> tiles;
    private Collection<MapTile> mapTiles;
    private final Projection projection;

    public MapTileBatch(Map<MapTile, Drawable> tiles, Collection<MapTile> mapTiles, Projection projection) {
        this.tiles = tiles;
        this.mapTiles = mapTiles;
        this.projection = projection;
    }

    public Collection<MapTile> getMapTiles() {
        return mapTiles;
    }

    public Drawable getTile(MapTile mapTile) {
        return tiles.get(mapTile);
    }

    public Projection getProjection() {
        return projection;
    }
}
