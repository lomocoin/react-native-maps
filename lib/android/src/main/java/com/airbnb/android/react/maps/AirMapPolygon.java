package com.airbnb.android.react.maps;

import android.content.Context;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.amap.api.maps.AMap;
import com.amap.api.maps.model.*;

import java.util.ArrayList;
import java.util.List;

public class AirMapPolygon extends AirMapFeature {

  private PolygonOptions polygonOptions;
  private Polygon polygon;

  private List<LatLng> coordinates;
  private int strokeColor;
  private int fillColor;
  private float strokeWidth;
  private boolean geodesic;
  private float zIndex;

  public AirMapPolygon(Context context) {
    super(context);
  }

  public void setCoordinates(ReadableArray coordinates) {
    // it's kind of a bummer that we can't run map() or anything on the ReadableArray
    this.coordinates = new ArrayList<>(coordinates.size());
    for (int i = 0; i < coordinates.size(); i++) {
      ReadableMap coordinate = coordinates.getMap(i);
      this.coordinates.add(i,
          new LatLng(coordinate.getDouble("latitude"), coordinate.getDouble("longitude")));
    }
    if (polygon != null) {
      polygon.setPoints(this.coordinates);
    }
  }

  public void setFillColor(int color) {
    this.fillColor = color;
    if (polygon != null) {
      polygon.setFillColor(color);
    }
  }

  public void setStrokeColor(int color) {
    this.strokeColor = color;
    if (polygon != null) {
      polygon.setStrokeColor(color);
    }
  }

  public void setStrokeWidth(float width) {
    this.strokeWidth = width;
    if (polygon != null) {
      polygon.setStrokeWidth(width);
    }
  }

  public void setGeodesic(boolean geodesic) {
    this.geodesic = geodesic;
    if (polygon != null) {

//      polygon.setGeodesic(geodesic);  // TODO: 17/7/27
    }
  }

  public void setZIndex(float zIndex) {
    this.zIndex = zIndex;
    if (polygon != null) {
      polygon.setZIndex(zIndex);
    }
  }

  public PolygonOptions getPolygonOptions() {
    if (polygonOptions == null) {
      polygonOptions = createPolygonOptions();
    }
    return polygonOptions;
  }

  private PolygonOptions createPolygonOptions() {
    PolygonOptions options = new PolygonOptions();
    options.addAll(coordinates);
    options.fillColor(fillColor);
    options.strokeColor(strokeColor);
    options.strokeWidth(strokeWidth);
//    options.geodesic(geodesic); // TODO: 17/7/27
    options.zIndex(zIndex);
    return options;
  }

  @Override
  public Object getFeature() {
    return polygon;
  }

  @Override
  public void addToMap(AMap map) {
    polygon = map.addPolygon(getPolygonOptions());
    //// TODO: 17/7/27
//    polygon.setClickable(true);
  }

  @Override
  public void removeFromMap(AMap map) {
    polygon.remove();
  }
}
