package com.mapbox.mapboxandroiddemo.examples.dds;

// #-code-snippet: temperature-change-activity full-java

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.expressions.Expression.all;
import static com.mapbox.mapboxsdk.style.expressions.Expression.concat;
import static com.mapbox.mapboxsdk.style.expressions.Expression.division;
import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.product;
import static com.mapbox.mapboxsdk.style.expressions.Expression.round;
import static com.mapbox.mapboxsdk.style.expressions.Expression.subtract;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;

public class ExpressionIntegrationActivity
  extends AppCompatActivity implements OnMapReadyCallback {

  private static final String GEOJSON_SRC_ID = "extremes_source_id";
  private static final String MIN_TEMP_LAYER_ID = "min_temp_layer_id";
  private static final String MAX_TEMP_LAYER_ID = "max_temp_layer_id";
  private static final String RED_PIN_IMAGE_ID = "red_pin_id";
  private static final String BLUE_PIN_IMAGE_ID = "blue_pin_id";

  private static final String DEGREES_C = "℃"; //"\u2103";
  private static final String DEGREES_F = "℉"; //"\u2109";

  private MapboxMap mapboxMap;
  private MapView mapView;
  private Menu menu;
  private FloatingActionButton unitsFab;
  private TextView unitsText;
  private boolean isImperial = true;

  /**
   * weather_data_per_state_before2006.geojson file (found in assets)
   * contains various weather related records per state/territory.
   * Once that file is parsed we will keep a list of states (with name and bounds info).
   * We need bounds of for data shown on map per state.
   * We first find out all latLng points per state to be shown, than we create a LatLngBounds
   * using those points.
   */
  private class State {
    List<LatLng> latLongs;
    LatLngBounds bounds;
    String name;

    State(String name, LatLng latLng) {
      this.name = name;
      latLongs = new ArrayList<>();
      latLongs.add(latLng);
      bounds = LatLngBounds.from(latLng.getLatitude(), latLng.getLongitude(),
        latLng.getLatitude(), latLng.getLongitude());
    }

    void add(LatLng latLng) {
      latLongs.add(latLng);
      bounds = new LatLngBounds.Builder().includes(latLongs).build();
    }
  }

  private List<State> states;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_dds_temperature_change);

    unitsFab = findViewById(R.id.change_units_fab);

    mapView = findViewById(R.id.mapView);
    unitsText = findViewById(R.id.units);

    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {

    this.mapboxMap = mapboxMap;

    setUpMapImagePins();

    // Initialize FeatureCollection object for future use with layers
    FeatureCollection featureCollection =
      FeatureCollection.fromJson(loadGeoJsonFromAsset("weather_data_per_state_before2006.geojson"));


    // Find out the states represented in the FeatureCollection
    // and bounds of the extreme conditions
    states = new ArrayList<>();
    for (Feature feature : featureCollection.features()) {
      String stateName = feature.getStringProperty("state");
      String lat = feature.getStringProperty("latitude");
      String lon = feature.getStringProperty("longitude");

      LatLng latLng = new LatLng(
        Double.parseDouble(lat),
        Double.parseDouble(lon));

      State state = null;
      for (State curState : states) {
        if (curState.name.equals(stateName)) {
          state = curState;
          break;
        }
      }
      if (state == null) {
        state = new State(stateName, latLng);
        states.add(state);
      } else {
        state.add(latLng);
      }
    }

    // Retrieves GeoJSON from local file and adds it to the map
    GeoJsonSource geoJsonSource =
      new GeoJsonSource(GEOJSON_SRC_ID, featureCollection);
    mapboxMap.addSource(geoJsonSource);

    initTemperatureLayers();
    populateMenu();

    // show Connecticut by default
    int indexOfState = indexOfState("Connecticut");
    selectState(states.get(indexOfState).name, indexOfState);

    // When user clicks the map, start the snapshotting process with the given parameters
    unitsFab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        if (mapboxMap != null) {
          changeTemperatureUnits(!isImperial);
        }
      }
    });
  }

  /**
   * Adds the marker image to the map for use as a SymbolLayer icon
   */
  private void setUpMapImagePins() {
    Bitmap icon = BitmapFactory.decodeResource(
      this.getResources(), R.drawable.red_marker);
    mapboxMap.addImage(RED_PIN_IMAGE_ID, icon);

    icon = BitmapFactory.decodeResource(
      this.getResources(), R.drawable.blue_marker);
    mapboxMap.addImage(BLUE_PIN_IMAGE_ID, icon);
  }

  private void initTemperatureLayers() {
    if (mapboxMap != null) {

      // Adds a SymbolLayer to display maximum temperature in state
      SymbolLayer maxTempLayer = new SymbolLayer(MAX_TEMP_LAYER_ID, GEOJSON_SRC_ID);
      maxTempLayer.withProperties(
        iconImage(RED_PIN_IMAGE_ID),
        textField(getTemperatureValue()),
        textSize(17f),
        textOffset(new Float[]{0f, -1.75f}),
        textColor(Color.RED),
        textAllowOverlap(true),
        textIgnorePlacement(true),
        iconAllowOverlap(true),
        iconIgnorePlacement(true)
      );
      // Only display Maximum Temperature in this layer
      maxTempLayer.setFilter(eq(get("element"), literal("All-Time Maximum Temperature")));
      mapboxMap.addLayer(maxTempLayer);

      // Adds a SymbolLayer to display minimum temperature in state
      SymbolLayer minTempLayer = new SymbolLayer(MIN_TEMP_LAYER_ID, GEOJSON_SRC_ID);
      minTempLayer.withProperties(
        iconImage(BLUE_PIN_IMAGE_ID),
        textField(getTemperatureValue()),
        textSize(17f),
        textOffset(new Float[]{0f, -2.5f}),
        textColor(Color.BLUE),
        textAllowOverlap(true),
        textIgnorePlacement(true),
        iconAllowOverlap(true),
        iconIgnorePlacement(true));
      // Only display Minimum Temperature in this layer
      minTempLayer.setFilter(eq(get("element"), literal("All-Time Minimum Temperature")));
      mapboxMap.addLayer(minTempLayer);

      unitsText.setText(isImperial ? DEGREES_C : DEGREES_F);
    }
  }

  private void changeTemperatureUnits(boolean isImperial) {
    if (mapboxMap != null && this.isImperial != isImperial) {
      this.isImperial = isImperial;

      // Apply new units to the data displayed in textfields of SymbolLayers
      SymbolLayer maxTempLayer = (SymbolLayer)mapboxMap.getLayer(MAX_TEMP_LAYER_ID);
      maxTempLayer.withProperties(textField(getTemperatureValue()));

      SymbolLayer minTempLayer = (SymbolLayer)mapboxMap.getLayer(MIN_TEMP_LAYER_ID);
      minTempLayer.withProperties(textField(getTemperatureValue()));

      unitsText.setText(isImperial ? DEGREES_C : DEGREES_F);
    }
  }

  private Expression getTemperatureValue() {

    if (isImperial) {
      return concat(get("value"), literal(DEGREES_F)); // For imperial we just need to add "F"
    }

    Expression value = Expression.toNumber(get("value"));  // value --> Number
    value = subtract(value, Expression.toNumber(literal(32.0))); // value - 32
    value = product(value, Expression.toNumber(literal(5.0))); // value * 5
    value = division(value, Expression.toNumber(literal(9.0))); // value / 9
    value = round(value); // round to nearest int
    return concat(Expression.toString(value), literal(DEGREES_C)); // add C at the end
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
    mapView.onPause();
    super.onPause();
  }

  @Override
  protected void onStop() {
    mapView.onStop();
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    unitsFab.setOnClickListener(null);

    menu.clear();

    states.clear();
    states = null;

    if (mapboxMap != null) {
      mapboxMap.removeImage(RED_PIN_IMAGE_ID);
      mapboxMap.removeImage(BLUE_PIN_IMAGE_ID);
      mapboxMap.removeLayer(MAX_TEMP_LAYER_ID);
      mapboxMap.removeLayer(MIN_TEMP_LAYER_ID);
      mapboxMap.removeSource(GEOJSON_SRC_ID);
    }
    mapView.onDestroy();

    mapView = null;
    mapboxMap = null;
    unitsFab = null;
    menu = null;

    super.onDestroy();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  private String loadGeoJsonFromAsset(String filename) {
    try {
      // Load GeoJSON file
      InputStream is = getAssets().open(filename);
      int size = is.available();
      byte[] buffer = new byte[size];
      is.read(buffer);
      is.close();
      return new String(buffer, "UTF-8");

    } catch (Exception exception) {
      Log.e("StyleLineActivity", "Exception Loading GeoJSON: " + exception.toString());
      exception.printStackTrace();
      return null;
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    this.menu = menu;
    populateMenu();

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    selectState(item.getTitle(), item.getItemId());

    return super.onOptionsItemSelected(item);
  }

  private void populateMenu() {
    if (menu != null && states != null) {
      for (int id = 0; id < states.size(); id++) {
        menu.add(Menu.NONE, id, Menu.NONE, states.get(id).name);
      }
    }
  }

  private void selectState(CharSequence stateName, int stateIndex) {

    if (indexOfState(stateName) == stateIndex) {
      // Adds a SymbolLayer to display maximum temperature in state
      SymbolLayer maxTempLayer = (SymbolLayer) mapboxMap.getLayer(MAX_TEMP_LAYER_ID);
      // Only display Maximum Temperature in this layer for SELECTED State
      maxTempLayer.setFilter(all(
        eq(get("element"), literal("All-Time Maximum Temperature")),
        eq(get("state"), literal(stateName))));


      // Adds a SymbolLayer to display minimum temperature in state
      SymbolLayer minTempLayer = (SymbolLayer) mapboxMap.getLayer(MIN_TEMP_LAYER_ID);
      // Only display Maximum Temperature in this layer for SELECTED State
      minTempLayer.setFilter(all(
        eq(get("element"), literal("All-Time Minimum Temperature")),
        eq(get("state"), literal(stateName))));

      CameraUpdate cameraUpdate =
        CameraUpdateFactory.newLatLngBounds(states.get(stateIndex).bounds, 100);
      mapboxMap.animateCamera(cameraUpdate);

      Toast.makeText(this,
        String.format(getString(R.string.temp_change_feedback), stateName),
        Toast.LENGTH_LONG)
        .show();
    }
  }

  private int indexOfState(CharSequence name) {
    if (states != null && name != null) {
      for (int i = 0; i < states.size(); i++) {
        if (name.equals(states.get(i).name)) {
          return i;
        }
      }
    }
    return -1;
  }
}
// #-end-code-snippet: temperature-change-activity full-java