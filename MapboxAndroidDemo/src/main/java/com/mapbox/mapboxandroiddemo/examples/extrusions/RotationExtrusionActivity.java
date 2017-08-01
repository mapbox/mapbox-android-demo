package com.mapbox.mapboxandroiddemo.examples.extrusions;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.functions.Function;
import com.mapbox.mapboxsdk.style.functions.stops.IdentityStops;
import com.mapbox.mapboxsdk.style.layers.FillExtrusionLayer;

import static com.mapbox.mapboxsdk.style.layers.Filter.eq;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionBase;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionHeight;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionOpacity;

/**
 * Change the bearing and tilt of camera when viewing extrusions to get full 3D effect
 */
public class RotationExtrusionActivity extends AppCompatActivity implements SensorEventListener{

    private MapView mapView;
    private MapboxMap mapboxMap;
    private SensorManager sensorManager;
    private Sensor gyro;
    private Sensor magnetic;
    float[] gravArray;
    float[] magneticArray;
    float azimut;
    float pitch;
    float roll;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token));

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_extrusion_rotation);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap map) {
                mapboxMap = map;
                setupBuildings();
            }
        });

        //initialize sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorManager.registerListener(this, gyro , 200);
        sensorManager.registerListener(this, magnetic , 200);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
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
            gravArray = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            magneticArray = event.values;
        if (gravArray != null && magneticArray != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, gravArray, magneticArray);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimut = orientation[0];
                pitch = orientation[1];
                roll = orientation[2];

                CameraPosition position = new CameraPosition.Builder()
                        .tilt(pitch * -90)
                        .bearing(roll * 90)
                        .build();

                if(mapboxMap != null){
                    mapboxMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(position), 100
                    );
                }
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
