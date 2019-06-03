package com.mapbox.mapboxandroiddemo.examples.styles;

import android.animation.ValueAnimator;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

/**
 * Use a SymbolLayer to display icons and then change an icon's size when tapped on.
 */
public class IconSizeChangeOnClickActivity extends AppCompatActivity implements
  OnMapReadyCallback, MapboxMap.OnMapClickListener {

  private ValueAnimator markerAnimator;
  private MapView mapView;
  private MapboxMap mapboxMap;
  private boolean markerSelected = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_style_symbol_icon_size_change_on_click);

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
        List<Feature> markerCoordinates = new ArrayList<>();
        markerCoordinates.add(Feature.fromGeometry(
          Point.fromLngLat(-71.065634, 42.354950))); // Boston Common Park
        markerCoordinates.add(Feature.fromGeometry(
          Point.fromLngLat(-71.097293, 42.346645))); // Fenway Park
        markerCoordinates.add(Feature.fromGeometry(
          Point.fromLngLat(-71.053694, 42.363725))); // The Paul Revere House

        style.addSource(new GeoJsonSource("marker-source",
          FeatureCollection.fromFeatures(markerCoordinates)));

        // Add the marker image to map
        style.addImage("my-marker-image", BitmapFactory.decodeResource(
          IconSizeChangeOnClickActivity.this.getResources(), R.drawable.blue_marker_view));

        // Adding an offset so that the bottom of the blue icon gets fixed to the coordinate, rather than the
        // middle of the icon being fixed to the coordinate point.
        style.addLayer(new SymbolLayer("marker-layer", "marker-source")
          .withProperties(PropertyFactory.iconImage("my-marker-image"),
            iconAllowOverlap(true),
            iconOffset(new Float[]{0f, -9f})));

        // Add the selected marker source and layer
        style.addSource(new GeoJsonSource("selected-marker"));

        // Adding an offset so that the bottom of the blue icon gets fixed to the coordinate, rather than the
        // middle of the icon being fixed to the coordinate point.
        style.addLayer(new SymbolLayer("selected-marker-layer", "selected-marker")
          .withProperties(PropertyFactory.iconImage("my-marker-image"),
            iconAllowOverlap(true),
            iconOffset(new Float[]{0f, -9f})));

        mapboxMap.addOnMapClickListener(IconSizeChangeOnClickActivity.this);
      }
    });
  }

  @Override
  public boolean onMapClick(@NonNull LatLng point) {
    Style style = mapboxMap.getStyle();
    if (style != null) {
      final SymbolLayer selectedMarkerSymbolLayer =
        (SymbolLayer) style.getLayer("selected-marker-layer");

      final PointF pixel = mapboxMap.getProjection().toScreenLocation(point);
      List<Feature> features = mapboxMap.queryRenderedFeatures(pixel, "marker-layer");
      List<Feature> selectedFeature = mapboxMap.queryRenderedFeatures(
        pixel, "selected-marker-layer");

      if (selectedFeature.size() > 0 && markerSelected) {
        return false;
      }

      if (features.isEmpty()) {
        if (markerSelected) {
          deselectMarker(selectedMarkerSymbolLayer);
        }
        return false;
      }

      GeoJsonSource source = style.getSourceAs("selected-marker");
      if (source != null) {
        source.setGeoJson(FeatureCollection.fromFeatures(
          new Feature[]{Feature.fromGeometry(features.get(0).geometry())}));
      }

      if (markerSelected) {
        deselectMarker(selectedMarkerSymbolLayer);
      }
      if (features.size() > 0) {
        selectMarker(selectedMarkerSymbolLayer);
      }
    }
    return true;
  }

  private void selectMarker(final SymbolLayer iconLayer) {
    markerAnimator = new ValueAnimator();
    markerAnimator.setObjectValues(1f, 2f);
    markerAnimator.setDuration(300);
    markerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

      @Override
      public void onAnimationUpdate(ValueAnimator animator) {
        iconLayer.setProperties(
          PropertyFactory.iconSize((float) animator.getAnimatedValue())
        );
      }
    });
    markerAnimator.start();
    markerSelected = true;
  }

  private void deselectMarker(final SymbolLayer iconLayer) {
    markerAnimator.setObjectValues(2f, 1f);
    markerAnimator.setDuration(300);
    markerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

      @Override
      public void onAnimationUpdate(ValueAnimator animator) {
        iconLayer.setProperties(
          PropertyFactory.iconSize((float) animator.getAnimatedValue())
        );
      }
    });
    markerAnimator.start();
    markerSelected = false;
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
    if (mapboxMap != null) {
      mapboxMap.removeOnMapClickListener(this);
    }
    if (markerAnimator != null) {
      markerAnimator.cancel();
    }
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}
