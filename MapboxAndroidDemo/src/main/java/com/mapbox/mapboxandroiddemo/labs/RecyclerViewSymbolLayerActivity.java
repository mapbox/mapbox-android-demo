package com.mapbox.mapboxandroiddemo.labs;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.google.gson.GsonBuilder;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.geojson.FeatureCollection;
import com.mapbox.services.commons.geojson.Geometry;
import com.mapbox.services.commons.geojson.Point;
import com.mapbox.services.commons.geojson.custom.GeometryDeserializer;
import com.mapbox.services.commons.geojson.custom.PositionDeserializer;
import com.mapbox.services.commons.models.Position;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.mapbox.mapboxsdk.style.functions.Function.property;
import static com.mapbox.mapboxsdk.style.functions.stops.Stop.stop;
import static com.mapbox.mapboxsdk.style.functions.stops.Stops.categorical;
import static com.mapbox.mapboxsdk.style.layers.Filter.eq;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

/**
 * Use a recyclerview with symbol layer markers to easily explore content all on one screen
 */
public class RecyclerViewSymbolLayerActivity extends AppCompatActivity implements OnMapReadyCallback {

  private static final String SOURCE_ID = "mapbox.poi";
  private static final String MAKI_LAYER_ID = "mapbox.poi.maki";
  private static final String CALLOUT_LAYER_ID = "mapbox.poi.callout";

  private static final String PROPERTY_SELECTED = "selected";
  private static final String PROPERTY_TITLE = "title";
  private static final String PROPERTY_FAVOURITE = "favourite";
  private static final String PROPERTY_DESCRIPTION = "description";
  private static final String PROPERTY_POI = "poi";
  private static final String PROPERTY_STYLE = "style";
  private static final String PROPERTY_SUB_STYLE = "sub-style";

  private static final long ANIMATION_TIME = 1950;

  private MapView mapView;
  private MapboxMap mapboxMap;
  private RecyclerView recyclerView;

