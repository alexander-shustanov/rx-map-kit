package com.depthguru.test;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.depthguru.rxmap.BoundingBoxE6;
import com.depthguru.rxmap.GeoPoint;
import com.depthguru.rxmap.RxMapView;
import com.depthguru.rxmap.overlay.OverlayManager;
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

import rx.Observable;

import static rx.Observable.just;

public class MainActivity extends Activity {

    private Drawable drawable = null;

    private IconProvider<String> iconProvider = new IconProvider<String>() {
        @Override
        protected Observable<Map<String, Drawable>> fetchIcons(List<String> types) {
            return just(Collections.singletonMap("1", drawable));
        }
    };

    private ItemizedDataProvider<String, Void> itemsProvider = new ItemizedDataProvider<String, Void>(iconProvider) {
        private final List<PlainItem<String>> items = new ArrayList<>();

        {
            for (int i = 0; i < 2000; i++) {
                items.add(new PlainItem<>("1", new GeoPoint(90.0 * Math.random() - 45.0, 180.0 * Math.random() - 90.0)));
            }
        }

        @Override
        protected Observable<List<Item<String, Void>>> fetchByBounds(BoundingBoxE6 boundingBoxE6) {
            return Observable.<Item<String, Void>>create(subscriber -> {
                for (PlainItem<String> item : this.items) {
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
        OverlayManager overlayManager = mapView.getOverlayManager();

        overlayManager.add(new TileOverlay(this));
        overlayManager.add(new ItemizedOverlay(itemsProvider));

        mapView.setTilesScaledToDpi(true);

        setContentView(mapView);
    }
}
