package com.depthguru.rxmap.overlay.itemized;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.depthguru.rxmap.Projection;
import com.depthguru.rxmap.overlay.Drawer;

/**
 * ItemizedOverlayDrawer
 * </p>
 * alexander.shustanov on 26.01.17.
 */

class ItemizedOverlayDrawer<T, D> extends Drawer {
    private ItemsBatch<T, D> batch;
    private final int minZoom, maxZoom;

    public ItemizedOverlayDrawer(Projection baseProjection, ItemsBatch<T, D> batch, int minZoom, int maxZoom) {
        super(baseProjection);
        this.batch = batch;
        this.minZoom = minZoom;
        this.maxZoom = maxZoom;
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
                icon.setBounds(x - halfWidth, y - halfHeight, x + halfWidth, y + halfHeight);
                icon.draw(canvas);
            }
        }
        canvas.restore();
    }
}
