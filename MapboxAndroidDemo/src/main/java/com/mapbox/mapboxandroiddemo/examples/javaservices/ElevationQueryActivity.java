package com.mapbox.mapboxandroiddemo.examples.javaservices;


import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.tilequery.MapboxTilequery;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textHaloBlur;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;

/**
 * Use the Mapbox Tilequery API to access global elevation data. More info about
 * elevation data can be found at
 * https://docs.mapbox.com/help/troubleshooting/access-elevation-data/
 */
public class ElevationQueryActivity extends AppCompatActivity implements
  OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener {

  private static final String RESULT_GEOJSON_SOURCE_ID = "RESULT_GEOJSON_SOURCE_ID";
  private static final String LAYER_ID = "LAYER_ID";
  private PermissionsManager permissionsManager;
  private MapboxMap mapboxMap;
  private MapView mapView;
  private TextView elevationQueryNumbersOnlyResponseTextView;
  private TextView elevationQueryJsonResponseTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_javaservices_elevation_query);

    elevationQueryJsonResponseTextView = findViewById(R.id.elevation_query_api_response_json_textview);
    elevationQueryNumbersOnlyResponseTextView = findViewById(R.id.elevation_query_api_response_elevation_numbers_only);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @SuppressWarnings( {"MissingPermission"})
  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
    ElevationQueryActivity.this.mapboxMap = mapboxMap;
    mapboxMap.setStyle(Style.OUTDOORS, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        addResultLayer(style);
        displayDeviceLocation(style);
        mapboxMap.addOnMapClickListener(ElevationQueryActivity.this);
        Toast.makeText(ElevationQueryActivity.this, R.string.click_on_map_instruction, Toast.LENGTH_SHORT).show();
      }
    });
  }

  /**
   * Add a map layer which will show a text number of the highest or lowest elevation number returned
   * by the Tilequery API.
   */
  private void addResultLayer(@NonNull Style loadedMapStyle) {
    loadedMapStyle.addSource(new GeoJsonSource(RESULT_GEOJSON_SOURCE_ID));
    loadedMapStyle.addLayer(new SymbolLayer(LAYER_ID, RESULT_GEOJSON_SOURCE_ID).withProperties(
      textField(get("ele")),
      textColor(Color.BLUE),
      textSize(23f),
      textHaloBlur(10f),
      textIgnorePlacement(true),
      textAllowOverlap(true)
    ));
  }


  @Override
  public boolean onMapClick(@NonNull LatLng point) {
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        // Use the map click location to make a request to the Tilequery API for elevation data.
        makeElevationRequestToTilequeryApi(style, point);
      }
    });
    return true;
  }

  /**
   * Use the Java SDK's MapboxTilequery class to build a API request and use the API response
   *
   * @param point where the Tilequery API should query Mapbox's "mapbox.mapbox-terrain-v2" tileset
   *              for elevation data.
   */
  private void makeElevationRequestToTilequeryApi(@NonNull final Style style, @NonNull LatLng point) {
    MapboxTilequery elevationQuery = MapboxTilequery.builder()
      .accessToken(getString(R.string.access_token))
      .tilesetIds("mapbox.mapbox-terrain-v2")
      .query(Point.fromLngLat(point.getLongitude(), point.getLatitude()))
      .geometry("polygon")
      .layers("contour")
      .build();

    elevationQuery.enqueueCall(new Callback<FeatureCollection>() {
      @Override
      public void onResponse(Call<FeatureCollection> call, Response<FeatureCollection> response) {

        if (response.body().features() != null) {
          List<Feature> featureList = response.body().features();

          String listOfElevationNumbers = "";

          // Build a list of the elevation numbers in the response.
          for (Feature singleFeature : featureList) {
            listOfElevationNumbers = listOfElevationNumbers + singleFeature.getStringProperty("ele") + ", ";
          }

          // Set this TextViews with the response info/JSON.
          elevationQueryNumbersOnlyResponseTextView.setText(String.format(getString(
            R.string.elevation_numbers_only_textview), featureList.size(), listOfElevationNumbers));
          elevationQueryJsonResponseTextView.setText(response.body().toJson());

          // Update the SymbolLayer that's responsible for showing the number text with the highest/lowest
          // elevation number
          if (featureList.size() > 0) {
            GeoJsonSource resultSource = style.getSourceAs(RESULT_GEOJSON_SOURCE_ID);
            if (resultSource != null) {
              resultSource.setGeoJson(featureList.get(featureList.size() - 1));
            }
          }
        } else {
          String noFeaturesString = getString(R.string.elevation_tilequery_no_features);
          Timber.d(noFeaturesString);
          Toast.makeText(ElevationQueryActivity.this, noFeaturesString, Toast.LENGTH_SHORT).show();
          elevationQueryNumbersOnlyResponseTextView.setText(noFeaturesString);
          elevationQueryJsonResponseTextView.setText(noFeaturesString);
        }
      }

      @Override
      public void onFailure(Call<FeatureCollection> call, Throwable throwable) {
        Timber.d("Request failed: %s", throwable.getMessage());
        Toast.makeText(ElevationQueryActivity.this,
          R.string.elevation_tilequery_api_response_error, Toast.LENGTH_SHORT).show();
      }
    });
  }

  /**
   * Use the Maps SDK's LocationComponent to display the device location on the map
   */
  @SuppressWarnings( {"MissingPermission"})
  private void displayDeviceLocation(@NonNull Style loadedMapStyle) {
    // Check if permissions are enabled and if not request
    if (PermissionsManager.areLocationPermissionsGranted(this)) {

      // Get an instance of the component
      LocationComponent locationComponent = mapboxMap.getLocationComponent();

      // Activate with options
      locationComponent.activateLocationComponent(
        LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

      // Enable to make component visible
      locationComponent.setLocationComponentEnabled(true);

      // Set the component's camera mode
      locationComponent.setCameraMode(CameraMode.TRACKING);
      locationComponent.setRenderMode(RenderMode.COMPASS);

      // Zoom the camera into the device's current location
      mapboxMap.animateCamera(CameraUpdateFactory
        .newCameraPosition(new CameraPosition.Builder()
          .zoom(10)
          .build()), 2000);
    } else {
      permissionsManager = new PermissionsManager(this);
      permissionsManager.requestLocationPermissions(this);
    }
  }

  // The following three methods are related to showing the device's location via the LocationComponent
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  @Override
  public void onExplanationNeeded(List<String> permissionsToExplain) {
    Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
  }

  @Override
  public void onPermissionResult(boolean granted) {
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        if (granted) {
          displayDeviceLocation(style);
        } else {
          Toast.makeText(ElevationQueryActivity.this,
              R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
          finish();
        }
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
    if (mapboxMap != null) {
      mapboxMap.removeOnMapClickListener(this);
    }
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}