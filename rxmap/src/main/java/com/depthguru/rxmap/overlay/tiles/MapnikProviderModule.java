package com.depthguru.rxmap.overlay.tiles;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import rx.subjects.PublishSubject;

/**
 * MapnikProviderModule
 * </p>
 * alexander.shustanov on 16.12.16
 */
public class MapnikProviderModule extends MapTileProviderModule {
    private final Context context;

    public MapnikProviderModule(Context context) {
        this.context = context;
    }

    @Override
    public Runnable process(MapTileState mapTileState, PublishSubject<MapTileState> loadSorter) {
        return () -> {
            MapTile mapTile = mapTileState.getMapTile();
            URL url = null;
            try {
                url = new URL(String.format("http://a.tile.openstreetmap.org/%s/%s/%s.png", mapTile.getZoomLevel(), mapTile.getX(), mapTile.getY()));
                try {
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    BitmapDrawable drawable = new BitmapDrawable(context.getResources(), bitmap);

                    File file = new File(context.getCacheDir(), String.format("%s/%s/%s.png", mapTile.getZoomLevel(), mapTile.getX(), mapTile.getY()));
                    if(!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }

                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));

                    mapTileState.setDrawable(drawable);
                } catch (IOException e) {
                    mapTileState.incState();
                }
                loadSorter.onNext(mapTileState);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

        };
    }
}
