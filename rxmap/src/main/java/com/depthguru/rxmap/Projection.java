package com.depthguru.rxmap;

import android.graphics.Matrix;
import android.graphics.Point;
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
    private final Matrix unScaleMatrix = new Matrix();

    private final float zoom;
    private final int discreteZoom;

    private final BoundingBoxE6 boundingBoxE6;
    private final double scaleFactor;

    private Rect screenRect;

    public Projection(RxMapView mapView) {
        mapWidth = mapView.getWidth();
        mapHeight = mapView.getHeight();

        zoom = mapView.getZoom();
        discreteZoom = (int) Math.floor(zoom);
        scaleFactor = Math.pow(2, zoom - discreteZoom);

        worldSize = TileSystem.getTileSize() << discreteZoom;

        offsetX = MathUtils.mod(-mapView.getScrollX(), worldSize);
        offsetY = MathUtils.mod(-mapView.getScrollY(), worldSize);

        pivotX = mapView.pivot.x;
        pivotY = mapView.pivot.y;

        scaleMatrix.set(mapView.scaleMatrix);
        scaleMatrix.invert(unScaleMatrix);

        screenRect = mapView.getScreenRect();

        boundingBoxE6 = new BoundingBoxE6(fromPixels(screenRect.left, screenRect.top), fromPixels(screenRect.right, screenRect.bottom));
    }

    public Matrix getScaleMatrix() {
        return scaleMatrix;
    }

    public Matrix getUnScaleMatrix() {
        return unScaleMatrix;
    }

    public Point toPixels(GeoPoint geoPoint, Point reuse) {
        Point point = TileSystem.LatLongToPixelXY(geoPoint.getLatitude(), geoPoint.getLongitude(), discreteZoom, reuse);
        point.negate();
        point.offset(offsetX, offsetY);
        return point;
    }

    public GeoPoint fromPixels(int x, int y) {
        return fromPixels(x, y, null);
    }

    public GeoPoint fromPixels(int x, int y, GeoPoint reuse) {
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

    public float getZoom() {
        return zoom;
    }

    public BoundingBoxE6 getBounds() {
        return boundingBoxE6;
    }

    public GeoPoint getCenter() {
        return boundingBoxE6.getCenter();
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public interface ProjectionVisitor<R> {
        R processProjectionBounds(int leftMercPx, int topMercPx, int rightMercPx, int bottomMercPx, int zoom);
    }
}
