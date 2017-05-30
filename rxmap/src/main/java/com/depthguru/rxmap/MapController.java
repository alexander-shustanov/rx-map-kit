package com.depthguru.rxmap;

import android.graphics.Point;
import android.graphics.PointF;

import com.depthguru.rxmap.touch.Rotation;

/**
 * Created by kandalin on 30.03.17.
 */

public class MapController {

    public static final int ANIMATION_DURATION_DEFAULT = 300;

    private final RxMapView rxMapView;

    private Point reusePoint = new Point();
    private PointF reusePointF = new PointF();

    public MapController(RxMapView rxMapView) {
        this.rxMapView = rxMapView;
    }

    public void animateTo(final IGeoPoint geoPoint) {
        animateTo(geoPoint, rxMapView.getWidth() / 2, rxMapView.getHeight() / 2);
    }

    public void animateTo(int x, int y) {
        animateTo(x, y, rxMapView.getWidth() / 2, rxMapView.getHeight() / 2);
    }

    public void animateTo(final IGeoPoint geoPoint, float screenX, float screenY) {
        rxMapView.projection.toPixels(geoPoint, reusePoint);
        animateTo(reusePoint.x, reusePoint.y, screenX, screenY);
    }

    public void animateTo(int x, int y, float screenX, float screenY) {
        Point mercatorPoint = rxMapView.projection.toMercatorPixels(x, y, null);
        rxMapView.projection.unRotateAndScalePoint(screenX, screenY, reusePointF);
        mercatorPoint.offset((int) -reusePointF.x, (int) -reusePointF.y);
        rxMapView.scroller.scrollBy(
                mercatorPoint.x - rxMapView.getScrollX(),
                mercatorPoint.y - rxMapView.getScrollY(),
                ANIMATION_DURATION_DEFAULT
        );
        rxMapView.invalidate();
    }

    public void setCenter(final IGeoPoint geoPoint) {
        Point point = rxMapView.projection.toPixels(geoPoint, null);
        point = rxMapView.projection.toMercatorPixels(point.x, point.y, point);
        point.offset(-rxMapView.getWidth() / 2, -rxMapView.getHeight() / 2);
        rxMapView.scroller.scrollTo(point.x, point.y);
        rxMapView.scrollTo(point.x, point.y);
        rxMapView.computeProjection(true);
        rxMapView.invalidate();
    }

    public void setZoom(float zoom) {
        int zoomDiff = rxMapView.zoom.setZoom(zoom);
        if (zoomDiff != 0) {
            rxMapView.scroller.reconfigureWithZoomFactor(
                    rxMapView.zoom.getDiscreteZoom(),
                    rxMapView.getWidth() / 2, rxMapView.getHeight() / 2
            );
            rxMapView.scrollTo(rxMapView.scroller.getCurrX(), rxMapView.scroller.getCurrY());
        }
        rxMapView.computeProjection(true);
        rxMapView.invalidate();
    }

    public void zoomTo(float zoom) {
        rxMapView.zoom.zoomTo(zoom);
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

    public void zoomIn(float screenX, float screenY) {
        rxMapView.updatePivot(screenX, screenY);
        zoomIn();
    }

    public void zoomOut(float screenX, float screenY) {
        rxMapView.updatePivot(screenX, screenY);
        zoomOut();
    }

    public void rotate(float rotation) {
        rxMapView.rotation.add(rotation, ANIMATION_DURATION_DEFAULT);
        rxMapView.computeProjection(false);
        rxMapView.invalidate();
    }

    public void rotate(float rotation, float pivotX, float pivotY) {
        rotate(rotation);
        rxMapView.updatePivot(pivotX, pivotY);
    }

    public void setRotation(float rotation) {
        rxMapView.rotation.setRotation(rotation);
        rxMapView.computeProjection(true);
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