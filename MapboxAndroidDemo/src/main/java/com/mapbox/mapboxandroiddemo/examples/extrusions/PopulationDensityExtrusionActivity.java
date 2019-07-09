package com.mapbox.mapboxandroiddemo.examples.extrusions;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillExtrusionLayer;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

import static com.mapbox.mapboxsdk.style.expressions.Expression.all;
import static com.mapbox.mapboxsdk.style.expressions.Expression.exponential;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.gt;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.lt;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgb;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
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
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    mapboxMap.setStyle(Style.DARK, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        style.addSource(
            new VectorSource("population", "mapbox://peterqliu.d0vin3el")
        );
        addFillsLayer(style);
        addExtrusionsLayer(style);
      }
    });
  }

  private void addFillsLayer(@NonNull Style loadedMapStyle) {
    FillLayer fillsLayer = new FillLayer("fills", "population");
    fillsLayer.setSourceLayer("outgeojson");
    fillsLayer.setFilter(all(lt(get("pkm2"), literal(300000))));
    fillsLayer.withProperties(
      fillColor(interpolate(exponential(1f), get("pkm2"),
        stop(0, rgb(22, 14, 35)),
        stop(14500, rgb(0, 97, 127)),
        stop(145000, rgb(85, 223, 255)))));
    loadedMapStyle.addLayerBelow(fillsLayer, "water");
  }

  private void addExtrusionsLayer(@NonNull Style loadedMapStyle) {
    FillExtrusionLayer fillExtrusionLayer = new FillExtrusionLayer("extrusions", "population");
    fillExtrusionLayer.setSourceLayer("outgeojson");
    fillExtrusionLayer.setFilter(all(gt(get("p"), 1), lt(get("pkm2"), 300000)));
    fillExtrusionLayer.withProperties(
      fillExtrusionColor(interpolate(exponential(1f), get("pkm2"),
        stop(0, rgb(22, 14, 35)),
        stop(14500, rgb(0, 97, 127)),
        stop(145000, rgb(85, 233, 255)))),
      fillExtrusionBase(0f),
      fillExtrusionHeight(interpolate(exponential(1f), get("pkm2"),
        stop(0, 0f),
        stop(1450000, 20000f))));
    loadedMapStyle.addLayerBelow(fillExtrusionLayer, "airport-label");
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
        return true;
      case R.id.los_angeles:
        goToNewLocation(34.04412546508576, -118.28636169433594);
        return true;
      case R.id.seattle:
        goToNewLocation(47.60651025683697, -122.33327865600585);
        return true;
      case R.id.new_orleans:
        goToNewLocation(29.946159058399612, -90.10042190551758);
        return true;
      case R.id.chicago:
        goToNewLocation(41.87531293759582, -87.6240348815918);
        return true;
      case R.id.philadelphia:
        goToNewLocation(39.95370120254379, -75.1626205444336);
        return true;
      case R.id.new_york:
        goToNewLocation(40.72228267283148, -73.99772644042969);
        return true;
      case R.id.atlanta:
        goToNewLocation(33.74910736130734, -84.39079284667969);
        return true;
      case R.id.portland:
        goToNewLocation(45.522104713562825, -122.67179489135742);
        return true;
      case R.id.denver:
        goToNewLocation(39.74428621972816, -104.99565124511719);
        return true;
      case R.id.minneapolis:
        goToNewLocation(44.969656023708175, -93.26637268066406);
        return true;
      case R.id.miami:
        goToNewLocation(25.773846629676616, -80.19624710083008);
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
