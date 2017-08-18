package com.mapbox.mapboxandroiddemo.labs;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.location.Location;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.services.android.location.LostLocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.location.LocationEnginePriority;
import com.mapbox.services.android.telemetry.permissions.PermissionsListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsManager;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class RadarCompassActivity extends AppCompatActivity implements LocationEngineListener, PermissionsListener,
  SensorEventListener {

  private MapView mapView;
  private MapboxMap mapboxMap;

  private static final String TAG = "CameraFragment";
  private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
  private String cameraId;
  protected CameraDevice cameraDevice;
  protected CameraCaptureSession cameraCaptureSessions;
  protected CaptureRequest.Builder captureRequestBuilder;
  private Size imageDimension;
  private ImageReader imageReader;
  private File file;
  private static final int REQUEST_CAMERA_PERMISSION = 200;
  private Handler backgroundHandler;
  private HandlerThread backgroundThread;
  private TextureView textureView;
  private PermissionsManager permissionsManager;
  private LocationLayerPlugin locationPlugin;
  private LocationEngine locationEngine;

  private SensorManager sensorManager;
  private Sensor magnetic;

  private SensorControl sensorControl;

  private float[] gravityArray;
  private float[] magneticArray;
  private float[] inclinationMatrix = new float[9];
  private float[] rotationMatrix = new float[9];
  private static final int BEARING_AMPLIFIER = 90;


  static {
    ORIENTATIONS.append(Surface.ROTATION_0, 90);
    ORIENTATIONS.append(Surface.ROTATION_90, 0);
    ORIENTATIONS.append(Surface.ROTATION_180, 270);
    ORIENTATIONS.append(Surface.ROTATION_270, 180);
  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_radar_compass);

    textureView = (TextureView) findViewById(R.id.camera_preview_textureview);
    assert textureView != null;

    textureView.setSurfaceTextureListener(textureListener);

    if (!deviceHasCamera(this)) {
      Toast.makeText(this, R.string.no_camera, Toast.LENGTH_LONG).show();
    }

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {

        // Customize map with markers, polylines, etc.
        RadarCompassActivity.this.mapboxMap = mapboxMap;
        enableLocationPlugin();

      }
    });
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
  }

  private CameraPosition createNewCameraPosition() {
    float[] orientation = new float[3];
    SensorManager.getOrientation(rotationMatrix, orientation);
    float roll = orientation[2];

    CameraPosition position = new CameraPosition.Builder()
      .bearing(roll * BEARING_AMPLIFIER)
      .build();

    return position;
  }


  private void registerSensorListeners() {
    int sensorEventDeliveryRate = 200;
    if (sensorControl.getMagnetic() != null) {
      sensorManager.registerListener(this, sensorControl.getMagnetic(), sensorEventDeliveryRate);
    } else {
      Log.d("RotationExtrusion", "Whoops, no magnetic sensor");
      Toast.makeText(this, R.string.no_magnetic_sensor, Toast.LENGTH_LONG).show();
    }
  }

  private class SensorControl {

    private Sensor magnetic;

    SensorControl(SensorManager sensorManager) {
      this.magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    private Sensor getMagnetic() {
      return magnetic;
    }
  }

  // Add the mapView lifecycle to the activity's lifecycle methods
  @Override
  public void onResume() {
    super.onResume();
    mapView.onResume();
    startBackgroundThread();
    if (textureView.isAvailable()) {
//      openCamera();
    } else {
      textureView.setSurfaceTextureListener(textureListener);
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (locationPlugin != null) {
      locationPlugin.onStart();
    }
    sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    sensorControl = new SensorControl(sensorManager);

    registerSensorListeners();
    mapView.onStart();
  }


  @Override
  protected void onStop() {
    super.onStop();
    if (locationEngine != null) {
      locationEngine.removeLocationUpdates();
    }
    if (locationPlugin != null) {
      locationPlugin.onStop();
    }
    sensorManager.unregisterListener(this, magnetic);
    mapView.onStop();
  }

  @Override
  public void onPause() {
    super.onPause();
    mapView.onPause();

    //closeCamera();
    stopBackgroundThread();
    super.onPause();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (locationEngine != null) {
      locationEngine.deactivate();
    }
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }


  @Override
  public void onExplanationNeeded(List<String> permissionsToExplain) {

  }

  @Override
  public void onPermissionResult(boolean granted) {
    if (granted) {
      enableLocationPlugin();
    } else {
      Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
      finish();
    }
  }

  @Override
  @SuppressWarnings( {"MissingPermission"})
  public void onConnected() {
    locationEngine.requestLocationUpdates();
  }

  @Override
  public void onLocationChanged(Location location) {
    if (location != null) {
      setCameraPosition(location);
      locationEngine.removeLocationEngineListener(this);
    }
  }

  @SuppressWarnings( {"MissingPermission"})
  private void enableLocationPlugin() {
    // Check if permissions are enabled and if not request
    if (PermissionsManager.areLocationPermissionsGranted(this)) {
      // Create an instance of LOST location engine
      initializeLocationEngine();

      locationPlugin = new LocationLayerPlugin(mapView, mapboxMap, locationEngine);
      locationPlugin.setLocationLayerEnabled(LocationLayerMode.COMPASS);
    } else {
      permissionsManager = new PermissionsManager(this);
      permissionsManager.requestLocationPermissions(this);
    }
  }

  @SuppressWarnings( {"MissingPermission"})
  private void initializeLocationEngine() {
    locationEngine = new LostLocationEngine(RadarCompassActivity.this);
    locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
    locationEngine.activate();

    Location lastLocation = locationEngine.getLastLocation();
    if (lastLocation != null) {
      setCameraPosition(lastLocation);
    } else {
      locationEngine.addLocationEngineListener(this);
    }
  }


  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  private boolean deviceHasCamera(Context context) {
    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
      // this device has a camera
      return true;
    } else {
      // no camera on this device
      return false;
    }
  }


  private void setCameraPosition(Location location) {
    mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
      new LatLng(location.getLatitude(), location.getLongitude()), 16));
  }


  private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
    @Override
    public void onOpened(CameraDevice camera) {
      //This is called when the camera is open
      Log.e(TAG, "onOpened");
      cameraDevice = camera;
      createCameraPreview();
    }

    @Override
    public void onDisconnected(CameraDevice camera) {
      cameraDevice.close();
    }

    @Override
    public void onError(CameraDevice camera, int error) {
      cameraDevice.close();
      cameraDevice = null;
    }
  };

  protected void createCameraPreview() {
    try {
      SurfaceTexture texture = textureView.getSurfaceTexture();
      texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
      Surface surface = new Surface(texture);
      captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
      captureRequestBuilder.addTarget(surface);
      cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
          //The camera is already closed
          if (null == cameraDevice) {
            return;
          }
          // When the session is ready, we start displaying the preview.
          cameraCaptureSessions = cameraCaptureSession;
          updatePreview();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
          Toast.makeText(RadarCompassActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
        }
      }, null);
    } catch (CameraAccessException exception) {
      exception.printStackTrace();
    }
  }

  protected void startBackgroundThread() {
    backgroundThread = new HandlerThread("Camera Background");
    backgroundThread.start();
    backgroundHandler = new Handler(backgroundThread.getLooper());
  }

  protected void stopBackgroundThread() {
    backgroundThread.quitSafely();
    try {
      backgroundThread.join();
      backgroundThread = null;
      backgroundHandler = null;
    } catch (InterruptedException exception) {
      exception.printStackTrace();
    }
  }

 /* private void openCamera() {
    CameraManager manager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
    Log.e(TAG, "is camera open");
    try {
      cameraId = manager.getCameraIdList()[0];
      CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
      StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
      assert map != null;
      imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
      // Add permission for camera and let user grant the permission
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.class) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
        return;
      }
      manager.openCamera(cameraId, stateCallback, null);
    } catch (CameraAccessException exception) {
      exception.printStackTrace();
    }
    Log.e(TAG, "openCamera X");
  }*/

  TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
      //open your camera here
//      openCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
      // Transform you image captured size according to the surface width and height
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
      return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }
  };

  protected void updatePreview() {
    if (null == cameraDevice) {
      Log.e(TAG, "updatePreview error, return");
    }
    captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    try {
      cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
    } catch (CameraAccessException exception) {
      exception.printStackTrace();
    }
  }

  private void closeCamera() {
    if (null != cameraDevice) {
      cameraDevice.close();
      cameraDevice = null;
    }
    if (null != imageReader) {
      imageReader.close();
      imageReader = null;
    }
  }

}