package com.mapbox.mapboxandroiddemo.examples.annotations;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;

/**
 * Draw a vector polygon on a map with the Mapbox Android SDK.
 */
public class DrawPolygonActivity extends AppCompatActivity {

  private MapView mapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_annotation_polygon);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull final MapboxMap mapboxMap) {

        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            List<Feature> polygonFeatureList = new ArrayList<>();
            polygonFeatureList.add(Feature.fromGeometry(Point.fromLngLat(-122.685699, 45.522585)));
            polygonFeatureList.add(Feature.fromGeometry(Point.fromLngLat(-122.708873, 45.534611)));
            polygonFeatureList.add(Feature.fromGeometry(Point.fromLngLat(-122.678833, 45.530883)));
            polygonFeatureList.add(Feature.fromGeometry(Point.fromLngLat(-122.667503, 45.547115)));
            polygonFeatureList.add(Feature.fromGeometry(Point.fromLngLat(-122.660121, 45.530643)));
            polygonFeatureList.add(Feature.fromGeometry(Point.fromLngLat(-122.636260, 45.533529)));
            polygonFeatureList.add(Feature.fromGeometry(Point.fromLngLat(-122.659091, 45.521743)));
            polygonFeatureList.add(Feature.fromGeometry(Point.fromLngLat(-122.648792, 45.510677)));
            polygonFeatureList.add(Feature.fromGeometry(Point.fromLngLat(-122.664070, 45.515008)));
            polygonFeatureList.add(Feature.fromGeometry(Point.fromLngLat(-122.669048, 45.502496)));
            polygonFeatureList.add(Feature.fromGeometry(Point.fromLngLat(-122.678489, 45.515369)));
            polygonFeatureList.add(Feature.fromGeometry(Point.fromLngLat(-122.702007, 45.506346)));
            polygonFeatureList.add(Feature.fromGeometry(Point.fromLngLat(-122.685699, 45.522585)));

            Log.d("DrawPolygonActivity", "onStyleLoaded: polygonFeatureList size = " + polygonFeatureList.size());

            GeoJsonSource geoJsonSource = new GeoJsonSource("source-id",
              FeatureCollection.fromFeatures(polygonFeatureList));
            mapboxMap.getStyle().addSource(geoJsonSource);
            Log.d("DrawPolygonActivity", "onMapReady: source added ");

            FillLayer polygonFillLayer = new FillLayer("layer-id", "source-id");
            polygonFillLayer.withProperties(
              fillColor(Color.RED)
            );


            mapboxMap.getStyle().addLayer(polygonFillLayer);

          }
        });
      }
    });
  }

  @Override
  public void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
  }

  @Override
  public void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
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
}
