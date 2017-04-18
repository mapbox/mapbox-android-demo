package com.mapbox.mapboxandroiddemo.examples.dds;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.functions.Function;
import com.mapbox.mapboxsdk.style.functions.stops.CategoricalStops;
import com.mapbox.mapboxsdk.style.functions.stops.Stop;
import com.mapbox.mapboxsdk.style.functions.stops.Stops;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;

import static com.mapbox.mapboxsdk.style.functions.stops.Stop.stop;

import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.PropertyValue;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class ChoroplethJsonVectorMixActivity extends AppCompatActivity implements OnMapReadyCallback {

  private MapView mapView;
  private MapboxMap map;

  private int maxValue = 13;

  private String mapId = "mapbox://mapbox.us_census_states_2015";
  private String vectorLayerName = "states";
  private String vectorMatchProp = "STATEFP";
  private String dataMatchProp = "STATE_ID";
  private String dataStyleUnemploymentProp = "unemployment";
  private String fillLayerId = "states-join";

  private String TAG = "JSOnVectorMix";
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

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    map = mapboxMap;


    // Add source for state polygons hosted on Mapbox
    VectorSource vectorSource = new VectorSource(vectorLayerName, mapId);
    map.addSource(vectorSource);

    loadJSON();

    try {
      statesArray = new JSONArray(sb.toString());

    } catch (Exception exception) {
      Log.e("JSONVectorMix", "Exception Loading GeoJSON: " + exception.toString());
    }


    Stop[] stops = new Stop[statesArray.length()];


    stops[0] = stop("0", PropertyFactory.fillColor("rgba(0,0,0,0)"));


    for (int statesArrayIndex = 0; statesArrayIndex < statesArray.length(); statesArrayIndex++) {

      try {

        JSONObject singleState = statesArray.getJSONObject(statesArrayIndex);

        double green = ((Double.parseDouble(singleState.getString(dataStyleUnemploymentProp)) / maxValue) * 255);

        String color = "rgba(" + 0 + ", " + green + ", " + 0 + ", 1)";

        stops[statesArrayIndex] = stop(singleState.getString(dataMatchProp), PropertyFactory.fillColor(color));


      } catch (JSONException e) {

        throw new RuntimeException(e);
      }

    }


    // Add layer from the vector tile source with data-driven style


    map.addLayer(new FillLayer(fillLayerId, vectorLayerName).withProperties(
      fillColor(
        Function.property(
          vectorMatchProp, Stops.categorical(stops)))));


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

  private StringBuilder loadJSON() {

    sb = new StringBuilder();

    try {
      // Load GeoJSON file
      InputStream inputStream = getAssets().open("state_unemployment_info.json");
      BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
      int cp;
      while ((cp = rd.read()) != -1) {
        sb.append((char) cp);
      }

      inputStream.close();


    } catch (Exception exception) {
      Log.e("JSONVectorMix", "Exception Loading GeoJSON: " + exception.toString());

    }


    return sb;
  }

}
