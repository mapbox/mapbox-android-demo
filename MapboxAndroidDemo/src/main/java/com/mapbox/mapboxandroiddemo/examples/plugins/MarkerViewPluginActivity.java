package com.mapbox.mapboxandroiddemo.examples.plugins;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerView;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Use the Mapbox MarkerView Plugin to create a marker which uses a custom Android view as the icon.
 */
public class MarkerViewPluginActivity extends AppCompatActivity {

  private MapView mapView;
  private MarkerView markerView;
  private MarkerViewManager markerViewManager;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_markerview_plugin);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull final MapboxMap mapboxMap) {

        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {

            // Initialize the MarkerViewManager
            markerViewManager = new MarkerViewManager(mapView, mapboxMap);

            // Use an XML layout to create a View object
            View customView = LayoutInflater.from(MarkerViewPluginActivity.this).inflate(
              R.layout.marker_view_bubble, null);
            customView.setLayoutParams(new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));

            // Set the View's TextViews with content
            TextView titleTextView = customView.findViewById(R.id.marker_window_title);
            titleTextView.setText(R.string.draw_marker_options_title);

            TextView snippetTextView = customView.findViewById(R.id.marker_window_snippet);
            snippetTextView.setText(R.string.draw_marker_options_snippet);

            // Use the View to create a MarkerView which will eventually be given to
            // the plugin's MarkerViewManager class
            markerView = new MarkerView(new LatLng(48.13863, 11.57603), customView);
            markerViewManager.addMarker(markerView);
          }
        });
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
  public void onDestroy() {
    super.onDestroy();
    if (markerViewManager != null) {
      markerViewManager.onDestroy();
    }
    mapView.onDestroy();
  }
}

