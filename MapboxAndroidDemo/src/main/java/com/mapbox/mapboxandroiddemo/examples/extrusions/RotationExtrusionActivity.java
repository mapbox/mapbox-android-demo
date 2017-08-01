package com.mapbox.mapboxandroiddemo.examples.extrusions;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationSource;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.style.functions.Function;
import com.mapbox.mapboxsdk.style.functions.stops.IdentityStops;
import com.mapbox.mapboxsdk.style.layers.FillExtrusionLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.light.Light;
import com.mapbox.mapboxsdk.style.light.Position;
import com.mapbox.services.android.telemetry.location.LocationEngine;

import java.util.List;

import static com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerMode.COMPASS;
import static com.mapbox.mapboxsdk.style.layers.Filter.eq;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionBase;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionHeight;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionOpacity;

/**
 * Created by Anthony-Agby on 8/1/17.
 */

public class RotationExtrusionActivity extends AppCompatActivity implements SensorEventListener{

    private MapView mapView;
    private MapboxMap mapboxMap;
    private Light light;
    private boolean isMapAnchorLight;
    private boolean isLowIntensityLight;
    private boolean isRedColor;
    private boolean isInitPosition;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private SensorManager sensorManager;
    private Sensor gyro;
    private Sensor magnetic;
    float[] mGravity;
    float[] mGeomagnetic;
    float azimut;
    float pitch;
    float roll;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token));

        setContentView(R.layout.activity_extrusion_rotation);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap map) {
                mapboxMap = map;
                setupBuildings();
                setupLocationPlugin();
            }
        });

        //initallize gyroscope
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorManager.registerListener(this, gyro , SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetic , SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void setupBuildings() {
        FillExtrusionLayer fillExtrusionLayer = new FillExtrusionLayer("3d-buildings", "composite");
        fillExtrusionLayer.setSourceLayer("building");
        fillExtrusionLayer.setFilter(eq("extrude", "true"));
        fillExtrusionLayer.setMinZoom(15);
        fillExtrusionLayer.setProperties(
                fillExtrusionColor(Color.LTGRAY),
                fillExtrusionHeight(Function.property("height", new IdentityStops<Float>())),
                fillExtrusionBase(Function.property("min_height", new IdentityStops<Float>())),
                fillExtrusionOpacity(0.9f)
        );
        mapboxMap.addLayer(fillExtrusionLayer);
    }

    private void setupLocationPlugin(){
//        locationEngine = new LocationSource(this);
//        locationLayerPlugin = new LocationLayerPlugin(mapView, mapboxMap, locationEngine);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        locationLayerPlugin.setLocationLayerEnabled(COMPASS);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_building, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (light != null) {
            int id = item.getItemId();
            if (id == R.id.menu_action_anchor) {
                isMapAnchorLight = !isMapAnchorLight;
                light.setAnchor(isMapAnchorLight ? Property.ANCHOR_MAP : Property.ANCHOR_VIEWPORT);
            } else if (id == R.id.menu_action_intensity) {
                isLowIntensityLight = !isLowIntensityLight;
                light.setIntensity(isLowIntensityLight ? 0.35f : 1.0f);
            } else if (id == android.R.id.home) {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                pitch = orientation[1];
                roll = orientation[2];

//                CameraPosition cameraPosition = new CameraPosition();

                CameraPosition position = new CameraPosition.Builder()
//                        .target(new LatLng(51.50550, -0.07520)) // Sets the new camera position
//                        .zoom(17) // Sets the zoom
//                        .bearing(180) // Rotate the camera
                        .tilt(pitch * 180) // Set the camera tilt
                        .build(); // Creates a CameraPosition from the builder

                mapboxMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(position), 1000);

                Log.e("Test", "azi: " + azimut + ", pitch: " + pitch + ", roll: " + roll);
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.e("Test", "accuracy Changed");
    }
}
