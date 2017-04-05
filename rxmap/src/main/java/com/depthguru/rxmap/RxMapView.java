package com.depthguru.rxmap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.Px;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.depthguru.rxmap.event.FlingEvent;
import com.depthguru.rxmap.event.ScrollEvent;
import com.depthguru.rxmap.overlay.OverlayManager;
import com.depthguru.rxmap.touch.Rotation;
import com.depthguru.rxmap.touch.Scroller;
import com.depthguru.rxmap.touch.TouchScroller;
import com.depthguru.rxmap.touch.Zoom;
import com.depthguru.rxmap.util.GeometryMath;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

/**
 * RxMapView
 * </p>
 * alexander.shustanov on 15.12.16
 */
public class RxMapView extends ViewGroup {

    public static final boolean DEBUG = false;

    final Matrix rotateAndScaleMatrix = new Matrix();
    final PointF pivot = new PointF();
    final Zoom zoom = new Zoom(2);
    final Scroller scroller = new Scroller(1000, 500, zoom.getDiscreteZoom());
    final Rotation rotation = new Rotation();

    private final PublishSubject<Projection> projectionSubject = PublishSubject.create();
    private final TouchScroller touchScroller;
    private final OverlayManager overlayManager = new OverlayManager(projectionSubject, this);
    private final BehaviorSubject<ScrollEvent> scrollEventObservable = BehaviorSubject.create();
    private final BehaviorSubject<FlingEvent> flingEventObservable = BehaviorSubject.create();
    private final BehaviorSubject<Void> flingEndEventObservable = BehaviorSubject.create();
    private final BehaviorSubject<Integer> onZoomEventObservable = BehaviorSubject.create();

    Projection projection;
    private final PointF reuse = new PointF();

    public RxMapView(Context context) {
        super(context);
        touchScroller = new MapTouchScroller(context);
        init();
    }

