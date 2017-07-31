package com.airbnb.android.react.maps;

import android.app.Activity;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONObject;

/**
 * 地图相关 utils
 *
 * @author qiaojiayan
 * @date 17/8/1 上午12:10
 */

public class MapUtilsManager extends ReactContextBaseJavaModule {
    private static AMapLocationClientOption locationClientOption = null;


    public MapUtilsManager(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "RCTMapUtils";
    }


    static {
        locationClientOption = new AMapLocationClientOption();
        locationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);// 设置定位模式
        //初始化定位参数
        //设置定位模式为Hight_Accuracy高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        //设置是否返回地址信息（默认返回地址信息）
        locationClientOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        locationClientOption.setOnceLocation(true);
        //设置是否强制刷新WIFI，默认为强制刷新
//        locationClientOption.setWifiActiveScan(true); //启用方法
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        locationClientOption.setMockEnable(true);
        //设置定位间隔,单位毫秒,默认为2000ms
        locationClientOption.setInterval(2000);
        //定位超时时间 10秒
        locationClientOption.setHttpTimeOut(10000);
    }


    /**
     * 获取定位位置
     */
    @ReactMethod
    public void getLocationRegion(final Promise promise) {
        /** 设置相关参数 */
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Activity mActivity = getCurrentActivity();
                final AMapLocationClient client = new AMapLocationClient(mActivity);
                client.setLocationListener(new AMapLocationListener() {
                    @Override
                    public void onLocationChanged(AMapLocation location) {
                        client.stopLocation();
                        String city = "";
                        double lng = 0D, lat = 0D;
                        if (location != null) {
                            city = location.getCity();
                            lng = location.getLongitude();
                            lat = location.getLatitude();
                        }
                        try {
                            WritableMap json = Arguments.createMap();
                            json.putString("city", city);
                            json.putDouble("lng", lng);
                            json.putDouble("lat", lat);
                            promise.resolve(json.toString());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            promise.reject("error","定位失败");
                        }
                    }
                });
                client.setLocationOption(locationClientOption);
                client.startLocation(); // 开始定位
            }
        });
    }

}
