package com.mapbox.mapboxandroiddemo.examples.plugins;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.building.BuildingPlugin;
import com.mapbox.mapboxsdk.style.light.Light;
import com.mapbox.mapboxsdk.style.light.Position;

/**
 *  Use the buildings plugin to display buildings' heights (extrusions) in 3D.
 */
public class BuildingPluginActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private BuildingPlugin buildingPlugin;
  private SeekBar seekbarLightAzimuthalAngle;
  private SeekBar seekbarLightRadialCoordinate;
  private SeekBar seekbarLightPolarAngle;
  private SeekBar seekbarBuildingOpacity;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    setContentView(R.layout.activity_building_plugin);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull final MapboxMap map) {
        BuildingPluginActivity.this.mapboxMap = map;
        map.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {

            seekbarLightRadialCoordinate = findViewById(R.id.seekbarLightRadialCoordinate);
            seekbarLightAzimuthalAngle = findViewById(R.id.seekbarLightAzimuthalAngle);
            seekbarLightPolarAngle = findViewById(R.id.seekbarLightPolarAngle);
            seekbarBuildingOpacity = findViewById(R.id.seekbarBuildingOpacity);

            buildingPlugin = new BuildingPlugin(mapView, map, map.getStyle());
            buildingPlugin.setVisibility(true);
            initLightSeekbar();
          }
        });
      }
    });
  }

  // See https://en.wikipedia.org/wiki/Spherical_coordinate_system for more information on these values
  private void initLightSeekbar() {
    seekbarLightRadialCoordinate.setMax(24);
    seekbarLightAzimuthalAngle.setMax(180);
    seekbarLightPolarAngle.setMax(180); // polar angle ranges from 0 to 180 degrees

    PositionChangeListener positionChangeListener = new PositionChangeListener();
    seekbarLightRadialCoordinate.setOnSeekBarChangeListener(positionChangeListener);
    seekbarLightAzimuthalAngle.setOnSeekBarChangeListener(positionChangeListener);
    seekbarLightPolarAngle.setOnSeekBarChangeListener(positionChangeListener);
    seekbarBuildingOpacity.setOnSeekBarChangeListener(positionChangeListener);
  }

  private class PositionChangeListener implements android.widget.SeekBar.OnSeekBarChangeListener {
    @Override
    public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
      invalidateLightPosition();
    }

    @Override
    public void onStartTrackingTouch(android.widget.SeekBar seekBar) {
      // Only listening to positionChange for onProgress.
    }

    @Override
    public void onStopTrackingTouch(android.widget.SeekBar seekBar) {
      // Only listening to positionChange for onProgress.
    }
  }

  private void invalidateLightPosition() {
    Light light = mapboxMap.getStyle().getLight();
    float radialCoordinate = seekbarLightRadialCoordinate.getProgress() / 20;
    float azimuthalAngle = seekbarLightAzimuthalAngle.getProgress();
    float polarAngle = seekbarLightPolarAngle.getProgress();
    float buildingOpacity = seekbarBuildingOpacity.getProgress();
    light.setPosition(new Position(radialCoordinate, azimuthalAngle, polarAngle));
    buildingPlugin.setOpacity(buildingOpacity);
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
}
