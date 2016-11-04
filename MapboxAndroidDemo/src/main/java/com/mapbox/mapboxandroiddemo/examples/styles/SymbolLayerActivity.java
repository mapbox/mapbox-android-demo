package com.mapbox.mapboxandroiddemo.examples.styles;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.MapboxAccountManager;
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

public class SymbolLayerActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener {

  private MapView mapView;
  private List<Feature> routeCoordinates;
  private boolean markerSelected = false;
  private MapboxMap mapboxMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    MapboxAccountManager.start(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the account manager
    setContentView(R.layout.activity_symbol_layer);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {

    this.mapboxMap = mapboxMap;

    FeatureCollection emptySource = FeatureCollection.fromFeatures(new Feature[]{});
    Source selectedMarkerSource = new GeoJsonSource("selected-marker", emptySource);
    mapboxMap.addSource(selectedMarkerSource);

    SymbolLayer selectedMarker = new SymbolLayer("selected-marker-layer", "selected-marker")
      .withProperties(PropertyFactory.iconImage("my-marker-image"));
    mapboxMap.addLayer(selectedMarker);

    Bitmap infoWindowBackground = BitmapFactory.decodeResource(SymbolLayerActivity.this.getResources(), R.drawable.mapbox_infowindow_icon_bg);

    // Add the marker image to map
    mapboxMap.addImage("info-window-bg", infoWindowBackground);

    Source infoWindow = new GeoJsonSource("info-window", emptySource);
    mapboxMap.addSource(infoWindow);

    SymbolLayer infoWindowLayer = new SymbolLayer("info-window", "info-window")
      .withProperties(
        PropertyFactory.textLineHeight(1f),
        PropertyFactory.textSize(16f),
        PropertyFactory.iconTextFit("both"),
        PropertyFactory.iconImage("info-window-bg"),
        PropertyFactory.iconTextFitPadding(new Float[]{16f, 20f, 20f, 20f}),
        PropertyFactory.textIgnorePlacement(true),
        PropertyFactory.textPadding(0f),
        PropertyFactory.textAnchor("top"),
        PropertyFactory.textField("My info window :)"),
//        PropertyFactory.textMaxWidth(8f),
        PropertyFactory.iconIgnorePlacement(true),
        PropertyFactory.textOffset(new Float[]{0f, -2f})
      );
    mapboxMap.addLayer(infoWindowLayer);


    routeCoordinates = new ArrayList<>();
    routeCoordinates.add(Feature.fromGeometry(Point.fromCoordinates(Position.fromCoordinates(-71.0566, 42.3597))));
    routeCoordinates.add(Feature.fromGeometry(Point.fromCoordinates(Position.fromCoordinates(-71.0796, 42.3668))));
    routeCoordinates.add(Feature.fromGeometry(Point.fromCoordinates(Position.fromCoordinates(-71.0358, 42.3675))));
    FeatureCollection featureCollection = FeatureCollection.fromFeatures(routeCoordinates);

    Source geoJsonSource = new GeoJsonSource("marker-source", featureCollection);
    mapboxMap.addSource(geoJsonSource);

    Bitmap icon = BitmapFactory.decodeResource(SymbolLayerActivity.this.getResources(), R.drawable.blue_marker);

    // Add the marker image to map
    mapboxMap.addImage("my-marker-image", icon);

    SymbolLayer markers = new SymbolLayer("marker-layer", "marker-source")
      .withProperties(PropertyFactory.iconImage("my-marker-image"));
    mapboxMap.addLayer(markers);

    mapboxMap.setOnMapClickListener(this);
  }


  @Override
  public void onMapClick(@NonNull LatLng point) {


    SymbolLayer marker = (SymbolLayer) mapboxMap.getLayer("selected-marker-layer");
    SymbolLayer infoWindow = (SymbolLayer) mapboxMap.getLayer("info-window");

    final PointF pixel = mapboxMap.getProjection().toScreenLocation(point);
    List<Feature> features = mapboxMap.queryRenderedFeatures(pixel, "marker-layer");

    if (features.size() <= 0) {
      if (markerSelected) {
        deselectMarker(marker, infoWindow);
      }
      return;
    }

    FeatureCollection featureCollection = FeatureCollection.fromFeatures(
      new Feature[]{Feature.fromGeometry(features.get(0).getGeometry())});
    GeoJsonSource source = mapboxMap.getSourceAs("selected-marker");
    source.setGeoJson(featureCollection);

    GeoJsonSource infoWindowSource = mapboxMap.getSourceAs("info-window");
    infoWindowSource.setGeoJson(featureCollection);

    List<Feature> temporaryMarkers = new ArrayList<>();

    for (Feature feature : routeCoordinates) {
      // Check whether the coordinates match up and if not add them to a temporary list.
      if ((Math.abs(((Point) feature.getGeometry()).getCoordinates().getLongitude()
        - ((Point) features.get(0).getGeometry()).getCoordinates().getLongitude()) >= 0.0001)) {
        temporaryMarkers.add(feature);
      }
    }

    FeatureCollection markerCollection = FeatureCollection.fromFeatures(temporaryMarkers);
    GeoJsonSource markers = mapboxMap.getSourceAs("marker-source");
    markers.setGeoJson(markerCollection);

    if (markerSelected) {
      // TODO Fix animation when a markers currently selected and another one is clicked on

      deselectMarker(marker, infoWindow);
      Toast.makeText(SymbolLayerActivity.this, "Deselected", Toast.LENGTH_LONG).show();
    }
    if (features.size() > 0) {
      selectMarker(marker, infoWindow);
      Toast.makeText(SymbolLayerActivity.this, "Selected", Toast.LENGTH_LONG).show();
    }
  }

  private void selectMarker(final SymbolLayer marker, SymbolLayer infoWindow) {
    infoWindow.setProperties(PropertyFactory.visibility("visible"));

    ValueAnimator markerAnimator = new ValueAnimator();
    markerAnimator.setObjectValues(1f, 180f);
    markerAnimator.setDuration(500);
    markerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

      @Override
      public void onAnimationUpdate(ValueAnimator animator) {

        marker.setProperties(
//          PropertyFactory.iconSize((float) animator.getAnimatedValue()),
          PropertyFactory.iconRotate((float) animator.getAnimatedValue())
        );
      }
    });
    markerAnimator.start();
    markerSelected = true;
  }

  private void deselectMarker(final SymbolLayer marker, SymbolLayer infoWindow) {
    infoWindow.setProperties(PropertyFactory.visibility("none"));

    ValueAnimator markerAnimator = new ValueAnimator();
    markerAnimator.setObjectValues(180f, 1f);
    markerAnimator.setDuration(500);
    markerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

      @Override
      public void onAnimationUpdate(ValueAnimator animator) {

        marker.setProperties(
//          PropertyFactory.iconSize((float) animator.getAnimatedValue()),
          PropertyFactory.iconRotate((float) animator.getAnimatedValue())
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
