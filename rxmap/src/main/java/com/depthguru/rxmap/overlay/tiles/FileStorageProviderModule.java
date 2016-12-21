package com.depthguru.rxmap.overlay.tiles;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import java.io.File;

import rx.subjects.PublishSubject;

/**
 * FileStorageProviderModule
 * </p>
 * alexander.shustanov on 19.12.16
 */
public class FileStorageProviderModule extends MapTileProviderModule {
    private final Context context;

    public FileStorageProviderModule(Context context) {
        this.context = context;
    }

    @Override
    public Runnable process(MapTileState mapTileState, PublishSubject<MapTileState> loadSorter) {
        return () -> {
            MapTile mapTile = mapTileState.getMapTile();
            File file = new File(context.getCacheDir(), String.format("%s/%s/%s.png", mapTile.getZoomLevel(), mapTile.getX(), mapTile.getY()));

                try {
                    if (file.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        mapTileState.setDrawable(new BitmapDrawable(context.getResources(), bitmap));
                    }
                } finally {
                    if(mapTileState.getDrawable() == null)  {
                        mapTileState.incState();
                    }
                    loadSorter.onNext(mapTileState);
                }

        };
    }
}
