package com.mapbox.mapboxandroiddemo.examples.dds;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Scanner;

import timber.log.Timber;

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

  private static final String STATE_UNEMPLOYMENT_INFO_JSON_FILE = "state_unemployment_info.json";
  private static final String VECTOR_SOURCE_NAME = "states";
  private static final String DATA_MATCH_PROP = "STATE_ID";
  private static final String DATA_STYLE_UNEMPLOYMENT_PROP = "unemployment";

  private MapView mapView;
  private MapboxMap mapboxMap;
  private JSONArray statesArray;

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
  public void onMapReady(@NonNull final MapboxMap map) {

    this.mapboxMap = map;
    map.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {

        // Add Mapbox-hosted vector source for state polygons
        style.addSource(new VectorSource(VECTOR_SOURCE_NAME, "mapbox://mapbox.us_census_states_2015"));

        new LoadJson(ChoroplethJsonVectorMixActivity.this).execute();
      }
    });
  }

  /**
   * Create layer from the vector tile source with data-driven style
   *
   * @param stops the array of stops to use in the FillLayer
   */
  private void addDataToMap(@NonNull Expression.Stop[] stops) {

    FillLayer statesJoinLayer = new FillLayer("states-join", VECTOR_SOURCE_NAME);
    statesJoinLayer.setSourceLayer(VECTOR_SOURCE_NAME);
    statesJoinLayer.withProperties(
      fillColor(match(toNumber(get(DATA_MATCH_PROP)),
        rgba(0, 0, 0, 1), stops))
    );

    // Add layer to map below the "waterway-label" layer
    if (mapboxMap != null) {
      mapboxMap.getStyle(style -> style.addLayerAbove(statesJoinLayer, "waterway-label"));
    }
  }

  private static class LoadJson extends AsyncTask<Void, Void, Expression.Stop[]> {

    private WeakReference<ChoroplethJsonVectorMixActivity> weakReference;
    private static final String TAG = "ChJSonVectorMix";

    LoadJson(ChoroplethJsonVectorMixActivity activity) {
      this.weakReference = new WeakReference<>(activity);
    }

    @Override
    protected Expression.Stop[] doInBackground(Void... voids) {
      try {
        ChoroplethJsonVectorMixActivity activity = weakReference.get();
        if (activity != null) {

          InputStream inputStream = activity.getAssets().open(STATE_UNEMPLOYMENT_INFO_JSON_FILE);

          activity.statesArray = new JSONArray(convertStreamToString(inputStream));

          // Create stops array
          Expression.Stop[] stops = new Expression.Stop[activity.statesArray.length()];

          for (int x = 0; x < activity.statesArray.length(); x++) {
            try {
              // Generate green color value for each state/stop
              JSONObject singleState = activity.statesArray.getJSONObject(x);
              double green = singleState.getDouble(DATA_STYLE_UNEMPLOYMENT_PROP) / 14 * 255;

              // Add new stop to array of stops
              stops[x] = stop(
                Double.parseDouble(singleState.getString(DATA_MATCH_PROP)),
                rgba(0, green, 0, 1)
              );

            } catch (JSONException exception) {
              throw new RuntimeException(exception);
            }
          }

          return stops;
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
    protected void onPostExecute(@Nullable Expression.Stop[] stopsArray) {
      super.onPostExecute(stopsArray);
      ChoroplethJsonVectorMixActivity activity = weakReference.get();
      if (activity != null && stopsArray != null) {
        activity.addDataToMap(stopsArray);
      }
    }
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
}
