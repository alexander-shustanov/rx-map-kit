package com.depthguru.rxmap.overlay.tiles;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;

import rx.Observer;

/**
 * FileStorageProviderModule
 * </p>
 * alexander.shustanov on 19.12.16
 */
public class FileStorageProviderModule extends MapTileProviderModule {
    private final BitmapFactory.Options opts = new BitmapFactory.Options();
    private final Context context;

    public FileStorageProviderModule(Context context) {
        this(context, null);
    }

    public FileStorageProviderModule(Context context, @Nullable Bitmap.Config config) {
        this.context = context;
        if (config != null) {
            opts.inPreferredConfig = config;
        }
    }

    @Override
    protected LoadTask createTask(MapTileState mapTileState, Observer<MapTileState> loadSorter) {
        return new LoadTask() {
            @Override
            protected void load() {
                Log.i("FileStorageProviderMod", "Start load " + mapTileState.getMapTile());
                MapTile mapTile = mapTileState.getMapTile();
                File file = new File(context.getCacheDir(), String.format("%s/%s/%s.png", mapTile.getZoomLevel(), mapTile.getX(), mapTile.getY()));

                try {
                    if (file.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            bitmap.setHasMipMap(true);
                        }
                        mapTileState.setDrawable(new BitmapDrawable(context.getResources(), bitmap));
                    }
                } finally {
                    if (mapTileState.getDrawable() == null) {
                        mapTileState.incState();
                    }
                    loadSorter.onNext(mapTileState);
                }
            }
        };
    }
}
