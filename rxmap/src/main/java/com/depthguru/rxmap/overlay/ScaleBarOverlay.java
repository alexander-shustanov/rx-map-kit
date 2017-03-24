package com.depthguru.rxmap.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.DisplayMetrics;

import com.depthguru.rxmap.GeoConstants;
import com.depthguru.rxmap.GeoPoint;
import com.depthguru.rxmap.IGeoPoint;
import com.depthguru.rxmap.Projection;
import com.depthguru.rxmap.R;
import com.depthguru.rxmap.RxMapView;

/**
 * Created by kandalin on 20.03.17.
 */

public class ScaleBarOverlay extends SimpleOverlay implements GeoConstants {

    private static final Rect sTextBoundsRect = new Rect();
    private static final int minZoom = 0;
    private static final int xOffset = 10;
    private static final int yOffset = 10;

    private final ScaleBarDrawer scaleBarDrawer = new ScaleBarDrawer();

    public enum UnitsOfMeasure {
        metric, imperial, nautical
    }

    private final RxMapView rxMapView;
    private final Context context;

    private final Path barPath = new Path();
    private final Rect latitudeBarRect = new Rect();

    private float xdpi;
    private float ydpi;
    private int screenWidth;
    private int screenHeight;

    private Paint barPaint;
    private Paint textPaint;

    private float maxLength;

    private float lastZoomLevel = -1;
    private float lastLatitude = 0;

    private UnitsOfMeasure unitsOfMeasure = UnitsOfMeasure.metric;

    public ScaleBarOverlay(RxMapView rxMapView) {
        this.rxMapView = rxMapView;
        this.context = rxMapView.getContext();
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();

        this.barPaint = new Paint();
        this.barPaint.setColor(Color.BLACK);
        this.barPaint.setAntiAlias(true);
        this.barPaint.setStyle(Paint.Style.STROKE);
        this.barPaint.setAlpha(255);
        this.barPaint.setStrokeWidth(2 * dm.density);

        this.textPaint = new Paint();
        this.textPaint.setColor(Color.BLACK);
        this.textPaint.setAntiAlias(true);
        this.textPaint.setStyle(Paint.Style.FILL);
        this.textPaint.setAlpha(255);
        this.textPaint.setTextSize(10 * dm.density);

        this.xdpi = dm.xdpi;
        this.ydpi = dm.ydpi;

        this.screenWidth = dm.widthPixels;
        this.screenHeight = dm.heightPixels;

        // set default max length to 1 inch
        maxLength = 2.54f;
    }

    public void setUnitsOfMeasure(UnitsOfMeasure unitsOfMeasure) {
        this.unitsOfMeasure = unitsOfMeasure;
        lastZoomLevel = -1; // Force redraw of scalebar
    }

    public UnitsOfMeasure getUnitsOfMeasure() {
        return unitsOfMeasure;
    }

    private void drawLatitudeText(final Canvas canvas, final Projection projection) {
        // calculate dots per centimeter
        int xdpcm = (int) (xdpi / 2.54);
        // get length in pixel
        int xLen = (int) (maxLength * xdpcm);

        double scaleFactor = projection.getScaleFactor();
        int x1 = (int) (((screenWidth / 2) - (xLen / 2)) / scaleFactor);
        int x2 = (int) (((screenWidth / 2) + (xLen / 2)) / scaleFactor);

        // Two points, xLen apart, at scale bar screen location
        IGeoPoint p1 = projection.fromPixels(x1, yOffset, null);
        IGeoPoint p2 = projection.fromPixels(x2, yOffset, null);

        // get distance in meters between points
        final int xMeters = ((GeoPoint) p1).distanceTo(p2);
        final int xBarLengthPixels = (int) (xLen * (double) xMeters / xMeters);

        // create text
        final String xMsg = scaleBarLengthText((int) (double) xMeters);
        textPaint.getTextBounds(xMsg, 0, xMsg.length(), sTextBoundsRect);
        final int xTextSpacing = (int) (sTextBoundsRect.height() / 5.0);

        float x = xBarLengthPixels / 2 - sTextBoundsRect.width() / 2;
        float y = sTextBoundsRect.height() + xTextSpacing;
        canvas.drawText(xMsg, x, y, textPaint);
    }

