package com.mapbox.mapboxandroiddemo.examples.styles;
// #-code-snippet: image-source-time-lapse-activity full-java
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngQuad;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.sources.ImageSource;

/**
 * Use a series of images to create an animation with an ImageSource
 */
public class ImageSourceTimeLapseActivity extends AppCompatActivity implements OnMapReadyCallback {

  private MapView mapView;
  private Handler handler;
  private Runnable runnable;
  private static final String ID_IMAGE_SOURCE = "animated_image_source";
  private static final String ID_IMAGE_LAYER = "animated_image_layer";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_image_source_time_lapse);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    // Add source
    LatLngQuad quad = new LatLngQuad(
      new LatLng(46.437, -80.425),
      new LatLng(46.437, -71.516),
      new LatLng(37.936, -71.516),
      new LatLng(37.936, -80.425));
    mapboxMap.addSource(new ImageSource(ID_IMAGE_SOURCE, quad, R.drawable.southeast_radar_0));

    // Add layer
    RasterLayer layer = new RasterLayer(ID_IMAGE_LAYER, ID_IMAGE_SOURCE);
    mapboxMap.addLayer(layer);

    // Loop the GeoJSON refreshing
    handler = new Handler();
    runnable = new RefreshImageRunnable(mapboxMap, handler);
    handler.postDelayed(runnable, 100);
  }

  private static class RefreshImageRunnable implements Runnable {
    private final MapboxMap mapboxMap;
    private final Handler handler;
    private int[] drawables;
    private int drawableIndex;

    RefreshImageRunnable(MapboxMap mapboxMap, Handler handler) {
      this.mapboxMap = mapboxMap;
      this.handler = handler;
      drawables = new int[4];
      drawables[0] = R.drawable.southeast_radar_0;
      drawables[1] = R.drawable.southeast_radar_1;
      drawables[2] = R.drawable.southeast_radar_2;
      drawables[3] = R.drawable.southeast_radar_3;
      drawableIndex = 1;
    }

    @Override
    public void run() {
      ((ImageSource) mapboxMap.getSource(ID_IMAGE_SOURCE)).setImage(drawables[drawableIndex++]);
      if (drawableIndex > 3) {
        drawableIndex = 0;
      }
      handler.postDelayed(this, 1000);
    }
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
    if (handler != null && runnable != null) {
      handler.removeCallbacks(runnable);
    }
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}
// #-end-code-snippet: image-source-time-lapse-activity full-java