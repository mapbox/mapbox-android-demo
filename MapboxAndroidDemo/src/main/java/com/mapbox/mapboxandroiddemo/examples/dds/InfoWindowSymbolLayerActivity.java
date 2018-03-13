package com.mapbox.mapboxandroiddemo.examples.dds;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.BubbleLayout;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

/**
 * Use a SymbolLayer to show a BubbleLayout above a SymbolLayer icon. This is a more performant
 * way to show the BubbleLayout that appears when using the MapboxMap.addMarker() method.
 */
public class InfoWindowSymbolLayerActivity extends AppCompatActivity implements
  OnMapReadyCallback, MapboxMap.OnMapClickListener {

  private static final String MARKER_IMAGE_ID = "MARKER_IMAGE_ID";
  private static final String MARKER_LAYER_ID = "MARKER_LAYER_ID";
  private static final String CALLOUT_LAYER_ID = "CALLOUT_LAYER_ID";
  private static final String PROPERTY_SELECTED = "selected";
  private static final String PROPERTY_NAME = "name";
  private static final String PROPERTY_CAPITAL = "capital";
  private MapView mapView;
  private MapboxMap mapboxMap;
  private String geojsonSourceId = "geojsonSourceId";
  private GeoJsonSource source;
  private FeatureCollection featureCollection;
  private HashMap<String, View> viewMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_info_window_symbol_layer);

    // Initialize the map view
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    new LoadGeoJsonDataTask(this).execute();
    mapboxMap.addOnMapClickListener(this);
  }

  @Override
  public void onMapClick(@NonNull LatLng point) {
    handleClickIcon(mapboxMap.getProjection().toScreenLocation(point));
  }

  /**
   * Sets up all of the sources and layers needed for this example
   *
   * @param collection the FeatureCollection to set equal to the globally-declared FeatureCollection
   */
  public void setUpData(final FeatureCollection collection) {
    if (mapboxMap == null) {
      return;
    }
    featureCollection = collection;
    setupSource();
    setUpImage();
    setUpMarkerLayer();
    setUpInfoWindowLayer();
  }

  /**
   * Adds the GeoJSON source to the map
   */
  private void setupSource() {
    source = new GeoJsonSource(geojsonSourceId, featureCollection);
    mapboxMap.addSource(source);
  }

  /**
   * Adds the marker image to the map for use as a SymbolLayer icon
   */
  private void setUpImage() {
    Bitmap icon = BitmapFactory.decodeResource(
      this.getResources(), R.drawable.red_marker);
    mapboxMap.addImage(MARKER_IMAGE_ID, icon);
  }

  /**
   * Updates the display of data on the map after the FeatureCollection has been modified
   */
  private void refreshSource() {
    if (source != null && featureCollection != null) {
      source.setGeoJson(featureCollection);
    }
  }

  /**
   * Setup a layer with maki icons, eg. west coast city.
   */
  private void setUpMarkerLayer() {
    mapboxMap.addLayer(new SymbolLayer(MARKER_LAYER_ID, geojsonSourceId)
      .withProperties(
        iconImage(MARKER_IMAGE_ID),
        iconAllowOverlap(true)
      ));
  }

  /**
   * Setup a layer with Android SDK call-outs
   * <p>
   * name of the feature is used as key for the iconImage
   * </p>
   */
  private void setUpInfoWindowLayer() {
    mapboxMap.addLayer(new SymbolLayer(CALLOUT_LAYER_ID, geojsonSourceId)
      .withProperties(
        /* show image with id title based on the value of the name feature property */
        iconImage("{name}"),

        /* set anchor of icon to bottom-left */
        iconAnchor(ICON_ANCHOR_BOTTOM),

        /* all info window and marker image to appear at the same time*/
        iconAllowOverlap(true),

        /* offset the info window to be above the marker */
        iconOffset(new Float[] {-2f, -25f})
      )
      /* add a filter to show only when selected feature property is true */
      .withFilter(eq((get(PROPERTY_SELECTED)), literal(true))));
  }

  /**
   * This method handles click events for SymbolLayer symbols.
   * <p>
   * When a SymbolLayer icon is clicked, we moved that feature to the selected state.
   * </p>
   *
   * @param screenPoint the point on screen clicked
   */
  private void handleClickIcon(PointF screenPoint) {
    List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, MARKER_LAYER_ID);
    if (!features.isEmpty()) {
      String name = features.get(0).getStringProperty(PROPERTY_NAME);
      List<Feature> featureList = featureCollection.features();
      for (int i = 0; i < featureList.size(); i++) {
        if (featureList.get(i).getStringProperty(PROPERTY_NAME).equals(name)) {
          if (featureSelectStatus(i)) {
            setFeatureSelectState(featureList.get(i), false);
          } else {
            setSelected(i);
          }
        }
      }
    }
  }

  /**
   * Set a feature selected state.
   *
   * @param index the index of selected feature
   */
  private void setSelected(int index) {
    Feature feature = featureCollection.features().get(index);
    setFeatureSelectState(feature, true);
    refreshSource();
  }

  /**
   * Selects the state of a feature
   *
   * @param feature the feature to be selected.
   */
  private void setFeatureSelectState(Feature feature, boolean selectedState) {
    feature.properties().addProperty(PROPERTY_SELECTED, selectedState);
    refreshSource();
  }

  /**
   * Checks whether a Feature's boolean "selected" property is true or false
   *
   * @param index the specific Feature's index position in the FeatureCollection's list of Features.
   * @return true if "selected" is true. False if the boolean property is false.
   */
  private boolean featureSelectStatus(int index) {
    if (featureCollection == null) {
      return false;
    }
    return featureCollection.features().get(index).getBooleanProperty(PROPERTY_SELECTED);
  }

  /**
   * Invoked when the bitmaps have been generated from a view.
   */
  public void setImageGenResults(HashMap<String, View> viewMap, HashMap<String, Bitmap> imageMap) {
    if (mapboxMap != null) {
      // calling addImages is faster as separate addImage calls for each bitmap.
      mapboxMap.addImages(imageMap);
    }
    // need to store reference to views to be able to use them as hitboxes for click events.
    this.viewMap = viewMap;
  }

  /**
   * AsyncTask to load data from the assets folder.
   */
  private static class LoadGeoJsonDataTask extends AsyncTask<Void, Void, FeatureCollection> {

    private final WeakReference<InfoWindowSymbolLayerActivity> activityRef;

    LoadGeoJsonDataTask(InfoWindowSymbolLayerActivity activity) {
      this.activityRef = new WeakReference<>(activity);
    }

    @Override
    protected FeatureCollection doInBackground(Void... params) {
      InfoWindowSymbolLayerActivity activity = activityRef.get();

      if (activity == null) {
        return null;
      }

      String geoJson = loadGeoJsonFromAsset(activity, "us_west_coast.geojson");
      return FeatureCollection.fromJson(geoJson);
    }

    @Override
    protected void onPostExecute(FeatureCollection featureCollection) {
      super.onPostExecute(featureCollection);
      InfoWindowSymbolLayerActivity activity = activityRef.get();
      if (featureCollection == null || activity == null) {
        return;
      }

      // This example runs on the premise that each GeoJSON Feature has a "selected" property,
      // with a boolean value. If your data's Features don't have this boolean property,
      // add it to the FeatureCollection 's features with the following code:
      for (Feature singleFeature : featureCollection.features()) {
        singleFeature.addBooleanProperty(PROPERTY_SELECTED, false);
      }

      activity.setUpData(featureCollection);
      new GenerateViewIconTask(activity).execute(featureCollection);
    }

    static String loadGeoJsonFromAsset(Context context, String filename) {
      try {
        // Load GeoJSON file from local asset folder
        InputStream is = context.getAssets().open(filename);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        return new String(buffer, "UTF-8");
      } catch (Exception exception) {
        throw new RuntimeException(exception);
      }
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

    private final HashMap<String, View> viewMap = new HashMap<>();
    private final WeakReference<InfoWindowSymbolLayerActivity> activityRef;
    private final boolean refreshSource;

    GenerateViewIconTask(InfoWindowSymbolLayerActivity activity, boolean refreshSource) {
      this.activityRef = new WeakReference<>(activity);
      this.refreshSource = refreshSource;
    }

    GenerateViewIconTask(InfoWindowSymbolLayerActivity activity) {
      this(activity, false);
    }

    @SuppressWarnings("WrongThread")
    @Override
    protected HashMap<String, Bitmap> doInBackground(FeatureCollection... params) {
      InfoWindowSymbolLayerActivity activity = activityRef.get();
      if (activity != null) {
        HashMap<String, Bitmap> imagesMap = new HashMap<>();
        LayoutInflater inflater = LayoutInflater.from(activity);

        FeatureCollection featureCollection = params[0];

        for (Feature feature : featureCollection.features()) {

          BubbleLayout bubbleLayout = (BubbleLayout)
            inflater.inflate(R.layout.symbol_layer_info_window_layout_callout, null);

          String name = feature.getStringProperty(PROPERTY_NAME);
          TextView titleTextView = bubbleLayout.findViewById(R.id.info_window_title);
          titleTextView.setText(name);

          String style = feature.getStringProperty(PROPERTY_CAPITAL);
          TextView descriptionTextView = bubbleLayout.findViewById(R.id.info_window_description);
          descriptionTextView.setText(
            String.format(activity.getString(R.string.capital), style));

          int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
          bubbleLayout.measure(measureSpec, measureSpec);

          int measuredWidth = bubbleLayout.getMeasuredWidth();

          bubbleLayout.setArrowPosition(measuredWidth / 2 - 5);

          Bitmap bitmap = SymbolGenerator.generate(bubbleLayout);
          imagesMap.put(name, bitmap);
          viewMap.put(name, bubbleLayout);
        }

        return imagesMap;
      } else {
        return null;
      }
    }

    @Override
    protected void onPostExecute(HashMap<String, Bitmap> bitmapHashMap) {
      super.onPostExecute(bitmapHashMap);
      InfoWindowSymbolLayerActivity activity = activityRef.get();
      if (activity != null && bitmapHashMap != null) {
        activity.setImageGenResults(viewMap, bitmapHashMap);
        if (refreshSource) {
          activity.refreshSource();
        }
      }
      Toast.makeText(activity, R.string.tap_on_marker_instruction, Toast.LENGTH_SHORT).show();
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
  protected void onStart() {
    super.onStart();
    mapView.onStart();
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
  protected void onStop() {
    super.onStop();
    mapView.onStop();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
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
}