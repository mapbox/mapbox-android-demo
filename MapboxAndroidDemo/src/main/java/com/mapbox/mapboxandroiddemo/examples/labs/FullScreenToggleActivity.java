package com.mapbox.mapboxandroiddemo.examples.labs;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class FullScreenToggleActivity extends AppCompatActivity implements MapboxMap.OnMapLongClickListener {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private boolean hasBeenGivenFullScreenToggleInstruction = false;
  private boolean isFullScreen = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_lab_full_screen_toggle);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull MapboxMap mapboxMap) {
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            FullScreenToggleActivity.this.mapboxMap = mapboxMap;
            mapboxMap.addOnMapLongClickListener(FullScreenToggleActivity.this);
            FloatingActionButton fullScreenToggleFab = findViewById(R.id.full_screen_toggle_fab);
            fullScreenToggleFab.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                fullScreenToggleFab.hide();
                adjustToolbars(isFullScreen);
                if (!hasBeenGivenFullScreenToggleInstruction) {
                  Toast.makeText(FullScreenToggleActivity.this,
                      R.string.full_screen_toggle_instruction, Toast.LENGTH_SHORT).show();
                  hasBeenGivenFullScreenToggleInstruction = true;
                }
              }
            });
          }
        });
      }
    });
  }

  @Override
  public boolean onMapLongClick(@NonNull LatLng point) {
    if (hasBeenGivenFullScreenToggleInstruction) {
      adjustToolbars(isFullScreen);
    }
    return true;
  }


  /**
   * Adjusts the visibility of the SupportActionBar and notification bars for the map to be full screen or not.
   * ActionBar isn't checked for because the current minimum SDK level for this app is 16, which is when the
   * SupportActionBar was introduced.
   *
   * @param showToolbars whether or not the toolbars should be displayed in order for the map to be full screen
   *                     or not.
   */
  private void adjustToolbars(boolean showToolbars) {
    if (getSupportActionBar() != null) {
      if (showToolbars) {
        getSupportActionBar().show();
      } else {
        getSupportActionBar().hide();
      }
    }
    getWindow().clearFlags(showToolbars ? WindowManager.LayoutParams.FLAG_FULLSCREEN :
        WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
    getWindow().addFlags(showToolbars ? WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN :
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
    isFullScreen = !showToolbars;
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
    if (mapboxMap != null) {
      mapboxMap.removeOnMapLongClickListener(this);
    }
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}