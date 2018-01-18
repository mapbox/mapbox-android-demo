package com.mapbox.mapboxandroiddemo.examples.snapshot;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.MainActivity;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.snapshotter.MapSnapshot;
import com.mapbox.mapboxsdk.snapshotter.MapSnapshotter;

import static android.app.PendingIntent.getActivity;

/**
 * Test activity showing how to use a the {@link com.mapbox.mapboxsdk.snapshotter.MapSnapshotter}
 * in a way that utilizes provided bitmaps in native notifications.
 */
public class SnapshotNotificationActivity extends AppCompatActivity {
  private MapView mapView;
  private MapSnapshotter mapSnapshotter;
  private MapboxMap mapboxMap;
  private NotificationManager notificationManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_snapshot_notification);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);

    // Set a callback for when MapboxMap is ready to be used
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(final MapboxMap mapboxMap) {

        SnapshotNotificationActivity.this.mapboxMap = mapboxMap;

        // When user clicks the map, start the snapshotting process with the given parameters
        mapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
          @Override
          public void onMapClick(@NonNull LatLng point) {
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
        new MapSnapshotter.Options(width, height).withStyle(mapboxMap.getStyleUrl()).withRegion(latLngBounds);

      mapSnapshotter = new MapSnapshotter(SnapshotNotificationActivity.this, options);
    } else {
      // Reuse pre-existing MapSnapshotter instance
      mapSnapshotter.setSize(width, height);
      mapSnapshotter.setRegion(latLngBounds);
    }

    mapSnapshotter.start(new MapSnapshotter.SnapshotReadyCallback() {
      @Override
      public void onSnapshotReady(MapSnapshot snapshot) {
        createNotification(snapshot.getBitmap());
      }
    });
  }

  /**
   * Creates a notification with given bitmap as a large icon
   *
   * @param bitmap to set as large icon
   */
  private void createNotification(Bitmap bitmap) {
    final int NOTIFY_ID = 1002;
    String id = "channel_id";
    Intent intent;

    PendingIntent pendingIntent;
    if (notificationManager == null) {
      notificationManager =
        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel notificationChannel = notificationManager.getNotificationChannel(id);
      if (notificationChannel == null) {
        notificationChannel = new NotificationChannel(id, "channel_name", NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setDescription("channel_description");
        notificationManager.createNotificationChannel(notificationChannel);
      }
    }
    intent = new Intent(this, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    pendingIntent = getActivity(this, 0, intent, 0);

    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, id)
      .setContentTitle("content")
      .setSmallIcon(R.drawable.ic_circle)
      .setContentTitle(getString(R.string.activity_image_generator_snapshot_notification_title))
      .setContentText(getString(R.string.activity_image_generator_snapshot_notification_description))
      .setContentIntent(pendingIntent)
      .setLargeIcon(bitmap);
    Notification notification = builder.build();
    notificationManager.notify(NOTIFY_ID, notification);
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
