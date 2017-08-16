package com.depthguru.test;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import com.depthguru.rxmap.BoundingBoxE6;
import com.depthguru.rxmap.GeoPoint;
import com.depthguru.rxmap.RxMapView;
import com.depthguru.rxmap.overlay.CompassOverlay;
import com.depthguru.rxmap.overlay.OverlayManager;
import com.depthguru.rxmap.overlay.ScaleBarOverlay;
import com.depthguru.rxmap.overlay.itemized.IconProvider;
import com.depthguru.rxmap.overlay.itemized.Item;
import com.depthguru.rxmap.overlay.itemized.ItemizedDataProvider;
import com.depthguru.rxmap.overlay.itemized.ItemizedOverlay;
import com.depthguru.rxmap.overlay.itemized.PlainItem;
import com.depthguru.rxmap.overlay.tiles.TileOverlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;

import static rx.Observable.just;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity.TAG";

    private Drawable drawable = null;

    private IconProvider<String> iconProvider = new IconProvider<String>() {
        @Override
        protected Observable<Map<String, Drawable>> fetchIcons(List<String> types) {
            return just(Collections.singletonMap("1", drawable));
        }
    };
    private ItemizedDataProvider<String, String> itemsProvider = new ItemizedDataProvider<String, String>(iconProvider) {
        private final List<IdItem> items = new ArrayList<>();

        {
            for (int i = 0; i < 2000; i++) {
                items.add(new IdItem("1", i + "", new GeoPoint(90.0 * Math.random() - 45.0, 180.0 * Math.random() - 90.0)));
            }
        }

        @Override
        protected Observable<List<Item<String, String>>> fetchByBounds(BoundingBoxE6 boundingBoxE6) {
            return Observable.<Item<String, String>>create(subscriber -> {
                for (IdItem item : this.items) {
                    if (boundingBoxE6.contains(item.getCoordinate())) {
                        subscriber.onNext(item);
                    }
                }
                subscriber.onCompleted();
            }).toList();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        drawable = getResources().getDrawable(R.mipmap.ic_launcher);

        RxMapView mapView = new RxMapView(this);
        mapView.setId(R.id.map);
        mapView.setTilesScaledToDpi(true);

        OverlayManager overlayManager = mapView.getOverlayManager();
        overlayManager.add(new TileOverlay(this));
//        UrlProviderModule urlProviderModule = new UrlProviderModule(this, mapTile -> format("http://mt1.google.com/vt/lyrs=m&x=%s&y=%s&z=%s", mapTile.getX(), mapTile.getY(), mapTile.getZoomLevel()), Bitmap.Config.ARGB_4444);
//        overlayManager.add(new TileOverlay(new MapTileProviderArray(Arrays.asList(new FileStorageProviderModule(this), urlProviderModule)), Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT));
        ItemizedOverlay<String, String> itemizedOverlay = new ItemizedOverlay<>(itemsProvider);
        overlayManager.add(itemizedOverlay);
        overlayManager.add(new ScaleBarOverlay(mapView));
        overlayManager.add(new CompassOverlay(mapView));

        itemizedOverlay.getTapItemsObservable().subscribe(o -> {
            System.out.println("Tap");
        });

        setContentView(mapView);

        mapView.addOnFirstLayoutListener((changed, l, t, r, b) -> {
//            mapView.getController().setZoom(13f);
//            mapView.getController().setCenter(new GeoPoint(53.211377999999996, 50.176505999999996));
        });

        mapView.getScrollEventObservable().debounce(200, TimeUnit.MILLISECONDS).subscribe(scrollEvent -> Log.d(TAG, "scrollEvent : " + scrollEvent));
        mapView.getFlingEventObservable().subscribe(flingEvent -> Log.d(TAG, "flingEvent : " + flingEvent));
        mapView.getFlingEndEventObservable().subscribe(flingEndEvent -> Log.d(TAG, "flingEndEvent : " + flingEndEvent));
        mapView.getOnZoomEventObservable().subscribe(zoom -> Log.d(TAG, "onZoomEvent : " + zoom));
    }

    private class IdItem implements Item<String, String> {

        private final String type, id;
        private final GeoPoint coordinate;

        private IdItem(String type, String id, GeoPoint coordinate) {
            this.type = type;
            this.id = id;
            this.coordinate = coordinate;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public GeoPoint getCoordinate() {
            return coordinate;
        }

        @Override
        public String getData() {
            return id;
        }
    }
}
