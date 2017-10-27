package com.lomocoin.map.amap;

import com.amap.api.maps.AMapOptions;
import com.facebook.react.bridge.ReactApplicationContext;

public class AirMapLiteManager extends AirMapManager {

  private static final String REACT_CLASS = "AMapLite";

  @Override
  public String getName() {
    return REACT_CLASS;
  }

  public AirMapLiteManager(ReactApplicationContext context) {
    super(context);
    this.googleMapOptions = new AMapOptions().tiltGesturesEnabled(true);
//    this.googleMapOptions = new AMapOptions().liteMode(true);
  }

}
