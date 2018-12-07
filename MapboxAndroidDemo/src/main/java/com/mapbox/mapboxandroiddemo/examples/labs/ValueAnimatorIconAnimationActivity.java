package com.mapbox.mapboxandroiddemo.examples.labs;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconTranslate;

/**
 * Combine SymbolLayer icons with the Android system's ValueAnimator and interpolator
 * animation for a fun pin drop effect. The interpolator movement can also be used with other
 * types of map layers, such as a LineLayer or CircleLayer.
 * <p>
 * More info about https://developer.android.com/reference/android/view/animation/Interpolator
 */
public class ValueAnimatorIconAnimationActivity extends AppCompatActivity implements
  OnMapReadyCallback, MapView.OnDidFinishRenderingMapListener {

  private static final String ICON_ID = "red-pin-icon-id";

  // This float's actual value will depend on the height of the SymbolLayer icon
  private static final float DEFAULT_DESIRED_ICON_OFFSET = -16;
  private static final float STARTING_DROP_HEIGHT = -100;
  private static final long DROP_SPEED_MILLISECONDS = 1200;
  private MapView mapView;
  private SymbolLayer pinSymbolLayer;
  private Style style;
  private TimeInterpolator currentSelectedTimeInterpolator;
  private ValueAnimator animator;
  private boolean firstRunThrough;
  private boolean animationHasStarted;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    currentSelectedTimeInterpolator = new BounceInterpolator();
    firstRunThrough = true;

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_animated_pin_drop);

    // Initialize the map view
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
    mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        ValueAnimatorIconAnimationActivity.this.style = style;
        mapView.addOnDidFinishRenderingMapListener(ValueAnimatorIconAnimationActivity.this);
        initLayerIcon(style);
        initDataSource(style);
      }
    });
  }

  /**
   * Implementing this interface so that animation only starts once all tiles have been loaded
   *
   * @param fully whether or not the mapy is finished rendering
   */
  @Override
  public void onDidFinishRenderingMap(boolean fully) {
    initAnimation(currentSelectedTimeInterpolator);
    initInterpolatorButtons();
  }

  /**
   * Add images to the map so that the SymbolLayers can reference the images.
   */
  private void initLayerIcon(@NonNull Style loadedMapStyle) {
    loadedMapStyle.addImage(ICON_ID, BitmapFactory.decodeResource(
      getResources(), R.drawable.map_marker_push_pin_pink));
  }

  /**
   * Add GeoJsonSource with random Features to the map.
   */
  private void initDataSource(@NonNull Style loadedMapStyle) {
    // Add a new source from the GeoJSON data
    loadedMapStyle.addSource(
      new GeoJsonSource("source-id",
        FeatureCollection.fromFeatures(new Feature[] {
          Feature.fromGeometry(Point.fromLngLat(
            119.86083984375,
            -1.834403324493515)),
          Feature.fromGeometry(Point.fromLngLat(
            116.06637239456177,
            5.970619502704659)),
          Feature.fromGeometry(Point.fromLngLat(
            114.58740234375,
            4.54357027937176)),
          Feature.fromGeometry(Point.fromLngLat(
            118.19091796875,
            5.134714634014467)),
          Feature.fromGeometry(Point.fromLngLat(
            110.36865234374999,
            1.4500404973608074)),
          Feature.fromGeometry(Point.fromLngLat(
            109.40185546874999,
            0.3076157096439005)),
          Feature.fromGeometry(Point.fromLngLat(
            115.79589843749999,
            1.5159363834516861)),
          Feature.fromGeometry(Point.fromLngLat(
            113.291015625,
            -0.9667509997666298)),
          Feature.fromGeometry(Point.fromLngLat(
            116.40083312988281,
            -0.3392008994314591))
        })
      )
    );
  }

  /**
   * Initialize and start the animation.
   *
   * @param desiredTimeInterpolator the type of Android system movement to animate the
   *                                SymbolLayer icons with.
   */
  private void initAnimation(TimeInterpolator desiredTimeInterpolator) {
    animator = ValueAnimator.ofFloat(STARTING_DROP_HEIGHT, 0);
    animator.setDuration(DROP_SPEED_MILLISECONDS);
    animator.setInterpolator(desiredTimeInterpolator);
    animator.setStartDelay(1000);
    animator.start();
    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator valueAnimator) {
        if (!animationHasStarted) {
          initSymbolLayer();
          animationHasStarted = true;
        }
        pinSymbolLayer.setProperties(iconTranslate(new Float[]{0f, (Float) valueAnimator.getAnimatedValue()}));
      }
    });
  }

  /**
   * Add the SymbolLayer to the map
   */
  private void initSymbolLayer() {
    pinSymbolLayer = new SymbolLayer("symbol-layer-id",
        "source-id");
    pinSymbolLayer.setProperties(
      iconImage(ICON_ID),
      iconIgnorePlacement(true),
      iconAllowOverlap(true),
      iconOffset(new Float[] {0f, DEFAULT_DESIRED_ICON_OFFSET}));
    style.addLayer(pinSymbolLayer);
  }

  /**
   * Initialize the interpolator selection spinner menu
   */
  private void initInterpolatorButtons() {

    FloatingActionButton bounceInterpolatorFab = findViewById(R.id.fab_bounce_interpolator);
    bounceInterpolatorFab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (firstRunThrough) {
          firstRunThrough = true;
        }
        currentSelectedTimeInterpolator = new BounceInterpolator();
        resetIcons();
      }
    });

    FloatingActionButton linearInterpolatorFab = findViewById(R.id.fab_linear_interpolator);
    linearInterpolatorFab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        currentSelectedTimeInterpolator = new LinearInterpolator();
        firstRunThrough = false;
        resetIcons();
      }
    });

    FloatingActionButton accelerateInterpolatorFab = findViewById(R.id.fab_accelerate_interpolator);
    accelerateInterpolatorFab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        currentSelectedTimeInterpolator = new AccelerateInterpolator();
        firstRunThrough = false;
        resetIcons();
      }
    });

    FloatingActionButton decelerateInterpolatorFab = findViewById(R.id.fab_decelerate_interpolator);
    decelerateInterpolatorFab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        currentSelectedTimeInterpolator = new DecelerateInterpolator();
        firstRunThrough = false;
        resetIcons();
      }
    });
  }

  private void resetIcons() {
    if (!firstRunThrough) {
      animationHasStarted = false;
      style.removeLayer("symbol-layer-id");
      initLayerIcon(style);
      initAnimation(currentSelectedTimeInterpolator);
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
    if (animator != null) {
      animator.end();
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
    mapView.onDestroy();
  }
}