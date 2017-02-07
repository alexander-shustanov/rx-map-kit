package com.depthguru.rxmap.overlay;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.depthguru.rxmap.Projection;

import rx.Observable;

/**
 * BackgroundOverlay
 * </p>
 * alexander.shustanov on 22.12.16
 */
public class BackgroundOverlay extends SimpleOverlay {

    private static final Paint backgroundPaint = new Paint();
    private static final Paint linesPaint = new Paint();

    static {
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(0xffd1d1d1);

        linesPaint.setColor(0xff9b987d);
    }

    @Override
    protected Observable<Projection> setupProjectionSubscribe(Observable<Projection> projectionObservable) {
        return projectionObservable.first();
    }

    @Override
    protected Drawer createDrawer(Projection projection) {
        return new Drawer(projection) {
            @Override
            public void draw(Canvas canvas, Projection projection) {
                canvas.drawRect(projection.getScreenRect(), backgroundPaint);
            }
        };
    }
}
