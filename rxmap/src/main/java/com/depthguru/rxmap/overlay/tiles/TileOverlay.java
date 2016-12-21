package com.depthguru.rxmap.overlay.tiles;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.depthguru.rxmap.Projection;
import com.depthguru.rxmap.overlay.Drawer;
import com.depthguru.rxmap.overlay.Overlay;

import java.util.Arrays;

/**
 * TileOverlay
 * </p>
 * alexander.shustanov on 15.12.16
 */
public class TileOverlay extends Overlay<MapTileBatch> {

    public TileOverlay(Context context) {
        super(new MapTileProviderArray(Arrays.asList(new FileStorageProviderModule(context), new MapnikProviderModule(context))));
    }

    public TileOverlay(MapTileProviderBase mapTileProviderBase) {
        super(mapTileProviderBase);
    }

    @Override
    protected Drawer createDrawer(MapTileBatch mapTileBatch) {
        return new Drawer(mapTileBatch.getProjection()) {
            @Override
            public void draw(Canvas canvas, Projection projection) {
                int startX = projection.getOffsetX();
                int startY = projection.getOffsetY();

                Rect screenRect = projection.getScreenRect();
                int worldSize = projection.getWorldSize();

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

                for (int i = -1; i < repeatsX; i++) {
                    for (int j = -1; j < repeatsY; j++) {
                        for (MapTile tile : mapTileBatch.getMapTiles()) {
                            int x = tile.getX() * 256 + worldSize * i + startX;
                            int y = tile.getY() * 256 + worldSize * j + startY;
                            Drawable drawable = mapTileBatch.getTile(tile);
                            if (drawable != null && !canvas.quickReject(x, y, x + 256, y + 256, Canvas.EdgeType.AA)) {
                                drawable.setBounds(x, y, x + 256, y + 256);
                                drawable.draw(canvas);
                            }
                        }
                    }
                }
            }
        };
    }
}
