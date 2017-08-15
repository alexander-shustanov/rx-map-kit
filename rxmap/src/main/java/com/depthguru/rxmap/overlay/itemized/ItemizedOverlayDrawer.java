package com.depthguru.rxmap.overlay.itemized;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.util.ArrayMap;

import com.depthguru.rxmap.Projection;
import com.depthguru.rxmap.overlay.Drawer;

import java.util.List;

import rx.subjects.PublishSubject;

/**
 * ItemizedOverlayDrawer
 * </p>
 * alexander.shustanov on 26.01.17.
 */

class ItemizedOverlayDrawer<T, D> extends Drawer {
    private ItemsBatch<T, D> batch;
    private final int minZoom, maxZoom;
    private final boolean rotate;
    private PublishSubject<D> tapItems;

    public ItemizedOverlayDrawer(Projection baseProjection, ItemsBatch<T, D> batch, int minZoom, int maxZoom, boolean rotate, PublishSubject<D> tapItems) {
        super(baseProjection);
        this.batch = batch;
        this.minZoom = minZoom;
        this.maxZoom = maxZoom;
        this.rotate = rotate;
        this.tapItems = tapItems;
    }

    @Override
    public void draw(Canvas canvas, Projection projection) {
        if(projection.getDiscreteZoom() < minZoom || projection.getDiscreteZoom() > maxZoom) {
            return;
        }
        int startX = projection.getOffsetX();
        int startY = projection.getOffsetY();

        Projection baseProjection = baseProjection();

        int baseOffsetX = baseProjection.getOffsetX();
        int baseOffsetY = baseProjection.getOffsetY();

        int projectionDelta = projection.getDiscreteZoom() - baseProjection.getDiscreteZoom();

        if (projectionDelta >= 0) {
            baseOffsetX <<= projectionDelta;
            baseOffsetY <<= projectionDelta;
        } else {
            baseOffsetX >>= -projectionDelta;
            baseOffsetY >>= -projectionDelta;
        }

        canvas.save();

        canvas.translate(startX - baseOffsetX, startY - baseOffsetY);

        for (ItemsBatch.ItemWithPixelCoordinates<T, D> itemWithPixelCoordinates : batch.getItemWithPixelCoordinates()) {
            int x = itemWithPixelCoordinates.getPixelX();
            int y = itemWithPixelCoordinates.getPixelY();

            if (projectionDelta >= 0) {
                x <<= projectionDelta;
                y <<= projectionDelta;
            } else {
                x >>= -projectionDelta;
                y >>= -projectionDelta;
            }

            T itemType = itemWithPixelCoordinates.getItemType();
            Drawable icon = batch.getIcon(itemType);
            if (icon == null) {
                continue;
            }
            int halfWidth = (int) (icon.getIntrinsicWidth() / 2 / projection.getScaleFactor());
            int halfHeight = (int) (icon.getIntrinsicHeight() / 2 / projection.getScaleFactor());
            if (!canvas.quickReject(x - halfWidth, y - halfHeight, x + halfWidth, y + halfHeight, Canvas.EdgeType.BW)) {
                if (rotate) {
                    canvas.save();
                    canvas.rotate(-projection.getMapOrientation(), x, y);
                }

                icon.setBounds(x - halfWidth, y - halfHeight, x + halfWidth, y + halfHeight);
                icon.draw(canvas);

                if (rotate) {
                    canvas.restore();
                }
            }
        }
        canvas.restore();
    }

    @Override
    public boolean onTap(float x, float y, Projection projection) {
        D closestItem = null;
        float closestDistance = Float.MAX_VALUE;

        int startX = projection.getOffsetX();
        int startY = projection.getOffsetY();

        Projection baseProjection = baseProjection();

        int baseOffsetX = baseProjection.getOffsetX();
        int baseOffsetY = baseProjection.getOffsetY();

        int projectionDelta = projection.getDiscreteZoom() - baseProjection.getDiscreteZoom();

        if (projectionDelta >= 0) {
            baseOffsetX <<= projectionDelta;
            baseOffsetY <<= projectionDelta;
        } else {
            baseOffsetX >>= -projectionDelta;
            baseOffsetY >>= -projectionDelta;
        }

        x -= startX - baseOffsetX;
        y -= startY - baseOffsetY;

        for (ItemsBatch.ItemWithPixelCoordinates<T, D> item : batch.getItemWithPixelCoordinates()) {
            T itemType = item.getItemType();
            Drawable icon = batch.getIcon(itemType);

            if (icon == null) {
                continue;
            }

            int halfWidth = (int) (icon.getIntrinsicWidth() / 2 / projection.getScaleFactor());
            int halfHeight = (int) (icon.getIntrinsicHeight() / 2 / projection.getScaleFactor());

            int itemX= item.getPixelX();
            int itemY= item.getPixelY();

            if (projectionDelta >= 0) {
                itemX<<= projectionDelta;
                itemY<<= projectionDelta;
            } else {
                itemX>>= -projectionDelta;
                itemY>>= -projectionDelta;
            }

            float deltaX = itemX - x;
            if(Math.abs(deltaX) > halfWidth) {
                continue;
            }

            float deltaY = itemY - y;
            if(Math.abs(deltaY) > halfHeight) {
                continue;
            }

            float currentDistance = ((float) Math.hypot(deltaX, deltaY));

            if(closestDistance > currentDistance) {
                closestDistance = currentDistance;
                closestItem = item.getData();
            }
        }

        if(closestItem == null) {
            return false;
        } else {
            tapItems.onNext(closestItem);
            return true;
        }

    }
}
