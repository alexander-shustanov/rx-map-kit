# RxMapKit
RxMapKit is replacement for osmdroid. Osmdroid is great, but it has problems, so the project is inspired by osmdroid. It's developed with RxJava, and it is fast, responsive, convinient. Sinse it is developeng as replacement for osmdroid, it adopts some architecture idea's from osmdroid (such as Projection, MapTile, etc).

The main idea is not to handle mapview events with rxjava, but to organize all processes inside mapview with rx streams. Consider following example.
User scrolls the map. He sees how one tiles are replaced with others. It is very important, what's happening inside.
Inside osmdroid, new Projection are created. On the system draw events TileOverlay requests tiles from tile provider. It requests them on evety frame, again and again, even if provider can not provide new tiles. It is not reactive, it causes performance issues.
Inside RxMapKit new Projection are also created. Next, it propagate to overlays with rxjava. The overlays use the DataProviders to retrieve data to draw, and construct Drawers, propagate them to map. And all this inside rxjava!
