package com.mapbox.mapboxandroiddemo.examples.plugins;
// #-code-snippet: localization-plugin-activity full-java
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.localization.LocalizationPlugin;
import com.mapbox.mapboxsdk.plugins.localization.MapLocale;

/**
 * Use the localization plugin to retrieve the device's language and set all map text labels to that language.
 */
public class LocalizationPluginActivity extends AppCompatActivity implements OnMapReadyCallback {

  private MapView mapView;
  private LocalizationPlugin localizationPlugin;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_localization_plugin);
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(final MapboxMap mapboxMap) {

    localizationPlugin = new LocalizationPlugin(mapView, mapboxMap);

    findViewById(R.id.language_one_cardview).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        localizationPlugin.setMapLanguage(MapLocale.ARABIC);
      }
    });
    findViewById(R.id.language_two_cardview).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        localizationPlugin.setMapLanguage(MapLocale.RUSSIAN);
      }
    });
    findViewById(R.id.language_three_cardview).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        localizationPlugin.setMapLanguage(MapLocale.SIMPLIFIED_CHINESE);
      }
    });
    findViewById(R.id.match_map_to_device_language).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Snackbar.make(view, R.string.change_device_language_instruction, Snackbar.LENGTH_LONG).show();
        try {
          localizationPlugin.matchMapLanguageWithDeviceDefault();
          CameraPosition position = new CameraPosition.Builder()
            .target(new LatLng(34.032666, -80.363160))
            .zoom(2.038777)
            .build();

          mapboxMap.animateCamera(CameraUpdateFactory
            .newCameraPosition(position), 1000);
        } catch (RuntimeException exception) {
          Snackbar.make(view, exception.toString(), Snackbar.LENGTH_LONG).show();
        }
      }
    });

    Toast.makeText(this, R.string.instruction_description, Toast.LENGTH_LONG).show();
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
// #-end-code-snippet: localization-plugin-activity full-java