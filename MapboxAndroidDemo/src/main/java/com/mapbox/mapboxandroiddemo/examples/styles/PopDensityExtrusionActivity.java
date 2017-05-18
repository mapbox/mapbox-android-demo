package com.mapbox.mapboxandroiddemo.examples.styles;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.functions.Function;
import com.mapbox.mapboxsdk.style.functions.stops.IdentityStops;
import com.mapbox.mapboxsdk.style.layers.FillExtrusionLayer;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

import static com.mapbox.mapboxsdk.style.layers.Filter.eq;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionBase;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionHeight;

public class PopDensityExtrusionActivity extends AppCompatActivity {

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

        VectorSource vectorSource = new VectorSource("population-data", "mapbox://peterqliu.d0vin3el");
        mapboxMap.addSource(vectorSource);

        GeoJsonSource highlightSource = new GeoJsonSource("highlight-source");
        GeoJsonSource radiusHighlightSource = new GeoJsonSource("radiusHighlight-source");
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

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_population_density_spinner_menu_cities, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {
      case R.id.san_francisco:
        goToNewLocation(37.784282779035216, -122.4232292175293);
        Log.d("PopDensityExtrusion", "onOptionsItemSelected: san_francisco");
        return true;
      case R.id.los_angeles:
        goToNewLocation(34.04412546508576, -118.28636169433594);
        Log.d("PopDensityExtrusion", "onOptionsItemSelected: los_angeles");
        return true;
      case R.id.seattle:
        goToNewLocation(47.60651025683697, -122.33327865600585);
        Log.d("PopDensityExtrusion", "onOptionsItemSelected: seattle");
        return true;
      case R.id.new_orleans:
        goToNewLocation(29.946159058399612, -90.10042190551758);
        Log.d("PopDensityExtrusion", "onOptionsItemSelected: new_orleans");
        return true;
      case R.id.chicago:
        goToNewLocation(41.87531293759582, -87.6240348815918);
        Log.d("PopDensityExtrusion", "onOptionsItemSelected: chicago");
        return true;
      case R.id.philadelphia:
        goToNewLocation(39.95370120254379, -75.1626205444336);
        Log.d("PopDensityExtrusion", "onOptionsItemSelected: philadelphia");
        return true;
      case R.id.new_york:
        goToNewLocation(40.72228267283148, -73.99772644042969);
        Log.d("PopDensityExtrusion", "onOptionsItemSelected: new york");
        return true;
      case R.id.atlanta:
        goToNewLocation(33.74910736130734, -84.39079284667969);
        Log.d("PopDensityExtrusion", "onOptionsItemSelected: atlanta");
        return true;
      case R.id.portland:
        goToNewLocation(45.522104713562825, -122.67179489135742);
        Log.d("PopDensityExtrusion", "onOptionsItemSelected: portland");
        return true;
      case R.id.denver:
        goToNewLocation(39.74428621972816, -104.99565124511719);
        Log.d("PopDensityExtrusion", "onOptionsItemSelected: denver");
        return true;
      case R.id.minneapolis:
        goToNewLocation(44.969656023708175, -93.26637268066406);
        Log.d("PopDensityExtrusion", "onOptionsItemSelected: minneapolis");
        return true;
      case R.id.miami:
        goToNewLocation(25.773846629676616, -80.19624710083008);
        Log.d("PopDensityExtrusion", "onOptionsItemSelected: miami");
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void goToNewLocation(double lat, double longitude) {
    LatLng newPosition = new LatLng();
    newPosition.setLatitude(lat);
    newPosition.setLongitude(longitude);
    CameraPosition position = new CameraPosition.Builder()
      .target(newPosition)
      .build();
    mapboxMap.moveCamera(CameraUpdateFactory
      .newCameraPosition(position));
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
          toggleRoads();
        }
      }
    });

    FloatingActionButton labelsToggleButton = (FloatingActionButton) findViewById(R.id.fab_label_toggle);
    labelsToggleButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        if (mapboxMap != null) {
          toggleLabels();
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

  private void toggleRoads() {
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

  private void toggleLabels() {
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
