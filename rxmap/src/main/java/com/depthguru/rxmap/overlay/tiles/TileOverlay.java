package com.depthguru.rxmap.overlay.tiles;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.depthguru.rxmap.Projection;
import com.depthguru.rxmap.TileSystem;
import com.depthguru.rxmap.overlay.Drawer;
import com.depthguru.rxmap.overlay.Overlay;

import java.util.Arrays;

/**
 * TileOverlay
 * </p>
 * alexander.shustanov on 15.12.16
 */
public class TileOverlay extends Overlay<MapTileBatch> {

    public TileOverlay(MapTileProviderBase mapTileProviderBase) {
        super(mapTileProviderBase);
    }

    public TileOverlay(Context context) {
        this(new MapTileProviderArray(Arrays.asList(new FileStorageProviderModule(context), new MapnikProviderModule(context))));
    }

    @Override
    protected Drawer createDrawer(MapTileBatch mapTileBatch) {
        return new TilesDrawer(mapTileBatch);
    }

    private static class TilesDrawer extends Drawer {

        private final MapTileBatch mapTileBatch;

        TilesDrawer(MapTileBatch mapTileBatch) {
            super(mapTileBatch.getProjection());
            this.mapTileBatch = mapTileBatch;
        }

        @Override
        public void draw(Canvas canvas, Projection projection) {
            int startX = projection.getOffsetX();
            int startY = projection.getOffsetY();

            Rect screenRect = projection.getScreenRect();
            int worldSize = projection.getWorldSize();

            int tilSize = TileSystem.getTileSize();

            if (baseProjection().getDiscreteZoom() != projection.getDiscreteZoom()) {
                int delta = projection.getDiscreteZoom() - baseProjection().getDiscreteZoom();
                float factor = (float) Math.pow(2, delta);

                tilSize *= factor;
            }

            startX = TilesInflater.mod(startX, worldSize);
            startY = TilesInflater.mod(startY, worldSize);

            int repeatsX = screenRect.width() / worldSize;
            int repeatsY = screenRect.height() / worldSize;

            if (screenRect.width() % worldSize != 0) {
                repeatsX++;
            }
            if (screenRect.height() % worldSize != 0) {
                repeatsY++;
            }

            canvas.save();

            for (int i = -1; i < repeatsX; i++) {
                for (int j = -1; j < repeatsY; j++) {
                    for (MapTile tile : mapTileBatch.getMapTiles()) {
                        int x = tile.getX() * tilSize + worldSize * i + startX;
                        int y = tile.getY() * tilSize + worldSize * j + startY;
                        Drawable drawable = mapTileBatch.getTile(tile);
                        if (drawable != null && !canvas.quickReject(x, y, x + tilSize, y + tilSize, Canvas.EdgeType.AA)) {
                            drawable.setBounds(x, y, x + tilSize, y + tilSize);
                            drawable.draw(canvas);
                        }
                    }
                }
            }

            canvas.restore();
        }
    }
}
