package com.mapbox.mapboxandroiddemo.examples.javaservices;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.net.URL;

import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.geometryType;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

/**
 * See how far a car can travel in certain time periods by requesting information from the Mapbox
 * Isochrone API (https://www.mapbox.com/api-documentation/#isochrone)
 */
public class IsochoneActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private static final String GEOJSON_SOURCE_ID = "GEOJSON_SOURCE_ID";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_isochrone);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {
        IsochoneActivity.this.mapboxMap = mapboxMap;
        initIsochroneGeoJsonSource();
        initFillLayer();
        initLineLayer();
      }
    });
  }

  private void initIsochroneGeoJsonSource() {
    try {
      // Retrieve GeoJSON information from the Mapbox Isochrone API
      GeoJsonSource source = new GeoJsonSource(GEOJSON_SOURCE_ID, new URL("https://api.mapbox.com/isochrone/v1/"
        + "mapbox/driving/" + "-122.43817518306757,37.762834146042294?access_token="
        + getString(R.string.access_token) + "&polygons=false&" + "contours_minutes=5,10,15&contours_colors"
        + "=6706ce,04e813,4286f4"));

      // Add the GeoJsonSource to map
      mapboxMap.addSource(source);

    } catch (Throwable throwable) {
      Log.e("IsochoneActivity", "Couldn't add GeoJsonSource to map", throwable);
    }
  }

  private void initFillLayer() {
    // Create and style a FillLayer based on information in the Isochrone API response
    FillLayer isochoneFillLayer = new FillLayer("polygon", GEOJSON_SOURCE_ID);
    isochoneFillLayer.setProperties(
      fillColor(get("color")),
      fillOpacity(get("fillOpacity")));
    isochoneFillLayer.setFilter(eq(geometryType(), literal("Polygon")));
    isochoneFillLayer.setFilter(eq(literal("$type"), literal("Polygon")));
    mapboxMap.addLayer(isochoneFillLayer);
  }

  private void initLineLayer() {
    // Create and style a LineLayer based on information in the Isochrone API response
    LineLayer isochoneLineLayer = new LineLayer("points", GEOJSON_SOURCE_ID);
    isochoneLineLayer.setProperties(
      lineColor(get("color")),
      lineWidth(5f),
      lineOpacity(get("opacity")));
    isochoneLineLayer.setFilter(eq(literal("$type"), literal("LineString")));
    mapboxMap.addLayer(isochoneLineLayer);
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
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
  protected void onStop() {
    super.onStop();
    mapView.onStop();
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