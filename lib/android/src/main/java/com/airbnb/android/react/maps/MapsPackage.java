package com.airbnb.android.react.maps;

import android.app.Activity;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MapsPackage implements ReactPackage {
    public MapsPackage(Activity activity) {
    } // backwards compatibility

    public MapsPackage() {
    }

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        return Arrays.<NativeModule>asList(new AirMapModule(reactContext), new MapUtilsManager(reactContext));
    }

    // Deprecated RN 0.47
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        AirMapCalloutManager calloutManager = new AirMapCalloutManager();
        AirMapMarkerManager annotationManager = new AirMapMarkerManager();
        AirMapPolylineManager polylineManager = new AirMapPolylineManager(reactContext);
        AirMapPolygonManager polygonManager = new AirMapPolygonManager(reactContext);
        AirMapCircleManager circleManager = new AirMapCircleManager(reactContext);
        AirMapManager mapManager = new AirMapManager(reactContext);
        AirMapLiteManager mapLiteManager = new AirMapLiteManager(reactContext);
        AirMapUrlTileManager tileManager = new AirMapUrlTileManager(reactContext);

        com.lomocoin.map.amap.AirMapCalloutManager calloutManager2 = new com.lomocoin.map.amap.AirMapCalloutManager();
        com.lomocoin.map.amap.AirMapMarkerManager annotationManager2 = new com.lomocoin.map.amap.AirMapMarkerManager();
        com.lomocoin.map.amap.AirMapPolylineManager polylineManager2 = new com.lomocoin.map.amap.AirMapPolylineManager(reactContext);
        com.lomocoin.map.amap.AirMapPolygonManager polygonManager2 = new com.lomocoin.map.amap.AirMapPolygonManager(reactContext);
        com.lomocoin.map.amap.AirMapCircleManager circleManager2 = new com.lomocoin.map.amap.AirMapCircleManager(reactContext);
        com.lomocoin.map.amap.AirMapManager mapManager2 = new com.lomocoin.map.amap.AirMapManager(reactContext);
        com.lomocoin.map.amap.AirMapLiteManager mapLiteManager2 = new com.lomocoin.map.amap.AirMapLiteManager(reactContext);
        com.lomocoin.map.amap.AirMapUrlTileManager tileManager2 = new com.lomocoin.map.amap.AirMapUrlTileManager(reactContext);

        return Arrays.<ViewManager>asList(
                calloutManager,
                annotationManager,
                polylineManager,
                polygonManager,
                circleManager,
                mapManager,
                mapLiteManager,
                tileManager,
                calloutManager2,
                annotationManager2,
                polylineManager2,
                polygonManager2,
                circleManager2,
                mapManager2,
                mapLiteManager2,
                tileManager2);
    }
}
