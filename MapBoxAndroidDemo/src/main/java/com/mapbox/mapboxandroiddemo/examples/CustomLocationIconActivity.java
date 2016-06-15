package com.mapbox.mapboxandroiddemo.examples;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationListener;
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

public class CustomLocationIconActivity extends AppCompatActivity {

    private MapView mapView;
    private MapboxMap map;
    private LocationServices locationServices;
    private Animation pulseAnimation;

    private static final int PERMISSIONS_LOCATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_custom_icon_mapview);

        locationServices = LocationServices.getLocationServices(CustomLocationIconActivity.this);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {

                map = mapboxMap;

                if (!locationServices.areLocationPermissionsGranted()) {
                    ActivityCompat.requestPermissions(CustomLocationIconActivity.this, new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_LOCATION);
                } else {
                    enableLocation();
                    map.getMyLocationViewSettings().setForegroundTintColor(ContextCompat.getColor(CustomLocationIconActivity.this, R.color.mapboxRed));
                    map.getMyLocationViewSettings().setAccuracyAlpha(0);

                    // TODO animate the user location properly
                    Drawable convertView = map.getMyLocationViewSettings().getForegroundDrawable();
                    pulseAnimation = AnimationUtils.loadAnimation(CustomLocationIconActivity.this, R.anim.pulse);
                    pulseAnimation.setRepeatCount(Animation.INFINITE);
                    pulseAnimation.setRepeatMode(Animation.RESTART);
                    pulseAnimation.start();
                }


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

    private void enableLocation() {
        locationServices.addLocationListener(new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    // Move the map camera to where the user location is
                    map.setCameraPosition(new CameraPosition.Builder()
                            .target(new LatLng(location))
                            .zoom(16)
                            .build());
                }
            }
        });
        // Enable or disable the location layer on the map
        map.setMyLocationEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_LOCATION: {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableLocation();
                }
            }
        }
    }
}
