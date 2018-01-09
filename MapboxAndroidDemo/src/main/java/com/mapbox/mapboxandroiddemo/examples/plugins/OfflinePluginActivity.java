package com.mapbox.mapboxandroiddemo.examples.plugins;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;
import com.mapbox.mapboxsdk.plugins.offline.model.NotificationOptions;
import com.mapbox.mapboxsdk.plugins.offline.model.OfflineDownloadOptions;
import com.mapbox.mapboxsdk.plugins.offline.offline.OfflinePlugin;
import com.mapbox.mapboxsdk.plugins.offline.utils.OfflineUtils;

public class OfflinePluginActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private int MIN_ZOOM = 2;
  private int MAX_ZOOM = 6;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_offline_plugin);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(final MapboxMap mapboxMap) {

        OfflinePluginActivity.this.mapboxMap = mapboxMap;

        // Customize map with markers, polylines, etc.

        findViewById(R.id.download_region_fab).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {

            double latitudeNorth = mapboxMap.getProjection().getVisibleRegion().latLngBounds.getLatNorth();
            double longitudeEast = mapboxMap.getProjection().getVisibleRegion().latLngBounds.getLonEast();
            double latitudeSouth = mapboxMap.getProjection().getVisibleRegion().latLngBounds.getLatSouth();
            double longitudeWest = mapboxMap.getProjection().getVisibleRegion().latLngBounds.getLonWest();

            if (!validCoordinates(latitudeNorth, longitudeEast, latitudeSouth, longitudeWest)) {
              Toast.makeText(OfflinePluginActivity.this,
                R.string.coordinates_need_to_valid_toast, Toast.LENGTH_SHORT).show();
              return;
            }

            // create offline definition from data
            OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
              mapboxMap.getStyleUrl(),
              new LatLngBounds.Builder()
                .include(new LatLng(latitudeNorth, longitudeEast))
                .include(new LatLng(latitudeSouth, longitudeWest))
                .build(),
              MIN_ZOOM,
              MAX_ZOOM,
              getResources().getDisplayMetrics().density
            );

            // customise notification appearance
            NotificationOptions notificationOptions = NotificationOptions.builder(OfflinePluginActivity.this)
              .smallIconRes(R.drawable.mapbox_logo_icon)
              .returnActivity(OfflinePluginActivity.class.getName()).build();

            // start offline download
            OfflinePlugin.getInstance(OfflinePluginActivity.this).startDownload(
              OfflineDownloadOptions.builder()
                .definition(definition)
                .metadata(OfflineUtils.convertRegionName("Test region"))
                .notificationOptions(notificationOptions)
                .build()
            );
          }
        });
      }
    });
  }

  private boolean validCoordinates(double latitudeNorth, double longitudeEast, double latitudeSouth,
                                   double longitudeWest) {
    if (latitudeNorth < -90 || latitudeNorth > 90) {
      return false;
    } else if (longitudeEast < -180 || longitudeEast > 180) {
      return false;
    } else if (latitudeSouth < -90 || latitudeSouth > 90) {
      return false;
    } else if (longitudeWest < -180 || longitudeWest > 180) {
      return false;
    }
    return true;
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