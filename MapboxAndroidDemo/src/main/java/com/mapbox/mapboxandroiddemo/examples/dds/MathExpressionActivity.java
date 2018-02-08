package com.mapbox.mapboxandroiddemo.examples.dds;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.geojson.FeatureCollection;

import java.io.IOException;
import java.io.InputStream;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;

/**
 * Shows how to style data based on a math expression to convert Celcius to Kelvin.
 */
public class MathExpressionActivity extends AppCompatActivity implements
  OnMapReadyCallback {

  private MapView mapView;
  private MapboxMap mapboxMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_dds_math_expression);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {

    this.mapboxMap = mapboxMap;

    // Add data to the map
    GeoJsonSource source = new GeoJsonSource("weather", loadJsonFromAsset("world_weather.geojson"));
    mapboxMap.addSource(source);


    mapboxMap.addImage(
        "my-marker-image",
        BitmapFactory.decodeResource(MathExpressionActivity.this.getResources(),
            R.drawable.mapbox_marker_icon_default)
    );

    SymbolLayer markers = new SymbolLayer("marker-layer", source.getId())
      .withProperties(
          textField(Expression.toString(celciusToKelvin(Expression.get("temperature")))),
          textColor("white"),
          iconImage("my-marker-image"));
    mapboxMap.addLayer(markers);

    // Add the selected marker source and layer
    FeatureCollection emptySource = FeatureCollection.fromFeatures(new Feature[]{});
    Source selectedMarkerSource = new GeoJsonSource("selected-marker", emptySource);
    mapboxMap.addSource(selectedMarkerSource);
  }

  private static Expression<Number> celciusToKelvin(Expression<Number> celcius) {
    return Expression.sum(celcius, Expression.literal(new Double(273.15)));
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

  private String loadJsonFromAsset(String filename) {
    // Using this method to load in GeoJSON files from the assets folder.
    try {
      InputStream is = getAssets().open(filename);
      int size = is.available();
      byte[] buffer = new byte[size];
      is.read(buffer);
      is.close();
      return new String(buffer, "UTF-8");

    } catch (IOException ex) {
      ex.printStackTrace();
      return null;
    }
  }
}