  private GeoJsonSource source;
  private FeatureCollection featureCollection;
  private HashMap<String, View> viewMap;
  private AnimatorSet animatorSet;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_rv_symbol_layer);

    recyclerView = (RecyclerView) findViewById(R.id.rv_on_top_of_map);

    // Initialize the map view
    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    mapboxMap.getUiSettings().setCompassEnabled(false);
    new LoadDataTask(RecyclerViewSymbolLayerActivity.this).execute();
  }

  public void setupData(final FeatureCollection collection) {
    if (mapboxMap == null) {
      return;
    }

    featureCollection = collection;
    setupSource();
    setupMakiLayer();
    setupCalloutLayer();
    setupClickListeners();
    setupRecyclerView();
    hideLabelLayers();
  }

  private void setupSource() {
    source = new GeoJsonSource(SOURCE_ID, featureCollection);
    mapboxMap.addSource(source);
  }

  private void refreshSource() {
    if (source != null) {
      source.setGeoJson(featureCollection);
    }
  }

  /**
   * Setup a layer with maki icons, eg. restaurant.
   */
  private void setupMakiLayer() {
    mapboxMap.addLayer(new SymbolLayer(MAKI_LAYER_ID, SOURCE_ID)
      .withProperties(
        /* show maki icon based on the value of poi feature property */
        iconImage("{poi}-15"),

        /* allows show all icons */
        iconAllowOverlap(true),

        /* when feature is in selected state, grow icon */
        iconSize(
          property(
            PROPERTY_SELECTED,
            categorical(
              stop(true, iconSize(1.45f)),
              stop(false, iconSize(1.0f))
            )
          )
        )
      )
    );
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
      .withFilter(eq(PROPERTY_SELECTED, true))
    );
  }

  /**
   * Use OnMapClickListener in combination with queryRenderedFeatures
   */
  private void setupClickListeners() {
    mapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
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
    });
  }

  private void setupRecyclerView() {
    RecyclerView.Adapter adapter = new LocationRecyclerViewAdapter(this, featureCollection);
    final LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setItemAnimator(new DefaultItemAnimator());
    recyclerView.setAdapter(adapter);
    recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
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
    recyclerView.post(new Runnable() {
      @Override
      public void run() {
        mapboxMap.setPadding(0, 0, 0, recyclerView.getMeasuredHeight());
      }
    });

    // init with default state
    setSelected(0, false);
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
    View textContainer = view.findViewById(R.id.title);


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
      Toast.makeText(RecyclerViewSymbolLayerActivity.this, callout, Toast.LENGTH_LONG).show();
    } else {
      // user clicked on icon
      List<Feature> featureList = featureCollection.getFeatures();
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
      List<Feature> featureList = featureCollection.getFeatures();
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
    deselectAll();

    Feature feature = featureCollection.getFeatures().get(index);
    selectFeature(feature);
    animateCameraToSelection(feature);
    refreshSource();

    if (withScroll) {
      recyclerView.scrollToPosition(index);
    }
  }

  /**
   * Deselects the state of all the features
   */
  private void deselectAll() {
    for (Feature feature : featureCollection.getFeatures()) {
      feature.getProperties().addProperty(PROPERTY_SELECTED, false);
    }
  }

  /**
   * Selects the state of a feature
   *
   * @param feature the feature to be selected.
   */
  private void selectFeature(Feature feature) {
    feature.getProperties().addProperty(PROPERTY_SELECTED, true);
  }

  /**
   * Animate camera to a feature.
   *
   * @param feature the feature to animate to
   */
  private void animateCameraToSelection(Feature feature) {
    CameraPosition cameraPosition = mapboxMap.getCameraPosition();

    if (animatorSet != null) {
      animatorSet.cancel();
    }

    animatorSet = new AnimatorSet();
    animatorSet.playTogether(
      createLatLngAnimator(cameraPosition.target, convertToLatLng(feature)),
      createZoomAnimator(cameraPosition.zoom, feature.getNumberProperty("zoom").doubleValue()),
      createBearingAnimator(cameraPosition.bearing, feature.getNumberProperty("bearing").doubleValue()),
      createTiltAnimator(cameraPosition.tilt, feature.getNumberProperty("tilt").doubleValue())
    );
    animatorSet.start();
  }

  /**
   * Set the favourite state of a feature based on the index.
   *
   * @param index the index of the feature to favourite/de-favourite
   */
  private void toggleFavourite(int index) {
    Feature feature = featureCollection.getFeatures().get(index);
    boolean currentState = feature.getBooleanProperty(PROPERTY_FAVOURITE);
    feature.getProperties().addProperty(PROPERTY_FAVOURITE, !currentState);
    new GenerateViewIconTask(RecyclerViewSymbolLayerActivity.this, true).execute(featureCollection);
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
    mapView.onDestroy();
  }

  private LatLng convertToLatLng(Feature feature) {
    Point symbolPoint = (Point) feature.getGeometry();
    Position position = symbolPoint.getCoordinates();
    return new LatLng(position.getLatitude(), position.getLongitude());
  }

  private Animator createLatLngAnimator(LatLng currentPosition, LatLng targetPosition) {
    ValueAnimator latLngAnimator = ValueAnimator.ofObject(new LatLngEvaluator(), currentPosition, targetPosition);
    latLngAnimator.setDuration(ANIMATION_TIME);
    latLngAnimator.setInterpolator(new FastOutSlowInInterpolator());
    latLngAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        mapboxMap.setLatLng((LatLng) animation.getAnimatedValue());
      }
    });
    return latLngAnimator;
  }

  private Animator createZoomAnimator(double currentZoom, double targetZoom) {
    ValueAnimator zoomAnimator = ValueAnimator.ofFloat((float) currentZoom, (float) targetZoom);
    zoomAnimator.setDuration(ANIMATION_TIME);
    zoomAnimator.setInterpolator(new FastOutSlowInInterpolator());
    zoomAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        mapboxMap.setZoom((Float) animation.getAnimatedValue());
      }
    });
    return zoomAnimator;
  }

  private Animator createBearingAnimator(double currentBearing, double targetBearing) {
    ValueAnimator bearingAnimator = ValueAnimator.ofFloat((float) currentBearing, (float) targetBearing);
    bearingAnimator.setDuration(ANIMATION_TIME);
    bearingAnimator.setInterpolator(new FastOutSlowInInterpolator());
    bearingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        mapboxMap.setBearing((Float) animation.getAnimatedValue());
      }
    });
    return bearingAnimator;
  }

  private Animator createTiltAnimator(double currentTilt, double targetTilt) {
    ValueAnimator tiltAnimator = ValueAnimator.ofFloat((float) currentTilt, (float) targetTilt);
    tiltAnimator.setDuration(ANIMATION_TIME);
    tiltAnimator.setInterpolator(new FastOutSlowInInterpolator());
    tiltAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        mapboxMap.setTilt((Float) animation.getAnimatedValue());
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
  private static class LoadDataTask extends AsyncTask<Void, Void, FeatureCollection> {

    private final RecyclerViewSymbolLayerActivity activity;

    LoadDataTask(RecyclerViewSymbolLayerActivity activity) {
      this.activity = activity;
    }

    @Override
    protected FeatureCollection doInBackground(Void... params) {
      String geoJson = loadGeoJsonFromAsset(activity, "sf_poi.geojson");
      return new GsonBuilder()
        .registerTypeAdapter(Geometry.class, new GeometryDeserializer())
        .registerTypeAdapter(Position.class, new PositionDeserializer())
        .create().fromJson(geoJson, FeatureCollection.class);
    }

    @Override
    protected void onPostExecute(FeatureCollection featureCollection) {
      super.onPostExecute(featureCollection);
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
    private final RecyclerViewSymbolLayerActivity activity;
    private final boolean refreshSource;

    GenerateViewIconTask(RecyclerViewSymbolLayerActivity activity, boolean refreshSource) {
      this.activity = activity;
      this.refreshSource = refreshSource;
    }

    GenerateViewIconTask(RecyclerViewSymbolLayerActivity activity) {
      this(activity, false);
    }

    @SuppressWarnings("WrongThread")
    @Override
    protected HashMap<String, Bitmap> doInBackground(FeatureCollection... params) {
      HashMap<String, Bitmap> imagesMap = new HashMap<>();
      LayoutInflater inflater = LayoutInflater.from(activity);
      FeatureCollection featureCollection = params[0];

      for (Feature feature : featureCollection.getFeatures()) {
        View view = inflater.inflate(R.layout.layout_callout, null);

        String name = feature.getStringProperty(PROPERTY_TITLE);
        TextView textView = (TextView) view.findViewById(R.id.title);
        textView.setText(name);

        boolean favourite = feature.getBooleanProperty(PROPERTY_FAVOURITE);
        ImageView imageView = (ImageView) view.findViewById(R.id.logoView);
        imageView.setImageResource(favourite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);

        Bitmap bitmap = SymbolGenerator.generate(view);
        imagesMap.put(name, bitmap);
        viewMap.put(name, view);
      }
      return imagesMap;
    }

    @Override
    protected void onPostExecute(HashMap<String, Bitmap> bitmapHashMap) {
      super.onPostExecute(bitmapHashMap);
      if (activity != null) {
        activity.setImageGenResults(viewMap, bitmapHashMap);
        if (refreshSource) {
          activity.refreshSource();
        }
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

  /**
   * RecyclerViewAdapter adapting features to cards.
   */
  static class LocationRecyclerViewAdapter extends RecyclerView.Adapter<LocationRecyclerViewAdapter.MyViewHolder> {

    private List<Feature> featureCollection;
    private RecyclerViewSymbolLayerActivity activity;

    LocationRecyclerViewAdapter(RecyclerViewSymbolLayerActivity activity, FeatureCollection featureCollection) {
      this.activity = activity;
      this.featureCollection = featureCollection.getFeatures();
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
      holder.style.setText(feature.getStringProperty(PROPERTY_STYLE) + " - "
        + feature.getStringProperty(PROPERTY_SUB_STYLE));
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
        title = (TextView) view.findViewById(R.id.textview_title);
        poi = (TextView) view.findViewById(R.id.textview_poi);
        style = (TextView) view.findViewById(R.id.textview_style);
        description = (TextView) view.findViewById(R.id.textview_description);
        singleCard = (CardView) view.findViewById(R.id.single_location_cardview);
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