package com.depthguru.rxmap;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;

/**
 * Projection
 * </p>
 * alexander.shustanov on 15.12.16
 */
public class Projection {
    private final int mapWidth;
    private final int mapHeight;

    private final int worldSize;

    private final int offsetX;
    private final int offsetY;

    private final float pivotX;
    private final float pivotY;

    private final Matrix scaleMatrix = new Matrix();

    private final float zoom;
    private final int discreteZoom;
    private Rect screenRect;

    public Projection(RxMapView mapView) {
        mapWidth = mapView.getWidth();
        mapHeight = mapView.getHeight();

        zoom = mapView.getZoom();
        discreteZoom = (int) Math.floor(zoom);

        worldSize = 256 << discreteZoom;

        offsetX = -mapView.getScrollX();
        offsetY = -mapView.getScrollY();

        pivotX = mapView.pivot.x;
        pivotY = mapView.pivot.y;

        scaleMatrix.set(mapView.scaleMatrix);

        screenRect = mapView.getScreenRect();
    }

    public IGeoPoint fromPixels(int x, int y) {
        return fromPixels(x, y, null);
    }

    public IGeoPoint fromPixels(int x, int y, GeoPoint reuse) {
        return TileSystem.PixelXYToLatLong(x - offsetX, y - offsetY, discreteZoom, reuse);
    }

    public Rect getScreenRect() {
        return screenRect;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public float getPivotX() {
        return pivotX;
    }

    public float getPivotY() {
        return pivotY;
    }

    public int getWorldSize() {
        return worldSize;
    }

    public <R> R visit(ProjectionVisitor<R> visitor) {
        return visitor.processProjectionBounds(screenRect.left - offsetX, screenRect.top - offsetY, screenRect.right - offsetX, screenRect.bottom - offsetY, discreteZoom);
    }

    public int getDiscreteZoom() {
        return (int) Math.floor(zoom);
    }

    public interface ProjectionVisitor<R> {
        R processProjectionBounds(int leftMercPx, int topMercPx, int rightMercPx, int bottomMercPx, int zoom);
    }
}
