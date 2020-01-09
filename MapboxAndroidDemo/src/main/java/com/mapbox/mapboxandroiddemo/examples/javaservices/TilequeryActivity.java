package com.mapbox.mapboxandroiddemo.examples.javaservices;


import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

/**
 * Use the Mapbox Tilequery API to retrieve information about Features on a Vector Tileset. More info about
 * the Tilequery API can be found at https://www.mapbox.com/api-documentation/#tilequery
 */
public class TilequeryActivity extends AppCompatActivity implements
  OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener {

  private static final String RESULT_GEOJSON_SOURCE_ID = "RESULT_GEOJSON_SOURCE_ID";
  private static final String CLICK_CENTER_GEOJSON_SOURCE_ID = "CLICK_CENTER_GEOJSON_SOURCE_ID";
  private static final String LAYER_ID = "LAYER_ID";
  private static final String RESULT_ICON_ID = "RESULT_ICON_ID";
  private static final String CLICK_ICON_ID = "CLICK_ICON_ID";
  private PermissionsManager permissionsManager;
  private MapboxMap mapboxMap;
  private MapView mapView;
  private TextView tilequeryResponseTextView;

  @Override

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_javaservices_tilequery);

    tilequeryResponseTextView = findViewById(R.id.tilequery_response_info_textview);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @SuppressWarnings( {"MissingPermission"})
  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
    TilequeryActivity.this.mapboxMap = mapboxMap;
    mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        addClickLayer(style);
        addResultLayer(style);
        displayDeviceLocation(style);
        mapboxMap.addOnMapClickListener(TilequeryActivity.this);
        Toast.makeText(TilequeryActivity.this, R.string.click_on_map_instruction, Toast.LENGTH_SHORT).show();
      }
    });
  }

  /**
   * Add a map layer which will show a marker icon where the map was clicked
   */
  private void addClickLayer(@NonNull Style loadedMapStyle) {
    loadedMapStyle.addImage(CLICK_ICON_ID, BitmapFactory.decodeResource(
      TilequeryActivity.this.getResources(), R.drawable.red_marker));

    loadedMapStyle.addSource(new GeoJsonSource(CLICK_CENTER_GEOJSON_SOURCE_ID,
      FeatureCollection.fromFeatures(new Feature[] {})));

    loadedMapStyle.addLayer(new SymbolLayer("click-layer", CLICK_CENTER_GEOJSON_SOURCE_ID).withProperties(
      iconImage(CLICK_ICON_ID),
      iconOffset(new Float[] {0f, -12f}),
      iconIgnorePlacement(true),
      iconAllowOverlap(true)
    ));
  }

  /**
   * Add a map layer which will show marker icons for all of the Tilequery API results
   */
  private void addResultLayer(@NonNull Style loadedMapStyle) {
    // Add the marker image to map
    loadedMapStyle.addImage(RESULT_ICON_ID, BitmapFactory.decodeResource(
      TilequeryActivity.this.getResources(), R.drawable.blue_marker));

    // Retrieve GeoJSON information from the Mapbox Tilequery API
    loadedMapStyle.addSource(new GeoJsonSource(RESULT_GEOJSON_SOURCE_ID));

    loadedMapStyle.addLayer(new SymbolLayer(LAYER_ID, RESULT_GEOJSON_SOURCE_ID).withProperties(
      iconImage(RESULT_ICON_ID),
      iconOffset(new Float[] {0f, -12f}),
      iconIgnorePlacement(true),
      iconAllowOverlap(true)
    ));
  }


  @Override
  public boolean onMapClick(@NonNull LatLng point) {
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        // Move and display the click center layer's red marker icon to wherever the map was clicked on
        GeoJsonSource clickLocationSource = style.getSourceAs(CLICK_CENTER_GEOJSON_SOURCE_ID);
        if (clickLocationSource != null) {
          clickLocationSource.setGeoJson(Point.fromLngLat(point.getLongitude(), point.getLatitude()));
        }

        // Use the map click location to make a Tilequery API call
        makeTilequeryApiCall(point);
      }
    });
    return true;
  }

  /**
   * Use the Java SDK's MapboxTilequery class to build a API request and use the API response
   *
   * @param point the center point that the the tilequery will originate from.
   */
  private void makeTilequeryApiCall(@NonNull LatLng point) {
    MapboxTilequery tilequery = MapboxTilequery.builder()
      .accessToken(getString(R.string.access_token))
      .tilesetIds("mapbox.mapbox-streets-v7")
      .query(Point.fromLngLat(point.getLongitude(), point.getLatitude()))
      .radius(50)
      .limit(10)
      .geometry("polygon")
      .dedupe(true)
      .layers("building")
      .build();

    tilequery.enqueueCall(new Callback<FeatureCollection>() {
      @Override
      public void onResponse(Call<FeatureCollection> call, Response<FeatureCollection> response) {
        if (response.body() != null) {
          FeatureCollection responseFeatureCollection = response.body();
          tilequeryResponseTextView.setText(responseFeatureCollection.toJson());
          mapboxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
              GeoJsonSource resultSource = style.getSourceAs(RESULT_GEOJSON_SOURCE_ID);
              if (resultSource != null && responseFeatureCollection.features() != null) {
                List<Feature> featureList = responseFeatureCollection.features();
                if (featureList.isEmpty()) {
                  Toast.makeText(TilequeryActivity.this,
                    getString(R.string.no_tilequery_response_features_toast), Toast.LENGTH_SHORT).show();
                } else {
                  resultSource.setGeoJson(FeatureCollection.fromFeatures(featureList));
                }
              }
            }
          });
        }
      }

      @Override
      public void onFailure(Call<FeatureCollection> call, Throwable throwable) {
        Timber.d("Request failed: %s", throwable.getMessage());
        Toast.makeText(TilequeryActivity.this, R.string.api_error, Toast.LENGTH_SHORT).show();
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
      locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(
        this, loadedMapStyle).build());

      // Enable to make component visible
      locationComponent.setLocationComponentEnabled(true);

      // Set the component's camera mode
      locationComponent.setCameraMode(CameraMode.TRACKING);
      locationComponent.setRenderMode(RenderMode.COMPASS);

      // Zoom the camera into the device's current location
      mapboxMap.animateCamera(CameraUpdateFactory
        .newCameraPosition(new CameraPosition.Builder()
          .zoom(17)
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
    Style style = mapboxMap.getStyle();
    if (granted && style != null) {
      displayDeviceLocation(style);
    } else {
      Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
      finish();
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