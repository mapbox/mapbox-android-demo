package com.mapbox.mapboxandroiddemo.labs;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InfoWindowSymbolLayerActivity extends AppCompatActivity implements
  OnMapReadyCallback, MapboxMap.OnMapClickListener {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private boolean markerSelected = false;
  private FeatureCollection featureCollection;
  private HashMap<String, View> viewMap;
  private GeoJsonSource geoJsonSource;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_info_window_symbol_layer);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {

    this.mapboxMap = mapboxMap;

    List<Feature> markerCoordinates = new ArrayList<>();
    markerCoordinates.add(Feature.fromGeometry(
      Point.fromCoordinates(Position.fromCoordinates(-66.9891357421875, 10.466205555063882))) // Caracas, Venezuela
    );

    featureCollection = FeatureCollection.fromFeatures(markerCoordinates);

    geoJsonSource = new GeoJsonSource("marker-source", featureCollection);
    mapboxMap.addSource(geoJsonSource);

    Bitmap icon = BitmapFactory.decodeResource(
      InfoWindowSymbolLayerActivity.this.getResources(), R.drawable.red_marker);

    // Add the marker image to map
    mapboxMap.addImage("my-marker-image", icon);

    SymbolLayer markers = new SymbolLayer("marker-layer", "marker-source")
      .withProperties(PropertyFactory.iconImage("my-marker-image"));
    mapboxMap.addLayer(markers);

    // Add the selected marker source and layer
    FeatureCollection emptySource = FeatureCollection.fromFeatures(new Feature[] {});
    Source selectedMarkerSource = new GeoJsonSource("selected-marker", emptySource);
    mapboxMap.addSource(selectedMarkerSource);

    SymbolLayer selectedMarker = new SymbolLayer("selected-marker-layer", "selected-marker")
      .withProperties(PropertyFactory.iconImage("my-marker-image"));
    mapboxMap.addLayer(selectedMarker);

    mapboxMap.addOnMapClickListener(this);

    new GenerateViewIconTask(this, false, mapboxMap,
      geoJsonSource).execute(featureCollection);
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
      new Feature[] {Feature.fromGeometry(features.get(0).getGeometry())});
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

  /**
   * This method handles click events for callout symbols.
   * <p>
   * It creates a hit rectangle based on the the textView, offsets that rectangle to the location
   * of the symbol on screen and hit tests that with the screen point.
   * </p>
   *
   * @param feature           the feature that was clicked
   * @param screenPoint       the point on screen clicked
   * @param symbolScreenPoint the point of the symbol on screen
   */
  private void handleClickCallout(Feature feature, PointF screenPoint, PointF symbolScreenPoint) {
    View view = viewMap.get(feature.getStringProperty(PROPERTY_TITLE));
    View textContainer = view.findViewById(R.id.text_container);

    // create hitbox for textView
    Rect hitRectText = new Rect();
    textContainer.getHitRect(hitRectText);

    // move hitbox to location of symbol
    hitRectText.offset((int) symbolScreenPoint.x, (int) symbolScreenPoint.y);

    // offset vertically to match anchor behaviour
    hitRectText.offset(0, -view.getMeasuredHeight());

    // hit test if clicked point is in textview hit box
    if (!hitRectText.contains((int) screenPoint.x, (int) screenPoint.y)) {
      List<Feature> featureList = featureCollection.getFeatures();
      for (int i = 0; i < featureList.size(); i++) {
        if (featureList.get(i).getStringProperty(PROPERTY_TITLE).equals(feature.getStringProperty(PROPERTY_TITLE))) {
          toggleFavourite(i);
        }
      }
    }
  }

  /**
   * Set the favourite state of a feature based on the index.
   *
   * @param index the index of the feature to favourite/de-favourite
   */
  private void toggleFavourite(int index) {
    Feature feature = featureCollection.getFeatures().get(index);
    String title = feature.getStringProperty(PROPERTY_TITLE);
    boolean currentState = feature.getBooleanProperty(PROPERTY_FAVOURITE);
    feature.getProperties().addProperty(PROPERTY_FAVOURITE, !currentState);
    View view = viewMap.get(title);

    Bitmap bitmap = SymbolGenerator.generate(view);
    mapboxMap.addImage(title, bitmap);
    refreshSource();
  }

  private void refreshSource() {
    if (geoJsonSource != null && featureCollection != null) {
      geoJsonSource.setGeoJson(featureCollection);
    }
  }

  /**
   * AsyncTask to generate Bitmap from Views to be used as iconImage in a SymbolLayer.
   * <p>
   * Call be optionally be called to update the underlying data source after execution.
   * </p>
   * <p>
   * Generating Views on background thread since we are not going to be adding them to the view hierarchy.
   * </p>
   */
  private static class GenerateViewIconTask extends AsyncTask<FeatureCollection, Void, HashMap<String, Bitmap>> {

    private HashMap<String, View> viewMap = new HashMap<>();
    private final WeakReference<Activity> activityRef;
    private final boolean refreshSource;
    private final MapboxMap mapboxMapForViewGeneration;
    private String TAG = "RegularMapFragment";
    private GeoJsonSource source;
    private FeatureCollection asyncFeatureCollection;


    GenerateViewIconTask(Activity activity, boolean refreshSource,
                         MapboxMap map, GeoJsonSource source) {
      this.activityRef = new WeakReference<>(activity);
      this.refreshSource = refreshSource;
      this.mapboxMapForViewGeneration = map;
      this.source = source;
    }

    @SuppressWarnings("WrongThread")
    @Override
    protected HashMap<String, Bitmap> doInBackground(FeatureCollection... params) {

      if (activityRef.get() != null) {
        HashMap<String, Bitmap> imagesMap = new HashMap<>();
        LayoutInflater inflater = LayoutInflater.from(activityRef.get());
        asyncFeatureCollection = params[0];

        for (Feature feature : asyncFeatureCollection.getFeatures()) {
          View view = inflater.inflate(R.layout.symbol_layer_info_window_layout_callout, null);

          String featureRankNum = feature.getStringProperty(CALLOUT_LAYER_BUBBLE_RANK);

          TextView titleNumTextView = view.findViewById(R.id.symbol_layer_info_window_layout_callout_title);
          titleNumTextView.setText(featureRankNum);

          TextView descriptionNumTextView = view.findViewById(R.id.symbol_layer_info_window_layout_callout_description);
          descriptionNumTextView.setText(featureRankNum);

          Bitmap bitmap = SymbolGenerator.generate(view);
          imagesMap.put(featureRankNum, bitmap);
          viewMap.put(featureRankNum, view);
        }

        return imagesMap;
      } else {
        return null;
      }
    }

    @Override
    protected void onPostExecute(HashMap<String, Bitmap> bitmapHashMap) {
      super.onPostExecute(bitmapHashMap);
      if (bitmapHashMap != null) {
        setImageGenResults(viewMap, bitmapHashMap);
        if (refreshSource) {
          refreshSource();
        }
      }
    }

    /**
     * Invoked when the bitmaps have been generated from a view.
     */
    public void setImageGenResults(HashMap<String, View> viewMap, HashMap<String, Bitmap> imageMap) {
      if (mapboxMapForViewGeneration != null) {
        // calling addImages is faster as separate addImage calls for each bitmap.
        mapboxMapForViewGeneration.addImages(imageMap);
        Log.d(TAG, "setImageGenResults: images added");

      }
      // need to store reference to views to be able to use them as hitboxes for click events.
      this.viewMap = viewMap;
    }

    private void refreshSource() {
      if (source != null && asyncFeatureCollection != null) {
        source.setGeoJson(asyncFeatureCollection);
      }
    }
  }

  /**
   * Utility class to generate Bitmaps for Symbol.
   * <p>
   * Bitmaps can be added to the map with {@link com.mapbox.mapboxsdk.maps.MapboxMap#addImage(String, Bitmap)}
   * </p>
   */
  private static class SymbolGenerator {

    /**
     * Generate a Bitmap from an Android SDK View.
     *
     * @param view the View to be drawn to a Bitmap
     * @return the generated bitmap
     */
    static Bitmap generate(@NonNull View view) {
      int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
      view.measure(measureSpec, measureSpec);

      int measuredWidth = view.getMeasuredWidth();
      int measuredHeight = view.getMeasuredHeight();

      view.layout(0, 0, measuredWidth, measuredHeight);
      Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
      bitmap.eraseColor(Color.TRANSPARENT);
      Canvas canvas = new Canvas(bitmap);
      view.draw(canvas);
      return bitmap;
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