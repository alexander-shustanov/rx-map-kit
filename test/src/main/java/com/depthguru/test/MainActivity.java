package com.depthguru.test;

import android.app.Activity;
import android.os.Bundle;

import com.depthguru.rxmap.RxMapView;
import com.depthguru.rxmap.overlay.OverlayManager;
import com.depthguru.rxmap.overlay.tiles.MapTileProviderArray;
import com.depthguru.rxmap.overlay.tiles.MapTileProviderModule;
import com.depthguru.rxmap.overlay.tiles.MapnikProviderModule;
import com.depthguru.rxmap.overlay.tiles.TileOverlay;

import java.util.Collections;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RxMapView mapView = new RxMapView(this);
        OverlayManager overlayManager = mapView.getOverlayManager();

        overlayManager.add(new TileOverlay(this));

        setContentView(mapView);
    }
}
