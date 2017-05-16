package com.mapbox.mapboxandroiddemo.examples.styles;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;

public class PopulationDensityExtrusionActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private boolean mapIsTilted = false;
  private boolean roadsDisplayed = true;
  private boolean labelsDisplayed = true;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));
    setContentView(R.layout.activity_population_density_extrusion);
    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull final MapboxMap map) {
        mapboxMap = map;
        setUpFabs();
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

  private void setUpFabs() {

    FloatingActionButton tiltMapToggleButton = (FloatingActionButton) findViewById(R.id.fab_tilt_toggle);
    tiltMapToggleButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        if (mapboxMap != null) {
          toggleMapTilt();
        }
      }
    });

    FloatingActionButton roadsToggleButton = (FloatingActionButton) findViewById(R.id.fab_road_toggle);
    roadsToggleButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (mapboxMap != null) {
          roadsAreVisible();
        }
      }
    });

    FloatingActionButton labelsToggleButton = (FloatingActionButton) findViewById(R.id.fab_label_toggle);
    labelsToggleButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        if (mapboxMap != null) {
          labelsAreVisible();
        }
      }
    });
  }

  private void toggleMapTilt() {
    if (!mapIsTilted) {
      CameraPosition position = new CameraPosition.Builder()
        .tilt(50) // Set the camera tilt
        .build(); // Creates a CameraPosition from the builder

      mapboxMap.animateCamera(CameraUpdateFactory
        .newCameraPosition(position), 500);
      mapIsTilted = true;

    } else {
      CameraPosition position = new CameraPosition.Builder()
        .tilt(0) // Set the camera tilt
        .build(); // Creates a CameraPosition from the builder

      mapboxMap.animateCamera(CameraUpdateFactory
        .newCameraPosition(position), 500);
      mapIsTilted = false;
    }
  }

  private void roadsAreVisible() {
    if (!roadsDisplayed) {
      for (int x = 0; x < mapboxMap.getLayers().size(); x++) {
        if (mapboxMap.getLayers().get(x).getId().contains("road")) {
          mapboxMap.getLayers().get(x).setProperties(PropertyFactory.visibility("visible"));
        }
      }
      roadsDisplayed = true;
    } else {
      for (int x = 0; x < mapboxMap.getLayers().size(); x++) {
        if (mapboxMap.getLayers().get(x).getId().contains("road")) {
          mapboxMap.getLayers().get(x).setProperties(PropertyFactory.visibility("none"));
        }
      }
      roadsDisplayed = false;
    }
  }

  private void labelsAreVisible() {
    if (!labelsDisplayed) {
      for (int x = 0; x < mapboxMap.getLayers().size(); x++) {
        if (mapboxMap.getLayers().get(x).getId().contains("label")
          || mapboxMap.getLayers().get(x).getId().contains("poi_label_3")
          || mapboxMap.getLayers().get(x).getId().contains("place")) {
          mapboxMap.getLayers().get(x).setProperties(PropertyFactory.visibility("visible"));
        }
      }
      labelsDisplayed = true;
    } else {
      for (int x = 0; x < mapboxMap.getLayers().size(); x++) {
        if (mapboxMap.getLayers().get(x).getId().contains("label")
          || mapboxMap.getLayers().get(x).getId().contains("poi_label_3")
          || mapboxMap.getLayers().get(x).getId().contains("place")) {
          mapboxMap.getLayers().get(x).setProperties(PropertyFactory.visibility("none"));
        }
      }
      labelsDisplayed = false;
    }
  }
}
