package com.mapbox.mapboxandroiddemo.examples.snapshot;
// #-code-snippet: snapshot-share-activity full-java
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.snapshotter.MapSnapshot;
import com.mapbox.mapboxsdk.snapshotter.MapSnapshotter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SnapshotShareActivity extends AppCompatActivity {
  private MapView mapView;
  private MapSnapshotter mapSnapshotter;
  private MapboxMap mapboxMap;
  private FloatingActionButton cameraFab;
  private boolean hasStartedSnapshotGeneration;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_snapshot_share);

    cameraFab = findViewById(R.id.camera_share_snapshot_image_fab);
    cameraFab.setImageResource(R.drawable.ic_camera);

    hasStartedSnapshotGeneration = false;

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);

    // Set a callback for when MapboxMap is ready to be used
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(final MapboxMap mapboxMap) {

        SnapshotShareActivity.this.mapboxMap = mapboxMap;

        // When user clicks the map, start the snapshotting process with the given parameters
        cameraFab.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {

            if (!hasStartedSnapshotGeneration) {
              hasStartedSnapshotGeneration = true;
              Toast.makeText(SnapshotShareActivity.this, R.string.loading_snapshot_image, Toast.LENGTH_LONG).show();
              startSnapShot(
                mapboxMap.getProjection().getVisibleRegion().latLngBounds,
                mapView.getMeasuredHeight(),
                mapView.getMeasuredWidth());
            }
          }
        });
      }
    });

    // To account for new security measures regarding file management that were released with Android Nougat.
    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
    StrictMode.setVmPolicy(builder.build());
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
        new MapSnapshotter.Options(width, height).withRegion(latLngBounds).withStyle(mapboxMap.getStyleUrl());

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

        Bitmap bitmapOfMapSnapshotImage = snapshot.getBitmap();

        Uri bmpUri = getLocalBitmapUri(bitmapOfMapSnapshotImage);

        Intent shareIntent = new Intent();
        shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
        shareIntent.setType("image/png");
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share map image"));

        hasStartedSnapshotGeneration = false;
      }
    });
  }

  private Uri getLocalBitmapUri(Bitmap bmp) {
    Uri bmpUri = null;
    File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
      "share_image_" + System.currentTimeMillis() + ".png");
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(file);
      bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
      try {
        out.close();
      } catch (IOException exception) {
        exception.printStackTrace();
      }
      bmpUri = Uri.fromFile(file);
    } catch (FileNotFoundException exception) {
      exception.printStackTrace();
    }
    return bmpUri;
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
// #-end-code-snippet: snapshot-share-activity full-java