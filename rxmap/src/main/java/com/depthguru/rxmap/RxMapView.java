package com.depthguru.rxmap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
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

import java.util.ArrayList;
import java.util.List;

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
    final Scroller scroller = new Scroller(0, 0, zoom.getDiscreteZoom());
    final Rotation rotation = new Rotation();

    private final TouchScroller touchScroller;
    private final MapController controller = new MapController(this);
    private final PointF reuse = new PointF();

    private final PublishSubject<Projection> projectionSubject = PublishSubject.create();
    private final BehaviorSubject<ScrollEvent> scrollEventObservable = BehaviorSubject.create();
    private final BehaviorSubject<FlingEvent> flingEventObservable = BehaviorSubject.create();
    private final BehaviorSubject<Void> flingEndEventObservable = BehaviorSubject.create();

    private final OverlayManager overlayManager = new OverlayManager(projectionSubject, this);

    private final List<OnFirstLayoutListener> onFirstLayoutListeners = new ArrayList<>();
    Projection projection;
    boolean multiTouch = true;
    boolean rotationAllowed = true;
    private boolean layoutOccurred = false;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(null);
        }
        setScrollX(scroller.getCurrX());
        setScrollY(scroller.getCurrY());
        computeProjection(false);
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle state = new Bundle();
        state.putInt("scrollX", (int) (scroller.getCurrX() + pivot.x));
        state.putInt("scrollY", ((int) (scroller.getCurrY() + pivot.y)));
        state.putFloat("zoom", zoom.getCurrentZoom());
        Parcelable superState = super.onSaveInstanceState();
        if (superState != null) {
            state.putParcelable("super", superState);
        }
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        Parcelable superState = bundle.getParcelable("super");
        if(superState != null) {
            super.onRestoreInstanceState(superState);
        }
        zoom.setZoom(bundle.getFloat("zoom"));
        scroller.setZoom(zoom.getDiscreteZoom());
        scroller.scrollTo(bundle.getInt("scrollX"), bundle.getInt("scrollY"));
    }

    public MapController getController() {
        return controller;
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
        return zoom.getOnZoomEventObservable();
    }

    public Projection getProjection() {
        return projection;
    }

    public float getOrientation() {
        return rotation.getRotation();
    }

    public void setOrientation(float orientation) {
        if(!rotationAllowed) {
            return;
        }
        rotation.setRotation(orientation);

        computeProjection(true);
    }

    public boolean isRotationAllowed() {
        return rotationAllowed;
    }

    public void setRotationAllowed(boolean rotationAllowed) {
        this.rotationAllowed = rotationAllowed;
    }

    public IGeoPoint getMapCenter() {
        return projection.fromPixels(getWidth() / 2, getHeight() / 2, null);
    }

    public void addOnFirstLayoutListener(OnFirstLayoutListener onFirstLayoutListener) {
        if (!layoutOccurred) {
            onFirstLayoutListeners.add(onFirstLayoutListener);
        }
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
                scroller.reconfigureWithZoomFactor(endZoom, pivot.x, pivot.y);
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
        int pivotX = getWidth() / 2;
        int pivotY = getHeight() / 2;
        float dx = pivotX - pivot.x;
        float dy = pivotY - pivot.y;

        projection.unRotateAndScaleVector(dx, dy, reuse);
        scroller.scrollTo(scroller.getCurrX() - reuse.x, scroller.getCurrY() - reuse.y);
        updatePivot(pivotX, pivotY);

        scrollTo(scroller.getCurrX(), scroller.getCurrY());
        computeProjection(true);
        if (!layoutOccurred) {
            layoutOccurred = true;
            for (OnFirstLayoutListener listener : onFirstLayoutListeners) {
                listener.onFirstLayout(changed, l, t, r, b);
            }
            onFirstLayoutListeners.clear();
        }
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

    public float getMinZoom() {
        return zoom.getMinZoom();
    }

    public void setMinZoom(float minZoom) {
        zoom.setMinZoom(minZoom);
    }

    public float getMaxZoom() {
        return zoom.getMaxZoom();
    }

    public void setMaxZoom(float maxZoom) {
        zoom.setMaxZoom(maxZoom);
    }

    public OverlayManager getOverlayManager() {
        return overlayManager;
    }

    public Rect getScreenRect(final Rect reuse) {
        final Rect out = getIntrinsicScreenRect(reuse);
        float mapOrientation = rotation.getRotation();
        if (mapOrientation != 0 && mapOrientation != 180) {
            GeometryMath.getBoundingBoxForRotatedRect(
                    out, (int) pivot.x, (int) pivot.y,
                    -mapOrientation, out
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
        scrollEventObservable.onCompleted();
        flingEndEventObservable.onCompleted();
        flingEndEventObservable.onCompleted();
        zoom.detach();
        overlayManager.detach();
        touchScroller.detach();
    }

    public void setMultiTouch(boolean multiTouch) {
        this.multiTouch = multiTouch;
    }

    public void setTilesScaledToDpi(boolean tilesScaledToDpi) {
        if (tilesScaledToDpi) {
            float density = Math.max(getResources().getDisplayMetrics().density, 1f);
            TileSystem.setScaledToDensity(density);
        } else {
            TileSystem.restoreTileSize();
        }
        scroller.updateWorldSize(zoom.getDiscreteZoom());
    }

    @Override
    public void scrollTo(@Px int x, @Px int y) {
        super.scrollTo(x, y);
        scrollEventObservable.onNext(new ScrollEvent(x, y));
    }

    public interface OnFirstLayoutListener {
        void onFirstLayout(boolean changed, int l, int t, int r, int b);
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
        protected void onZoomAndRotate(float zoom, float rotation, PointF pivot) {
            if (!multiTouch) {
                return;
            }
            int zoomDiff = RxMapView.this.zoom.add(zoom);
            int endZoom = RxMapView.this.zoom.getDiscreteZoom();
            if (zoomDiff != 0) {
                scroller.reconfigureWithZoomFactor(endZoom, pivot.x, pivot.y);
            }
            if (rotationAllowed) {
                RxMapView.this.rotation.setRotation(RxMapView.this.rotation.getRotation() + rotation);
            }

            computeProjection(false);
            updatePivot(pivot.x, pivot.y);
        }

        @Override
        protected void onStartMotion() {
            scroller.stopScroll();
        }

        @Override
        protected void onTap(float x, float y) {
            overlayManager.onTap(x, y, projection);
        }

        @Override
        protected void onDoubleTap(float x, float y) {
            updatePivot(x, y);
            zoom.zoomIn();
            invalidate();
        }
    }
}
