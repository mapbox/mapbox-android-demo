package com.mapbox.mapboxandroiddemo.labs;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.mapboxsdk.style.sources.TileSet;
import com.mapbox.mapboxsdk.style.sources.VectorSource;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.mapbox.mapboxsdk.style.expressions.Expression.all;
import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.exponential;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.gte;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.lt;
import static com.mapbox.mapboxsdk.style.expressions.Expression.match;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.toNumber;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

public class SymbolLayerMapillaryActivity extends AppCompatActivity implements OnMapReadyCallback,
  MapboxMap.OnMapClickListener {
  private static final String SOURCE_ID = "mapbox.poi";
  private static final String MAKI_LAYER_ID = "mapbox.poi.maki";
  private static final String LOADING_LAYER_ID = "mapbox.poi.loading";
  private static final String CALLOUT_LAYER_ID = "mapbox.poi.callout";

  private static final String PROPERTY_SELECTED = "selected";
  private static final String PROPERTY_LOADING = "loading";
  private static final String PROPERTY_LOADING_PROGRESS = "loading_progress";
  private static final String PROPERTY_TITLE = "title";
  private static final String PROPERTY_FAVOURITE = "favourite";
  private static final String PROPERTY_DESCRIPTION = "description";
  private static final String PROPERTY_POI = "poi";
  private static final String PROPERTY_STYLE = "style";

  private static final long CAMERA_ANIMATION_TIME = 1950;
  private static final float LOADING_CIRCLE_RADIUS = 60;
  private static final int LOADING_PROGRESS_STEPS = 25; //number of steps in a progress animation
  private static final int LOADING_STEP_DURATION = 50; //duration between each step

  private MapView mapView;
  private MapboxMap mapboxMap;
  private RecyclerView recyclerView;

  private GeoJsonSource source;
  private FeatureCollection featureCollection;
  private HashMap<String, View> viewMap;
  private AnimatorSet animatorSet;

  private LoadMapillaryDataTask loadMapillaryDataTask;

  @ActivityStep
  private int currentStep;

  @Retention(RetentionPolicy.SOURCE)
  @IntDef( {STEP_INITIAL, STEP_LOADING, STEP_READY})
  public @interface ActivityStep {
  }

  private static final int STEP_INITIAL = 0;
  private static final int STEP_LOADING = 1;
  private static final int STEP_READY = 2;

  private static final Map<Integer, Double> stepZoomMap = new HashMap<>();

  static {
    stepZoomMap.put(STEP_INITIAL, 11.0);
    stepZoomMap.put(STEP_LOADING, 13.5);
    stepZoomMap.put(STEP_READY, 18.0);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_symbol_layer_mapillary);

    recyclerView = findViewById(R.id.rv_on_top_of_map);

    // Initialize the map view
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    mapboxMap.getUiSettings().setCompassEnabled(false);
    mapboxMap.getUiSettings().setLogoEnabled(false);
    mapboxMap.getUiSettings().setAttributionEnabled(false);
    new LoadPoiDataTask(this).execute();
    mapboxMap.addOnMapClickListener(this);
  }

  @Override
  public void onMapClick(@NonNull LatLng point) {
    PointF screenPoint = mapboxMap.getProjection().toScreenLocation(point);
    List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, CALLOUT_LAYER_ID);
    if (!features.isEmpty()) {
      // we received a click event on the callout layer
      Feature feature = features.get(0);
      PointF symbolScreenPoint = mapboxMap.getProjection().toScreenLocation(convertToLatLng(feature));
      handleClickCallout(feature, screenPoint, symbolScreenPoint);
    } else {
      // we didn't find a click event on callout layer, try clicking maki layer
      handleClickIcon(screenPoint);
    }
  }

  public void setupData(final FeatureCollection collection) {
    if (mapboxMap == null) {
      return;
    }

    featureCollection = collection;
    setupSource();
    setupMakiLayer();
    setupLoadingLayer();
    setupCalloutLayer();
    setupRecyclerView();
    hideLabelLayers();
    setupMapillaryTiles();
  }

  private void setupSource() {
    source = new GeoJsonSource(SOURCE_ID, featureCollection);
    mapboxMap.addSource(source);
  }

  private void refreshSource() {
    if (source != null && featureCollection != null) {
      source.setGeoJson(featureCollection);
    }
  }

  /**
   * Setup a layer with maki icons, eg. restaurant.
   */
  private void setupMakiLayer() {
    mapboxMap.addLayer(new SymbolLayer(MAKI_LAYER_ID, SOURCE_ID)
      .withProperties(
        /* show maki icon based on the value of poi feature property
         * https://www.mapbox.com/maki-icons/
         */
        iconImage("{poi}-15"),

        /* allows show all icons */
        iconAllowOverlap(true),

        /* when feature is in selected state, grow icon */
        iconSize(match(get(PROPERTY_SELECTED), literal(1.0f),
          stop(true, 1.5f))))
    );
  }

  /**
   * Setup layer indicating that there is an ongoing progress.
   */
  private void setupLoadingLayer() {
    mapboxMap.addLayerBelow(new CircleLayer(LOADING_LAYER_ID, SOURCE_ID)
        .withProperties(
          circleRadius(interpolate(exponential(1), get(PROPERTY_LOADING_PROGRESS), getLoadingAnimationStops())),
          circleColor(Color.GRAY),
          circleOpacity(0.6f)
        )
        /*.withFilter(eq(PROPERTY_LOADING, true)),*/
        .withFilter(eq((get(PROPERTY_LOADING)), literal(0))),
      MAKI_LAYER_ID
    );
  }

  private Expression.Stop[] getLoadingAnimationStops() {
    List<Expression.Stop> stops = new ArrayList<>();
    for (int i = 0; i < LOADING_PROGRESS_STEPS; i++) {
      stops.add(stop(i, LOADING_CIRCLE_RADIUS * i / LOADING_PROGRESS_STEPS));
    }

    return stops.toArray(new Expression.Stop[LOADING_PROGRESS_STEPS]);
  }

  /**
   * Setup a layer with Android SDK call-outs
   * <p>
   * title of the feature is used as key for the iconImage
   * </p>
   */
  private void setupCalloutLayer() {
    mapboxMap.addLayer(new SymbolLayer(CALLOUT_LAYER_ID, SOURCE_ID)
      .withProperties(
        /* show image with id title based on the value of the title feature property */
        iconImage("{title}"),

        /* set anchor of icon to bottom-left */
        iconAnchor("bottom-left"),

        /* offset icon slightly to match bubble layout */
        iconOffset(new Float[] {-20.0f, -10.0f})
      )

      /* add a filter to show only when selected feature property is true */
      .withFilter(eq((get(PROPERTY_SELECTED)), literal(true))));
  }

  private void setupRecyclerView() {
    RecyclerView.Adapter adapter = new LocationRecyclerViewAdapter(this, featureCollection);
    final LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setItemAnimator(new DefaultItemAnimator());
    recyclerView.setAdapter(adapter);
    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        if (newState == SCROLL_STATE_IDLE) {
          int index = layoutManager.findFirstVisibleItemPosition();
          setSelected(index, false);
        }
      }
    });
    SnapHelper snapHelper = new PagerSnapHelper();
    snapHelper.attachToRecyclerView(recyclerView);
  }

  private void hideLabelLayers() {
    String id;
    for (Layer layer : mapboxMap.getLayers()) {
      id = layer.getId();
      if (id.startsWith("place") || id.startsWith("poi") || id.startsWith("marine") || id.startsWith("road-label")) {
        layer.setProperties(visibility("none"));
      }
    }
  }

  private void setupMapillaryTiles() {
    mapboxMap.addSource(MapillaryTiles.createSource());
    mapboxMap.addLayerBelow(MapillaryTiles.createLineLayer(), LOADING_LAYER_ID);
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

    // hit test if clicked point is in textview hitbox
    if (hitRectText.contains((int) screenPoint.x, (int) screenPoint.y)) {
      // user clicked on text
      String callout = feature.getStringProperty("call-out");
      Toast.makeText(this, callout, Toast.LENGTH_LONG).show();
    } else {
      // user clicked on icon
      List<Feature> featureList = featureCollection.features();
      for (int i = 0; i < featureList.size(); i++) {
        if (featureList.get(i).getStringProperty(PROPERTY_TITLE).equals(feature.getStringProperty(PROPERTY_TITLE))) {
          toggleFavourite(i);
        }
      }
    }
  }

  /**
   * This method handles click events for maki symbols.
   * <p>
   * When a maki symbol is clicked, we moved that feature to the selected state.
   * </p>
   *
   * @param screenPoint the point on screen clicked
   */
  private void handleClickIcon(PointF screenPoint) {
    List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, MAKI_LAYER_ID);
    if (!features.isEmpty()) {
      String title = features.get(0).getStringProperty(PROPERTY_TITLE);
      List<Feature> featureList = featureCollection.features();
      for (int i = 0; i < featureList.size(); i++) {
        if (featureList.get(i).getStringProperty(PROPERTY_TITLE).equals(title)) {
          setSelected(i, true);
        }
      }
    }
  }

  /**
   * Set a feature selected state with the ability to scroll the RecycleViewer to the provided index.
   *
   * @param index      the index of selected feature
   * @param withScroll indicates if the recyclerView position should be updated
   */
  private void setSelected(int index, boolean withScroll) {
    if (recyclerView.getVisibility() == View.GONE) {
      recyclerView.setVisibility(View.VISIBLE);
    }

    deselectAll(false);

    Feature feature = featureCollection.features().get(index);
    selectFeature(feature);
    animateCameraToSelection(feature);
    refreshSource();
    loadMapillaryData(feature);

    if (withScroll) {
      recyclerView.scrollToPosition(index);
    }
  }

  /**
   * Deselects the state of all the features
   */
  private void deselectAll(boolean hideRecycler) {
    for (Feature feature : featureCollection.features()) {
      feature.properties().addProperty(PROPERTY_SELECTED, false);
    }

    if (hideRecycler) {
      recyclerView.setVisibility(View.GONE);
    }
  }

  /**
   * Selects the state of a feature
   *
   * @param feature the feature to be selected.
   */
  private void selectFeature(Feature feature) {
    feature.properties().addProperty(PROPERTY_SELECTED, true);
  }

  private Feature getSelectedFeature() {
    if (featureCollection != null) {
      for (Feature feature : featureCollection.features()) {
        if (feature.getBooleanProperty(PROPERTY_SELECTED)) {
          return feature;
        }
      }
    }

    return null;
  }

  /**
   * Animate camera to a feature.
   *
   * @param feature the feature to animate to
   */
  private void animateCameraToSelection(Feature feature, double newZoom) {
    CameraPosition cameraPosition = mapboxMap.getCameraPosition();

    if (animatorSet != null) {
      animatorSet.cancel();
    }

    animatorSet = new AnimatorSet();
    animatorSet.playTogether(
      createLatLngAnimator(cameraPosition.target, convertToLatLng(feature)),
      createZoomAnimator(cameraPosition.zoom, newZoom),
      createBearingAnimator(cameraPosition.bearing, feature.getNumberProperty("bearing").doubleValue()),
      createTiltAnimator(cameraPosition.tilt, feature.getNumberProperty("tilt").doubleValue())
    );
    animatorSet.start();
  }

  private void animateCameraToSelection(Feature feature) {
    double zoom = feature.getNumberProperty("zoom").doubleValue();
    animateCameraToSelection(feature, zoom);
  }

  private void loadMapillaryData(Feature feature) {
    if (loadMapillaryDataTask != null) {
      loadMapillaryDataTask.cancel(true);
    }

    loadMapillaryDataTask = new LoadMapillaryDataTask(this,
      mapboxMap, Picasso.with(getApplicationContext()), new Handler(), feature);
    loadMapillaryDataTask.execute(50);
  }

  /**
   * Set the favourite state of a feature based on the index.
   *
   * @param index the index of the feature to favourite/de-favourite
   */
  private void toggleFavourite(int index) {
    Feature feature = featureCollection.features().get(index);
    String title = feature.getStringProperty(PROPERTY_TITLE);
    boolean currentState = feature.getBooleanProperty(PROPERTY_FAVOURITE);
    feature.properties().addProperty(PROPERTY_FAVOURITE, !currentState);
    View view = viewMap.get(title);

    ImageView imageView = (ImageView) view.findViewById(R.id.logoView);
    imageView.setImageResource(currentState ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
    Bitmap bitmap = SymbolGenerator.generate(view);
    mapboxMap.addImage(title, bitmap);
    refreshSource();
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

  private void setActivityStep(@ActivityStep int activityStep) {
    Feature selectedFeature = getSelectedFeature();
    double zoom = stepZoomMap.get(activityStep);
    animateCameraToSelection(selectedFeature, zoom);

    currentStep = activityStep;
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

    if (loadMapillaryDataTask != null) {
      loadMapillaryDataTask.cancel(true);
    }
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

  @Override
  public void onBackPressed() {
    if (currentStep == STEP_LOADING || currentStep == STEP_READY) {
      if (loadMapillaryDataTask != null) {
        loadMapillaryDataTask.cancel(true);
      }
      setActivityStep(STEP_INITIAL);
      deselectAll(true);
      refreshSource();
    } else {
      super.onBackPressed();
    }
  }

  private LatLng convertToLatLng(Feature feature) {
    Point symbolPoint = (Point) feature.geometry();
    return new LatLng(symbolPoint.latitude(), symbolPoint.longitude());
  }

  private Animator createLatLngAnimator(LatLng currentPosition, LatLng targetPosition) {
    ValueAnimator latLngAnimator = ValueAnimator.ofObject(new LatLngEvaluator(), currentPosition, targetPosition);
    latLngAnimator.setDuration(CAMERA_ANIMATION_TIME);
    latLngAnimator.setInterpolator(new FastOutSlowInInterpolator());
    latLngAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        mapboxMap.moveCamera(CameraUpdateFactory.newLatLng((LatLng) animation.getAnimatedValue()));
      }
    });
    return latLngAnimator;
  }

  private Animator createZoomAnimator(double currentZoom, double targetZoom) {
    ValueAnimator zoomAnimator = ValueAnimator.ofFloat((float) currentZoom, (float) targetZoom);
    zoomAnimator.setDuration(CAMERA_ANIMATION_TIME);
    zoomAnimator.setInterpolator(new FastOutSlowInInterpolator());
    zoomAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        mapboxMap.moveCamera(CameraUpdateFactory.zoomTo((Float) animation.getAnimatedValue()));
      }
    });
    return zoomAnimator;
  }

  private Animator createBearingAnimator(double currentBearing, double targetBearing) {
    ValueAnimator bearingAnimator = ValueAnimator.ofFloat((float) currentBearing, (float) targetBearing);
    bearingAnimator.setDuration(CAMERA_ANIMATION_TIME);
    bearingAnimator.setInterpolator(new FastOutSlowInInterpolator());
    bearingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        mapboxMap.moveCamera(CameraUpdateFactory.bearingTo((Float) animation.getAnimatedValue()));
      }
    });
    return bearingAnimator;
  }

  private Animator createTiltAnimator(double currentTilt, double targetTilt) {
    ValueAnimator tiltAnimator = ValueAnimator.ofFloat((float) currentTilt, (float) targetTilt);
    tiltAnimator.setDuration(CAMERA_ANIMATION_TIME);
    tiltAnimator.setInterpolator(new FastOutSlowInInterpolator());
    tiltAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        mapboxMap.moveCamera(CameraUpdateFactory.tiltTo((Float) animation.getAnimatedValue()));
      }
    });
    return tiltAnimator;
  }

  /**
   * Helper class to evaluate LatLng objects with a ValueAnimator
   */
  private static class LatLngEvaluator implements TypeEvaluator<LatLng> {

    private final LatLng latLng = new LatLng();

    @Override
    public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
      latLng.setLatitude(startValue.getLatitude()
        + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
      latLng.setLongitude(startValue.getLongitude()
        + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
      return latLng;
    }
  }

  /**
   * AsyncTask to load data from the assets folder.
   */
  private static class LoadPoiDataTask extends AsyncTask<Void, Void, FeatureCollection> {

    private final WeakReference<SymbolLayerMapillaryActivity> activityRef;

    LoadPoiDataTask(SymbolLayerMapillaryActivity activity) {
      this.activityRef = new WeakReference<>(activity);
    }

    @Override
    protected FeatureCollection doInBackground(Void... params) {
      SymbolLayerMapillaryActivity activity = activityRef.get();

      if (activity == null) {
        return null;
      }

      String geoJson = loadGeoJsonFromAsset(activity, "sf_poi.geojson");
      return FeatureCollection.fromJson(geoJson);
    }

    @Override
    protected void onPostExecute(FeatureCollection featureCollection) {
      super.onPostExecute(featureCollection);
      SymbolLayerMapillaryActivity activity = activityRef.get();
      if (featureCollection == null || activity == null) {
        return;
      }
      activity.setupData(featureCollection);
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
    private final WeakReference<SymbolLayerMapillaryActivity> activityRef;
    private final boolean refreshSource;

    GenerateViewIconTask(SymbolLayerMapillaryActivity activity, boolean refreshSource) {
      this.activityRef = new WeakReference<>(activity);
      this.refreshSource = refreshSource;
    }

    GenerateViewIconTask(SymbolLayerMapillaryActivity activity) {
      this(activity, false);
    }

    @SuppressWarnings("WrongThread")
    @Override
    protected HashMap<String, Bitmap> doInBackground(FeatureCollection... params) {
      SymbolLayerMapillaryActivity activity = activityRef.get();
      if (activity != null) {
        HashMap<String, Bitmap> imagesMap = new HashMap<>();
        LayoutInflater inflater = LayoutInflater.from(activity);
        FeatureCollection featureCollection = params[0];

        for (Feature feature : featureCollection.features()) {
          View view = inflater.inflate(R.layout.mapillary_layout_callout, null);

          String name = feature.getStringProperty(PROPERTY_TITLE);
          TextView titleTv = (TextView) view.findViewById(R.id.title);
          titleTv.setText(name);

          String style = feature.getStringProperty(PROPERTY_STYLE);
          TextView styleTv = (TextView) view.findViewById(R.id.style);
          styleTv.setText(style);

          boolean favourite = feature.getBooleanProperty(PROPERTY_FAVOURITE);
          ImageView imageView = (ImageView) view.findViewById(R.id.logoView);
          imageView.setImageResource(favourite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);

          Bitmap bitmap = SymbolGenerator.generate(view);
          imagesMap.put(name, bitmap);
          viewMap.put(name, view);
        }

        return imagesMap;
      } else {
        return null;
      }
    }

    @Override
    protected void onPostExecute(HashMap<String, Bitmap> bitmapHashMap) {
      super.onPostExecute(bitmapHashMap);
      SymbolLayerMapillaryActivity activity = activityRef.get();
      if (activity != null && bitmapHashMap != null) {
        activity.setImageGenResults(viewMap, bitmapHashMap);
        if (refreshSource) {
          activity.refreshSource();
        }
      }
    }
  }

  /**
   * Async task which fetches pictures from around the POI using Mapillary services.
   * https://www.mapillary.com/developer/api-documentation/
   */
  private static class LoadMapillaryDataTask extends AsyncTask<Integer, Void, MapillaryDataLoadResult> {

    static final String URL_IMAGE_PLACEHOLDER = "https://d1cuyjsrcm0gby.cloudfront.net/%s/thumb-320.jpg";
    static final String KEY_UNIQUE_FEATURE = "key";
    static final String TOKEN_UNIQUE_FEATURE = "{" + KEY_UNIQUE_FEATURE + "}";
    static final String ID_SOURCE = "cluster_source";
    static final String ID_LAYER_UNCLUSTERED = "unclustered_layer";
    static final int IMAGE_SIZE = 128;
    static final String API_URL = "https://a.mapillary.com/v3/images/"
      + "?lookat=%f,%f&closeto=%f,%f&radius=%d"
      + "&client_id=bjgtc1FDTnFPaXpxeTZuUDNabmJ5dzozOGE1ODhkMmEyYTkyZTI4";

    private WeakReference<SymbolLayerMapillaryActivity> activityRef;
    private MapboxMap map;
    private Picasso picasso;
    private final Handler progressHandler;
    private int loadingProgress;
    private boolean loadingIncrease = true;
    private Feature feature;

    public LoadMapillaryDataTask(SymbolLayerMapillaryActivity activity, MapboxMap map, Picasso picasso,
                                 Handler progressHandler, Feature feature) {
      this.activityRef = new WeakReference<>(activity);
      this.map = map;
      this.picasso = picasso;
      this.progressHandler = progressHandler;
      this.feature = feature;
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      loadingProgress = 0;
      setLoadingState(true, false);
    }

    @Override
    protected MapillaryDataLoadResult doInBackground(Integer... radius) {
      progressHandler.post(progressRunnable);
      try {
        Thread.sleep(2500); //ensure loading visualisation
      } catch (InterruptedException exception) {
        exception.printStackTrace();
      }
      OkHttpClient okHttpClient = new OkHttpClient();
      try {
        Point poiPosition = (Point) feature.geometry();

        @SuppressLint("DefaultLocale") Request request = new Request.Builder()
          .url(String.format(API_URL,
            poiPosition.longitude(), poiPosition.latitude(),
            poiPosition.longitude(), poiPosition.latitude(),
            radius[0]
          ))
          .build();

        Response response = okHttpClient.newCall(request).execute();
        FeatureCollection featureCollection = FeatureCollection.fromJson(response.body().string());
        MapillaryDataLoadResult mapillaryDataLoadResult = new MapillaryDataLoadResult(featureCollection);
        for (Feature feature : featureCollection.features()) {
          String imageId = feature.getStringProperty(KEY_UNIQUE_FEATURE);
          String imageUrl = String.format(URL_IMAGE_PLACEHOLDER, imageId);
          Bitmap bitmap = picasso.load(imageUrl).resize(IMAGE_SIZE, IMAGE_SIZE).get();

          //cropping bitmap to be circular
          bitmap = getCroppedBitmap(bitmap);

          mapillaryDataLoadResult.add(feature, bitmap);
        }
        return mapillaryDataLoadResult;

      } catch (Exception exception) {
        Timber.e(exception);
      }
      return null;
    }

    @Override
    protected void onPostExecute(MapillaryDataLoadResult mapillaryDataLoadResult) {
      super.onPostExecute(mapillaryDataLoadResult);
      setLoadingState(false, true);
      if (mapillaryDataLoadResult == null) {
        SymbolLayerMapillaryActivity activity = activityRef.get();
        if (activity != null) {
          Toast.makeText(activity, "Error. Unable to load Mapillary data.", Toast.LENGTH_LONG).show();
        }
        return;
      }

      FeatureCollection featureCollection = mapillaryDataLoadResult.mapillaryFeatureCollection;

      Map<Feature, Bitmap> bitmapMap = mapillaryDataLoadResult.bitmapHashMap;
      for (Map.Entry<Feature, Bitmap> featureBitmapEntry : bitmapMap.entrySet()) {
        Feature feature = featureBitmapEntry.getKey();
        String key = feature.getStringProperty(KEY_UNIQUE_FEATURE);
        map.addImage(key, featureBitmapEntry.getValue());
      }

      GeoJsonSource mapillarySource = (GeoJsonSource) map.getSource(ID_SOURCE);
      if (mapillarySource == null) {
        map.addSource(new GeoJsonSource(ID_SOURCE, featureCollection, new GeoJsonOptions()
          .withCluster(true)
          .withClusterMaxZoom(17)
          .withClusterRadius(IMAGE_SIZE / 3)
        ));

        // unclustered
        map.addLayerBelow(new SymbolLayer(ID_LAYER_UNCLUSTERED, ID_SOURCE).withProperties(
          iconImage(TOKEN_UNIQUE_FEATURE),
          iconAllowOverlap(true),
          iconSize(interpolate(exponential(1f), zoom(),
            stop(18, 1.7f),
            stop(17, 1.4f),
            stop(16, 1.1f),
            stop(15, 0.8f),
            stop(12, 0.0f)))), MAKI_LAYER_ID);

        // clustered
        int[][] layers = new int[][] {
          new int[] {20, Color.RED},
          new int[] {10, Color.BLUE},
          new int[] {0, Color.GREEN}
        };

        for (int i = 0; i < layers.length; i++) {

          Expression pointCount = toNumber(Expression.get("point_count"));

          //Add cluster circles
          CircleLayer clusterLayer = new CircleLayer("cluster-" + i, ID_SOURCE);
          clusterLayer.setProperties(
            circleColor(layers[i][1]),
            circleRadius(
              interpolate(
                exponential(1f),
                zoom(),
                stop(12, 10f),
                stop(14, 16f),
                stop(15, 18f),
                stop(16, 20f)
              )
            ),
            circleOpacity(0.6f));
          // Add a filter to the cluster layer that hides the circles based on "point_count"
          clusterLayer.setFilter(
            i == 0
              ? gte(pointCount, literal(layers[i][0])) :
              all(
                gte(pointCount, literal(layers[i][0])),
                lt(pointCount, literal(layers[i - 1][0]))
              )
          );
          map.addLayerBelow(clusterLayer, MAKI_LAYER_ID);
        }

        //Add the count labels
        SymbolLayer count = new SymbolLayer("count", ID_SOURCE);
        count.setProperties(
          textField("{point_count}"),
          textSize(8f),
          textOffset(new Float[] {0.0f, 0.0f}),
          textColor(Color.WHITE),
          textIgnorePlacement(true)
        );
        map.addLayerBelow(count, MAKI_LAYER_ID);
      } else {
        mapillarySource.setGeoJson(featureCollection);
      }
    }

    static Bitmap getCroppedBitmap(Bitmap bitmap) {
      Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
        bitmap.getHeight(), Bitmap.Config.ARGB_8888);
      Canvas canvas = new Canvas(output);

      final int color = 0xff424242;
      final Paint paint = new Paint();
      final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

      paint.setAntiAlias(true);
      canvas.drawARGB(0, 0, 0, 0);
      paint.setColor(color);
      // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
      canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
        bitmap.getWidth() / 2, paint);
      paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
      canvas.drawBitmap(bitmap, rect, rect, paint);
      //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
      //return _bmp;
      return output;
    }

    private Runnable progressRunnable = new Runnable() {
      @Override
      public void run() {
        if (isCancelled()) {
          setLoadingState(false, false);
          return;
        }

        if (loadingIncrease) {
          if (loadingProgress >= LOADING_PROGRESS_STEPS) {
            loadingIncrease = false;
          }
        } else {
          if (loadingProgress <= 0) {
            loadingIncrease = true;
          }
        }

        loadingProgress = loadingIncrease ? loadingProgress + 1 : loadingProgress - 1;

        feature.addNumberProperty(PROPERTY_LOADING_PROGRESS, loadingProgress);
        SymbolLayerMapillaryActivity activity = activityRef.get();
        if (activity != null) {
          activity.refreshSource();
        }
        progressHandler.postDelayed(this, LOADING_STEP_DURATION);
      }
    };

    private void setLoadingState(boolean isLoading, boolean isSuccess) {
      progressHandler.removeCallbacksAndMessages(null);
      feature.addBooleanProperty(PROPERTY_LOADING, isLoading);
      SymbolLayerMapillaryActivity activity = activityRef.get();
      if (activity != null) {
        activity.refreshSource();

        if (isLoading) { //zooming to a loading state
          activity.setActivityStep(STEP_LOADING);
        } else if (isSuccess) { //if success zooming to a ready state, otherwise do nothing
          activity.setActivityStep(STEP_READY);
        }
      }
    }
  }

  private static class MapillaryDataLoadResult {
    private final HashMap<Feature, Bitmap> bitmapHashMap = new HashMap<>();
    private final FeatureCollection mapillaryFeatureCollection;

    MapillaryDataLoadResult(FeatureCollection mapillaryFeatureCollection) {
      this.mapillaryFeatureCollection = mapillaryFeatureCollection;
    }

    public void add(Feature feature, Bitmap bitmap) {
      bitmapHashMap.put(feature, bitmap);
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

  /**
   * Util class that creates a Source and a Layer based on Mapillary data.
   * https://www.mapillary.com/developer/tiles-documentation/
   */
  private static class MapillaryTiles {

    static final String ID_SOURCE = "mapillary.source";
    static final String ID_LINE_LAYER = "mapillary.layer.line";
    static final String URL_TILESET = "https://d25uarhxywzl1j.cloudfront.net/v0.1/{z}/{x}/{y}.mvt";

    static Source createSource() {
      TileSet mapillaryTileset = new TileSet("2.1.0", MapillaryTiles.URL_TILESET);
      mapillaryTileset.setMinZoom(0);
      mapillaryTileset.setMaxZoom(14);
      return new VectorSource(MapillaryTiles.ID_SOURCE, mapillaryTileset);
    }

    static Layer createLineLayer() {
      LineLayer lineLayer = new LineLayer(MapillaryTiles.ID_LINE_LAYER, MapillaryTiles.ID_SOURCE);
      lineLayer.setSourceLayer("mapillary-sequences");
      lineLayer.setProperties(
        lineCap(Property.LINE_CAP_ROUND),
        lineJoin(Property.LINE_JOIN_ROUND),
        lineOpacity(0.6f),
        lineWidth(2.0f),
        lineColor(Color.GREEN)
      );
      return lineLayer;
    }
  }

  /**
   * RecyclerViewAdapter adapting features to cards.
   */
  static class LocationRecyclerViewAdapter extends
    RecyclerView.Adapter<SymbolLayerMapillaryActivity.LocationRecyclerViewAdapter.MyViewHolder> {

    private List<Feature> featureCollection;
    private SymbolLayerMapillaryActivity activity;

    LocationRecyclerViewAdapter(SymbolLayerMapillaryActivity activity, FeatureCollection featureCollection) {
      this.activity = activity;
      this.featureCollection = featureCollection.features();
    }

    @Override
    public LocationRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View itemView = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.cardview_symbol_layer, parent, false);
      return new LocationRecyclerViewAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(LocationRecyclerViewAdapter.MyViewHolder holder, int position) {
      Feature feature = featureCollection.get(position);
      holder.title.setText(feature.getStringProperty(PROPERTY_TITLE));
      holder.description.setText(feature.getStringProperty(PROPERTY_DESCRIPTION));
      holder.poi.setText(feature.getStringProperty(PROPERTY_POI));
      holder.style.setText(feature.getStringProperty(PROPERTY_STYLE));
      holder.setClickListener(new ItemClickListener() {
        @Override
        public void onClick(View view, int position) {
          if (activity != null) {
            activity.toggleFavourite(position);
          }
        }
      });
    }

    @Override
    public int getItemCount() {
      return featureCollection.size();
    }

    /**
     * ViewHolder for RecyclerView.
     */
    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
      TextView title;
      TextView poi;
      TextView style;
      TextView description;
      CardView singleCard;
      ItemClickListener clickListener;

      MyViewHolder(View view) {
        super(view);
        title = view.findViewById(R.id.textview_title);
        poi = view.findViewById(R.id.textview_poi);
        style = view.findViewById(R.id.textview_style);
        description = view.findViewById(R.id.textview_description);
        singleCard = view.findViewById(R.id.single_location_cardview);
        singleCard.setOnClickListener(this);
      }

      void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
      }

      @Override
      public void onClick(View view) {
        clickListener.onClick(view, getLayoutPosition());
      }
    }
  }

  interface ItemClickListener {
    void onClick(View view, int position);
  }
}