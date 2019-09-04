package com.mapbox.mapboxandroiddemo.examples.query;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.BubbleLayout;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

/**
 * Display map property information for a clicked map feature.
 */
public class QueryFeatureActivity extends AppCompatActivity implements OnMapReadyCallback,
  MapboxMap.OnMapClickListener {

  private static final String GEOJSON_SOURCE_ID = "GEOJSON_SOURCE_ID";
  private static final String MARKER_IMAGE_ID = "MARKER_IMAGE_ID";
  private static final String CALLOUT_IMAGE_ID = "CALLOUT_IMAGE_ID";
  private static final String MARKER_LAYER_ID = "MARKER_LAYER_ID";
  private static final String CALLOUT_LAYER_ID = "CALLOUT_LAYER_ID";
  private GeoJsonSource source;
  private MapView mapView;
  private MapboxMap mapboxMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_query_feature);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
    QueryFeatureActivity.this.mapboxMap = mapboxMap;
    mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        setUpData();
        mapboxMap.addOnMapClickListener(QueryFeatureActivity.this);
        Toast.makeText(QueryFeatureActivity.this,
          getString(R.string.click_on_map_instruction), Toast.LENGTH_SHORT).show();
      }
    });
  }

  /**
   * Sets up all of the sources and layers needed for this example
   */
  public void setUpData() {
    if (mapboxMap != null) {
      mapboxMap.getStyle(style -> {
        setupSource(style);
        setUpClickLocationIconImage(style);
        setUpClickLocationMarkerLayer(style);
        setUpInfoWindowLayer(style);
      });
    }
  }

  /**
   * Adds the GeoJSON source to the map
   */
  private void setupSource(@NonNull Style loadedStyle) {
    source = new GeoJsonSource(GEOJSON_SOURCE_ID);
    loadedStyle.addSource(source);
  }

  /**
   * Adds the marker image to the map for use as a SymbolLayer icon
   */
  private void setUpClickLocationIconImage(@NonNull Style loadedStyle) {
    loadedStyle.addImage(MARKER_IMAGE_ID, BitmapFactory.decodeResource(
      this.getResources(), R.drawable.red_marker));
  }

  /**
   * Needed to show the Feature properties info window.
   */
  private void refreshSource(Feature featureAtClickPoint) {
    if (source != null) {
      source.setGeoJson(featureAtClickPoint);
    }
  }

  /**
   * Adds a SymbolLayer to the map to show the click location marker icon.
   */
  private void setUpClickLocationMarkerLayer(@NonNull Style loadedStyle) {
    loadedStyle.addLayer(new SymbolLayer(MARKER_LAYER_ID, GEOJSON_SOURCE_ID)
      .withProperties(
        iconImage(MARKER_IMAGE_ID),
        iconAllowOverlap(true),
        iconIgnorePlacement(true),
        iconOffset(new Float[] {0f, -8f})
      ));
  }

  /**
   * Adds a SymbolLayer to the map to show the Feature properties info window.
   */
  private void setUpInfoWindowLayer(@NonNull Style loadedStyle) {
    loadedStyle.addLayer(new SymbolLayer(CALLOUT_LAYER_ID, GEOJSON_SOURCE_ID)
      .withProperties(
        // show image with id title based on the value of the name feature property
        iconImage(CALLOUT_IMAGE_ID),

        // set anchor of icon to bottom-left
        iconAnchor(ICON_ANCHOR_BOTTOM),

        // prevent the feature property window icon from being visible even
        // if it collides with other previously drawn symbols
        iconAllowOverlap(false),

        // prevent other symbols from being visible even if they collide with the feature property window icon
        iconIgnorePlacement(false),

        // offset the info window to be above the marker
        iconOffset(new Float[] {-2f, -28f})
      ));
  }

  /**
   * This method handles click events for SymbolLayer symbols.
   *
   * @param screenPoint the point on screen clicked
   */
  private boolean handleClickIcon(PointF screenPoint) {
    List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint);
    if (!features.isEmpty()) {
      Feature feature = features.get(0);

      StringBuilder stringBuilder = new StringBuilder();

      if (feature.properties() != null) {
        for (Map.Entry<String, JsonElement> entry : feature.properties().entrySet()) {
          stringBuilder.append(String.format("%s - %s", entry.getKey(), entry.getValue()));
          stringBuilder.append(System.getProperty("line.separator"));
        }
        new GenerateViewIconTask(QueryFeatureActivity.this).execute(FeatureCollection.fromFeature(feature));
      }
    } else {
      Toast.makeText(this, getString(R.string.query_feature_no_properties_found), Toast.LENGTH_SHORT).show();
    }
    return true;
  }

  @Override
  public boolean onMapClick(@NonNull LatLng point) {
    return handleClickIcon(mapboxMap.getProjection().toScreenLocation(point));
  }

  /**
   * Invoked when the bitmap has been generated from a view.
   */
  public void setImageGenResults(HashMap<String, Bitmap> imageMap) {
    if (mapboxMap != null) {
      mapboxMap.getStyle(style -> {
        style.addImages(imageMap);
      });
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

    private final WeakReference<QueryFeatureActivity> activityRef;
    private Feature featureAtMapClickPoint;

    GenerateViewIconTask(QueryFeatureActivity activity) {
      this.activityRef = new WeakReference<>(activity);
    }

    @SuppressWarnings("WrongThread")
    @Override
    protected HashMap<String, Bitmap> doInBackground(FeatureCollection... params) {
      QueryFeatureActivity activity = activityRef.get();
      HashMap<String, Bitmap> imagesMap = new HashMap<>();
      if (activity != null) {
        LayoutInflater inflater = LayoutInflater.from(activity);

        if (params[0].features() != null) {
          featureAtMapClickPoint = params[0].features().get(0);

          StringBuilder stringBuilder = new StringBuilder();

          BubbleLayout bubbleLayout = (BubbleLayout) inflater.inflate(
            R.layout.activity_query_feature_window_symbol_layer, null);

          TextView titleTextView = bubbleLayout.findViewById(R.id.info_window_title);
          titleTextView.setText(activity.getString(R.string.query_feature_marker_title));

          if (featureAtMapClickPoint.properties() != null) {
            for (Map.Entry<String, JsonElement> entry : featureAtMapClickPoint.properties().entrySet()) {
              stringBuilder.append(String.format("%s - %s", entry.getKey(), entry.getValue()));
              stringBuilder.append(System.getProperty("line.separator"));
            }

            TextView propertiesListTextView = bubbleLayout.findViewById(R.id.info_window_feature_properties_list);
            propertiesListTextView.setText(stringBuilder.toString());

            int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            bubbleLayout.measure(measureSpec, measureSpec);

            float measuredWidth = bubbleLayout.getMeasuredWidth();

            bubbleLayout.setArrowPosition(measuredWidth / 2 - 5);

            Bitmap bitmap = QueryFeatureActivity.SymbolGenerator.generate(bubbleLayout);
            imagesMap.put(CALLOUT_IMAGE_ID, bitmap);
          }
        }
      }

      return imagesMap;
    }

    @Override
    protected void onPostExecute(HashMap<String, Bitmap> bitmapHashMap) {
      super.onPostExecute(bitmapHashMap);
      QueryFeatureActivity activity = activityRef.get();
      if (activity != null && bitmapHashMap != null) {
        activity.setImageGenResults(bitmapHashMap);
        activity.refreshSource(featureAtMapClickPoint);
      }
    }

  }

  /**
   * Utility class to generate Bitmaps for Symbol.
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
