package com.mapbox.mapboxandroiddemo.examples.labs;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.animation.LinearInterpolator;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.net.URI;
import java.net.URISyntaxException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.Property.ICON_PITCH_ALIGNMENT_MAP;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconPitchAlignment;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconRotate;

/**
 * Use an Android-system ValueAnimator and the Maps SDK's runtime styling functionality to
 * continually updated a SymbolLayer icons' rotation. This creates a spinning effect.
 */
public class SpinningSymbolLayerIconActivity extends AppCompatActivity implements OnMapReadyCallback {

  private static final String SOURCE_ID = "SOURCE_ID";
  private static final String ICON_ID = "ICON_ID";
  private static final String LAYER_ID = "LAYER_ID";
  private final int desiredSecondsPerOneFull360Spin = 1;
  private final int desiredMillisecondsPerOneFull360Spin = desiredSecondsPerOneFull360Spin * 1000;
  private MapView mapView;
  private MapboxMap mapboxMap;
  private ValueAnimator iconSpinningAnimator;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_spinning_symbol_layer_icon);

    // Initialize the MapView
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(final MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;

    // Create a Style object
    mapboxMap.setStyle(new Style.Builder().fromUri(Style.MAPBOX_STREETS)
      .withImage(ICON_ID, BitmapUtils.getBitmapFromDrawable(
        getResources().getDrawable(R.drawable.hurricane_icon))), new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            try {
              // Adding a GeoJson source for the SymbolLayer icons.
              style.addSource(new GeoJsonSource(SOURCE_ID, new URI("asset://spinning_icon.geojson")));

              // Adding the actual SymbolLayer to the map style.
              style.addLayer(new SymbolLayer(LAYER_ID, SOURCE_ID)
                .withProperties(PropertyFactory.iconImage(ICON_ID),
                  iconAllowOverlap(true),
                  iconPitchAlignment(ICON_PITCH_ALIGNMENT_MAP)));

              startIconSpinningAnimation();

            } catch (URISyntaxException exception) {
              Timber.d(exception);
            }
          }
        });
  }

  /**
   * Set up and start the icon spinning animation. The Android system ValueAnimator emits a new value, which is
   * used as the SymbolLayer icons' rotation value. The value is animated from 360 to 0 because of the
   * hurricane icon's spin design. You might have to adjust the values depending on the design of your icon.
   */
  private void startIconSpinningAnimation() {
    if (iconSpinningAnimator != null) {
      iconSpinningAnimator.cancel();
    }
    iconSpinningAnimator = ValueAnimator.ofFloat(360, 0);
    iconSpinningAnimator.setDuration(desiredMillisecondsPerOneFull360Spin);
    iconSpinningAnimator.setInterpolator(new LinearInterpolator());
    iconSpinningAnimator.setRepeatCount(ValueAnimator.INFINITE);
    iconSpinningAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator valueAnimator) {
        // Retrieve the new animation number to use as the map camera bearing value
        Float newIconRotateValue = (Float) valueAnimator.getAnimatedValue();
        mapboxMap.getStyle(new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            Layer iconSymbolLayer = style.getLayerAs(LAYER_ID);
            iconSymbolLayer.setProperties(
              iconRotate(newIconRotateValue)
            );
          }
        });
      }
    });
    iconSpinningAnimator.start();
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
  public void onSaveInstanceState(Bundle outState) {
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
    if (iconSpinningAnimator != null) {
      iconSpinningAnimator.cancel();
    }
    mapView.onDestroy();
  }
}

