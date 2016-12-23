package com.depthguru.rxmap.overlay.tiles;

import android.graphics.drawable.Drawable;
import android.util.SparseArray;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * TileCache
 * </p>
 * alexander.shustanov on 16.12.16
 */
public class TileCache {
    private final int maxSize;
    private final Map<MapTile, Item> tiles = new HashMap<>();
    private final SparseArray<MapTile> lastUse = new SparseArray<>();
    private int i = 0;

    public TileCache(int maxSize) {
        this.maxSize = maxSize;
    }

    public void put(MapTile key, Drawable value) {
        Item item = new Item(value);
        tiles.put(key, item);
        lastUse.put(item.lastUse, key);
        gc();
    }

    public Drawable get(MapTile key) {
        Item item = tiles.get(key);
        if (item != null) {
            lastUse.remove(item.lastUse);
            lastUse.put(item.use(), key);
            return item.drawable;
        }
        return null;
    }

    private void gc() {
        if (tiles.size() < maxSize) {
            return;
        }
        MapTile mapTile = lastUse.valueAt(0);
        lastUse.removeAt(0);
        tiles.remove(mapTile);
    }

    public Set<MapTile> keySet() {
        return tiles.keySet();
    }

    public void detach() {
//        for (Item item : tiles.values()) {
//            if (item.drawable instanceof BitmapDrawable) {
//                ((BitmapDrawable) item.drawable).getBitmap().recycle();
//            }
//        }
    }

    private class Item {
        Drawable drawable;
        int lastUse;

        public Item(Drawable drawable) {
            this.drawable = drawable;
            lastUse = i++;
        }

        public int use() {
            return (lastUse = i++);
        }
    }
}
