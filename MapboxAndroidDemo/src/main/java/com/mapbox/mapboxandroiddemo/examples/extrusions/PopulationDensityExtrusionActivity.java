package com.mapbox.mapboxandroiddemo.examples.extrusions;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.functions.Function;
import com.mapbox.mapboxsdk.style.layers.FillExtrusionLayer;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Filter;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

import static com.mapbox.mapboxsdk.style.functions.stops.Stop.stop;
import static com.mapbox.mapboxsdk.style.functions.stops.Stops.exponential;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionBase;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionHeight;

/**
 * Use imported vector data to set the height of 3D building extrusions
 */
public class PopulationDensityExtrusionActivity extends AppCompatActivity implements OnMapReadyCallback {

  private MapView mapView;
  private MapboxMap mapboxMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));
    setContentView(R.layout.activity_population_density_extrusion);
    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);

  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;

    VectorSource vectorSource = new VectorSource("population", "mapbox://peterqliu.d0vin3el");
    mapboxMap.addSource(vectorSource);

    addFillsLayer();
    addExtrusionsLayer();
  }

  private void addFillsLayer() {
    FillLayer fillsLayer = new FillLayer("fills", "population");
    fillsLayer.setSourceLayer("outgeojson");
    fillsLayer.setFilter(Filter.all(Filter.lt("pkm2", 300000)));
    fillsLayer.withProperties(
      fillColor(Function.property("pkm2", exponential(
        stop(0, fillColor(Color.parseColor("#160e23"))),
        stop(14500, fillColor(Color.parseColor("#00617f"))),
        stop(145000, fillColor(Color.parseColor("#55e9ff"))))
        .withBase(1f)))
    );
    mapboxMap.addLayerBelow(fillsLayer, "water");
  }

  private void addExtrusionsLayer() {
    FillExtrusionLayer fillExtrusionLayer = new FillExtrusionLayer("extrusions", "population");
    fillExtrusionLayer.setSourceLayer("outgeojson");
    fillExtrusionLayer.setFilter(Filter.all(Filter.gt("p", 1), Filter.lt("pkm2", 300000)));
    fillExtrusionLayer.withProperties(
      fillExtrusionColor(Function.property("pkm2", exponential(
        stop(0, fillColor(Color.parseColor("#160e23"))),
        stop(14500, fillColor(Color.parseColor("#00617f"))),
        stop(145000, fillColor(Color.parseColor("#55e9ff"))))
        .withBase(1f))),
      fillExtrusionBase(0f),
      fillExtrusionHeight(Function.property("pkm2", exponential(
        stop(0, fillExtrusionHeight(0f)),
        stop(1450000, fillExtrusionHeight(20000f)))
        .withBase(1f))));
    mapboxMap.addLayerBelow(fillExtrusionLayer, "airport-label");
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
      case android.R.id.home:
        finish();
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

}