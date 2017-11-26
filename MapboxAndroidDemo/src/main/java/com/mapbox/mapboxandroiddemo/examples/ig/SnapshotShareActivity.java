package com.mapbox.mapboxandroiddemo.examples.ig;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.snapshotter.MapSnapshot;
import com.mapbox.mapboxsdk.snapshotter.MapSnapshotter;

public class SnapshotShareActivity extends AppCompatActivity {
  private MapView mapView;
  private MapSnapshotter mapSnapshotter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_snapshot_share);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);

    // Set a callback for when MapboxMap is ready to be used
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(final MapboxMap mapboxMap) {

        // When user clicks the map, start the snapshotting process with the given parameters
        FloatingActionButton cameraFab = (FloatingActionButton) findViewById(R.id.camera_fab);

        cameraFab.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {

            Log.d("SnapshotShareActivity", "onClick:  clicked on fab");

            startSnapShot(
              mapboxMap.getProjection().getVisibleRegion().latLngBounds,
              mapView.getMeasuredHeight(),
              mapView.getMeasuredWidth());
          }
        });
      }
    });
  }

  /**
   * Creates bitmap from given parameters, and creates a notification with that bitmap
   *
   * @param latLngBounds of map
   * @param height       of map
   * @param width        of map
   */
  private void startSnapShot(LatLngBounds latLngBounds, int height, int width) {
    if (mapSnapshotter == null) {
      // Initialize snapshotter with map dimensions and given bounds
      MapSnapshotter.Options options =
        new MapSnapshotter.Options(width, height)
          .withRegion(latLngBounds);

      mapSnapshotter = new MapSnapshotter(SnapshotShareActivity.this, options);
    } else {
      // Reuse pre-existing MapSnapshotter instance
      mapSnapshotter.setSize(width, height);
      mapSnapshotter.setRegion(latLngBounds);
      mapSnapshotter.setRegion(latLngBounds);
    }

    mapSnapshotter.start(new MapSnapshotter.SnapshotReadyCallback() {
      @Override
      public void onSnapshotReady(MapSnapshot snapshot) {

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, snapshot.getBitmap());
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, "Share Image"));

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
    // Make sure to stop the snapshotter on pause if it exists
    if (mapSnapshotter != null) {
      mapSnapshotter.cancel();
    }
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
    mapView.onDestroy();
  }
}
