package com.mapbox.mapboxandroiddemo.examples.styles;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

/**
 * Example loads the map style via a locally stored style JSON file or custom raster style
 */
public class LocalStyleSourceActivity extends AppCompatActivity {

  private MapView mapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_style_local_style_source);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {

        Button loadCustomRasterStyleButton = (Button) findViewById(R.id.load_custom_raster_button);
        loadCustomRasterStyleButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            // Reference the custom raster file URL and pass through as the string parameter
            mapView.setStyleUrl("https://www.mapbox.com/android-docs/files/mapbox-raster-v8.json");
          }
        });

        Button loadLocalStyleButton = (Button) findViewById(R.id.load_local_style_button);
        loadLocalStyleButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            // Reference the local JSON style file in the assets folder and pass through as the string parameter
            mapView.setStyleUrl("asset://local_style_file.json");
          }
        });
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
