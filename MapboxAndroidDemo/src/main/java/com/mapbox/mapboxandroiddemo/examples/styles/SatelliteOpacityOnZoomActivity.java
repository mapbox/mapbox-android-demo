package com.mapbox.mapboxandroiddemo.examples.styles;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.PropertyValue;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.sources.RasterSource;

import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.rasterOpacity;

/**
 * Create an effect of seamlessly fading from one map style to another with runtime styling opacity.
 * Go from the Mapbox Streets style to a satellite photo raster layer as the map camera zooms in.
 * This is similar to how Snap uses Mapbox for Snap Maps.
 */
public class SatelliteOpacityOnZoomActivity extends AppCompatActivity implements
  OnMapReadyCallback {

  private static final String SATELLITE_RASTER_SOURCE_ID = "SATELLITE_RASTER_SOURCE_ID";
  private static final String SATELLITE_RASTER_LAYER_ID = "SATELLITE_RASTER_LAYER_ID";
  private MapView mapView;
  private MapboxMap mapboxMap;
  private boolean satelliteSetToReveal = false;

  private PropertyValue<Expression> expressionRevealingSatellite = rasterOpacity(interpolate(linear(), zoom(),
    stop(15, 0),
    stop(18, 1)
  ));

  private PropertyValue<Expression> expressionRevealingMapboxStreets = rasterOpacity(interpolate(linear(), zoom(),
    stop(12, 1),
    stop(16, 0)
  ));

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_satellite_opacity_on_zoom);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
    mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {

        SatelliteOpacityOnZoomActivity.this.mapboxMap = mapboxMap;

        // Create a data source for the satellite raster image and add the source to the map
        style.addSource(new RasterSource(SATELLITE_RASTER_SOURCE_ID,
          "mapbox://mapbox.satellite", 512));

        // Create a new map layer for the satellite raster images and add the satellite layer to the map.
        // Use runtime styling to adjust the satellite layer's opacity based on the map camera's zoom level
        style.addLayer(
          new RasterLayer(SATELLITE_RASTER_LAYER_ID, SATELLITE_RASTER_SOURCE_ID).withProperties(
            expressionRevealingSatellite));

        // Create a new camera position and animate the map camera to show the fade in/out UI of the satellite layer
        mapboxMap.animateCamera(
          CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
            .zoom(19)
            .build()), 9000);

        findViewById(R.id.swap_streets_and_satellite_order_toggle_fab).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            swapStreetsAndSatelliteOrder();
            Toast.makeText(SatelliteOpacityOnZoomActivity.this,
              R.string.zoom_in_and_out_instruction, Toast.LENGTH_SHORT).show();
          }
        });
      }
    });
  }

  private void swapStreetsAndSatelliteOrder() {
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        Layer satelliteLayer = style.getLayer(SATELLITE_RASTER_LAYER_ID);
        if (satelliteLayer != null) {
          satelliteLayer.setProperties(
            satelliteSetToReveal ? expressionRevealingSatellite : expressionRevealingMapboxStreets
          );
        }
        satelliteSetToReveal = !satelliteSetToReveal;
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
