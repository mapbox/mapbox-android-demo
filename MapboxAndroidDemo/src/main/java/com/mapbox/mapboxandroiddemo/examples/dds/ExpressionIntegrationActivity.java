package com.mapbox.mapboxandroiddemo.examples.dds;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
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
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import timber.log.Timber;

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
  private final List<State> states = new ArrayList<>();
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


  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_dds_multiple_expression_integration);

    unitsFab = findViewById(R.id.change_units_fab);

    mapView = findViewById(R.id.mapView);
    unitsText = findViewById(R.id.units);

    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull final Style style) {
        setUpMapImagePins(style);
        new LoadGeoJson(ExpressionIntegrationActivity.this).execute();
      }
    });
  }

  private void addDataToMap(@NonNull FeatureCollection featureCollection) {
    // Retrieves GeoJSON from local file and adds it to the map
    GeoJsonSource geoJsonSource = new GeoJsonSource(GEOJSON_SRC_ID, featureCollection);
    if (mapboxMap != null) {
      mapboxMap.getStyle(style -> {
        style.addSource(geoJsonSource);
        initTemperatureLayers(style);
        populateMenu();

        // show Connecticut by default
        int indexOfState = indexOfState("Connecticut");
        selectState(states.get(indexOfState).name, indexOfState, style);

        // When user clicks the map, start the snapshotting process with the given parameters
        unitsFab.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            if (mapboxMap != null) {
              changeTemperatureUnits(!isImperial, style);
            }
          }
        });
      });
    }
  }

  private static class LoadGeoJson extends AsyncTask<Void, Void, FeatureCollection> {

    private WeakReference<ExpressionIntegrationActivity> weakReference;

    LoadGeoJson(ExpressionIntegrationActivity activity) {
      this.weakReference = new WeakReference<>(activity);
    }

    @Override
    protected FeatureCollection doInBackground(Void... voids) {
      try {
        ExpressionIntegrationActivity activity = weakReference.get();
        if (activity != null) {

          InputStream inputStream = activity.getAssets().open("weather_data_per_state_before2006.geojson");

          // Initialize FeatureCollection object for future use with layers
          FeatureCollection featureCollection = FeatureCollection.fromJson(convertStreamToString(inputStream));

          // Find out the states represented in the FeatureCollection
          // and bounds of the extreme conditions
          if (featureCollection.features() != null) {
            for (Feature feature : featureCollection.features()) {
              String stateName = feature.getStringProperty("state");
              String lat = feature.getStringProperty("latitude");
              String lon = feature.getStringProperty("longitude");

              LatLng latLng = new LatLng(
                Double.parseDouble(lat),
                Double.parseDouble(lon));

              State state = null;
              for (State curState : activity.states) {
                if (curState.name.equals(stateName)) {
                  state = curState;
                  break;
                }
              }
              if (state == null) {
                activity.states.add(activity.createState(stateName, latLng));
              } else {
                state.add(latLng);
              }
            }
          }
          return featureCollection;
        }
      } catch (Exception exception) {
        Timber.d("Exception Loading GeoJSON: %s", exception.toString());
      }
      return null;
    }

    static String convertStreamToString(InputStream is) {
      Scanner scanner = new Scanner(is).useDelimiter("\\A");
      return scanner.hasNext() ? scanner.next() : "";
    }

    @Override
    protected void onPostExecute(@Nullable FeatureCollection featureCollection) {
      super.onPostExecute(featureCollection);
      ExpressionIntegrationActivity activity = weakReference.get();
      if (activity != null && featureCollection != null) {
        activity.addDataToMap(featureCollection);
      }
    }
  }

  private State createState(String stateName, LatLng latLng) {
    return new State(stateName, latLng);
  }

  /**
   * Adds the marker image to the map for use as a SymbolLayer icon
   */
  private void setUpMapImagePins(@NonNull Style loadedMapStyle) {
    Bitmap icon = BitmapFactory.decodeResource(
      this.getResources(), R.drawable.red_marker);
    loadedMapStyle.addImage(RED_PIN_IMAGE_ID, icon);

    icon = BitmapFactory.decodeResource(
      this.getResources(), R.drawable.blue_marker);
    loadedMapStyle.addImage(BLUE_PIN_IMAGE_ID, icon);
  }

  private void initTemperatureLayers(@NonNull Style loadedMapStyle) {
    if (mapboxMap != null) {

      // Adds a SymbolLayer to display maximum temperature in state
      SymbolLayer maxTempLayer = new SymbolLayer(MAX_TEMP_LAYER_ID, GEOJSON_SRC_ID);
      maxTempLayer.withProperties(
        iconImage(RED_PIN_IMAGE_ID),
        textField(getTemperatureValue()),
        textSize(17f),
        textOffset(new Float[] {0f, -1.75f}),
        textColor(Color.RED),
        textAllowOverlap(true),
        textIgnorePlacement(true),
        iconAllowOverlap(true),
        iconIgnorePlacement(true)
      );
      // Only display Maximum Temperature in this layer
      maxTempLayer.setFilter(eq(get("element"), literal("All-Time Maximum Temperature")));
      loadedMapStyle.addLayer(maxTempLayer);

      // Adds a SymbolLayer to display minimum temperature in state
      SymbolLayer minTempLayer = new SymbolLayer(MIN_TEMP_LAYER_ID, GEOJSON_SRC_ID);
      minTempLayer.withProperties(
        iconImage(BLUE_PIN_IMAGE_ID),
        textField(getTemperatureValue()),
        textSize(17f),
        textOffset(new Float[] {0f, -2.5f}),
        textColor(Color.BLUE),
        textAllowOverlap(true),
        textIgnorePlacement(true),
        iconAllowOverlap(true),
        iconIgnorePlacement(true));
      // Only display Minimum Temperature in this layer
      minTempLayer.setFilter(eq(get("element"), literal("All-Time Minimum Temperature")));
      loadedMapStyle.addLayer(minTempLayer);

      unitsText.setText(isImperial ? DEGREES_C : DEGREES_F);
    }
  }

  private void changeTemperatureUnits(boolean isImperial, @NonNull Style loadedMapStyle) {

    if (mapboxMap != null && this.isImperial != isImperial) {
      this.isImperial = isImperial;

      // Apply new units to the data displayed in text fields of SymbolLayers
      SymbolLayer maxTempLayer = (SymbolLayer) loadedMapStyle.getLayer(MAX_TEMP_LAYER_ID);
      if (maxTempLayer != null) {
        maxTempLayer.withProperties(textField(getTemperatureValue()));
      }

      SymbolLayer minTempLayer = (SymbolLayer) loadedMapStyle.getLayer(MIN_TEMP_LAYER_ID);
      if (minTempLayer != null) {
        minTempLayer.withProperties(textField(getTemperatureValue()));
      }
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

    if (menu != null) {
      menu.clear();
    }

    states.clear();
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

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    this.menu = menu;
    populateMenu();

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (mapboxMap != null) {
      Style style = mapboxMap.getStyle();
      if (style != null) {
        selectState(item.getTitle(), item.getItemId(), style);
      }
    }

    return super.onOptionsItemSelected(item);
  }

  private void populateMenu() {
    if (menu != null) {
      for (int id = 0; id < states.size(); id++) {
        menu.add(Menu.NONE, id, Menu.NONE, states.get(id).name);
      }
    }
  }

  private void selectState(CharSequence stateName, int stateIndex, @NonNull Style loadedMapStyle) {

    if (indexOfState(stateName) == stateIndex) {
      // Adds a SymbolLayer to display maximum temperature in state
      SymbolLayer maxTempLayer = (SymbolLayer) loadedMapStyle.getLayer(MAX_TEMP_LAYER_ID);
      // Only display Maximum Temperature in this layer for SELECTED State
      if (maxTempLayer != null) {
        maxTempLayer.setFilter(all(
          eq(get("element"), literal("All-Time Maximum Temperature")),
          eq(get("state"), literal(stateName))));
      }

      // Adds a SymbolLayer to display minimum temperature in state
      SymbolLayer minTempLayer = (SymbolLayer) loadedMapStyle.getLayer(MIN_TEMP_LAYER_ID);
      // Only display Maximum Temperature in this layer for SELECTED State
      if (minTempLayer != null) {
        minTempLayer.setFilter(all(
          eq(get("element"), literal("All-Time Minimum Temperature")),
          eq(get("state"), literal(stateName))));
      }

      CameraUpdate cameraUpdate =
        CameraUpdateFactory.newLatLngBounds(states.get(stateIndex).bounds, 100);
      mapboxMap.animateCamera(cameraUpdate);

      Toast.makeText(this,
        String.format(getString(R.string.temp_change_feedback), stateName),
        Toast.LENGTH_LONG).show();
    }
  }

  private int indexOfState(CharSequence name) {
    if (name != null) {
      for (int i = 0; i < states.size(); i++) {
        if (name.equals(states.get(i).name)) {
          return i;
        }
      }
    }
    return -1;
  }
}
