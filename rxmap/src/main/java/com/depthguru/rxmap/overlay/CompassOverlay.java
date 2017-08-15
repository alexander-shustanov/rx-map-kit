package com.depthguru.rxmap.overlay;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import com.depthguru.rxmap.Projection;
import com.depthguru.rxmap.RxMapView;

/**
 * Created by kandalin on 10.05.17.
 */

public class CompassOverlay extends SimpleOverlay {

    private static final float baseCompassCenterX = 35.0f;
    private static final float baseCompassCenterY = 35.0f;
    private static final float baseCompassRadius = 18.0f;
    private static final Paint smoothPaint = new Paint(Paint.FILTER_BITMAP_FLAG);

    private final RxMapView rxMapView;
    private final float scale;
    private final CompassDrawer compassDrawer = new CompassDrawer();

    private final Matrix compassMatrix = new Matrix();
    private final float compassCenterX;
    private final float compassCenterY;
    private final float compassRadius;
    private final float сompassFrameCenterX;
    private final float сompassFrameCenterY;
    private final float сompassRoseCenterX;
    private final float сompassRoseCenterY;

    private Bitmap compassFrameBitmap;
    private Bitmap compassRoseBitmap;

    public CompassOverlay(RxMapView rxMapView) {
        this.rxMapView = rxMapView;
        this.scale = rxMapView.getContext().getResources().getDisplayMetrics().density;

        compassCenterX = baseCompassCenterX * scale;
        compassCenterY = baseCompassCenterY * scale;
        compassRadius = baseCompassRadius * scale;

        createCompassFramePicture();
        createCompassRosePicture();

        сompassFrameCenterX = compassFrameBitmap.getWidth() / 2 - 0.5f;
        сompassFrameCenterY = compassFrameBitmap.getHeight() / 2 - 0.5f;
        сompassRoseCenterX = compassRoseBitmap.getWidth() / 2 - 0.5f;
        сompassRoseCenterY = compassRoseBitmap.getHeight() / 2 - 0.5f;
    }

    private void draw(Canvas canvas, Projection projection) {
        compassMatrix.setTranslate(-сompassFrameCenterX, -сompassFrameCenterY);
        compassMatrix.postTranslate(compassCenterX, compassCenterY);

        canvas.save();
        canvas.concat(projection.getUnRotateAndScaleMatrix());
        canvas.concat(compassMatrix);
        canvas.drawBitmap(compassFrameBitmap, 0, 0, smoothPaint);
        canvas.restore();

        compassMatrix.setRotate(rxMapView.getOrientation(), сompassRoseCenterX, сompassRoseCenterY);
        compassMatrix.postTranslate(-сompassRoseCenterX, -сompassRoseCenterY);
        compassMatrix.postTranslate(compassCenterX, compassCenterY);

        canvas.save();
        canvas.concat(projection.getUnRotateAndScaleMatrix());
        canvas.concat(compassMatrix);
        canvas.drawBitmap(compassRoseBitmap, 0, 0, smoothPaint);
        canvas.restore();
    }

