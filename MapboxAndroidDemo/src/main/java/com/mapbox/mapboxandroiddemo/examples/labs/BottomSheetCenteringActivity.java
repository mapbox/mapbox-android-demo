package com.mapbox.mapboxandroiddemo.examples.labs;

import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;


/**
 * The most basic example of adding a map to an activity.
 */
public class BottomSheetCenteringActivity extends AppCompatActivity {

  private MapView mapView;
  private String TAG = "BottomSheetCenteringActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_bottom_sheet_centering);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull MapboxMap mapboxMap) {
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {

            // Map is set up and the style has loaded. Now you can add data or make other map adjustments.

            // get the bottom sheet view
            LinearLayout llBottomSheet = findViewById(R.id.bottom_sheet);

// init the bottom sheet behavior
            BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);

            // set callback for changes
            bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
              @Override
              public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                  case BottomSheetBehavior.STATE_HIDDEN:

                    Log.d(TAG, "onStateChanged: STATE_HIDDEN");
                    break;
                  case BottomSheetBehavior.STATE_EXPANDED:
                    Log.d(TAG, "onStateChanged: STATE_EXPANDED");

                    /*CameraPosition position = new CameraPosition.Builder()
                      .target(new LatLng(mapboxMap.getProjection().getVisibleRegion()))
                      .build();
                    mapboxMap.moveCamera(CameraUpdateFactory
                      .newCameraPosition(position));*/

/*
                    Log.d(TAG, "onStateChanged: STATE_EXPANDED mapboxMap.getProjection().getVisibleRegion() = " + mapboxMap.getProjection().getVisibleRegion());
                    Log.d(TAG, "onStateChanged: STATE_EXPANDED mapboxMap.getProjection().getVisibleRegion(false) = " + mapboxMap.getProjection().getVisibleRegion(false));
                    Log.d(TAG, "onStateChanged: STATE_EXPANDED mapboxMap.getUiSettings().getFocalPoint() = " + mapboxMap.getUiSettings().getFocalPoint());
                    Log.d(TAG, "onStateChanged: STATE_EXPANDED mapboxMap.getUiSettings().getWidth() = " + mapboxMap.getUiSettings().getWidth());
                    Log.d(TAG, "onStateChanged: STATE_EXPANDED mapboxMap.getUiSettings().getHeight() = " + mapboxMap.getUiSettings().getHeight());
*/

                    Log.d(TAG, "onStateChanged: STATE_EXPANDED mapboxMap.getCameraPosition().target = " + mapboxMap.getCameraPosition().target);
/*

                    PointF pointF = mapboxMap.getUiSettings().getFocalPoint();
                    if (pointF!=null) {
                      mapboxMap.getUiSettings().setFocalPoint(new PointF(pointF.x, pointF.y - 20));
                    }


*/

                    break;
                  case BottomSheetBehavior.STATE_COLLAPSED:
                    Log.d(TAG, "onStateChanged: STATE_COLLAPSED");

                    Log.d(TAG, "onStateChanged: STATE_COLLAPSED mapboxMap.getCameraPosition().target = " + mapboxMap.getCameraPosition().target);

                    break;
                  case BottomSheetBehavior.STATE_DRAGGING:
                    Log.d(TAG, "onStateChanged: STATE_DRAGGING");

                    break;
                  case BottomSheetBehavior.STATE_SETTLING:


                    break;
                }


              }

              @Override
              public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                Log.d(TAG, "onStateChanged: slideOffset = " + slideOffset);


               /* PointF pointF = mapboxMap.getUiSettings().getFocalPoint();

                PointF offsetPointF = new PointF(pointF.x, pointF.y - slideOffset * 40);

                LatLng offsetLatLng = mapboxMap.getProjection().fromScreenLocation(pointF);



                if (pointF!=null) {
                  mapboxMap.getUiSettings().setFocalPoint(offsetPointF);
                }


                CameraPosition position = new CameraPosition.Builder()
                  .target(new LatLng(offsetLatLng.getLatitude(), offsetLatLng.getLongitude()))
                  .build(); // Creates a CameraPosition from the builder

                mapboxMap.moveCamera(CameraUpdateFactory
                  .newCameraPosition(position));
*/

              }
            });


          }
        });
      }
    });
  }

  // Add the mapView lifecycle to the activity's lifecycle methods
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
}
