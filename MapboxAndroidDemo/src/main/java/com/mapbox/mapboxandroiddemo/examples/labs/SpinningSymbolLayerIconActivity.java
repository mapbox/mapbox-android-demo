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

import java.io.InputStream;

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
  private static final int DESIRED_SECONDS_PER_ONE_FULL_360_SPIN = 1;
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

      // Add the SymbolLayer icon image to the map style
      .withImage(ICON_ID, BitmapUtils.getBitmapFromDrawable(
        getResources().getDrawable(R.drawable.hurricane_icon)))

      // Adding a GeoJson source for the SymbolLayer icons.
      .withSource(new GeoJsonSource(SOURCE_ID, loadGeoJsonFromAsset("spinning_icon.geojson")))

      // Adding the actual SymbolLayer to the map style.
      .withLayer(new SymbolLayer(LAYER_ID, SOURCE_ID)
        .withProperties(PropertyFactory.iconImage(ICON_ID),
          iconAllowOverlap(true))
      ), new Style.OnStyleLoaded() {
        @Override
        public void onStyleLoaded(@NonNull Style style) {
          startIconSpinningAnimation();
        }
      });
  }

  /**
   * Set up and start the icon spinning animation. The Android system ValueAnimator emits a new value, which is
   * used as the SymbolLayer icons' rotation value.
   */
  private void startIconSpinningAnimation() {
    if (iconSpinningAnimator != null) {
      iconSpinningAnimator.cancel();
    }
    iconSpinningAnimator = ValueAnimator.ofFloat(0, 360);
    iconSpinningAnimator.setDuration(DESIRED_SECONDS_PER_ONE_FULL_360_SPIN * 1000);
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
              iconRotate(newIconRotateValue),
              iconPitchAlignment(ICON_PITCH_ALIGNMENT_MAP)
            );
          }
        });
      }
    });
    iconSpinningAnimator.start();
  }

  private String loadGeoJsonFromAsset(String filename) {
    try {
      // Load GeoJSON file
      InputStream is = getAssets().open(filename);
      int size = is.available();
      byte[] buffer = new byte[size];
      is.read(buffer);
      is.close();
      return new String(buffer, "UTF-8");
    } catch (Exception exception) {
      Timber.e("Exception Loading GeoJSON: %s", exception.toString());
      exception.printStackTrace();
      return null;
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
      iconSpinningAnimator.end();
    }
    mapView.onDestroy();
  }
}

