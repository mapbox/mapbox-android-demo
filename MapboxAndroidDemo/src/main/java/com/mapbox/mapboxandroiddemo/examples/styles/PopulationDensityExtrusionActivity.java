package com.mapbox.mapboxandroiddemo.examples.styles;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.LineLayer;

public class PopulationDensityExtrusionActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private boolean mapIsTilted = false;
  private FloatingActionButton tiltMapToggleButton;
  private FloatingActionButton roadsToggleButton;
  private FloatingActionButton labelsToggleButton;
  private FloatingActionButton parentFab;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    setContentView(R.layout.activity_population_density_extrusion);

    tiltMapToggleButton = (FloatingActionButton) findViewById(R.id.fab_tilt_toggle);
    roadsToggleButton = (FloatingActionButton) findViewById(R.id.fab_road_toggle);
    labelsToggleButton = (FloatingActionButton) findViewById(R.id.fab_label_toggle);
    parentFab = (FloatingActionButton) findViewById(R.id.multiple_actions_parent_fab);
    
    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull final MapboxMap map) {

        mapboxMap = map;


      }
    });

    tiltMapToggleButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        if (mapboxMap != null) {
          toggleMapTilt();
        }
      }
    });

    roadsToggleButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        Toast.makeText(PopulationDensityExtrusionActivity.this, "Clicked roadsToggleButton", Toast.LENGTH_SHORT).show();


      }
    });

    labelsToggleButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        Toast.makeText(PopulationDensityExtrusionActivity.this, "Clicked labelsToggleButton", Toast.LENGTH_SHORT).show();


      }
    });


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

  private void toggleMapTilt() {

    if (!mapIsTilted) {
      CameraPosition position = new CameraPosition.Builder()
        .tilt(50) // Set the camera tilt
        .build(); // Creates a CameraPosition from the builder

      mapboxMap.animateCamera(CameraUpdateFactory
        .newCameraPosition(position), 1000);
      mapIsTilted = true;

    } else {
      CameraPosition position = new CameraPosition.Builder()
        .tilt(0) // Set the camera tilt
        .build(); // Creates a CameraPosition from the builder

      mapboxMap.animateCamera(CameraUpdateFactory
        .newCameraPosition(position), 1000);

      mapIsTilted = false;

    }
  }
}
