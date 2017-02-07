package com.depthguru.rxmap.overlay.tiles;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.depthguru.rxmap.MathUtils;
import com.depthguru.rxmap.Projection;
import com.depthguru.rxmap.TileSystem;
import com.depthguru.rxmap.overlay.Drawer;
import com.depthguru.rxmap.overlay.Overlay;
import com.depthguru.rxmap.rx.MapSchedulers;
import com.depthguru.rxmap.rx.ProcessWithLast;
import com.depthguru.rxmap.rx.SingleItemBuffer;

import java.util.Arrays;

import rx.Observable;

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
        this(new MapTileProviderArray(Arrays.asList(new FileStorageProviderModule(context, Bitmap.Config.ARGB_4444), new MapnikProviderModule(context, Bitmap.Config.ARGB_4444))));
    }

    @Override
    protected Observable<MapTileBatch> postProcessData(Observable<MapTileBatch> dataObservable) {
        return super
                .postProcessData(dataObservable)
                .compose(SingleItemBuffer.dropOldest())
                .observeOn(MapSchedulers.tilesBatchAssembleScheduler())
                .lift(ProcessWithLast.of(MapTileBatch::completeWith));
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

            int repeatsX = screenRect.width() / worldSize;
            int repeatsY = screenRect.height() / worldSize;

            if (screenRect.width() % worldSize != 0) {
                repeatsX++;
            }
            if (screenRect.height() % worldSize != 0) {
                repeatsY++;
            }

            canvas.save();


            for (MapTile tile : mapTileBatch.getMapTiles()) {
                Drawable drawable = mapTileBatch.getTile(tile);
                if (drawable == null) {
                    continue;
                }
                float tileSize = TileSystem.getTileSize();

                int zoomDelta = projection.getDiscreteZoom() - tile.getZoomLevel();
                if (zoomDelta >= 0) {
                    tileSize = tileSize * (1 << zoomDelta);
                } else {
                    tileSize = tileSize / (1 << (-zoomDelta));
                }

                for (int i = -1; i < repeatsX; i++) {
                    for (int j = -1; j < repeatsY; j++) {
                        int x = (int) (tile.getX() * tileSize + worldSize * i + startX);
                        int y = (int) (tile.getY() * tileSize + worldSize * j + startY);

                        if (!canvas.quickReject(x, y, x + tileSize, y + tileSize, Canvas.EdgeType.BW)) {
                            drawable.setBounds(x, y, x + ((int) tileSize), (int) (y + tileSize));
                            drawable.draw(canvas);
                        }
                    }
                }
            }

            canvas.restore();
        }
    }
}
