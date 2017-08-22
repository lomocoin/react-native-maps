package com.airbnb.android.react.maps;


import com.amap.api.maps2d.model.*;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.LatLngBounds;

public class LatLngBoundsUtils {
  public static boolean BoundsAreDifferent(LatLngBounds a, LatLngBounds b) {
    LatLng centerA = getCenter(a);
    double latA = centerA.latitude;
    double lngA = centerA.longitude;
    double latDeltaA = a.northeast.latitude - a.southwest.latitude;
    double lngDeltaA = a.northeast.longitude - a.southwest.longitude;

    LatLng centerB = getCenter(b);
    double latB = centerB.latitude;
    double lngB = centerB.longitude;
    double latDeltaB = b.northeast.latitude - b.southwest.latitude;
    double lngDeltaB = b.northeast.longitude - b.southwest.longitude;

    double latEps = LatitudeEpsilon(a, b);
    double lngEps = LongitudeEpsilon(a, b);

    return
        different(latA, latB, latEps) ||
            different(lngA, lngB, lngEps) ||
            different(latDeltaA, latDeltaB, latEps) ||
            different(lngDeltaA, lngDeltaB, lngEps);
  }

  private static boolean different(double a, double b, double epsilon) {
    return Math.abs(a - b) > epsilon;
  }

  private static double LatitudeEpsilon(LatLngBounds a, LatLngBounds b) {
    double sizeA = a.northeast.latitude - a.southwest.latitude; // something mod 180?
    double sizeB = b.northeast.latitude - b.southwest.latitude; // something mod 180?
    double size = Math.min(Math.abs(sizeA), Math.abs(sizeB));
    return size / 2560;
  }

  private static double LongitudeEpsilon(LatLngBounds a, LatLngBounds b) {
    double sizeA = a.northeast.longitude - a.southwest.longitude;
    double sizeB = b.northeast.longitude - b.southwest.longitude;
    double size = Math.min(Math.abs(sizeA), Math.abs(sizeB));
    return size / 2560;
  }


  //// TODO: 17/7/27  添加获取中心点方法
  private static LatLng getCenter(LatLngBounds latLngBounds) {
    double var1 = (latLngBounds.southwest.latitude + latLngBounds.northeast.latitude) / 2.0D;
    double var3 = latLngBounds.northeast.longitude;
    double var5 = latLngBounds.southwest.longitude;
    double var7;
    if(var5 <= var3) {
      var7 = (var3 + var5) / 2.0D;
    } else {
      var7 = (var3 + 360.0D + var5) / 2.0D;
    }
    return new LatLng(var1, var7);
  }
}
