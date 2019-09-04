package com.mapbox.mapboxandroiddemo.examples.basics;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;

/**
 * Include a map fragment within your app using Android support library.
 */
public class SupportMapFragmentActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_basic_support_map_frag);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // Create supportMapFragment
    SupportMapFragment mapFragment;
    if (savedInstanceState == null) {

      // Create fragment
      final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

      // Build mapboxMap
      MapboxMapOptions options = MapboxMapOptions.createFromAttributes(this, null);
      options.camera(new CameraPosition.Builder()
        .target(new LatLng(-52.6885, -70.1395))
        .zoom(9)
        .build());

      // Create map fragment
      mapFragment = SupportMapFragment.newInstance(options);

      // Add map fragment to parent container
      transaction.add(R.id.container, mapFragment, "com.mapbox.map");
      transaction.commit();
    } else {
      mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentByTag("com.mapbox.map");
    }

    if (mapFragment != null) {
      mapFragment.getMapAsync(new OnMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull MapboxMap mapboxMap) {
          mapboxMap.setStyle(Style.SATELLITE, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

              // Map is set up and the style has loaded. Now you can add data or make other map adjustments


            }
          });
        }
      });
    }
  }
}
