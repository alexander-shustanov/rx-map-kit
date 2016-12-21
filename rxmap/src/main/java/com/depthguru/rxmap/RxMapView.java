package com.depthguru.rxmap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.*;

import com.depthguru.rxmap.overlay.OverlayManager;
import com.depthguru.rxmap.touch.TouchScroller;

import rx.subjects.PublishSubject;

/**
 * RxMapView
 * </p>
 * alexander.shustanov on 15.12.16
 */
public class RxMapView extends ViewGroup {
    private final PublishSubject<Projection> projectionSubject = PublishSubject.create();
    private final Scroller scroller;
    private final TouchScroller touchScroller;

    private final OverlayManager overlayManager;

    private Projection projection;

    private float zoom = 5;

    public RxMapView(Context context) {
        super(context);
        overlayManager = new OverlayManager(projectionSubject, this);
        touchScroller = new MapTouchScroller(context);
        scroller = new Scroller();
//        scroller.fling(0,0,100,100,0,10000,0,10000);
//        scroller.startScroll(0, 0, 5000, 5000, 15000);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return touchScroller.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {

        if (scroller.computeScrollOffset()) {
            scrollTo((int) scroller.getCurrX(), ((int) scroller.getCurrY()));
            computeProjection();
            invalidate();
        }
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

        overlayManager.draw(canvas, projection);


        canvas.restore();

        super.dispatchDraw(canvas);

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

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        projectionSubject.onCompleted();
        overlayManager.detach();
        touchScroller.detach();
    }

    private class MapTouchScroller extends TouchScroller {

        private MapTouchScroller(Context context) {
            super(context);
        }

        @Override
        protected void onScroll(float dx, float dy) {
            scroller.scrollBy(((int) dx), (int) dy);
//            if (!scroller.computeScrollOffset()) {
//            } else {
//
//            }
//            scrollBy(((int) dx), (int) dy);
            invalidate();
//            scrollBy();
//            scrollTo(dx, dy);
        }

        @Override
        protected void onFling(float xVelocity, float yVelocity) {
            scroller.fling(xVelocity, yVelocity);
//            scroller.fling(scroller.getFinalX(), scroller.getFinalY(), ((int) xVelocity), ((int) yVelocity), -Integer.MAX_VALUE, Integer.MAX_VALUE, -Integer.MAX_VALUE, Integer.MAX_VALUE);
            invalidate();
        }
    }
}
