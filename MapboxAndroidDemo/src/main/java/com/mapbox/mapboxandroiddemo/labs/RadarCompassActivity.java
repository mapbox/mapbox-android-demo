package com.mapbox.mapboxandroiddemo.labs;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.CompassListener;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.services.android.location.LostLocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.location.LocationEnginePriority;
import com.mapbox.services.android.telemetry.permissions.PermissionsListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Place a rotating mini-window compass map on top of a camera preview.
 */
public class RadarCompassActivity extends AppCompatActivity implements OnMapReadyCallback,
  LocationEngineListener, PermissionsListener, SurfaceHolder.Callback, Handler.Callback {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private LocationLayerPlugin locationLayerPlugin;
  private PermissionsManager permissionsManager;
  private LocationEngine locationEngine;
  static final String TAG = "RadarCompassActivity";
  static final int MY_PERMISSIONS_REQUEST_CAMERA = 1242;
  private static final int MSG_CAMERA_OPENED = 1;
  private static final int MSG_SURFACE_READY = 2;
  private final Handler handler = new Handler(this);
  private SurfaceView surfaceView;
  private SurfaceHolder surfaceHolder;
  private CameraManager cameraManager;
  private String[] mCameraIDsList;
  private CameraDevice.StateCallback stateCallback;
  private CameraDevice cameraDevice;
  private CameraCaptureSession cameraCaptureSession;
  boolean surfaceCreated = true;
  boolean isCameraConfigured = false;
  private Surface cameraSurface = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    setContentView(R.layout.activity_radar_compass);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);

    this.surfaceView = findViewById(R.id.camera_preview_surface_view);
    this.surfaceHolder = this.surfaceView.getHolder();
    this.surfaceHolder.addCallback(this);
    this.cameraManager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);

  }

  @Override
  public void onMapReady(final MapboxMap mapboxMap) {

    RadarCompassActivity.this.mapboxMap = mapboxMap;
    enableLocationPlugin();

  }


  @SuppressWarnings( {"MissingPermission"})
  private void enableLocationPlugin() {
    // Check if permissions are enabled and if not request
    if (PermissionsManager.areLocationPermissionsGranted(this)) {
      // Create an instance of LOST location engine
      initializeLocationEngine();

      locationLayerPlugin = new LocationLayerPlugin(mapView, mapboxMap, locationEngine);
      locationLayerPlugin.setLocationLayerEnabled(LocationLayerMode.COMPASS);
      locationLayerPlugin.addCompassListener(new CompassListener() {
        @Override
        public void onCompassChanged(float userHeading) {
          CameraPosition cameraPosition = new CameraPosition.Builder().bearing(userHeading).build();
          mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

        @Override
        public void onCompassAccuracyChange(int compassStatus) {
          System.out.println(compassStatus);
        }
      });

    } else {
      permissionsManager = new PermissionsManager(this);
      permissionsManager.requestLocationPermissions(this);
    }

    setupCamera();
  }


  @TargetApi(21)
  private void setupCamera() {
    try {
      mCameraIDsList = this.cameraManager.getCameraIdList();
      for (String id : mCameraIDsList) {
        Log.v(TAG, "CameraID: " + id);
      }
    } catch (CameraAccessException exception) {
      exception.printStackTrace();
    }

    stateCallback = new CameraDevice.StateCallback() {
      @Override
      public void onOpened(CameraDevice camera) {
        Toast.makeText(getApplicationContext(), "onOpened", Toast.LENGTH_SHORT).show();

        cameraDevice = camera;
        handler.sendEmptyMessage(MSG_CAMERA_OPENED);
      }

      @Override
      public void onDisconnected(CameraDevice camera) {
        Toast.makeText(getApplicationContext(), "onDisconnected", Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onError(CameraDevice camera, int error) {
        Toast.makeText(getApplicationContext(), "onError", Toast.LENGTH_SHORT).show();
      }
    };
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

  private void setCameraPosition(Location location) {
    mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
      new LatLng(location.getLatitude(), location.getLongitude()), 15.5));
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

  @Override
  @SuppressWarnings( {"MissingPermission"})
  protected void onStart() {
    super.onStart();
    if (locationLayerPlugin != null) {
      locationLayerPlugin.onStart();
    }
    mapView.onStart();
    permissionCheck();
  }

  @TargetApi(21)
  private void permissionCheck() {
    //requesting permission
    int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
      if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {

      } else {
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        Toast.makeText(getApplicationContext(), "request permission", Toast.LENGTH_SHORT).show();
      }
    } else {
      Toast.makeText(getApplicationContext(), "PERMISSION_ALREADY_GRANTED", Toast.LENGTH_SHORT).show();
      try {
        cameraManager.openCamera(mCameraIDsList[1], stateCallback, new Handler());
      } catch (CameraAccessException e) {
        e.printStackTrace();
      }
    }
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
    if (locationEngine != null) {
      locationEngine.removeLocationUpdates();
    }
    if (locationLayerPlugin != null) {
      locationLayerPlugin.onStop();
    }
    mapView.onStop();
    closeCamera();
  }

  @TargetApi(21)
  private void closeCamera() {
    try {
      if (cameraCaptureSession != null) {
        cameraCaptureSession.stopRepeating();
        cameraCaptureSession.close();
        cameraCaptureSession = null;
      }

      isCameraConfigured = false;
    } catch (final CameraAccessException e) {
      // Doesn't matter, closing device anyway
      e.printStackTrace();
    } catch (final IllegalStateException e2) {
      // Doesn't matter, closing device anyway
      e2.printStackTrace();
    } finally {
      if (cameraDevice != null) {
        cameraDevice.close();
        cameraDevice = null;
        cameraCaptureSession = null;
      }
    }
  }

  @Override
  public boolean handleMessage(Message msg) {
    switch (msg.what) {
      case MSG_CAMERA_OPENED:
      case MSG_SURFACE_READY:
        // if both surface is created and camera device is opened
        // - ready to set up preview and other things
        if (surfaceCreated && (cameraDevice != null)
          && !isCameraConfigured) {
          configureCamera();
        }
        break;
    }

    return true;
  }

  private void configureCamera() {
    // prepare list of surfaces to be used in capture requests
    List<Surface> sfl = new ArrayList<Surface>();

    sfl.add(cameraSurface); // surface for viewfinder preview

    // configure camera with all the surfaces to be ever used
    try {
      cameraDevice.createCaptureSession(sfl,
        new CaptureSessionListener(), null);
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }

    isCameraConfigured = true;
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    cameraSurface = holder.getSurface();
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    cameraSurface = holder.getSurface();
    surfaceCreated = true;
    handler.sendEmptyMessage(MSG_SURFACE_READY);
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    surfaceCreated = false;
  }

  private class CaptureSessionListener extends
    CameraCaptureSession.StateCallback {
    @Override
    public void onConfigureFailed(final CameraCaptureSession session) {
      Log.d(TAG, "CaptureSessionConfigure failed");
    }

    @Override
    public void onConfigured(final CameraCaptureSession session) {
      Log.d(TAG, "CaptureSessionConfigure onConfigured");
      cameraCaptureSession = session;

      try {
        CaptureRequest.Builder previewRequestBuilder = cameraDevice
          .createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        previewRequestBuilder.addTarget(cameraSurface);
        cameraCaptureSession.setRepeatingRequest(previewRequestBuilder.build(),
          null, null);
      } catch (CameraAccessException e) {
        Log.d(TAG, "setting up preview failed");
        e.printStackTrace();
      }
    }
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
    if (locationEngine != null) {
      locationEngine.deactivate();
    }
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }
}
