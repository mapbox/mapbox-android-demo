package com.mapbox.mapboxandroiddemo.examples;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.MyLocationTracking;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationListener;
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

public class LocationPickerActivity extends AppCompatActivity {

    private MapView mapView;
    private MapboxMap map;
    private LocationServices locationServices;
    private Marker resultsMarker;
    private ImageView dropPinView;
    private Button selectLocationButton;
    private boolean initialCameraPositonSet = false;

    private static final int PERMISSIONS_LOCATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_location_picker);

        locationServices = LocationServices.getLocationServices(LocationPickerActivity.this);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;

                if (!locationServices.areLocationPermissionsGranted()) {
                    ActivityCompat.requestPermissions(LocationPickerActivity.this, new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_LOCATION);
                } else {
                    enableLocation();
                }
            }
        });

        dropPinView = new ImageView(this);
        dropPinView.setImageResource(R.drawable.default_marker);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        dropPinView.setLayoutParams(params);
        mapView.addView(dropPinView);

        selectLocationButton = (Button) findViewById(R.id.select_location_button);
        selectLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (map != null) {
                    if (resultsMarker == null) {
                        float[] coords = getDropPinTipCoordinates();
                        LatLng latLng = map.getProjection().fromScreenLocation(new PointF(coords[0], coords[1]));
                        selectLocationButton.setBackgroundColor(ContextCompat.getColor(LocationPickerActivity.this, R.color.colorAccent));
                        selectLocationButton.setText("Cancel");
                        dropPinView.setVisibility(View.GONE);

                        IconFactory iconFactory = IconFactory.getInstance(LocationPickerActivity.this);
                        Drawable iconDrawable = ContextCompat.getDrawable(LocationPickerActivity.this, R.drawable.purple_marker);
                        Icon icon = iconFactory.fromDrawable(iconDrawable);


                        //resultsMarker = map.addMarker(new MarkerOptions().position(latLng).icon(icon));
                        resultsMarker = map.addMarker(new MarkerViewOptions().icon(icon).position(latLng));

                        //geocode(latLng);
                    } else {
                        map.removeMarker(resultsMarker);

                        selectLocationButton.setBackgroundColor(ContextCompat.getColor(LocationPickerActivity.this, R.color.colorPrimary));
                        selectLocationButton.setText("Select a location");

                        dropPinView.setVisibility(View.VISIBLE);
                        resultsMarker = null;
                    }
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void enableLocation() {

        if (locationServices.getLastLocation() != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationServices.getLastLocation()), 16));
            initialCameraPositonSet = true;
        }

        locationServices.addLocationListener(new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null && !initialCameraPositonSet) {
                    // Move the map camera to where the user location is
                    map.setCameraPosition(new CameraPosition.Builder()
                            .target(new LatLng(location))
                            .zoom(16)
                            .build());

                    // Set to true so we aren't setting the camera every time location is updated
                    initialCameraPositonSet = true;
                }
            }
        });
        // Enable the location layer on the map
        map.setMyLocationEnabled(true);
        map.getTrackingSettings().setMyLocationTrackingMode(MyLocationTracking.TRACKING_FOLLOW);
    }

    private float[] getDropPinTipCoordinates() {
        float x = dropPinView.getLeft() + (dropPinView.getWidth() / 2);
        float y = dropPinView.getBottom();

        return new float[]{x, y};
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
