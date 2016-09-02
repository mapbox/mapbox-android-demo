package com.mapbox.mapboxandroiddemo.examples.styles;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.services.commons.geojson.BaseFeatureCollection;
import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.geojson.FeatureCollection;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.geojson.Point;
import com.mapbox.services.commons.models.Position;

import java.util.ArrayList;
import java.util.List;

public class LineLayerActivity extends AppCompatActivity {

  private MapView mapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_style_line_layer);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {

        Position origin = Position.fromCoordinates(-118.39676, 33.39244);
        Position destination = Position.fromCoordinates(-118.37004, 33.39123);

        List<Position> routeCoordinates = new ArrayList<>();

        routeCoordinates.add(origin);
        routeCoordinates.add(destination);

        LineString lineString = LineString.fromCoordinates(routeCoordinates);

//        FeatureCollection featureCollection = FeatureCollection.fromFeatures(new Feature[] {Feature.fromGeometry(lineString)});

//        FeatureCollection featureCollection = FeatureCollection.fromFeatures(new Feature[] {Feature.fromGeometry(lineString) });

        FeatureCollection featureCollection = BaseFeatureCollection.fromJson("{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":["
          + "[" + origin.getLongitude() + "," + origin.getLatitude() + "]" + ","
          + "[" + destination.getLongitude() + "," + destination.getLatitude() + "]"
          + "]},\"properties\":{}}]}");

        Source geoJsonSource = new GeoJsonSource("line-source", featureCollection);

        mapboxMap.addSource(geoJsonSource);


        LineLayer lineLayer = new LineLayer("linelayer", "line-source");

        lineLayer.setProperties(
          PropertyFactory.lineDasharray(new Float[]{0.01f, 2f}),
          PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
          PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
          PropertyFactory.lineWidth(5f),
          PropertyFactory.lineColor(Color.RED)
        );

        mapboxMap.addLayer(lineLayer);

      }
    });
  }

  @Override
  public void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}