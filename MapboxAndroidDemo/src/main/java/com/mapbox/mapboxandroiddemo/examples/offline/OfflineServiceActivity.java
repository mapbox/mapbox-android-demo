package com.mapbox.mapboxandroiddemo.examples.offline;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;

import org.json.JSONObject;

public class OfflineServiceActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private OfflineManager offlineManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    setContentView(R.layout.activity_offline_service);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull final MapboxMap map) {

        // Set up the OfflineManager
        offlineManager = OfflineManager.getInstance(SimpleOfflineMapActivity.this);

        // Create a bounding box for the offline region
        LatLngBounds latLngBounds = new LatLngBounds.Builder()
          .include(new LatLng(37.7897, -119.5073)) // Northeast
          .include(new LatLng(37.6744, -119.6815)) // Southwest
          .build();

        // Define the offline region
        OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
          mapboxMap.getStyleUrl(),
          latLngBounds,
          10,
          20,
          SimpleOfflineMapActivity.this.getResources().getDisplayMetrics().density);

        // Set the metadata
        byte[] metadata;
        try {
          JSONObject jsonObject = new JSONObject();
          jsonObject.put(JSON_FIELD_REGION_NAME, "Yosemite National Park");
          String json = jsonObject.toString();
          metadata = json.getBytes(JSON_CHARSET);
        } catch (Exception exception) {
          Log.e(TAG, "Failed to encode metadata: " + exception.getMessage());
          metadata = null;
        }


      }
    });

    Intent startServiceIntent = new Intent(this, DownloadOfflineMapService.class);
    startService(startServiceIntent);


  }


  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
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
    mapView.onDestroy();
  }


}
