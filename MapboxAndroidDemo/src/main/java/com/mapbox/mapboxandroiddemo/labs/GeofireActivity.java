package com.mapbox.mapboxandroiddemo.labs;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.geojson.FeatureCollection;
import com.mapbox.services.commons.geojson.Point;
import com.mapbox.services.commons.models.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeofireActivity extends AppCompatActivity implements GeoQueryEventListener,
  MapboxMap.OnCameraMoveListener {

  private MapView mapView;
  private String TAG = "GeofireActivity";
  private MapboxMap mapboxMap;

  private SymbolLayer circleLayer;
  private GeoFire geoFire;
  private GeoQuery geoQuery;
  private Map<String, Marker> markers;
  private FirebaseApp app;

  private static final GeoLocation INITIAL_CENTER = new GeoLocation(37.7789, -122.4017);
  private static final int INITIAL_ZOOM_LEVEL = 12;
  private static final String GEO_FIRE_DB = "https://mapbox-android-demo.firebaseio.com";
  private static final String GEO_FIRE_REF = GEO_FIRE_DB + "/_geofire";


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_geofire_example);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {

        GeofireActivity.this.mapboxMap = mapboxMap;

        addMarkersToMapViaSymbolLayer();

        addCircleLayer();

//        mapboxMap.setOnCameraMoveListener(GeofireActivity.this);

        FirebaseOptions options = new FirebaseOptions.Builder().setApplicationId("geofire").setDatabaseUrl(GEO_FIRE_DB).build();
        if (!FirebaseApp.getApps(GeofireActivity.this).isEmpty()) {
          app = FirebaseApp.getInstance();
          geoFire = new GeoFire(FirebaseDatabase.getInstance(app).getReferenceFromUrl(GEO_FIRE_REF));
        } else {
          app = FirebaseApp.initializeApp(GeofireActivity.this, options);
        }
//        geoQuery = geoFire.queryAtLocation(INITIAL_CENTER, 1);

//        geoQuery.addGeoQueryEventListener(GeofireActivity.this);


        // setup markers
        markers = new HashMap<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("path/to/geofire");
        GeoFire geoFire = new GeoFire(ref);

        geoFire.setLocation("Great Sphinx of Giza", new GeoLocation(29.9752687, 31.1375673),
          new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

              Log.d(TAG, "onComplete: finished setting location");
              Toast.makeText(GeofireActivity.this, "finished setting location", Toast.LENGTH_SHORT).show();

            }
          });
      }
    });


  }

  // Add the mapView lifecycle to the activity's lifecycle methods
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
    geoQuery.removeAllListeners();
    for (Marker marker : markers.values()) {
      marker.remove();
    }
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

  private void addCircleLayer() {
    List<Feature> markerCoordinates = new ArrayList<>();
    markerCoordinates.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(-122.3998, 37.788339))) // Mapbox's SF office
    );
    FeatureCollection featureCollection = FeatureCollection.fromFeatures(markerCoordinates);
    Source circleSource = new GeoJsonSource("circle-source", featureCollection);
    mapboxMap.addSource(circleSource);

    circleLayer = new SymbolLayer("circleLayer", "circle-source");
    circleLayer.setProperties(PropertyFactory.fillColor(Color.argb(66, 255, 0, 255)));
    circleLayer.setProperties(PropertyFactory.circleStrokeColor(Color.argb(66, 0, 0, 0)));
    mapboxMap.addLayer(circleLayer);
  }

  private void addMarkersToMapViaSymbolLayer() {
    List<Feature> markerCoordinates = new ArrayList<>();
    markerCoordinates.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(31.1375673, 29.9752687))) // Great Sphinx of Giza
    );
    FeatureCollection featureCollection = FeatureCollection.fromFeatures(markerCoordinates);

    Source geoJsonSource = new GeoJsonSource("marker-source", featureCollection);
    mapboxMap.addSource(geoJsonSource);
    Bitmap icon = BitmapFactory.decodeResource(GeofireActivity.this.getResources(), R.drawable.blue_marker_view);

    // Add the marker image to map
    mapboxMap.addImage("blue-marker", icon);

    SymbolLayer markers = new SymbolLayer("marker-layer", "marker-source")
      .withProperties(PropertyFactory.iconImage("blue-marker"));
    mapboxMap.addLayer(markers);
  }

  @Override
  public void onKeyEntered(String key, GeoLocation location) {
    // Add a new marker to the map
    Marker marker = mapboxMap.addMarker(new MarkerOptions().position(
      new LatLng(location.latitude, location.longitude)));
    markers.put(key, marker);
  }

  @Override
  public void onKeyExited(String key) {
    // Remove any old marker
    Marker marker = markers.get(key);
    if (marker != null) {
      marker.remove();
      markers.remove(key);
    }
  }

  @Override
  public void onKeyMoved(String key, GeoLocation location) {
    // Move the marker
    Marker marker = markers.get(key);
    if (marker != null) {
      animateMarkerTo(marker, location.latitude, location.longitude);
    }
  }

  @Override
  public void onGeoQueryReady() {
  }


  @Override
  public void onGeoQueryError(DatabaseError error) {
    new AlertDialog.Builder(this)
      .setTitle("Error")
      .setMessage("There was an unexpected error querying GeoFire: " + error.getMessage())
      .setPositiveButton(android.R.string.ok, null)
      .setIcon(android.R.drawable.ic_dialog_alert)
      .show();
  }

  @Override
  public void onCameraMove() {
    // Update the search criteria for this geoQuery and the circle on the map

    Log.d(TAG, "onCameraMove: camera moved");

    LatLng center = mapboxMap.getCameraPosition().target;
    double radius = zoomLevelToRadius(mapboxMap.getCameraPosition().zoom);
    // circleLayer.set(center);
    // circleLayer.set(radius);
    geoQuery.setCenter(new GeoLocation(center.getLatitude(), center.getLongitude()));
    // radius in km
    geoQuery.setRadius(radius / 1000);
  }

  private double zoomLevelToRadius(double zoomLevel) {
    // Approximation to fit circle into view
    return 16384000 / Math.pow(2, zoomLevel);
  }

  // Animation handler for old APIs without animation support
  private void animateMarkerTo(final Marker marker, final double lat, final double lng) {
    final Handler handler = new Handler();
    final long start = SystemClock.uptimeMillis();
    final long duration_ms = 3000;
    final Interpolator interpolator = new AccelerateDecelerateInterpolator();
    final LatLng startPosition = marker.getPosition();
    handler.post(new Runnable() {
      @Override
      public void run() {
        float elapsed = SystemClock.uptimeMillis() - start;
        float time = elapsed / duration_ms;
        float variable = interpolator.getInterpolation(time);

        double currentLat = (lat - startPosition.getLatitude()) * variable + startPosition.getLatitude();
        double currentLng = (lng - startPosition.getLongitude()) * variable + startPosition.getLongitude();

        marker.setPosition(new LatLng(currentLat, currentLng));

        // if animation is not finished yet, repeat
        if (time < 1) {
          handler.postDelayed(this, 16);
        }
      }
    });
  }
}
