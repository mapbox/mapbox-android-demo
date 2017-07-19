package com.mapbox.mapboxandroiddemo.labs;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.functions.Function;
import com.mapbox.mapboxsdk.style.functions.stops.Stop;
import com.mapbox.mapboxsdk.style.functions.stops.Stops;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.api.utils.turf.TurfException;
import com.mapbox.services.api.utils.turf.TurfJoins;
import com.mapbox.services.commons.models.Position;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

/**
 * Display an indoor map of a building with toggles to switch between floor levels
 */
public class IndoorMapActivity extends AppCompatActivity {

  private GeoJsonSource indoorBuildingSource;
  private List<Position> boundingBox;
  private View levelButtons;
  private MapView mapView;
  private MapboxMap map;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_lab_indoor_map);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(final MapboxMap mapboxMap) {
        map = mapboxMap;

        levelButtons = findViewById(R.id.floor_level_buttons);

        boundingBox = new ArrayList<>();
        boundingBox.add(Position.fromCoordinates(-77.03791, 38.89715));
        boundingBox.add(Position.fromCoordinates(-77.03791, 38.89811));
        boundingBox.add(Position.fromCoordinates(-77.03532, 38.89811));
        boundingBox.add(Position.fromCoordinates(-77.03532, 38.89708));

        mapboxMap.setOnCameraChangeListener(new MapboxMap.OnCameraChangeListener() {
          @Override
          public void onCameraChange(CameraPosition position) {

            if (position.zoom > 16) {
              try {
                if (TurfJoins.inside(Position.fromCoordinates(
                  position.target.getLongitude(),
                  position.target.getLatitude()),
                  boundingBox)) {
                  if (levelButtons.getVisibility() != View.VISIBLE) {
                    showLevelButton();
                  }
                } else {
                  if (levelButtons.getVisibility() == View.VISIBLE) {
                    hideLevelButton();
                  }
                }
              } catch (TurfException turfException) {
                turfException.printStackTrace();
              }
            } else if (levelButtons.getVisibility() == View.VISIBLE) {
              hideLevelButton();
            }
          }
        });

        indoorBuildingSource = new GeoJsonSource("indoor-building", loadJsonFromAsset("white_house_lvl_0.geojson"));
        mapboxMap.addSource(indoorBuildingSource);

        // Add the building layers since we know zoom levels in range
        loadBuildingLayer();
      }
    });

    Button buttonSecondLevel = (Button) findViewById(R.id.second_level_button);
    buttonSecondLevel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        indoorBuildingSource.setGeoJson(loadJsonFromAsset("white_house_lvl_1.geojson"));
      }
    });

    Button buttonGroundLevel = (Button) findViewById(R.id.ground_level_button);
    buttonGroundLevel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        indoorBuildingSource.setGeoJson(loadJsonFromAsset("white_house_lvl_0.geojson"));
      }
    });
  }

  @Override
  protected void onResume() {
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
  protected void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  private void hideLevelButton() {
    // When the user moves away from our bounding box region or zooms out far enough the floor level
    // buttons are faded out and hidden.
    AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
    animation.setDuration(500);
    levelButtons.startAnimation(animation);
    levelButtons.setVisibility(View.GONE);
  }

  private void showLevelButton() {
    // When the user moves inside our bounding box region or zooms in to a high enough zoom level,
    // the floor level buttons are faded out and hidden.
    AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
    animation.setDuration(500);
    levelButtons.startAnimation(animation);
    levelButtons.setVisibility(View.VISIBLE);
  }

  private void loadBuildingLayer() {
    // Method used to load the indoor layer on the map. First the fill layer is drawn and then the
    // line layer is added.

    FillLayer indoorBuildingLayer = new FillLayer("indoor-building-fill", "indoor-building").withProperties(
      fillColor(Color.parseColor("#eeeeee")),
      // Function.zoom is used here to fade out the indoor layer if zoom level is beyond 16. Only
      // necessary to show the indoor map at high zoom levels.
      fillOpacity(Function.zoom(Stops.exponential(
        Stop.stop(17f, fillOpacity(1f)),
        Stop.stop(16.5f, fillOpacity(0.5f)),
        Stop.stop(16f, fillOpacity(0f))
      )))

    );

    map.addLayer(indoorBuildingLayer);

    LineLayer indoorBuildingLineLayer = new LineLayer("indoor-building-line", "indoor-building").withProperties(
      lineColor(Color.parseColor("#50667f")),
      lineWidth(0.5f),
      lineOpacity(Function.zoom(Stops.exponential(
        Stop.stop(17f, lineOpacity(1f)),
        Stop.stop(16.5f, lineOpacity(0.5f)),
        Stop.stop(16f, lineOpacity(0f))
      )))

    );
    map.addLayer(indoorBuildingLineLayer);
  }

  private String loadJsonFromAsset(String filename) {
    // Using this method to load in GeoJSON files from the assets folder.

    try {
      InputStream is = getAssets().open(filename);
      int size = is.available();
      byte[] buffer = new byte[size];
      is.read(buffer);
      is.close();
      return new String(buffer, "UTF-8");

    } catch (IOException ex) {
      ex.printStackTrace();
      return null;
    }
  }
}
