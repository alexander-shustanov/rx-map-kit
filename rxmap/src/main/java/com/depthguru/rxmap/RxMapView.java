package com.depthguru.rxmap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.Px;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.depthguru.rxmap.event.FlingEvent;
import com.depthguru.rxmap.event.ScrollEvent;
import com.depthguru.rxmap.overlay.OverlayManager;
import com.depthguru.rxmap.touch.Scroller;
import com.depthguru.rxmap.touch.TouchScroller;
import com.depthguru.rxmap.touch.Zoom;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

/**
 * RxMapView
 * </p>
 * alexander.shustanov on 15.12.16
 */
public class RxMapView extends ViewGroup {
    final Matrix scaleMatrix = new Matrix();
    final PointF pivot = new PointF();
    final Zoom zoom = new Zoom(5.5f);

    private final PublishSubject<Projection> projectionSubject = PublishSubject.create();
    private final Scroller scroller;
    private final TouchScroller touchScroller;
    private final OverlayManager overlayManager;

    private final BehaviorSubject<ScrollEvent> scrollEventObservable = BehaviorSubject.create();
    private final BehaviorSubject<FlingEvent> flingEventObservable = BehaviorSubject.create();
    private final BehaviorSubject<Void> flingEndEventObservable = BehaviorSubject.create();
    private final BehaviorSubject<Integer> onZoomEventObservable = BehaviorSubject.create();

    private Projection projection;

    public RxMapView(Context context) {
        super(context);
        setBackground(null);

        overlayManager = new OverlayManager(projectionSubject, this);
        touchScroller = new MapTouchScroller(context);
        scroller = new Scroller(1000, 500);
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return touchScroller.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        int startZoom = zoom.getDiscreteZoom();
        boolean isFlinging = scroller.isFlinging();
        if (scroller.computeScrollOffset() || zoom.computeZoomOffset()) {
            int endZoom = zoom.getDiscreteZoom();
            int zoomDiff = endZoom - startZoom;

            if (scroller.isFinished() && zoom.isFinished()) {
                int pivotX = getWidth() / 2;
                int pivotY = getHeight() / 2;

                float scaleFactor = zoom.getScaleFactor();
                if (zoomDiff != 1 && scaleFactor != 1f) {
                    updatePivot(pivotX, pivotY, scaleFactor);
                } else {
                    updatePivot(pivotX, pivotY, (float) Math.pow(2f, zoomDiff));
                }
            }

            if (zoomDiff != 0) {
                scroller.reconfigureWithZoomFactor(zoomDiff, pivot, TileSystem.getTileSize() << endZoom);
            } else {
                postInvalidate();
            }
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            computeProjection();
        }

        if (isFlinging && !scroller.isFlinging()) {
            flingEndEventObservable.onNext(null);
        }
    }

    private void updatePivot(float pivotX, float pivotY) {
        updatePivot(pivotX, pivotY, zoom.getScaleFactor());
    }

    private void updatePivot(float pivotX, float pivotY, float scaleFactor) {
        float dx = (pivotX - pivot.x) * (1f - 1f / scaleFactor);
        float dy = (pivotY - pivot.y) * (1f - 1f / scaleFactor);
        scroller.offsetBy(-dx, -dy);
        pivot.set(pivotX, pivotY);
        invalidate();
    }

    private void computeProjection() {
        scaleMatrix.reset();

        float scale = zoom.getScaleFactor();
        scaleMatrix.preScale(scale, scale, pivot.x, pivot.y);

        projection = new Projection(this);
        projectionSubject.onNext(projection);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        computeProjection();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(getScrollX(), getScrollY());

        canvas.concat(scaleMatrix);

        overlayManager.draw(canvas, projection);

        canvas.restore();

        super.dispatchDraw(canvas);

    }

    public float getZoom() {
        return zoom.getCurrentZoom();
    }

    public OverlayManager getOverlayManager() {
        return overlayManager;
    }

    public Rect getScreenRect() {
        Rect rect = getIntrinsicScreenRect();
        //todo orientation
        return rect;
    }

    private Rect getIntrinsicScreenRect() {
        return new Rect(0, 0, getWidth(), getHeight());
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
            float factor = zoom.getScaleFactor();
            scroller.scrollBy(dx / factor, dy / factor);
            invalidate();
        }

        @Override
        protected void onFling(float xVelocity, float yVelocity) {
            scroller.fling(xVelocity, yVelocity);
            flingEventObservable.onNext(new FlingEvent(xVelocity, yVelocity));
            invalidate();
        }

        @Override
        protected void onZoom(float zoom, PointF pivot) {
            int zoomDiff = RxMapView.this.zoom.add(zoom);
            int endZoom = RxMapView.this.zoom.getDiscreteZoom();
            updatePivot(pivot.x, pivot.y);

            if (zoomDiff != 0) {
                scroller.reconfigureWithZoomFactor(zoomDiff, pivot, TileSystem.getTileSize() << endZoom);
                onZoomEventObservable.onNext(endZoom);
            }
        }

        @Override
        protected void onStartMotion() {
            scroller.stopScroll();
        }

        @Override
        protected void onDoubleTap(float x, float y) {
            super.onDoubleTap(x, y);
            updatePivot(x, y);
            zoom.zoomIn();
            invalidate();
        }
    }
}
