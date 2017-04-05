package com.depthguru.rxmap;

import android.graphics.Point;

import com.depthguru.rxmap.touch.Rotation;

/**
 * Created by kandalin on 30.03.17.
 */

public class MapController {

    public static final int ANIMATION_DURATION_DEFAULT = 300;

    private final RxMapView rxMapView;

    private Point reusePoint = new Point(0, 0);

    public MapController(RxMapView rxMapView) {
        this.rxMapView = rxMapView;
    }

    public void animateTo(final IGeoPoint geoPoint) {
        rxMapView.projection.toPixels(geoPoint, reusePoint);
        animateTo(reusePoint.x, reusePoint.y);
    }

    public void animateTo(int x, int y) {
        Point mercatorPoint = rxMapView.projection.toMercatorPixels(x, y, null);
        mercatorPoint.offset(-rxMapView.getWidth() / 2, -rxMapView.getHeight() / 2);
        rxMapView.scroller.scrollBy(mercatorPoint.x - rxMapView.getScrollX(),
                mercatorPoint.y - rxMapView.getScrollY(), ANIMATION_DURATION_DEFAULT);
        rxMapView.invalidate();
    }

    public void setCenter(final IGeoPoint geoPoint) {
        Point point = rxMapView.projection.toPixels(geoPoint, null);
        point = rxMapView.projection.toMercatorPixels(point.x, point.y, point);
        point.offset(-rxMapView.getWidth() / 2, -rxMapView.getHeight() / 2);
        rxMapView.scroller.scrollTo(point.x, point.y);
        rxMapView.invalidate();
    }

    public void setZoom(float zoom) {
        int zoomDiff = rxMapView.zoom.setZoom(zoom);
        if (zoomDiff != 0) {
            rxMapView.scroller.setZoom(rxMapView.zoom.getDiscreteZoom());
            rxMapView.scroller.reconfigureWithZoomFactor(zoomDiff, rxMapView.getWidth() / 2, rxMapView.getHeight() / 2);
        }
        rxMapView.computeProjection(false);
        rxMapView.invalidate();
    }

    public void zoomIn() {
        rxMapView.zoom.zoomIn();
        rxMapView.invalidate();
    }

    public void zoomOut() {
        rxMapView.zoom.zoomOut();
        rxMapView.invalidate();
    }

    public void rotate(float rotation) {
        rxMapView.rotation.add(rotation, ANIMATION_DURATION_DEFAULT);
        rxMapView.computeProjection(false);
        rxMapView.invalidate();
    }

    public void rotate(float rotation, float pivotX, float pivotY) {
        rxMapView.rotation.add(rotation, ANIMATION_DURATION_DEFAULT);
        rxMapView.computeProjection(false);
        rxMapView.invalidate();
        rxMapView.updatePivot(pivotX, pivotY);
    }

    public void setRotation(float rotation) {
        rxMapView.rotation.setRotation(rotation);
        rxMapView.computeProjection(false);
        rxMapView.invalidate();
    }

    public void resetRotation() {
        float dif;
        float currentRotation = rxMapView.rotation.getRotation();
        if (currentRotation < Rotation.FULL_VALUE / 2) {
            dif = -currentRotation;
        } else {
            dif = Rotation.FULL_VALUE - currentRotation;
        }

        rxMapView.rotation.add(dif, ANIMATION_DURATION_DEFAULT);
        rxMapView.computeProjection(false);
        rxMapView.invalidate();
    }

}