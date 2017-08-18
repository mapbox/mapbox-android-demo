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
import android.util.Log;
import android.widget.Toast;

import com.mapbox.androidsdk.plugins.building.BuildingPlugin;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

/**
 * Change the camera's bearing and tilt based on device movement while viewing building extrusions
 */
public class RotationExtrusionActivity extends AppCompatActivity implements SensorEventListener {
  private MapView mapView;
  private MapboxMap mapboxMap;
  private SensorManager sensorManager;
  private SensorControl sensorControl;
  private float[] gravityArray;
  private float[] magneticArray;
  private float[] inclinationMatrix = new float[9];
  private float[] rotationMatrix = new float[9];

  // Amplifiers that translate small phone orientation movements into larger viewable map changes.
  // Pitch is negative to compensate for the negative readings from the device while face up
  // 90 is used based on the viewable angle when viewing the map (from phone being flat to facing you).
  private static final int PITCH_AMPLIFIER = -90;
  private static final int BEARING_AMPLIFIER = 90;

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
        setupBuildingExtrusionPlugin();
      }
    });
  }

  private void setupBuildingExtrusionPlugin() {
    BuildingPlugin buildingPlugin = new BuildingPlugin(mapView, mapboxMap);
    buildingPlugin.setColor(Color.LTGRAY);
    buildingPlugin.setOpacity(0.6f);
    buildingPlugin.setMinZoomLevel(15);
    buildingPlugin.setVisibility(true);
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
    sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    sensorControl = new SensorControl(sensorManager);

    registerSensorListeners();
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
    sensorManager.unregisterListener(this, sensorControl.getGyro());
    sensorManager.unregisterListener(this, sensorControl.getMagnetic());
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
    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
      gravityArray = event.values;
    }
    if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
      magneticArray = event.values;
    }
    if (gravityArray != null && magneticArray != null) {
      boolean success = SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, gravityArray, magneticArray);
      if (success) {
        if (mapboxMap != null) {
          int mapCameraAnimationMillisecondsSpeed = 100;
          mapboxMap.animateCamera(CameraUpdateFactory
            .newCameraPosition(createNewCameraPosition()), mapCameraAnimationMillisecondsSpeed
          );
        }
      }
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
    // Intentionally left empty
  }

  private CameraPosition createNewCameraPosition() {
    float[] orientation = new float[3];
    SensorManager.getOrientation(rotationMatrix, orientation);
    float pitch = orientation[1];
    float roll = orientation[2];

    CameraPosition position = new CameraPosition.Builder()
      .tilt(pitch * PITCH_AMPLIFIER)
      .bearing(roll * BEARING_AMPLIFIER)
      .build();

    return position;
  }

  private void registerSensorListeners() {
    int sensorEventDeliveryRate = 200;
    if (sensorControl.getGyro() != null) {
      sensorManager.registerListener(this, sensorControl.getGyro(), sensorEventDeliveryRate);
    } else {
      Log.d("RotationExtrusion", "Whoops, no accelerometer sensor");
      Toast.makeText(this, R.string.no_accelerometer, Toast.LENGTH_SHORT).show();
    }
    if (sensorControl.getMagnetic() != null) {
      sensorManager.registerListener(this, sensorControl.getMagnetic(), sensorEventDeliveryRate);
    } else {
      Log.d("RotationExtrusion", "Whoops, no magnetic sensor");
      Toast.makeText(this, R.string.no_magnetic, Toast.LENGTH_SHORT).show();
    }
  }

  private class SensorControl {
    private Sensor gyro;
    private Sensor magnetic;

    SensorControl(SensorManager sensorManager) {
      this.gyro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
      this.magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    Sensor getGyro() {
      return gyro;
    }

    Sensor getMagnetic() {
      return magnetic;
    }
  }
}

