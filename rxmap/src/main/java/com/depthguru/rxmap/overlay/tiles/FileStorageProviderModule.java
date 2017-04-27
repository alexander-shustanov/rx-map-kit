package com.depthguru.rxmap.overlay.tiles;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;

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
    public Drawable process(MapTile mapTile) {
        Log.i("FileStorageProviderMod", "Start load " + mapTile);
        File file = new File(context.getCacheDir(), String.format("%s/%s/%s.png", mapTile.getZoomLevel(), mapTile.getX(), mapTile.getY()));

        try {
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
                return new BitmapDrawable(context.getResources(), bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
