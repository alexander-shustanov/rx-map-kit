package com.depthguru.rxmap.overlay.tiles;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.depthguru.rxmap.Projection;
import com.depthguru.rxmap.TileSystem;
import com.depthguru.rxmap.overlay.Drawer;
import com.depthguru.rxmap.overlay.Overlay;
import com.depthguru.rxmap.rx.MapSchedulers;
import com.depthguru.rxmap.rx.SingleItemBuffer;
import com.depthguru.rxmap.rx.StateMonad;

import java.util.Arrays;
import java.util.Map;

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
                .lift(StateMonad.create(MapTileBatch::completeWith));
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

            Rect screenRect = projection.getScreenRect();
            int worldSize = projection.getWorldSize();
            int repeatsX = (int) Math.ceil(screenRect.width() / (float) worldSize);
            int repeatsY = (int) Math.ceil(screenRect.height() / (float) worldSize);

            int startX = projection.getOffsetX();
            int startY = projection.getOffsetY();

            canvas.save();

            int renderedCount = 0;
            int rejectedCount = 0;

            for (Map.Entry<MapTile, Drawable> entry : mapTileBatch.getTiles().entrySet()) {
                Drawable drawable = entry.getValue();
                if (drawable == null) {
                    continue;
                }
                MapTile tile = entry.getKey();
                double tileSize = TileSystem.getTileSize();

                int zoomDelta = projection.getDiscreteZoom() - tile.getZoomLevel();
                if (zoomDelta >= 0) {
                    tileSize = tileSize * (1 << zoomDelta);
                } else {
                    tileSize = tileSize / (1 << (-zoomDelta));
                }

                int start_i = -1;
                for (; ; start_i--) {
                    int x = calculateTileStart(tile.getX(), tileSize, worldSize * start_i, startX);
                    if (x <= screenRect.left) {
                        break;
                    }
                }

                int start_j = -1;
                for (; ; start_j--) {
                    int y = calculateTileStart(tile.getY(), tileSize, worldSize * start_j, startY);
                    if (y <= screenRect.left) {
                        break;
                    }
                }

                for (int i = start_i; i < repeatsX; i++) {
                    for (int j = start_j; j < repeatsY; j++) {
                        int x = calculateTileStart(tile.getX(), tileSize, worldSize * i, startX);
                        int y = calculateTileStart(tile.getY(), tileSize, worldSize * j, startY);

                        if (!canvas.quickReject(x, y, ((int) (x + tileSize)), ((int) (y + tileSize)), Canvas.EdgeType.BW)) {
                            drawable.setBounds(x, y, x + ((int) tileSize), (int) (y + tileSize));
                            drawable.draw(canvas);
                            renderedCount++;
                        } else {
                            rejectedCount++;
                        }
                    }
                }
            }

            canvas.restore();
            System.out.println(String.format("lalala renderedCount=%s  rejectedCount=%s", renderedCount, rejectedCount));
        }

        private int calculateTileStart(int tileNo, double tileSize, int worldOffset, int offset) {
            return (int) (tileNo * tileSize + worldOffset + offset);
        }
    }
}
