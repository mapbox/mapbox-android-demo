package com.mapbox.mapboxandroiddemo.examples;

import android.app.FragmentTransaction;
import android.location.Location;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationSource;
import com.mapbox.mapboxsdk.maps.MapFragment;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;

/**
 * Include a map fragment within your app using Android support library.
 */
public class MapFragmentActivity extends WearableActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_basic_support_map_frag);

    LocationEngine locationEngine = new LocationSource(this);
    locationEngine.activate();

    locationEngine.addLocationEngineListener(new LocationEngineListener() {
      @Override
      public void onConnected() {

      }

      @Override
      public void onLocationChanged(Location location) {

      }
    });

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // Create supportMapFragment
    MapFragment mapFragment;
    if (savedInstanceState == null) {

      // Create fragment
      final FragmentTransaction transaction = getFragmentManager().beginTransaction();

      LatLng patagonia = new LatLng(-52.6885, -70.1395);

      // Build mapboxMap
      MapboxMapOptions options = new MapboxMapOptions();
      options.styleUrl(Style.SATELLITE);
      options.camera(new CameraPosition.Builder()
        .target(patagonia)
        .zoom(9)
        .build());

      // Create map fragment
      mapFragment = MapFragment.newInstance(options);

      // Add map fragment to parent container
      transaction.add(R.id.container, mapFragment, "com.mapbox.map");
      transaction.commit();
    } else {
      mapFragment = (MapFragment) getFragmentManager().findFragmentByTag("com.mapbox.map");
    }

    mapFragment.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {

        // Customize map with markers, polylines, etc.

      }
    });
  }
}
