package com.mapbox.mapboxandroiddemo.examples.annotations;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

/**
 * Attach a view to a given position on the map.
 */
public class BasicMarkerViewActivity extends AppCompatActivity {

  private MapView mapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_annotation_basic_marker_view);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {

        Icon icon = IconFactory.getInstance(BasicMarkerViewActivity.this).fromResource(R.drawable.purple_marker);

        // The easiest way to add a marker view
        mapboxMap.addMarker(new MarkerViewOptions()
          .position(new LatLng(-37.821629, 144.978535)));

        // marker view using all the different options available
        mapboxMap.addMarker(new MarkerViewOptions()
          .position(new LatLng(-37.822829, 144.981842))
          .icon(icon)
          .rotation(90)
          .anchor(0.5f, 0.5f)
          .alpha(0.5f)
          .title(getString(R.string.activity_basic_mapbox_options_title))
          .snippet(getString(R.string.basic_marker_activity_marker_options_snippet))
          .infoWindowAnchor(0.5f, 0.5f)
          .flat(true));
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