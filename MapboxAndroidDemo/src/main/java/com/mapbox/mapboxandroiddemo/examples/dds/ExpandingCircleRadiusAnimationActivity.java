package com.mapbox.mapboxandroiddemo.examples.dds;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static android.graphics.Color.parseColor;
import static com.mapbox.mapboxsdk.style.layers.Property.CIRCLE_PITCH_ALIGNMENT_MAP;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_PITCH_ALIGNMENT_MAP;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circlePitchAlignment;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconPitchAlignment;

/**
 * Use an Android-system ValueAnimator to continually expand CircleLayer circles' radii.
 */
public class ExpandingCircleRadiusAnimationActivity extends AppCompatActivity {

  private static final String TAG = "ExpandingCircleActivity";
  private static final String POWER_PLANT_SOURCE_ID = "POWER_PLANT_SOURCE_ID";
  private static final String POWER_PLANT_SYMBOL_ICON_ID = "POWER_PLANT_SYMBOL_ICON_ID";
  private static final String POWER_PLANT_SYMBOL_LAYER_ID = "POWER_PLANT_SYMBOL_LAYER_ID";
  private static final String CIRCLE_LAYER_ID = "CIRCLE_LAYER_ID";
  private static final String CIRCLE_COLOR = "#FEFE22";

  // Adjust the static final variables below to customize the UI of this example.
  private static final float MAX_RADIUS = 20f;
  private static final float EXPAND_SPEED_MILLISECONDS = 2000;
  private static final float PULSE_LAYER_OPACITY = .55f;
  private static final boolean PULSE_FADING_ENABLED = true;
  private static final TimeInterpolator INTERPOLATOR_TYPE = new DecelerateInterpolator();

  private MapView mapView;
  private CircleLayer circleLayer;
  private ValueAnimator expandingCircleAnimator;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_expanding_circle_radius_animation_activity);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull final MapboxMap mapboxMap) {

        // Create a VectorSource with a Mapbox Tileset ID
        VectorSource vectorSource = new VectorSource(POWER_PLANT_SOURCE_ID,
            "mapbox://appsatmapboxcom.cjygi0jqq01sx2inr1d3ktmj6-1979c");

        mapboxMap.setStyle(new Style.Builder().fromUri(Style.DARK)
            // add the data source to the map
            .withSource(vectorSource)
            // add the nuclear power plant icon to the map
            .withImage(POWER_PLANT_SYMBOL_ICON_ID, BitmapFactory.decodeResource(
                ExpandingCircleRadiusAnimationActivity.this.getResources(),
                R.drawable.nuclear_power_plant)), new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {

                    // Create and style the SymbolLayer, which will show the nuclear power plant icons
                    SymbolLayer powerPlantSymbolLayer = new SymbolLayer(
                        POWER_PLANT_SYMBOL_LAYER_ID, POWER_PLANT_SOURCE_ID)
                        .withProperties(
                            iconImage(POWER_PLANT_SYMBOL_ICON_ID),
                            iconAllowOverlap(true),
                            iconPitchAlignment(ICON_PITCH_ALIGNMENT_MAP),
                            iconIgnorePlacement(true)
                        );
                    // Set the source layer because we're using a VectorSource object
                    powerPlantSymbolLayer.setSourceLayer("global_nuclear_power_plants");

                    // Add the layer to the map
                    style.addLayer(powerPlantSymbolLayer);

                    // Create and style the CircleLayer, which will show the pulsing circles behind the icons
                    circleLayer = new CircleLayer(CIRCLE_LAYER_ID, POWER_PLANT_SOURCE_ID);
                    circleLayer.withProperties(
                        circleOpacity(PULSE_LAYER_OPACITY),
                        circlePitchAlignment(CIRCLE_PITCH_ALIGNMENT_MAP),
                        circleColor(parseColor(CIRCLE_COLOR))
                    );

                    // Set the source layer because we're using a VectorSource object
                    circleLayer.setSourceLayer("global_nuclear_power_plants");

                    // Add the layer to the map and underneath the icons' SymbolLayer so that the expanding
                    // circles appear to originate from the icons themselves.
                    style.addLayerBelow(circleLayer, POWER_PLANT_SYMBOL_LAYER_ID);

                    // Start the circle animation process
                    animateCircleLayerRadius();
                }
        });
      }
    });
  }

  /**
   * Use a ValueAnimator to increase a number and use the number as the circles' radii.
   */
  private void animateCircleLayerRadius() {
    if (expandingCircleAnimator != null) {
      expandingCircleAnimator.cancel();
    }
    expandingCircleAnimator = ValueAnimator.ofFloat(0f, MAX_RADIUS);
    expandingCircleAnimator.setDuration((long) EXPAND_SPEED_MILLISECONDS);
    expandingCircleAnimator.setRepeatMode(ValueAnimator.RESTART);
    expandingCircleAnimator.setRepeatCount(ValueAnimator.INFINITE);
    expandingCircleAnimator.setInterpolator(INTERPOLATOR_TYPE);
    expandingCircleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator valueAnimator) {
        float newPulsingRadiusValue = (Float) valueAnimator.getAnimatedValue();

        // Update the circles' radii with the new values
        circleLayer.setProperties(circleRadius(newPulsingRadiusValue),
            circleOpacity(PULSE_FADING_ENABLED ? (float) (1 - ((newPulsingRadiusValue / 55) * 2.5))
                : PULSE_LAYER_OPACITY)
        );
      }
    });
    expandingCircleAnimator.start();
  }

  // Add the mapView's own lifecycle methods to the activity's lifecycle methods
  @Override
  public void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  public void onResume() {
    super.onResume();
    Log.d(TAG, "onResume: ");
    if (expandingCircleAnimator != null) {
      animateCircleLayerRadius();
    }
    mapView.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  public void onStop() {
    super.onStop();
    if (expandingCircleAnimator != null) {
      expandingCircleAnimator.end();
    }
    mapView.onStop();
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