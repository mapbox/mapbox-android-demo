package com.mapbox.mapboxandroiddemo.examples.labs;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

public class InsetMapActivity extends AppCompatActivity implements MapboxMap.OnCameraMoveListener {

  private static final String STYLE_URL = Style.DARK;
  private static final String INSET_FRAGMENT_TAG = "com.mapbox.insetMapFragment";
  private static final String BOUNDS_LINE_LAYER_SOURCE_ID = "BOUNDS_LINE_LAYER_SOURCE_ID";
  private static final String BOUNDS_LINE_LAYER_LAYER_ID = "BOUNDS_LINE_LAYER_LAYER_ID";
  private static final int ZOOM_DISTANCE_BETWEEN_MAIN_AND_INSET_MAPS = 3;

  private MapView mainMapView;
  private MapboxMap mainMapboxMap;
  private MapboxMap insetMapboxMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_inset_map);

    mainMapView = findViewById(R.id.mapView);
    mainMapView.onCreate(savedInstanceState);
    mainMapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull MapboxMap mapboxMap) {
        InsetMapActivity.this.mainMapboxMap = mapboxMap;
        mapboxMap.setStyle(new Style.Builder().fromUri(STYLE_URL), new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            mainMapboxMap.addOnCameraMoveListener(InsetMapActivity.this);
          }
        });
      }
    });

    SupportMapFragment insetMapFragment =
      (SupportMapFragment) getSupportFragmentManager().findFragmentByTag(INSET_FRAGMENT_TAG);

    if (insetMapFragment == null) {
      // Create fragment transaction for the inset fragment
      final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

      // Build map fragment options
      MapboxMapOptions options = MapboxMapOptions.createFromAttributes(this, null);
      options.attributionEnabled(false);
      options.logoEnabled(false);
      options.compassEnabled(false);
      options.scrollGesturesEnabled(false);
      options.tiltGesturesEnabled(false);
      options.rotateGesturesEnabled(false);
      options.camera(new CameraPosition.Builder()
        .target(new LatLng(11.302318, 106.025839))
        .zoom(2)
        .build());

      // Create map fragment and pass through map options
      insetMapFragment = SupportMapFragment.newInstance(options);

      // Add fragmentMap fragment to parent container
      transaction.add(R.id.mini_map_fragment_container, insetMapFragment, INSET_FRAGMENT_TAG);
      transaction.commit();
    }

    insetMapFragment.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull MapboxMap mapboxMap) {
        insetMapboxMap = mapboxMap;
        mapboxMap.setStyle(new Style.Builder().fromUri(STYLE_URL), new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {

            // Create the LineString from the list of coordinates and then make a GeoJSON
            // FeatureCollection so we can add the line to our map as a layer.
            style.addSource(new GeoJsonSource(BOUNDS_LINE_LAYER_SOURCE_ID));

            // The layer properties for our line. This is where we make the line dotted, set the
            // color, etc.
            style.addLayer(new LineLayer(BOUNDS_LINE_LAYER_LAYER_ID, BOUNDS_LINE_LAYER_SOURCE_ID)
              .withProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(3f),
                lineColor(Color.YELLOW),
                visibility(VISIBLE)
              ));

            updateInsetMapLineLayerBounds(style);
          }
        });
      }
    });

    findViewById(R.id.show_bounds_toggle_fab).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // Toggle the visibility of the camera bounds LineLayer
        insetMapboxMap.getStyle(new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            Layer lineLayer = style.getLayer(BOUNDS_LINE_LAYER_LAYER_ID);
            if (lineLayer != null) {
              lineLayer.setProperties(
                VISIBLE.equals(lineLayer.getVisibility().getValue()) ? visibility(NONE) : visibility(VISIBLE));
            }
          }
        });
      }
    });
  }

  @Override
  public void onCameraMove() {
    CameraPosition mainCameraPosition = mainMapboxMap.getCameraPosition();
    CameraPosition insetCameraPosition = new CameraPosition.Builder(mainCameraPosition)
      .zoom(mainCameraPosition.zoom - ZOOM_DISTANCE_BETWEEN_MAIN_AND_INSET_MAPS).build();
    insetMapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(insetCameraPosition));
    if (insetMapboxMap != null) {
      insetMapboxMap.getStyle(new Style.OnStyleLoaded() {
        @Override
        public void onStyleLoaded(@NonNull Style style) {
          updateInsetMapLineLayerBounds(style);
        }
      });
    }
  }

  /**
   * Update the LineLayer with the latest coordinates of the main map's viewport bounds.
   *
   * @param fullyLoadedStyle the inset map's fully loaded style
   */
  private void updateInsetMapLineLayerBounds(@NonNull Style fullyLoadedStyle) {
    GeoJsonSource lineLayerSource = fullyLoadedStyle.getSourceAs(BOUNDS_LINE_LAYER_SOURCE_ID);
    if (lineLayerSource != null) {
      LatLngBounds bounds = mainMapboxMap.getProjection().getVisibleRegion().latLngBounds;
      List<Point> pointList = new ArrayList<>();
      pointList.add(Point.fromLngLat(bounds.getNorthWest().getLongitude(),
        bounds.getNorthWest().getLatitude()));
      pointList.add(Point.fromLngLat(bounds.getNorthEast().getLongitude(),
        bounds.getNorthEast().getLatitude()));
      pointList.add(Point.fromLngLat(bounds.getSouthEast().getLongitude(),
        bounds.getSouthEast().getLatitude()));
      pointList.add(Point.fromLngLat(bounds.getSouthWest().getLongitude(),
        bounds.getSouthWest().getLatitude()));
      pointList.add(Point.fromLngLat(bounds.getNorthWest().getLongitude(),
        bounds.getNorthWest().getLatitude()));

      lineLayerSource.setGeoJson(Feature.fromGeometry(LineString.fromLngLats(pointList)));
    }
  }

  // Add the mapView lifecycle to the activity's lifecycle methods
  @Override
  public void onResume() {
    super.onResume();
    mainMapView.onResume();
  }

  @Override
  protected void onStart() {
    super.onStart();
    mainMapView.onStart();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mainMapView.onStop();
  }

  @Override
  public void onPause() {
    super.onPause();
    mainMapView.onPause();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mainMapView.onLowMemory();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mainMapboxMap != null) {
      mainMapboxMap.removeOnCameraMoveListener(this);
    }
    mainMapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mainMapView.onSaveInstanceState(outState);
  }
}