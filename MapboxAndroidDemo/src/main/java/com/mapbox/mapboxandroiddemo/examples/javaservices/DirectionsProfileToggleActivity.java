package com.mapbox.mapboxandroiddemo.examples.javaservices;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

/**
 * Use the Mapbox Java SDK to request directions from the Mapbox Directions API
 * and show the various directions profile.
 */
public class DirectionsProfileToggleActivity extends AppCompatActivity
    implements MapboxMap.OnMapClickListener {

  private static final String TAG = "DirectionsProfileToggleActivity";
  private static final String ROUTE_LAYER_ID = "route-layer-id";
  private static final String ROUTE_SOURCE_ID = "route-source-id";
  private static final String ICON_LAYER_ID = "icon-layer-id";
  private static final String ICON_SOURCE_ID = "icon-source-id";
  private static final String RED_PIN_ICON_ID = "red-pin-icon-id";
  private MapView mapView;
  private MapboxMap mapboxMap;
  private DirectionsRoute drivingRoute;
  private DirectionsRoute walkingRoute;
  private DirectionsRoute cyclingRoute;
  private MapboxDirections client;
  private Point origin = Point.fromLngLat(-99.13037323366, 19.40488375253);
  private Point destination = Point.fromLngLat(-99.167663574, 19.426984786987);
  private String lastSelectedDirectionsProfile = DirectionsCriteria.PROFILE_DRIVING;
  private Button drivingButton;
  private Button walkingButton;
  private Button cyclingButton;
  private boolean firstRouteDrawn = false;
  private String[] profiles = new String[]{
    DirectionsCriteria.PROFILE_DRIVING,
    DirectionsCriteria.PROFILE_CYCLING,
    DirectionsCriteria.PROFILE_WALKING
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_javaservices_directions_profile_toggle);

    drivingButton = findViewById(R.id.driving_profile_button);
    drivingButton.setTextColor(Color.WHITE);
    walkingButton = findViewById(R.id.walking_profile_button);
    cyclingButton = findViewById(R.id.cycling_profile_button);

    // Setup the MapView
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull MapboxMap mapboxMap) {
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {

            DirectionsProfileToggleActivity.this.mapboxMap = mapboxMap;

            initSource(style);

            initLayers(style);

            getAllRoutes(false);

            initButtonClickListeners();

            mapboxMap.addOnMapClickListener(DirectionsProfileToggleActivity.this);

            Toast.makeText(DirectionsProfileToggleActivity.this,
                R.string.instruction, Toast.LENGTH_SHORT).show();
          }
        });
      }
    });
  }

  /**
   * Load route info for each Directions API profile.
   *
   * @param fromMapClick whether the route loading is being triggered from tapping
   *                     on the map
   */
  private void getAllRoutes(boolean fromMapClick) {
    for (String profile : profiles) {
      getSingleRoute(profile, fromMapClick);
    }
  }

  @Override
  public boolean onMapClick(@NonNull LatLng point) {
    destination = Point.fromLngLat(point.getLongitude(), point.getLatitude());
    moveDestinationMarkerToNewLocation(point);
    getAllRoutes(true);
    return true;
  }

  /**
   * Move the destination marker to wherever the map was tapped on.
   *
   * @param pointToMoveMarkerTo where the map was tapped on
   */
  private void moveDestinationMarkerToNewLocation(LatLng pointToMoveMarkerTo) {
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        GeoJsonSource destinationIconGeoJsonSource = style.getSourceAs(ICON_SOURCE_ID);
        if (destinationIconGeoJsonSource != null) {
          destinationIconGeoJsonSource.setGeoJson(Feature.fromGeometry(Point.fromLngLat(
              pointToMoveMarkerTo.getLongitude(), pointToMoveMarkerTo.getLatitude())));
        }
      }
    });
  }

  /**
   * Add the source for the Directions API route line LineLayer.
   */
  private void initSource(@NonNull Style loadedMapStyle) {
    loadedMapStyle.addSource(new GeoJsonSource(ROUTE_SOURCE_ID));
    GeoJsonSource iconGeoJsonSource = new GeoJsonSource(ICON_SOURCE_ID,
        Feature.fromGeometry(Point.fromLngLat(destination.longitude(),
            destination.latitude())));
    loadedMapStyle.addSource(iconGeoJsonSource);
  }

  /**
   * Set up the click listeners on the buttons for each Directions API profile.
   */
  private void initButtonClickListeners() {
    drivingButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        drivingButton.setTextColor(Color.WHITE);
        walkingButton.setTextColor(Color.BLACK);
        cyclingButton.setTextColor(Color.BLACK);
        lastSelectedDirectionsProfile = DirectionsCriteria.PROFILE_DRIVING;
        showRouteLine();
      }
    });
    walkingButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        drivingButton.setTextColor(Color.BLACK);
        walkingButton.setTextColor(Color.WHITE);
        cyclingButton.setTextColor(Color.BLACK);
        lastSelectedDirectionsProfile = DirectionsCriteria.PROFILE_WALKING;
        showRouteLine();
      }
    });
    cyclingButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        drivingButton.setTextColor(Color.BLACK);
        walkingButton.setTextColor(Color.BLACK);
        cyclingButton.setTextColor(Color.WHITE);
        lastSelectedDirectionsProfile = DirectionsCriteria.PROFILE_CYCLING;
        showRouteLine();
      }
    });
  }

  /**
   * Display the Directions API route line depending on which profile was last
   * selected.
   */
  private void showRouteLine() {
    if (mapboxMap != null) {
      mapboxMap.getStyle(new Style.OnStyleLoaded() {
        @Override
        public void onStyleLoaded(@NonNull Style style) {

          // Retrieve and update the source designated for showing the directions route
          GeoJsonSource routeLineSource = style.getSourceAs(ROUTE_SOURCE_ID);

          // Create a LineString with the directions route's geometry and
          // reset the GeoJSON source for the route LineLayer source
          if (routeLineSource != null) {
            switch (lastSelectedDirectionsProfile) {
              case DirectionsCriteria.PROFILE_DRIVING:
                routeLineSource.setGeoJson(LineString.fromPolyline(drivingRoute.geometry(),
                    PRECISION_6));
                break;
              case DirectionsCriteria.PROFILE_WALKING:
                routeLineSource.setGeoJson(LineString.fromPolyline(walkingRoute.geometry(),
                    PRECISION_6));
                break;
              case DirectionsCriteria.PROFILE_CYCLING:
                routeLineSource.setGeoJson(LineString.fromPolyline(cyclingRoute.geometry(),
                    PRECISION_6));
                break;
              default:
                break;
            }
          }
        }
      });
    }
  }

  /**
   * Add the route and icon layers to the map
   */
  private void initLayers(@NonNull Style loadedMapStyle) {
    LineLayer routeLayer = new LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID);

    // Add the LineLayer to the map. This layer will display the directions route.
    routeLayer.setProperties(
        lineCap(Property.LINE_CAP_ROUND),
        lineJoin(Property.LINE_JOIN_ROUND),
        lineWidth(5f),
        lineColor(Color.parseColor("#006eff"))
    );
    loadedMapStyle.addLayer(routeLayer);

    // Add the red marker icon image to the map
    loadedMapStyle.addImage(RED_PIN_ICON_ID, BitmapUtils.getBitmapFromDrawable(
        getResources().getDrawable(R.drawable.red_marker)));

    // Add the red marker icon SymbolLayer to the map
    loadedMapStyle.addLayer(new SymbolLayer(ICON_LAYER_ID, ICON_SOURCE_ID).withProperties(
        iconImage(RED_PIN_ICON_ID),
        iconIgnorePlacement(true),
        iconAllowOverlap(true),
        iconOffset(new Float[]{0f, -9f})));
  }

  /**
   * Make a request to the Mapbox Directions API. Once successful, pass the route to the
   * route layer.
   *
   * @param profile the directions profile to use in the Directions API request
   */
  private void getSingleRoute(String profile, boolean fromMapClick) {
    client = MapboxDirections.builder()
        .origin(origin)
        .destination(destination)
        .overview(DirectionsCriteria.OVERVIEW_FULL)
        .profile(profile)
        .accessToken(getString(R.string.access_token))
        .build();

    client.enqueueCall(new Callback<DirectionsResponse>() {
      @Override
      public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
        // You can get the generic HTTP info about the response
        Timber.d("Response code: " + response.code());
        if (response.body() == null) {
          Timber.e("No routes found, make sure you set the right user and access token.");
          return;
        } else if (response.body().routes().size() < 1) {
          Timber.e("No routes found");
          return;
        }

        switch (profile) {
          case DirectionsCriteria.PROFILE_DRIVING:
            drivingRoute = response.body().routes().get(0);
            drivingButton.setText(String.format(getString(R.string.driving_profile),
                String.valueOf(TimeUnit.SECONDS.toMinutes(drivingRoute.duration().longValue()))));
            if (!firstRouteDrawn) {
              showRouteLine();
              firstRouteDrawn = true;
            }
            break;
          case DirectionsCriteria.PROFILE_WALKING:
            walkingRoute = response.body().routes().get(0);
            walkingButton.setText(String.format(getString(R.string.walking_profile),
                String.valueOf(TimeUnit.SECONDS
                    .toMinutes(walkingRoute.duration().longValue()))));
            break;
          case DirectionsCriteria.PROFILE_CYCLING:
            cyclingRoute = response.body().routes().get(0);
            cyclingButton.setText(String.format(getString(R.string.cycling_profile),
                String.valueOf(TimeUnit.SECONDS
                    .toMinutes(cyclingRoute.duration().longValue()))));
            break;
          default:
            break;
        }
        if (fromMapClick) {
          showRouteLine();
        }
      }

      @Override
      public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
        Timber.e("Error: " + throwable.getMessage());
        Toast.makeText(DirectionsProfileToggleActivity.this,
            "Error: " + throwable.getMessage(),
            Toast.LENGTH_SHORT).show();
      }
    });
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
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // Cancel the Directions API request
    if (client != null) {
      client.cancelCall();
    }
    if (mapboxMap != null) {
      mapboxMap.removeOnMapClickListener(this);
    }
    mapView.onDestroy();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }
}
