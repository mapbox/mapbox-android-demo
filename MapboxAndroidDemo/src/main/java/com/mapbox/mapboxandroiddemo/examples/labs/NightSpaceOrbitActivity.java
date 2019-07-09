package com.mapbox.mapboxandroiddemo.examples.labs;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.RasterSource;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.Property.CIRCLE_PITCH_ALIGNMENT_MAP;
import static com.mapbox.mapboxsdk.style.layers.Property.CIRCLE_PITCH_SCALE_MAP;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleBlur;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circlePitchAlignment;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circlePitchScale;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.rasterOpacity;

/**
 * Create an effect of seamlessly fading from one map style to another with runtime styling opacity.
 * Go from the Mapbox Streets style to a satellite photo raster layer as the map camera zooms in.
 * This is similar to how Snap uses Mapbox for Snap Maps.
 */
public class NightSpaceOrbitActivity extends AppCompatActivity implements
    OnMapReadyCallback {

  private final static String SATELLITE_RASTER_SOURCE_ID = "SATELLITE_RASTER_SOURCE_ID";
  private final static String SATELLITE_RASTER_LAYER_ID = "SATELLITE_RASTER_LAYER_ID";
  private final static String POPULATION_SOURCE_ID = "POPULATION_SOURCE_ID";
  private final static String POPULATION_CIRCLE_LAYER_ID = "POPULATION_CIRCLE_LAYER_ID";
  private MapView mapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_lab_night_space_orbit);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {

    mapboxMap.setStyle(Style.DARK, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        // Create a data source for the satellite raster image and add the source to the map
        style.addSource(new RasterSource(SATELLITE_RASTER_SOURCE_ID,
            "mapbox://mapbox.satellite", 512));

        // Create a new map layer for the satellite raster images and add the satellite layer to the map.
        // Use runtime styling to adjust the satellite layer's opacity based on the map camera's zoom level
        style.addLayer(
            new RasterLayer(SATELLITE_RASTER_LAYER_ID, SATELLITE_RASTER_SOURCE_ID).withProperties(
                rasterOpacity(interpolate(linear(), zoom(),
                    stop(15, 0),
                    stop(18, 1)
                ))));

        try {
          style.addSource(
              new GeoJsonSource(POPULATION_SOURCE_ID,
                  new URI("https://www.mapbox.com/mapbox-gl-js/assets/earthquakes.geojson"))
          );
        } catch (URISyntaxException uriSyntaxExpression) {
          Timber.e("Check the URL %s", uriSyntaxExpression.getMessage());
        }

        style.addLayer(
            new CircleLayer(POPULATION_CIRCLE_LAYER_ID, POPULATION_SOURCE_ID).withProperties(
                circleOpacity(.8f),
                circleColor(Color.parseColor("#FDF9D0")),
                circlePitchAlignment(CIRCLE_PITCH_ALIGNMENT_MAP),
                circlePitchScale(CIRCLE_PITCH_SCALE_MAP),
                circleBlur(interpolate(linear(), get("mag"),
                    stop(1, 1),
                    stop(6, 4),
                    stop(18, 1)
                ))
            ));
/*

        // Create a new camera position and animate the map camera to show the fade in/out UI of the satellite layer
        mapboxMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                .zoom(18)
                .build()), 2000);*/
      }
    });
  }

  // Add the mapView lifecycle to the activity's lifecycle methods
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
}
