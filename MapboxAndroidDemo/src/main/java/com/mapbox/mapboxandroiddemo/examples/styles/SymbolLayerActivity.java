package com.mapbox.mapboxandroiddemo.examples.styles;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
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
import java.util.List;

/**
 * Display markers on the map by adding a symbol layer
 */
public class SymbolLayerActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener {

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
    setContentView(R.layout.activity_symbol_layer);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {

    this.mapboxMap = mapboxMap;

    List<Feature> markerCoordinates = new ArrayList<>();
    markerCoordinates.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(-71.065634, 42.354950))) // Boston Common Park
    );
    markerCoordinates.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(-71.097293, 42.346645))) // Fenway Park
    );
    markerCoordinates.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(-71.053694, 42.363725))) // The Paul Revere House
    );
    FeatureCollection featureCollection = FeatureCollection.fromFeatures(markerCoordinates);

    Source geoJsonSource = new GeoJsonSource("marker-source", featureCollection);
    mapboxMap.addSource(geoJsonSource);

    Bitmap icon = BitmapFactory.decodeResource(SymbolLayerActivity.this.getResources(), R.drawable.blue_marker_view);

    // Add the marker image to map
    mapboxMap.addImage("my-marker-image", icon);

    SymbolLayer markers = new SymbolLayer("marker-layer", "marker-source")
      .withProperties(PropertyFactory.iconImage("my-marker-image"));
    mapboxMap.addLayer(markers);

    // Add the selected marker source and layer
    FeatureCollection emptySource = FeatureCollection.fromFeatures(new Feature[]{});
    Source selectedMarkerSource = new GeoJsonSource("selected-marker", emptySource);
    mapboxMap.addSource(selectedMarkerSource);

    SymbolLayer selectedMarker = new SymbolLayer("selected-marker-layer", "selected-marker")
      .withProperties(PropertyFactory.iconImage("my-marker-image"));
    mapboxMap.addLayer(selectedMarker);

    mapboxMap.setOnMapClickListener(this);
  }

  @Override
  public void onMapClick(@NonNull LatLng point) {

    final SymbolLayer marker = (SymbolLayer) mapboxMap.getLayer("selected-marker-layer");

    final PointF pixel = mapboxMap.getProjection().toScreenLocation(point);
    List<Feature> features = mapboxMap.queryRenderedFeatures(pixel, "marker-layer");
    List<Feature> selectedFeature = mapboxMap.queryRenderedFeatures(pixel, "selected-marker-layer");

    if (selectedFeature.size() > 0 && markerSelected) {
      return;
    }

    if (features.isEmpty()) {
      if (markerSelected) {
        deselectMarker(marker);
      }
      return;
    }

    FeatureCollection featureCollection = FeatureCollection.fromFeatures(
      new Feature[]{Feature.fromGeometry(features.get(0).getGeometry())});
    GeoJsonSource source = mapboxMap.getSourceAs("selected-marker");
    if (source != null) {
      source.setGeoJson(featureCollection);
    }

    if (markerSelected) {
      deselectMarker(marker);
    }
    if (features.size() > 0) {
      selectMarker(marker);
    }
  }

  private void selectMarker(final SymbolLayer marker) {
    ValueAnimator markerAnimator = new ValueAnimator();
    markerAnimator.setObjectValues(1f, 2f);
    markerAnimator.setDuration(300);
    markerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

      @Override
      public void onAnimationUpdate(ValueAnimator animator) {
        marker.setProperties(
          PropertyFactory.iconSize((float) animator.getAnimatedValue())
        );
      }
    });
    markerAnimator.start();
    markerSelected = true;
  }

  private void deselectMarker(final SymbolLayer marker) {
    ValueAnimator markerAnimator = new ValueAnimator();
    markerAnimator.setObjectValues(2f, 1f);
    markerAnimator.setDuration(300);
    markerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

      @Override
      public void onAnimationUpdate(ValueAnimator animator) {
        marker.setProperties(
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
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}