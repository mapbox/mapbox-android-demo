package com.mapbox.mapboxandroiddemo.examples.ig;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.snapshotter.MapSnapshot;
import com.mapbox.mapboxsdk.snapshotter.MapSnapshotter;

import java.util.ArrayList;
import java.util.List;

public class SnapshotNotificationActivity extends AppCompatActivity {
    private MapView mapView;
    private List<MapSnapshotter> snapshotters = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token));

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_camera_bounding_box);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {

                // Toast instructing user to tap on the map to start animation and set bounds
                Toast.makeText(
                        SnapshotNotificationActivity.this,
                        getString(R.string.tap_on_map_for_notification),
                        Toast.LENGTH_LONG
                ).show();

                // When user clicks the map, fit the camera to the bounding box
                mapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng point) {
                        startSnapShot(mapboxMap.getProjection().getVisibleRegion().latLngBounds);
                    }
                });
            }
        });
    }

    private void startSnapShot(LatLngBounds latLngBounds) {

        // Define the dimensions based on map dimensions
        MapSnapshotter.Options options = new MapSnapshotter.Options(
                mapView.getMeasuredWidth(), mapView.getMeasuredHeight())
                .withRegion(latLngBounds);

        MapSnapshotter snapshotter = new MapSnapshotter(SnapshotNotificationActivity.this, options);

        snapshotter.start(new MapSnapshotter.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(MapSnapshot snapshot) {
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(SnapshotNotificationActivity.this, "123")
                                .setSmallIcon(R.drawable.ic_circle)
                                .setContentTitle(getString(R.string.activity_image_generator_snapshot_notification_title))
                                .setContentText(getString(R.string.activity_image_generator_snapshot_notification_description))
                                .setLargeIcon(snapshot.getBitmap());

                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(1, mBuilder.build());
            }
        });
        snapshotters.add(snapshotter);
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

        // Make sure to stop the snapshotters on pause
        for (MapSnapshotter snapshotter : snapshotters) {
            snapshotter.cancel();
        }
        snapshotters.clear();
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
