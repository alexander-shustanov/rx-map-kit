package com.depthguru.rxmap.overlay.tiles;

/**
 * MapTile
 * </p>
 * alexander.shustanov on 15.12.16
 */
public class MapTile {
    private final int x;
    private final int y;
    private final int zoomLevel;

    public MapTile(int x, int y, int zoomLevel) {
        this.x = x;
        this.y = y;
        this.zoomLevel = zoomLevel;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZoomLevel() {
        return zoomLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MapTile mapTile = (MapTile) o;

        if (x != mapTile.x) return false;
        if (y != mapTile.y) return false;
        return zoomLevel == mapTile.zoomLevel;

    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + zoomLevel;
        return result;
    }
}
