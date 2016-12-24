package com.depthguru.rxmap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.depthguru.rxmap.overlay.OverlayManager;
import com.depthguru.rxmap.touch.TouchScroller;

import rx.subjects.PublishSubject;

/**
 * RxMapView
 * </p>
 * alexander.shustanov on 15.12.16
 */
public class RxMapView extends ViewGroup {
    final Matrix scaleMatrix = new Matrix();
    final PointF pivot = new PointF();
    private final PublishSubject<Projection> projectionSubject = PublishSubject.create();
    private final Scroller scroller;
    private final TouchScroller touchScroller;
    private final OverlayManager overlayManager;
    private final float MIN_ZOOM = 0.0f;
    private final float MAX_ZOOM = 18.5f;
    float zoom = 5.5f;
    private Projection projection;

    public RxMapView(Context context) {
        super(context);
        setBackground(null);

        overlayManager = new OverlayManager(projectionSubject, this);
        touchScroller = new MapTouchScroller(context);
        scroller = new Scroller();
        scroller.scrollBy(1000, 500);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return touchScroller.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            if (scroller.isFinished()) {
                int pivotX = getWidth() / 2;
                int pivotY = getHeight() / 2;
                updatePivot(pivotX, pivotY);
            }
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            computeProjection();
            postInvalidate();
        }
    }

    private void updatePivot(float pivotX, float pivotY) {
        float scaleFactor = getScaleFactor();
        float dx = pivotX - pivot.x - (pivotX - pivot.x) / scaleFactor;
        float dy = pivotY - pivot.y - (pivotY - pivot.y) / scaleFactor;
        scroller.offsetBy(-dx, -dy);
        pivot.set(pivotX, pivotY);
        invalidate();
    }

    private void computeProjection() {
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
        scaleMatrix.reset();

        float scale = getScaleFactor();
        scaleMatrix.preScale(scale, scale, projection.getPivotX(), projection.getPivotY());
        canvas.concat(scaleMatrix);

        overlayManager.draw(canvas, projection);

        canvas.restore();

        super.dispatchDraw(canvas);

    }

    private float getScaleFactor() {
        return (float) Math.pow(2, zoom - Math.floor(zoom));
    }

    public float getZoom() {
        return zoom;
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

    private void onZoom(float zoom, PointF pivot) {
        int startZoom = (int) Math.floor(this.zoom);
        this.zoom += zoom;
        this.zoom = Math.max(Math.min(this.zoom, MAX_ZOOM), MIN_ZOOM);
        int endZoom = (int) Math.floor(this.zoom);
        int zoomDiff = endZoom - startZoom;
        updatePivot(pivot.x, pivot.y);
        computeProjection();
        if (zoomDiff != 0) {
            scroller.reconfigureWithZoomFactor(zoomDiff, pivot, TileSystem.getTileSize() << endZoom);
        }
        invalidate();
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
            float density = Math.max(getResources().getDisplayMetrics().density / 1.5f, 1f);
            TileSystem.setScaledToDensity(density);
        } else {
            TileSystem.restoreTileSize();
        }
    }

    private class MapTouchScroller extends TouchScroller {

        private MapTouchScroller(Context context) {
            super(context);
        }

        @Override
        protected void onScroll(float dx, float dy) {
            float factor = getScaleFactor();
            scroller.scrollBy(dx / factor, dy / factor);
            invalidate();
        }

        @Override
        protected void onFling(float xVelocity, float yVelocity) {
            scroller.fling(xVelocity, yVelocity);
            invalidate();
        }

        @Override
        protected void onZoom(float zoom, PointF pivot) {
            RxMapView.this.onZoom(zoom, pivot);
        }

        @Override
        protected void onStartMotion() {
            scroller.stopScroll();
        }
    }
}