    public RxMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        touchScroller = new MapTouchScroller(context);
        init();
    }

    private void init() {
        setBackground(null);
        setScrollX(scroller.getCurrX());
        setScrollY(scroller.getCurrY());
        computeProjection(true);
    }

    public Observable<Projection> getProjectionObservable() {
        return projectionSubject;
    }

    public Observable<ScrollEvent> getScrollEventObservable() {
        return scrollEventObservable;
    }

    public Observable<FlingEvent> getFlingEventObservable() {
        return flingEventObservable;
    }

    public Observable<Void> getFlingEndEventObservable() {
        return flingEndEventObservable;
    }

    public Observable<Integer> getOnZoomEventObservable() {
        return onZoomEventObservable;
    }

    public float getRotation() {
        return rotation.getRotation();
    }

    public IGeoPoint getMapCenter() {
        return projection.fromPixels(getWidth() / 2, getHeight() / 2, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return touchScroller.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        int startZoom = zoom.getDiscreteZoom();
        boolean isFlinging = scroller.isFlinging();

        boolean inScroll = scroller.computeScrollOffset();
        boolean inZoom = zoom.computeZoomOffset();
        boolean inRotate = rotation.computeRotation();

        if (inScroll || inZoom || inRotate
                || getScrollX() != scroller.getCurrX()
                || getScrollY() != scroller.getCurrY()) {
            int endZoom = zoom.getDiscreteZoom();
            int zoomDiff = endZoom - startZoom;

            if (scroller.isFinished() && zoom.isFinished() && rotation.isFinished()) {
                updatePivot(getWidth() / 2, getHeight() / 2);
            }

            if (zoomDiff != 0) {
                scroller.setZoom(endZoom);
                scroller.reconfigureWithZoomFactor(zoomDiff, pivot.x, pivot.y);
            } else {
                postInvalidate();
            }

            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            computeProjection(true);
        }

        if (isFlinging && !scroller.isFlinging()) {
            flingEndEventObservable.onNext(null);
        }
    }

    void updatePivot(float pivotX, float pivotY) {
        projection.unRotateAndScalePoint(pivotX, pivotY, reuse);//real offset pivot
        float dx = pivotX - reuse.x;
        float dy = pivotY - reuse.y;
        scroller.offsetBy(-dx, -dy);

        pivot.set(pivotX, pivotY);
        computeProjection(false);
        invalidate();
    }

    void computeProjection(boolean broadcast) {
        rotateAndScaleMatrix.reset();

        float scale = zoom.getScaleFactor();
        rotateAndScaleMatrix.preScale(scale, scale, pivot.x, pivot.y);
        rotateAndScaleMatrix.preRotate(rotation.getRotation(), pivot.x, pivot.y);

        projection = new Projection(this);
        if (broadcast) {
            projectionSubject.onNext(projection);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        pivot.set(getWidth() / 2, getHeight() / 2);
        computeProjection(true);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(getScrollX(), getScrollY());
        canvas.concat(rotateAndScaleMatrix);
        overlayManager.draw(canvas, projection);
        canvas.restore();
        super.dispatchDraw(canvas);
    }

    public float getZoom() {
        return zoom.getCurrentZoom();
    }

    public int getDiscreteZoom() {
        return zoom.getDiscreteZoom();
    }

    public OverlayManager getOverlayManager() {
        return overlayManager;
    }

    public Rect getScreenRect(final Rect reuse) {
        final Rect out = getIntrinsicScreenRect(reuse);
        float mapOrientation = rotation.getRotation();
        if (mapOrientation != 0 && mapOrientation != 180) {
            GeometryMath.getBoundingBoxForRotatedRectangle(
                    out, out.centerX(), out.centerY(),
                    mapOrientation, out
            );
        }
        return out;
    }

    public Rect getIntrinsicScreenRect(final Rect reuse) {
        final Rect out = reuse == null ? new Rect() : reuse;
        out.set(0, 0, getWidth(), getHeight());
        return out;
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        projectionSubject.onCompleted();
        overlayManager.detach();
        touchScroller.detach();
    }

    public void setTilesScaledToDpi(boolean tilesScaledToDpi) {
        if (tilesScaledToDpi) {
            float density = Math.max(getResources().getDisplayMetrics().density, 1f);
            TileSystem.setScaledToDensity(density);
        } else {
            TileSystem.restoreTileSize();
        }
        scroller.setZoom(zoom.getDiscreteZoom());
    }

    @Override
    public void scrollTo(@Px int x, @Px int y) {
        super.scrollTo(x, y);
        scrollEventObservable.onNext(new ScrollEvent(x, y));
    }

    private class MapTouchScroller extends TouchScroller {

        private MapTouchScroller(Context context) {
            super(context);
        }

        @Override
        protected void onScroll(float dx, float dy) {
            projection.unRotateAndScaleVector(dx, dy, reuse);//scrollVector
            scroller.scrollBy(reuse.x, reuse.y);
            invalidate();
        }

        @Override
        protected void onFling(float xVelocity, float yVelocity) {
            projection.unRotateAndScaleVector(xVelocity, yVelocity, reuse);//flingVector
            scroller.fling(reuse.x, reuse.y);
            flingEventObservable.onNext(new FlingEvent(xVelocity, yVelocity));
            invalidate();
        }

        @Override
        protected void onZoom(float zoom, PointF pivot) {
            int zoomDiff = RxMapView.this.zoom.add(zoom);
            int endZoom = RxMapView.this.zoom.getDiscreteZoom();
            computeProjection(false);
            updatePivot(pivot.x, pivot.y);

            if (zoomDiff != 0) {
                scroller.setZoom(endZoom);
                scroller.reconfigureWithZoomFactor(zoomDiff, pivot.x, pivot.y);
                onZoomEventObservable.onNext(endZoom);
            }
        }

        @Override
        protected void onRotate(float rotation, PointF pivot) {
            RxMapView.this.rotation.add(rotation);
            computeProjection(false);
            invalidate();
            updatePivot(pivot.x, pivot.y);
        }

        @Override
        protected void onStartMotion() {
            scroller.stopScroll();
        }

        @Override
        protected void onDoubleTap(float x, float y) {
            updatePivot(x, y);
            zoom.zoomIn();
            invalidate();
        }
    }
}
