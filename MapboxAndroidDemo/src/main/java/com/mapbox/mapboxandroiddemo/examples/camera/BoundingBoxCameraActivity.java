package com.mapbox.mapboxandroiddemo.examples.camera;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

/**
 * Fit a map to a bounding box
 */
public class BoundingBoxCameraActivity extends AppCompatActivity implements OnMapReadyCallback,
  MapboxMap.OnMapClickListener {

  private MapView mapView;
  private MapboxMap mapboxMap;

  private static final LatLng locationOne = new LatLng(36.532128, -93.489121);
  private static final LatLng locationTwo = new LatLng(25.837058, -106.646234);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_camera_bounding_box);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {

    BoundingBoxCameraActivity.this.mapboxMap = mapboxMap;

    mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {

        addMarkerIconsToMap(style);

        // Toast instructing user to tap on the map to start animation and set bounds
        Toast.makeText(
          BoundingBoxCameraActivity.this,
          getString(R.string.tap_on_map_instruction),
          Toast.LENGTH_LONG
        ).show();

        mapboxMap.addOnMapClickListener(BoundingBoxCameraActivity.this);
      }
    });
  }

  private void addMarkerIconsToMap(@NonNull Style loadedMapStyle) {
    loadedMapStyle.addImage("icon-id", BitmapUtils.getBitmapFromDrawable(
      getResources().getDrawable(R.drawable.red_marker)));

    loadedMapStyle.addSource(new GeoJsonSource("source-id",
      FeatureCollection.fromFeatures(new Feature[] {
        Feature.fromGeometry(Point.fromLngLat(locationOne.getLongitude(), locationOne.getLatitude())),
        Feature.fromGeometry(Point.fromLngLat(locationTwo.getLongitude(), locationTwo.getLatitude())),
      })));

    loadedMapStyle.addLayer(new SymbolLayer("layer-id",
      "source-id").withProperties(
      iconImage("icon-id"),
      iconOffset(new Float[]{0f,-8f})
    ));
  }

  @Override
  public boolean onMapClick(@NonNull LatLng point) {
    LatLngBounds latLngBounds = new LatLngBounds.Builder()
      .include(locationOne) // Northeast
      .include(locationTwo) // Southwest
      .build();

    mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 50), 5000);
    return true;
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
    if (mapboxMap != null) {
      mapboxMap.removeOnMapClickListener(this);
    }
    mapView.onDestroy();
  }
}