    private void createCompassFramePicture() {
        // The inside of the compass is white and transparent
        final Paint innerPaint = new Paint();
        innerPaint.setColor(Color.WHITE);
        innerPaint.setAntiAlias(true);
        innerPaint.setStyle(Paint.Style.FILL);
        innerPaint.setAlpha(200);

        // The outer part (circle and little triangles) is gray and transparent
        final Paint outerPaint = new Paint();
        outerPaint.setColor(Color.GRAY);
        outerPaint.setAntiAlias(true);
        outerPaint.setStyle(Paint.Style.STROKE);
        outerPaint.setStrokeWidth(2.0f);
        outerPaint.setAlpha(200);

        final int picBorderWidthAndHeight = (int) ((baseCompassRadius + 5) * 2 * scale);
        final int center = picBorderWidthAndHeight / 2;

        compassFrameBitmap = Bitmap.createBitmap(picBorderWidthAndHeight, picBorderWidthAndHeight,
                Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(compassFrameBitmap);

        // draw compass inner circle and border
        canvas.drawCircle(center, center, compassRadius, innerPaint);
        canvas.drawCircle(center, center, compassRadius, outerPaint);

        // Draw little triangles north, south, west and east (don't move)
        // to make those move use "-bearing + 0" etc. (Note: that would mean to draw the triangles
        // in the onDraw() method)
        drawTriangle(canvas, center, center, compassRadius, 0, outerPaint);
        drawTriangle(canvas, center, center, compassRadius, 90, outerPaint);
        drawTriangle(canvas, center, center, compassRadius, 180, outerPaint);
        drawTriangle(canvas, center, center, compassRadius, 270, outerPaint);
    }

    private void drawTriangle(final Canvas canvas, final float x, final float y,
                              final float radius, final float degrees, final Paint paint) {
        canvas.save();
        final Point point = calculatePointOnCircle(x, y, radius, degrees);
        canvas.rotate(degrees, point.x, point.y);
        final Path p = new Path();
        p.moveTo(point.x - 2 * scale, point.y);
        p.lineTo(point.x + 2 * scale, point.y);
        p.lineTo(point.x, point.y - 5 * scale);
        p.close();
        canvas.drawPath(p, paint);
        canvas.restore();
    }

    private Point calculatePointOnCircle(final float centerX, final float centerY,
                                         final float radius, final float degrees) {
        // for trigonometry, 0 is pointing east, so subtract 90
        // compass degrees are the wrong way round
        final double dblRadians = Math.toRadians(-degrees + 90);

        final int intX = (int) (radius * Math.cos(dblRadians));
        final int intY = (int) (radius * Math.sin(dblRadians));

        return new Point((int) centerX + intX, (int) centerY - intY);
    }

    private void createCompassRosePicture() {
        // Paint design of north triangle (it's common to paint north in red color)
        final Paint northPaint = new Paint();
        northPaint.setColor(0xFFA00000);
        northPaint.setAntiAlias(true);
        northPaint.setStyle(Paint.Style.FILL);
        northPaint.setAlpha(220);

        // Paint design of south triangle (black)
        final Paint southPaint = new Paint();
        southPaint.setColor(Color.BLACK);
        southPaint.setAntiAlias(true);
        southPaint.setStyle(Paint.Style.FILL);
        southPaint.setAlpha(220);

        // Create a little white dot in the middle of the compass rose
        final Paint centerPaint = new Paint();
        centerPaint.setColor(Color.WHITE);
        centerPaint.setAntiAlias(true);
        centerPaint.setStyle(Paint.Style.FILL);
        centerPaint.setAlpha(220);

        final int picBorderWidthAndHeight = (int) ((baseCompassRadius + 5) * 2 * scale);
        final int center = picBorderWidthAndHeight / 2;

        compassRoseBitmap = Bitmap.createBitmap(picBorderWidthAndHeight, picBorderWidthAndHeight,
                Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(compassRoseBitmap);

        // Blue triangle pointing north
        final Path pathNorth = new Path();
        pathNorth.moveTo(center, center - (baseCompassRadius - 3) * scale);
        pathNorth.lineTo(center + 4 * scale, center);
        pathNorth.lineTo(center - 4 * scale, center);
        pathNorth.lineTo(center, center - (baseCompassRadius - 3) * scale);
        pathNorth.close();
        canvas.drawPath(pathNorth, northPaint);

        // Red triangle pointing south
        final Path pathSouth = new Path();
        pathSouth.moveTo(center, center + (baseCompassRadius - 3) * scale);
        pathSouth.lineTo(center + 4 * scale, center);
        pathSouth.lineTo(center - 4 * scale, center);
        pathSouth.lineTo(center, center + (baseCompassRadius - 3) * scale);
        pathSouth.close();
        canvas.drawPath(pathSouth, southPaint);

        // Draw a little white dot in the middle
        canvas.drawCircle(center, center, 2, centerPaint);
    }

    @Override
    protected Drawer createDrawer(Projection projection) {
        return compassDrawer;
    }

    private class CompassDrawer extends Drawer {

        CompassDrawer() {
            super(null);
        }

        @Override
        public void draw(Canvas canvas, Projection projection) {
            CompassOverlay.this.draw(canvas, projection);
        }
    }
}
