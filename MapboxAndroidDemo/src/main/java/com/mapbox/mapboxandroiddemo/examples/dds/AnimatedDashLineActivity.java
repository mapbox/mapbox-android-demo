package com.mapbox.mapboxandroiddemo.examples.dds;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

import java.net.MalformedURLException;
import java.net.URL;

import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.exponential;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.match;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgb;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgba;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.toColor;
import static com.mapbox.mapboxsdk.style.expressions.Expression.toRgba;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_CAP_ROUND;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_ROUND;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineBlur;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineDasharray;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

/**
 * Create an effect of animated and moving LineLayer dashes by rapidly adjusting the
 * dash and gap lengths.
 */
public class AnimatedDashLineActivity extends AppCompatActivity implements OnMapReadyCallback {

  private static final String ANIMATE_LINE_LAYER_ID = "animated_line_layer_id";
  private MapView mapView;
  private MapboxMap mapboxMap;
  private Handler handler;
  private String TAG = "AnimatedDashLine";
  private Layer animatedLineLayer;
  private RefreshDashAndGapRunnable refreshDashAndGapRunnable;
  private int animationSpeedMillseconds = 50;
  private ValueAnimator animator;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_animated_dash_line);

    handler = new Handler();
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(final MapboxMap mapboxMap) {
    AnimatedDashLineActivity.this.mapboxMap = mapboxMap;

//    try {
    mapboxMap.setStyle(Style.TRAFFIC_NIGHT
        /*.withSource(new GeoJsonSource("animated_line_source", new URL(
            "https://raw.githubusercontent.com/Chicago/osd-bike-routes/master/data/Bikeroutes.geojson"
          ))
        )*/, new Style.OnStyleLoaded() {
        @Override
        public void onStyleLoaded(@NonNull Style style) {
//          initAnimation();

          style.addSource(new VectorSource("traffic-data-source-id", "mapbox://mapbox.mapbox-traffic-v1"));

          LineLayer antsTrafficLayer = new LineLayer(ANIMATE_LINE_LAYER_ID,
            "traffic-data-source-id").withProperties(
            lineBlur(4f),

            lineDasharray(new Float[] {2f, 2f}),

            lineWidth(interpolate(exponential(1.5f), zoom(),
              stop(6, 1),
              stop(13, 4),
              stop(18, match(get("class"), literal(0f),
                stop("motorway", 18),
                stop("trunk", 18),
                stop("primary", 11),
                stop("tertiary", 7),
                stop("secondary", 7)
              )),
              stop(20, match(get("class"), literal(0f),
                stop("motorway", 33),
                stop("trunk", 33),
                stop("primary", 18.5),
                stop("tertiary", 14),
                stop("secondary", 14)
              )))),

            lineColor(
              match(get("congestion"), toColor(rgba(0, 0, 0,0)),
                stop("low", toColor(rgb(62, 116, 85))),
                stop("moderate", toColor(rgb(182, 116, 58))),
                stop("heavy", toColor(rgb(205, 112, 112))),
                stop("severe", toColor(rgb(102, 0, 17)))
              )),

            lineOffset(interpolate(exponential(1.5), zoom(),
              stop(7, 0),
              stop(9, 1),
              stop(15, match(get("class"), literal(1f),
                stop("motorway", 4),
                stop("trunk", 4),
                stop("primary", 4),
                stop("tertiary", 3),
                stop("secondary", 3)
              )),
              stop(18, match(get("class"), literal(0f),
                stop("motorway", 11),
                stop("trunk", 11),
                stop("primary", 10),
                stop("tertiary", 9),
                stop("secondary", 9)
              )),
              stop(20, 21.5)
            ))
          );

          antsTrafficLayer.setSourceLayer("traffic");
          antsTrafficLayer.setFilter(match(get("class"), literal(false),
            stop("motorway", literal(true)),
            stop("trunk", literal(true)),
            stop("primary", literal(true)),
            stop("secondary", literal(true)),
            stop("tertiary", literal(true))
          ));
          style.addLayerBelow(antsTrafficLayer, "bridge-oneway-arrows-blue-major");


          animatedLineLayer = mapboxMap.getStyle().getLayer(ANIMATE_LINE_LAYER_ID);

          Runnable runnable = new RefreshDashAndGapRunnable();
          handler.postDelayed(runnable, animationSpeedMillseconds);
        }
      });

    /*} catch (MalformedURLException malformedUrlException) {
      Log.d("AnimatedDashLine", "Check the URL: " + malformedUrlException.getMessage());
    }*/
  }

  private class RefreshDashAndGapRunnable implements Runnable {
    private float valueOne, valueTwo, valueThree, valueFour, ValueFive;
    private float dashLength = 3;
    private float gapLength = 3;

    // We divide the animation up into 40 totalNumberOfSteps to make careful use of the finite space in
    // LineAtlas
    private float totalNumberOfSteps = 40;

    // A # of totalNumberOfSteps proportional to the dashLength are devoted to manipulating the dash
    private float dashSteps = totalNumberOfSteps * dashLength / (gapLength + dashLength);

    // A # of totalNumberOfSteps proportional to the gapLength are devoted to manipulating the gap
    private float gapSteps = totalNumberOfSteps - dashSteps;

    // The current currentStep #
//    private int currentStep = 0;
    private int gap = 0;

    private String TAG = "AnimatedDashLine";

    @Override
    public void run() {

/*
      if (gap >= totalNumberOfSteps) {
        Log.d(TAG, "run: currentStep >= totalNumberOfSteps");
        gap = 0;
      }
*/

      Float[] newFloatArray = new Float[] {
        0f, Float.valueOf(gap++ / 10 % 4), 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f
      };

      mapboxMap.getStyle().getLayer(ANIMATE_LINE_LAYER_ID).setProperties(
        lineDasharray(newFloatArray));
      handler.postDelayed(this, animationSpeedMillseconds);
    }
  }

  /*private class RefreshDashAndGapRunnable implements Runnable {
    private float valueOne, valueTwo, valueThree, valueFour, ValueFive;
    private float dashLength = 3;
    private float gapLength = 3;

    // We divide the animation up into 40 totalNumberOfSteps to make careful use of the finite space in
    // LineAtlas
    private float totalNumberOfSteps = 40;

    // A # of totalNumberOfSteps proportional to the dashLength are devoted to manipulating the dash
    private float dashSteps = totalNumberOfSteps * dashLength / (gapLength + dashLength);

    // A # of totalNumberOfSteps proportional to the gapLength are devoted to manipulating the gap
    private float gapSteps = totalNumberOfSteps - dashSteps;

    // The current currentStep #
    private int currentStep = 0;

    private String TAG = "AnimatedDashLine";

    @Override
    public void run() {
      Log.d(TAG, "RefreshDashAndGapRunnable run: ");
      currentStep = currentStep + 1;
      if (currentStep >= totalNumberOfSteps) {
        Log.d(TAG, "run: currentStep >= totalNumberOfSteps");
        currentStep = 0;
      }
      if (currentStep < dashSteps) {
        Log.d(TAG, "run: currentStep < dashSteps");
        valueOne = currentStep / dashSteps;
        valueTwo = (1 - valueOne) * dashLength;
        valueThree = gapLength;
        valueFour = valueOne * dashLength;
        ValueFive = 0;
      } else {
        valueOne = (currentStep - dashSteps) / (gapSteps);
        valueTwo = 0;
        valueThree = (1 - valueOne) * gapLength;
        valueFour = dashLength;
        ValueFive = valueOne * gapLength;
      }
      Log.d(TAG, "RefreshDashAndGapRunnable run: here");

      Float[] newFloatArray = new Float[] {valueTwo, valueThree, valueFour, ValueFive};

      mapboxMap.getStyle().getLayer("animated_line_layer_id").setProperties(
        lineDasharray(newFloatArray));
      Log.d(TAG, "RefreshDashAndGapRunnable run: layer done being gotten");
      handler.postDelayed(this, animationSpeedMillseconds);
    }
  }*/

 /* private void initAnimation() {
    animator = ValueAnimator.ofFloat(0, 30);
    animator.setDuration(animationSpeedMillseconds);
    animator.setInterpolator(new LinearInterpolator());
    animator.setRepeatMode(ValueAnimator.REVERSE);
    animator.setRepeatCount(ValueAnimator.INFINITE);
    animator.setStartDelay(1000);
    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator valueAnimator) {
        if (animatedLineLayer != null) {
          animatedLineLayer.setProperties(lineDasharray(
            new Float[] {0f, (Float) valueAnimator.getAnimatedValue(), 0f, (Float) valueAnimator.getAnimatedValue()}));
          Log.d(TAG, "RefreshDashAndGapRunnable valueAnimator.getAnimatedValue() = "
            + valueAnimator.getAnimatedValue());

        }
      }
    });
    animator.start();
  }*/

  // Add the mapView lifecycle to the activity's lifecycle methods
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
    if (animator != null) {
      animator.cancel();
    }
    mapView.onStop();
    handler.removeCallbacks(refreshDashAndGapRunnable);
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
    refreshDashAndGapRunnable = null;
    handler = null;
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}