package com.depthguru.rxmap.overlay.tiles;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import rx.Observer;
import rx.subjects.PublishSubject;

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

    public MapnikProviderModule(Context context) {
        this(context, null);
    }

    @Override
    public LoadTask createTask(MapTileState mapTileState, Observer<MapTileState> loadSorter) {
        return new LoadTask() {
            @Override
            protected void load() {
                MapTile mapTile = mapTileState.getMapTile();
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
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            bitmap.setHasMipMap(true);
                        }
                        BitmapDrawable drawable = new BitmapDrawable(context.getResources(), bitmap);

                        File file = new File(context.getCacheDir(), String.format("%s/%s/%s.png", mapTile.getZoomLevel(), mapTile.getX(), mapTile.getY()));
                        if(!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }

                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));

                        mapTileState.setDrawable(drawable);
                    } catch (IOException e) {
                        mapTileState.incState();
                    } finally {
                        if(connection != null) {
                            connection.disconnect();
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                loadSorter.onNext(mapTileState);
            }
        };
    }
}
