package com.mapbox.mapboxandroiddemo.examples.dds;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.match;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgba;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.toNumber;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;

/**
 * Style a choropleth map by merging local JSON data with vector tile geometries
 */
public class ChoroplethJsonVectorMixActivity extends AppCompatActivity implements OnMapReadyCallback {

  private static final String STATE_UNEMPLOYMENT_INFO_JSON = "state_unemployment_info.json";
  private static final String VECTOR_SOURCE_NAME = "states";
  private static final String VECTOR_MATCH_PROP = "STATEFP";
  private static final String DATA_MATCH_PROP = "STATE_ID";
  private static final String DATA_STYLE_UNEMPLOYMENT_PROP = "unemployment";

  private MapView mapView;
  private JSONArray statesArray;
  private StringBuilder sb;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the account manager
    setContentView(R.layout.activity_choropleth_json_vector_mix);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap map) {

    // Add Mapbox-hosted vector source for state polygons
    VectorSource vectorSource = new VectorSource(VECTOR_SOURCE_NAME, "mapbox://mapbox.us_census_states_2015");
    map.addSource(vectorSource);

    loadJson();

    try {
      statesArray = new JSONArray(sb.toString());
    } catch (Exception exception) {
      Log.e("JSONVectorMix", "Exception Loading GeoJSON: ", exception);
    }

    // Create stops array
    Expression.Stop[] stops = new Expression.Stop[statesArray.length()];

    for (int x = 0; x < statesArray.length(); x++) {
      try {
        // Generate green color value for each state/stop
        JSONObject singleState = statesArray.getJSONObject(x);
        double green = singleState.getDouble(DATA_STYLE_UNEMPLOYMENT_PROP) / 14 * 255;

        Log.d("Choropleth", "onMapReady: green value = " + String.valueOf(green) + " for x = " + x);
        // Add new stop to array of stops
        stops[x] = stop(
          Double.parseDouble(singleState.getString(DATA_MATCH_PROP)),
          rgba(0, green, 0, 1)
        );

      } catch (JSONException exception) {
        throw new RuntimeException(exception);
      }
    }

    // Create layer from the vector tile source with data-driven style
    FillLayer statesJoinLayer = new FillLayer("states-join", VECTOR_SOURCE_NAME);
    statesJoinLayer.setSourceLayer(VECTOR_SOURCE_NAME);
    statesJoinLayer.withProperties(
      fillColor(match(toNumber(get(VECTOR_MATCH_PROP)), rgba(0, 0, 0, 1), stops))
    );

    // Add layer to map below the "waterway-label" layer
    map.addLayerAbove(statesJoinLayer, "waterway-label");
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  public void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
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

  private StringBuilder loadJson() {
    sb = new StringBuilder();
    try {
      // Load GeoJSON file
      InputStream inputStream = getAssets().open(STATE_UNEMPLOYMENT_INFO_JSON);
      BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
      int cp;
      while ((cp = rd.read()) != -1) {
        sb.append((char) cp);
      }
      inputStream.close();
    } catch (Exception exception) {
      Log.e("JSONVectorMix", "Exception Loading GeoJSON: ", exception);
    }
    return sb;
  }
}
