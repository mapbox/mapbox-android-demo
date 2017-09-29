package com.mapbox.mapboxandroiddemo.examples.mas;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.api.directions.v5.DirectionsCriteria;
import com.mapbox.services.api.directionsmatrix.v1.MapboxDirectionsMatrix;
import com.mapbox.services.api.directionsmatrix.v1.models.DirectionsMatrixResponse;
import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.geojson.FeatureCollection;
import com.mapbox.services.commons.geojson.Point;
import com.mapbox.services.commons.models.Position;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;

public class DirectionsMatrixApiActivity extends AppCompatActivity implements MapboxMap.OnMapClickListener {

  private static final LatLngBounds BOSTON_BOUNDS = new LatLngBounds.Builder()
    .include(new LatLng(42.363581, -71.097695))
    .include(new LatLng(42.350399, -71.040765))
    .build();

  private MapView mapView;
  private MapboxMap mapboxMap;
  private List<Position> positionList;
  private FeatureCollection featureCollection;
  private String TAG = "DirectionsMatrixApiActivity";
  public static final String MARKER_SOURCE = "marker-source";
  public static final String MARKER_LAYER = "marker-layer";
  public static final String LIGHTING_BOLT_IMAGE = "bolt-image";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_matrix_api);

    setUpPositionList();

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(final MapboxMap mapboxMap) {
        DirectionsMatrixApiActivity.this.mapboxMap = mapboxMap;
        mapboxMap.setLatLngBoundsForCameraTarget(BOSTON_BOUNDS);
        addMarkers();
      }
    });
  }

  private void makeMatrixApiCall(Position positionOfClickedMarker) {
    MapboxDirectionsMatrix directionsMatrixClient = new MapboxDirectionsMatrix.Builder()
      .setAccessToken(getString(R.string.access_token))
      .setProfile(DirectionsCriteria.PROFILE_DRIVING)
//      .setCoordinates(positionList)
      .setOrigin(positionList.get(0))
      .setDestination(positionOfClickedMarker)
//      .setDestinations(positionList.size())
      .build();

    // Handle the API response
    directionsMatrixClient.enqueueCall(new Callback<DirectionsMatrixResponse>() {
      @Override
      public void onResponse(Call<DirectionsMatrixResponse> call, Response<DirectionsMatrixResponse> response) {

        double[][] array = response.body().getDurations();

        String finalDouble = String.valueOf(String.valueOf(array[0][1]));

        Toast.makeText(DirectionsMatrixApiActivity.this, finalDouble, Toast.LENGTH_SHORT).show();

      }

      @Override
      public void onFailure(Call<DirectionsMatrixResponse> call, Throwable throwable) {
        Toast.makeText(DirectionsMatrixApiActivity.this, R.string.call_error, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onResponse");

      }
    });
  }

  private void addMarkers() {

    // Add lighting bolt marker image to map
    mapboxMap.addImage(
      LIGHTING_BOLT_IMAGE,
      BitmapFactory.decodeResource(DirectionsMatrixApiActivity.this.getResources(),
        R.drawable.lightning_bolt)
    );

    // Add a source
    mapboxMap.addSource(new GeoJsonSource(MARKER_SOURCE, featureCollection));

    // Add the symbol layer
    mapboxMap.addLayer(
      new SymbolLayer(MARKER_LAYER, MARKER_SOURCE)
        .withProperties(
          iconImage(LIGHTING_BOLT_IMAGE),
          iconAllowOverlap(true)
        )
    );

    // Set a click listener so we can manipulate the map
    mapboxMap.setOnMapClickListener(this);
  }

  @Override
  public void onMapClick(@NonNull LatLng point) {
    // Query which features are clicked
    PointF screenLoc = mapboxMap.getProjection().toScreenLocation(point);
    List<Feature> features = mapboxMap.queryRenderedFeatures(screenLoc, MARKER_LAYER);

    SymbolLayer layer = mapboxMap.getLayerAs(MARKER_LAYER);
    if (features.size() == 0) {
      // Reset marker bolts to regular size if map is clicked on
      layer.setProperties(iconSize(1f));
    } else {
      // Increases size of marker bolts if a marker is clicked on
      layer.setProperties(iconSize(1.5f));

      // TODO: Figure out marker onclick
      /*makeMatrixApiCall(Position.fromCoordinates(clickedMarker.getPosition().getLongitude(),
        clickedMarker.getPosition().getLatitude()));*/

    }
  }


  private String loadGeoJsonFromAsset(String filename) {
    try {
      // Load GeoJSON file from local asset folder
      InputStream is = getAssets().open(filename);
      int size = is.available();
      byte[] buffer = new byte[size];
      is.read(buffer);
      is.close();
      return new String(buffer, "UTF-8");
    } catch (Exception exception) {
      Log.e(TAG, "Exception Loading GeoJSON: " + exception.toString());
      exception.printStackTrace();
      return null;
    }
  }

  private void setUpPositionList() {

    // Get GeoJSON features from GeoJSON file in the assets folder
    featureCollection = FeatureCollection.fromJson(loadGeoJsonFromAsset("boston_charge_stations.geojson"));

    // Initialize List<Position> for eventual use in the Matrix API call
    positionList = new ArrayList<>();

    // Get the position of each GeoJSON feature and build the list of Position
    // objects for eventual use in the Matrix API call
    for (int x = 0; x < featureCollection.getFeatures().size(); x++) {
      Feature singleLocation = featureCollection.getFeatures().get(x);
      Position singleLocationPosition = (Position) singleLocation.getGeometry().getCoordinates();
      positionList.add(singleLocationPosition);
    }
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
}
