package com.depthguru.rxmap.overlay.itemized;

import android.graphics.Point;
import android.graphics.drawable.Drawable;

import com.depthguru.rxmap.Projection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ItemsBatch
 * </p>
 * alexander.shustanov on 25.01.17.
 */

/**
 * ItemsBatch
 * </p>
 * alexander.shustanov on 25.01.17.
 *
 * @param <T> item type - is used to map item and icon
 */
public class ItemsBatch<T, D> {
    private final List<ItemWithPixelCoordinates<T, D>> itemWithPixelCoordinates;
    private final Projection projection;
    private final Map<T, Drawable> icons;

    public ItemsBatch(List<ItemWithPixelCoordinates<T, D>> itemWithPixelCoordinates, Projection projection, Map<T, Drawable> icons) {
        this.itemWithPixelCoordinates = itemWithPixelCoordinates;
        this.projection = projection;
        this.icons = icons;
    }

    public ItemsBatch(List<Item<T, D>> items, Projection projection) {
        this.projection = projection;
        icons = new HashMap<>();
        itemWithPixelCoordinates = new ArrayList<>();
        Point point = new Point();
        for (Item<T, D> item : items) {
            projection.toPixels(item.getCoordinate(), point);
            itemWithPixelCoordinates.add(new ItemWithPixelCoordinates<>(point.x, point.y, item.getType(), item.getData()));
        }
    }

    public Drawable getIcon(T t) {
        return icons.get(t);
    }

    public Projection getProjection() {
        return projection;
    }

    public List<ItemWithPixelCoordinates<T, D>> getItemWithPixelCoordinates() {
        return new ArrayList<>(itemWithPixelCoordinates);
    }

    public static class ItemWithPixelCoordinates<T, D> {
        private final int pixelX;
        private final int pixelY;
        private final T itemType;
        private final D data;

        public ItemWithPixelCoordinates(int pixelX, int pixelY, T itemType, D data) {
            this.pixelX = pixelX;
            this.pixelY = pixelY;
            this.itemType = itemType;
            this.data = data;
        }

        public T getItemType() {
            return itemType;
        }

        public D getData() {
            return data;
        }

        public int getPixelX() {
            return pixelX;
        }

        public int getPixelY() {
            return pixelY;
        }
    }
}