    private void rebuildBarPath(final Projection projection) {
        // We want the scale bar to be as long as the closest round-number miles/kilometers
        // to 1-inch at the latitude at the current center of the screen.

        // calculate dots per centimeter
        int xdpcm = (int) (xdpi / 2.54);
        int ydpcm = (int) (ydpi / 2.54);

        // get length in pixel
        int xLen = (int) (maxLength * xdpcm);
        int yLen = (int) (maxLength * ydpcm);

        // Two points, xLen apart, at scale b-ar screen location
        double scaleFactor = projection.getScaleFactor();
        IGeoPoint p1 = projection.fromPixels(((int) (((screenWidth / 2) - (xLen / 2)) / scaleFactor)), yOffset, null);
        IGeoPoint p2 = projection.fromPixels(((int) (((screenWidth / 2) + (xLen / 2)) / scaleFactor)), yOffset, null);

        // get distance in meters between points
        final int xMeters = ((GeoPoint) p1).distanceTo(p2);
        final int xBarLengthPixels = (int) (xLen * (double) xMeters / xMeters);

        // Two points, yLen apart, at scale bar screen location
        p1 = projection.fromPixels(((int) (screenWidth / 2)), (int) (screenHeight / 2) - (yLen / 2), null);
        p2 = projection.fromPixels(((int) (screenWidth / 2)), (int) (screenHeight / 2) + (yLen / 2), null);

        // create text
        final String xMsg = scaleBarLengthText((int) (double) xMeters);
        final Rect xTextRect = new Rect();
        textPaint.getTextBounds(xMsg, 0, xMsg.length(), xTextRect);
        int xTextSpacing = (int) (xTextRect.height() / 5.0);

        // create text
        final String yMsg = scaleBarLengthText((int) (double) ((GeoPoint) p1).distanceTo(p2));
        final Rect yTextRect = new Rect();
        textPaint.getTextBounds(yMsg, 0, yMsg.length(), yTextRect);
        int xTextHeight = xTextRect.height();

        barPath.rewind();

        //** alignBottom ad-ons
        int barOriginX = 0;
        int barOriginY = 0;

        // draw latitude bar
        barPath.moveTo(xBarLengthPixels, barOriginY + xTextHeight + xTextSpacing * 2);
        barPath.lineTo(xBarLengthPixels, barOriginY);
        barPath.lineTo(barOriginX, barOriginY);
        barPath.lineTo(barOriginX, barOriginY + xTextHeight + xTextSpacing * 2);

        latitudeBarRect.set(barOriginX, barOriginY, xBarLengthPixels, barOriginY + xTextHeight + xTextSpacing * 2);
    }

    private String scaleBarLengthText(final int meters) {
        switch (unitsOfMeasure) {
            default:
            case metric:
                if (meters >= 1000 * 5) {
                    return context.getResources().getString(R.string.format_distance_kilometers, (meters / 1000));
                } else if (meters >= 1000 / 5) {
                    return context.getResources().getString(R.string.format_distance_kilometers,
                            (int) (meters / 100.0) / 10.0);
                } else {
                    return context.getResources().getString(R.string.format_distance_meters, meters);
                }
            case imperial:
                if (meters >= METERS_PER_STATUTE_MILE * 5) {
                    return context.getResources().getString(R.string.format_distance_miles,
                            (int) (meters / METERS_PER_STATUTE_MILE));

                } else if (meters >= METERS_PER_STATUTE_MILE / 5) {
                    return context.getResources().getString(R.string.format_distance_miles,
                            ((int) (meters / (METERS_PER_STATUTE_MILE / 10.0))) / 10.0);
                } else {
                    return context.getResources().getString(R.string.format_distance_feet,
                            (int) (meters * FEET_PER_METER));
                }
            case nautical:
                if (meters >= METERS_PER_NAUTICAL_MILE * 5) {
                    return context.getResources().getString(R.string.format_distance_nautical_miles,
                            ((int) (meters / METERS_PER_NAUTICAL_MILE)));
                } else if (meters >= METERS_PER_NAUTICAL_MILE / 5) {
                    return context.getResources().getString(R.string.format_distance_nautical_miles,
                            (((int) (meters / (METERS_PER_NAUTICAL_MILE / 10.0))) / 10.0));
                } else {
                    return context.getResources().getString(R.string.format_distance_feet,
                            ((int) (meters * FEET_PER_METER)));
                }
        }
    }

    private void draw(Canvas canvas, Projection projection) {
        final float zoomLevel = projection.getZoom();

        if (zoomLevel < minZoom) {
            return;
        }

        screenWidth = rxMapView.getWidth();
        screenHeight = rxMapView.getHeight();
        final IGeoPoint center = projection.fromPixels(screenWidth / 2, screenHeight / 2, null);

        if (zoomLevel != lastZoomLevel
                || (int) (center.getLatitudeE6() / 1E6) != (int) (lastLatitude / 1E6)) {
            lastZoomLevel = zoomLevel;
            lastLatitude = center.getLatitudeE6();
            rebuildBarPath(projection);
        }

        canvas.save();
        canvas.concat(projection.getUnRotateAndScaleMatrix());
        canvas.translate(xOffset, yOffset);
        canvas.drawPath(barPath, barPaint);
        drawLatitudeText(canvas, projection);
        canvas.restore();
    }

    @Override
    protected Drawer createDrawer(Projection projection) {
        return scaleBarDrawer;
    }

    private class ScaleBarDrawer extends Drawer {

        ScaleBarDrawer() {
            super(null);
        }

        @Override
        public void draw(Canvas canvas, Projection projection) {
            ScaleBarOverlay.this.draw(canvas, projection);
        }
    }
}
