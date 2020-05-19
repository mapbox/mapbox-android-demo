package com.mapbox.mapboxandroiddemo.examples.styles;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngQuad;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.sources.ImageSource;

/**
 * Use an ImageSource to add an image to the map.
 */
public class ImageSourceActivity extends AppCompatActivity implements OnMapReadyCallback {

  private MapView mapView;
  private static final String ID_IMAGE_SOURCE = "image_source-id";
  private static final String ID_IMAGE_LAYER = "image_layer-id";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_image_source);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {

    mapboxMap.setStyle(Style.DARK, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {

        // Set the latitude and longitude values for the image's four corners
        LatLngQuad quad = new LatLngQuad(
          new LatLng(25.7836, -80.11725),
          new LatLng(25.783548, -80.1397431334),
          new LatLng(25.7680, -80.13964),
          new LatLng(25.76795, -80.11725)
        );

        // Add an ImageSource to the map
        style.addSource(new ImageSource(ID_IMAGE_SOURCE, quad, R.drawable.miami_beach));

        // Create a raster layer and use the imageSource's ID as the layer's data. Then add a RasterLayer to the map.
        style.addLayer(new RasterLayer(ID_IMAGE_LAYER, ID_IMAGE_SOURCE));
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
