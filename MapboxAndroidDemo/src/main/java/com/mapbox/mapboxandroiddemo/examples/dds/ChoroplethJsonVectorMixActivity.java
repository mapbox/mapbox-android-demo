package com.mapbox.mapboxandroiddemo.examples.dds;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

import static com.mapbox.mapboxsdk.style.functions.Function.property;
import static com.mapbox.mapboxsdk.style.functions.stops.Stop.stop;
import static com.mapbox.mapboxsdk.style.functions.stops.Stops.categorical;

public class ChoroplethJsonVectorMixActivity extends AppCompatActivity implements OnMapReadyCallback {

  private MapView mapView;
  private MapboxMap map;

  private int maxValue = 13;
  private String mapId = "mapbox.us_census_states_2015";
  private String vtMatchProp = "STATEFP";
  private String dataMatchProp = "STATE_ID";
  private String dataStyleProp = "unemployment";

//  TODO: Fix stops below
//  private int[] stops = [["0","rgba(0,0,0,0)"]];

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

    setUpLocalJsonData();

    // Add source for state polygons hosted on Mapbox
    VectorSource vectorSource = new VectorSource("vectorTiles", "mapbox://" + mapId);
    map.addSource(vectorSource);

//    TODO: First value is the default, used where the is no data


//    TODO: Calculate color for each state based on the unemployment rate

    data.forEach(function(row) {
      int green = ((row[dataStyleProp] / maxValue) * 255);
      String color = "rgba(" + 0 + ", " + green + ", " + 0 + ", 1)";
      stops.push([row[dataMatchProp], color]);
    });

    // Add layer from the vector tile source with data-driven style
    map.addLayer(new FillLayer("states-join", "vectorTiles").withProperties(
      PropertyFactory.fillColor(
        property(vtMatchProp,
          categorical(
            stop(stops, PropertyFactory.lineColor(Color.parseColor("#F7455D")))))
      ),
      PropertyFactory.visibility(Property.VISIBLE),
      PropertyFactory.lineWidth(3f)));


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

  private void setUpLocalJsonData() {

    // Join local JSON data with vector tile geometry
    // USA unemployment rate in 2009
    // Source https://data.bls.gov/timeseries/LNS14000000


  }


}
