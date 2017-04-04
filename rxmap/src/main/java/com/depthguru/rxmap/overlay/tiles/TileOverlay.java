package com.depthguru.rxmap.overlay.tiles;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.depthguru.rxmap.Projection;
import com.depthguru.rxmap.RxMapView;
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
        private final Paint debugPaint = new Paint();

        TilesDrawer(MapTileBatch mapTileBatch) {
            super(mapTileBatch.getProjection());
            this.mapTileBatch = mapTileBatch;

            debugPaint.setStyle(Paint.Style.STROKE);
            debugPaint.setTextSize(30);
        }

        @Override
        public void draw(Canvas canvas, Projection projection) {

            int offsetX = projection.getOffsetX();
            int offsetY = projection.getOffsetY();
            Rect screenRect = projection.getScreenRect();
            int worldSize = baseProjection().getWorldSize();

            int repeatsX = (int) Math.ceil((screenRect.width() - offsetX) / (float) worldSize);
            int repeatsY = (int) Math.ceil((screenRect.height() - offsetY) / (float) worldSize);

            int start_i = 0;
            if (screenRect.left < offsetX) {
                start_i = -(int) Math.ceil((offsetX - screenRect.left) / (float) worldSize);
            }
            int start_j = 0;
            if (screenRect.top < offsetY) {
                start_j = -(int) Math.ceil((offsetY - screenRect.top) / (float) worldSize);
            }

            int zoomDelta = projection.getDiscreteZoom() - baseProjection().getDiscreteZoom();
            double scale = 1;
            if (zoomDelta > 0) {
                scale = 1 << zoomDelta;
            } else if (zoomDelta < 0) {
                scale = 1 / (double) (1 << -zoomDelta);
            }

            canvas.save();

            for (int i = start_i; i < repeatsX; i++) {
                for (int j = start_j; j < repeatsY; j++) {

                    double worldXOffset = worldSize * i * scale + offsetX;
                    double worldYOffset = worldSize * j * scale + offsetY;

                    canvas.save();

                    for (Map.Entry<MapTile, TileDrawable> entry : mapTileBatch.getTiles().entrySet()) {
                        TileDrawable tileDrawable = entry.getValue();
                        if (tileDrawable == null) {
                            continue;
                        }

                        double startX = tileDrawable.startX * scale;
                        double endX = tileDrawable.endX * scale;
                        double startY = tileDrawable.startY * scale;
                        double endY = tileDrawable.endY * scale;

                        if (!canvas.quickReject(
                                (float) (startX + worldXOffset),
                                (float) (startY + worldYOffset),
                                (float) (endX + worldXOffset),
                                (float) (endY + worldYOffset), Canvas.EdgeType.BW)) {

                            MapTile mapTile = entry.getKey();

                            tileDrawable.drawable.setBounds(
                                    (int) (startX + worldXOffset),
                                    (int) (startY + worldYOffset),
                                    (int) (endX + worldXOffset),
                                    (int) (endY + worldYOffset));
                            tileDrawable.drawable.draw(canvas);

                            if (RxMapView.DEBUG) {
                                String text = String.format("z=%s x=%s y=%s", mapTile.getZoomLevel(), mapTile.getX(), mapTile.getY());
                                canvas.drawText(text, 0, text.length(), ((float) (startX + 20)), (float) (startY + 20), debugPaint);
                                canvas.drawRect(((float) (startX + 10)), ((float) (startY + 10)), ((float) (endX - 10)), ((float) (endY - 10)), debugPaint);
                            }
                        }
                    }

                    canvas.restore();
                }
            }

            canvas.restore();
        }
    }
}
