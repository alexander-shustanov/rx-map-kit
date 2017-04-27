package com.depthguru.rxmap.overlay.tiles;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * MapnikProviderModule
 * </p>
 * alexander.shustanov on 16.12.16
 */
public class MapnikProviderModule extends MapTileProviderModule {
    private final BitmapFactory.Options opts = new BitmapFactory.Options();
    private final Context context;

    public MapnikProviderModule(Context context, @Nullable Bitmap.Config config) {
        this.context = context;
        if (config != null) {
            opts.inPreferredConfig = config;
        }
    }

    @Override
    public Drawable process(MapTile mapTile) {
        URL url = null;
        try {
            url = new URL(String.format("http://a.tile.openstreetmap.org/%s/%s/%s.png", mapTile.getZoomLevel(), mapTile.getX(), mapTile.getY()));
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input, null, opts);
                BitmapDrawable drawable = new BitmapDrawable(context.getResources(), bitmap);

                File file = new File(context.getCacheDir(), String.format("%s/%s/%s.png", mapTile.getZoomLevel(), mapTile.getX(), mapTile.getY()));
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));

                return drawable;

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
