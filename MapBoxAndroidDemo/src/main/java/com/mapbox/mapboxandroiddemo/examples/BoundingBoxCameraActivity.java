package com.mapbox.mapboxandroiddemo.examples;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

public class BoundingBoxCameraActivity extends AppCompatActivity {

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_animate);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {

                final Marker marker1 = mapboxMap.addMarker(new MarkerOptions().position(new LatLng(-36.848380, 174.762275)).title("Sky Tower"));
                final Marker marker2 = mapboxMap.addMarker(new MarkerOptions().position(new LatLng(-36.847179, 174.777072)).title("Vector Arena"));
                final Marker marker3 = mapboxMap.addMarker(new MarkerOptions().position(new LatLng(-36.801887, 175.108709)).title("Waiheke Island"));
                final Marker marker4 = mapboxMap.addMarker(new MarkerOptions().position(new LatLng(-36.835059, 174.691237)).title("Waitemata Harbour"));

                // When user clicks the map, animate to new camera location
                mapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng point) {
                        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                                .include(marker1.getPosition())
                                .include(marker2.getPosition())
                                .include(marker3.getPosition())
                                .include(marker4.getPosition())
                                .build();

                        mapboxMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 50));

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
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
