package com.depthguru.rxmap;

import android.graphics.Matrix;
import android.graphics.Point;
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

    private final Matrix rotateAndScaleMatrix = new Matrix();
    private final Matrix unRotateAndScaleMatrix = new Matrix();

    private final float[] rotateAndScalePoints = new float[2];
    private final float[] rotateAndScaleVectors = new float[2];

    private final float zoom;
    private final int discreteZoom;

    private final float mapOrientation;

    private final BoundingBoxE6 boundingBoxE6;
    private final double scaleFactor;

    private Rect screenRect;
    private Rect intrinsicScreenRect;

    public Projection(RxMapView mapView) {
        mapWidth = mapView.getWidth();
        mapHeight = mapView.getHeight();

        mapOrientation = mapView.rotation.getRotation();

        zoom = mapView.getZoom();
        discreteZoom = (int) Math.floor(zoom);
        scaleFactor = Math.pow(2, zoom - discreteZoom);

        worldSize = TileSystem.getTileSize() << discreteZoom;

        offsetX = -mapView.getScrollX();
        offsetY = -mapView.getScrollY();

        pivotX = mapView.pivot.x;
        pivotY = mapView.pivot.y;

        rotateAndScaleMatrix.set(mapView.rotateAndScaleMatrix);
        rotateAndScaleMatrix.invert(unRotateAndScaleMatrix);

        screenRect = mapView.getScreenRect(null);
        intrinsicScreenRect = mapView.getIntrinsicScreenRect(null);

        boundingBoxE6 = new BoundingBoxE6(fromPixels(screenRect.left, screenRect.top), fromPixels(screenRect.right, screenRect.bottom));
    }

    public Rect getScreenRect() {
        return screenRect;
    }

    public Rect getIntrinsicScreenRect() {
        return intrinsicScreenRect;
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public float getMapOrientation() {
        return mapOrientation;
    }

    public Matrix getRotateAndScaleMatrix() {
        return rotateAndScaleMatrix;
    }

    public Matrix getUnRotateAndScaleMatrix() {
        return unRotateAndScaleMatrix;
    }

    public Point toPixels(IGeoPoint geoPoint, Point reuse) {
        Point point = TileSystem.LatLongToPixelXY(geoPoint.getLatitude(), geoPoint.getLongitude(), discreteZoom, reuse);
        point.offset(offsetX, offsetY);
        return point;
    }

    public GeoPoint fromPixels(int x, int y) {
        return fromPixels(x, y, null);
    }

    public GeoPoint fromPixels(int x, int y, GeoPoint reuse) {
        return TileSystem.PixelXYToLatLong(x - offsetX, y - offsetY, discreteZoom, reuse);
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
        return discreteZoom;
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

    public Point toMercatorPixels(int x, int y, Point reuse) {
        final Point out = reuse != null ? reuse : new Point();
        out.set(x, y);
        out.offset(-offsetX, -offsetY);
        return out;
    }

    /**
     * This will revert the current map's scaling and rotation for a point. This can be useful when
     * drawing to a fixed location on the screen.
     */
    public PointF unRotateAndScalePoint(float x, float y, PointF reuse) {
        if (reuse == null)
            reuse = new PointF();

        if (getMapOrientation() != 0 || zoom != 1.0f) {
            rotateAndScalePoints[0] = x;
            rotateAndScalePoints[1] = y;
            unRotateAndScaleMatrix.mapPoints(rotateAndScalePoints);
            reuse.set(rotateAndScalePoints[0], rotateAndScalePoints[1]);
        } else
            reuse.set(x, y);
        return reuse;
    }

    public PointF unRotateAndScaleVector(float x, float y, PointF reuse) {
        if (reuse == null)
            reuse = new PointF();

        if (getMapOrientation() != 0 || zoom != 1.0f) {
            rotateAndScaleVectors[0] = x;
            rotateAndScaleVectors[1] = y;
            unRotateAndScaleMatrix.mapVectors(rotateAndScaleVectors);
            reuse.set(rotateAndScaleVectors[0], rotateAndScaleVectors[1]);
        } else
            reuse.set(x, y);
        return reuse;
    }

    /**
     * This will apply the current map's scaling and rotation for a point. This can be useful when
     * converting MotionEvents to a screen point.
     */
    public PointF rotateAndScalePoint(float x, float y, PointF reuse) {
        if (reuse == null)
            reuse = new PointF();

        if (getMapOrientation() != 0 || zoom != 1.0f) {
            rotateAndScalePoints[0] = x;
            rotateAndScalePoints[1] = y;
            rotateAndScaleMatrix.mapPoints(rotateAndScalePoints);
            reuse.set(rotateAndScalePoints[0], rotateAndScalePoints[1]);
        } else
            reuse.set(x, y);
        return reuse;
    }

    public PointF rotateAndScaleVector(float x, float y, PointF reuse) {
        if (reuse == null)
            reuse = new PointF();

        if (getMapOrientation() != 0 || zoom != 1.0f) {
            rotateAndScalePoints[0] = x;
            rotateAndScalePoints[1] = y;
            rotateAndScaleMatrix.mapVectors(rotateAndScalePoints);
            reuse.set(rotateAndScalePoints[0], rotateAndScalePoints[1]);
        } else
            reuse.set(x, y);
        return reuse;
    }

    public interface ProjectionVisitor<R> {
        R processProjectionBounds(int leftMercPx, int topMercPx, int rightMercPx, int bottomMercPx, int zoom);
    }
}
