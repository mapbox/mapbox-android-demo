package com.mapbox.mapboxandroiddemo.examples.annotations;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.geojson.FeatureCollection;
import com.mapbox.services.commons.geojson.Point;
import com.mapbox.services.commons.models.Position;

import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

public class MultipleMovingMarkersActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private Handler handler;
  private Runnable runnable;
  private List<Feature> locationList;
  private static final String CAR_SYMBOL_LAYER_ID = "car_marker_layer_id";
  private static final String CAR_GEOJSON_SOURCE_ID = "CAR_GEOJSON_SOURCE_ID";
  private static final String CAR_MARKER_IMAGE_ID = "CAR_MARKER_IMAGE_ID";
  private FeatureCollection carFeatureCollection;
  private GeoJsonSource carGeoJsonSource;
  private SymbolLayer carLocationLayer;
  private Bitmap carMarkerSymbolLayerImage;

  @Override

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_multiple_moving_markers);

    initLocationList();
    initFeatureCollection();

    carMarkerSymbolLayerImage = BitmapFactory.decodeResource(MultipleMovingMarkersActivity.
      this.getResources(), R.drawable.car_icon);

    carGeoJsonSource = new GeoJsonSource(CAR_GEOJSON_SOURCE_ID, carFeatureCollection);

    carLocationLayer = new SymbolLayer(CAR_SYMBOL_LAYER_ID, CAR_GEOJSON_SOURCE_ID)
      .withProperties(iconImage(CAR_MARKER_IMAGE_ID), icon iconAllowOverlap(true));


    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {
        MultipleMovingMarkersActivity.this.mapboxMap = mapboxMap;
        mapboxMap.addImage(CAR_MARKER_IMAGE_ID, carMarkerSymbolLayerImage);
        mapboxMap.addSource(carGeoJsonSource);
        mapboxMap.addLayer(carLocationLayer);

        // Use the RefreshCarIconLocationRunnable class and runnable to keep updating the car locations
        runnable = new RefreshCarIconLocationRunnable(handler = new Handler(), carFeatureCollection);
        handler.postDelayed(runnable, 1000);





      }
    });
  }

  private static class LatLngEvaluator implements TypeEvaluator<LatLng> {
    // Method is used to interpolate the marker animation.

    private LatLng latLng = new LatLng();

    @Override
    public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
      latLng.setLatitude(startValue.getLatitude()
        + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
      latLng.setLongitude(startValue.getLongitude()
        + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
      return latLng;
    }
  }

  private static class RefreshCarIconLocationRunnable implements Runnable {

    private Handler handler;
    private FeatureCollection featureCollection;

    public RefreshCarIconLocationRunnable(Handler handler, FeatureCollection featureCollection) {
      this.handler = handler;
      this.featureCollection = featureCollection;
    }

    @Override
    public void run() {

      for (Feature singleFeature : featureCollection.getFeatures()) {

        singleFeature.setProperties();

      }


      /*ValueAnimator markerAnimator = ObjectAnimator.ofObject(marker, "position",
        new MultipleMovingMarkersActivity().LatLngEvaluator(), marker.getPosition(), point);
      markerAnimator.setDuration(2000);
      markerAnimator.start();
*/

      handler.postDelayed(this, 50);
    }
  }

  @Override
  public void onResume() {
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
  public void onPause() {
    super.onPause();
    mapView.onPause();
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

  private void initFeatureCollection() {
    carFeatureCollection = FeatureCollection.fromFeatures(locationList);
  }

  private void initLocationList() {
    locationList = new ArrayList<>();
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.457761, 31.223612))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.433578, 31.203922))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.426813, 31.216156))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.531865, 31.218743))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.417334, 31.20144))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.516148, 31.208591))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.5236, 31.200713))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.503879, 31.20379))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.49006, 31.221197))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.445382, 31.218662))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.395577, 31.260226))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.494313, 31.213893))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.47999, 31.207333))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.488394, 31.223176))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.512189, 31.239005))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.46114, 31.223891))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.38207, 31.258623))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.444588, 31.23733))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.502773, 31.196006))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.477018, 31.2334))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.473235, 31.207648))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.48233, 31.224315))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.416214, 31.253812))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.463561, 31.214624))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.477191, 31.215741))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.496586, 31.214168))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.438357, 31.226763))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.447077, 31.213005))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.445382, 31.207859))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.486622, 31.207648))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.507228, 31.19509))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.455534, 31.231078))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.513306, 31.197579))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.459351, 31.237944))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.511693, 31.211004))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.483629, 31.200085))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.475366, 31.216643))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.509628, 31.232898))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.464742, 31.210181))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.443611, 31.197662))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.521988, 31.217793))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.386572, 31.238414))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.473011, 31.24362))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.482421, 31.232005))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.515359, 31.222877))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.460709, 31.20145))));
    locationList.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(121.461475, 31.193431))));
  }

}