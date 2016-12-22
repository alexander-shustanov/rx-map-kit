package com.depthguru.rxmap.overlay;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.depthguru.rxmap.Projection;

import rx.Observable;

/**
 * BackgroundOverlay
 * </p>
 * alexander.shustanov on 22.12.16
 */
class BackgroundOverlay extends SimpleOverlay {
    private static final int GRID_DENSITY = 32;

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
                int offsetX = projection.getOffsetX();
                int offsetY = projection.getOffsetY();
                offsetX %= GRID_DENSITY;
                offsetY %= GRID_DENSITY;

                offsetX -= GRID_DENSITY;
                offsetY -= GRID_DENSITY;

                canvas.drawRect(projection.getScreenRect(), backgroundPaint);

                for (int i = -1; i <= projection.getScreenRect().width() / GRID_DENSITY; i++) {
                    canvas.drawLine(offsetX, 0, offsetX, projection.getScreenRect().height(), linesPaint);
                    offsetX += GRID_DENSITY;
                }

                for (int j = -1; j <= projection.getScreenRect().height() / GRID_DENSITY; j++) {
                    canvas.drawLine(0, offsetY, projection.getScreenRect().width(), offsetY, linesPaint);
                    offsetY += GRID_DENSITY;
                }
            }
        };
    }
}
