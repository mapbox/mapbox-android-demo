package com.mapbox.mapboxandroiddemo.examples.labs;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;

import static androidx.biometric.BiometricConstants.ERROR_NEGATIVE_BUTTON;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

/**
 * Integrate the Maps SDK with the Android-system biometric unlock system. Use a saved fingerprint to
 * add data to the map once the user is verified.
 */
public class BiometricFingerprintLayerUnlockActivity extends AppCompatActivity {

  private static final String SOURCE_ID = "SOURCE_ID";
  private static final String ICON_ID = "ICON_ID";
  private static final String LAYER_ID = "LAYER_ID";
  private MapView mapView;
  private MapboxMap mapboxMap;
  private BiometricPrompt biometricPrompt;
  private Executor executor = Executors.newSingleThreadExecutor();

  /**
   * Authentication callback used after the biometric authentication process is run
   */
  private BiometricPrompt.AuthenticationCallback callback = new BiometricPrompt.AuthenticationCallback() {
    @Override
    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
      if (errorCode == ERROR_NEGATIVE_BUTTON && biometricPrompt != null) {
        biometricPrompt.cancelAuthentication();
      }
      runOnUiThread(() -> Toast.makeText(BiometricFingerprintLayerUnlockActivity.this,
        errString.toString(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
      runOnUiThread(new Runnable() {
        public void run() {
          mapboxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

              style.addImage(ICON_ID, BitmapFactory.decodeResource(
                BiometricFingerprintLayerUnlockActivity.this.getResources(), R.drawable.red_marker));

              // Adding a GeoJSON source for the SymbolLayer icons.
              style.addSource(new GeoJsonSource(SOURCE_ID,
                FeatureCollection.fromFeatures(initFeaturePointList())));

              // Adding the actual SymbolLayer to the map style. An offset is added that the bottom of the red
              // marker icon gets fixed to the coordinate, rather than the middle of the icon being fixed to
              // the coordinate point. This is offset is not always needed and is dependent on the image
              // that you use for the SymbolLayer icon.
              style.addLayer(new SymbolLayer(LAYER_ID, SOURCE_ID)
                .withProperties(
                  iconImage(ICON_ID),
                  iconAllowOverlap(true),
                  iconIgnorePlacement(true),
                  iconAnchor(ICON_ANCHOR_BOTTOM)));
            }
          });
        }
      });
    }

    @Override
    public void onAuthenticationFailed() {
      runOnUiThread(new Runnable() {
        public void run() {
          Toast.makeText(BiometricFingerprintLayerUnlockActivity.this, R.string.fingerprint_fail,
            Toast.LENGTH_SHORT).show();
        }
      });
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_labs_biometric_fingerprint);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        mapboxMap.setStyle(Style.SATELLITE_STREETS, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            BiometricFingerprintLayerUnlockActivity.this.mapboxMap = mapboxMap;
            if (isFingerprintAvailable()) {
              if (biometricPrompt == null) {
                biometricPrompt = new BiometricPrompt(
                  BiometricFingerprintLayerUnlockActivity.this, executor, callback);
              }
              biometricPrompt.authenticate(buildBiometricPrompt());
            } else {
              Toast.makeText(BiometricFingerprintLayerUnlockActivity.this, R.string.fingerprint_not_possible,
                Toast.LENGTH_SHORT).show();
            }
          }
        });
      }
    });
  }

  /**
   * Check whether the a fingerprint can be authenticated by the particular device. There's a chance that the device
   * doesn't have a fingerprint sensor and/or the user's fingerprint hasn't been set up yet in the device's settings.
   *
   * @return boolean about whether the device supports fingerprint authentication.
   */
  private boolean isFingerprintAvailable() {
    return BiometricManager.from(BiometricFingerprintLayerUnlockActivity.this).canAuthenticate()
      == BiometricManager.BIOMETRIC_SUCCESS;
  }

  /**
   * Build a {@link BiometricPrompt} to provide information and instruct the user to use a thumbprint to display
   * data on the map.
   *
   * @return a built {@link BiometricPrompt.PromptInfo} object to authenticate.
   */
  private BiometricPrompt.PromptInfo buildBiometricPrompt() {
    return new BiometricPrompt.PromptInfo.Builder()
      .setTitle(getString(R.string.prompt_title))
      .setSubtitle(getString(R.string.prompt_subtitle))
      .setDescription(getString(R.string.prompt_description))
      .setNegativeButtonText(getString(R.string.prompt_negative_button))
      .build();
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
    if (biometricPrompt != null) {
      biometricPrompt.cancelAuthentication();
    }
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  private List<Feature> initFeaturePointList() {
    List<Feature> symbolLayerIconFeatureList = new ArrayList<>();
    symbolLayerIconFeatureList.add(Feature.fromGeometry(
      Point.fromLngLat(
        66.298729,
        42.928145)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(
      Point.fromLngLat(
        77.28305,
        39.854244)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(
      Point.fromLngLat(
        61.544321,
        46.026979)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(
      Point.fromLngLat(
        57.691611,
        36.108244)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(
      Point.fromLngLat(
        53.756929,
        32.797029)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(
      Point.fromLngLat(
        80.725897,
        42.203685)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(
      Point.fromLngLat(
        76.217407,
        44.876952)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(
      Point.fromLngLat(
        72.77456,
        38.584169)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(
      Point.fromLngLat(
        74.414011,
        31.687743)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(
      Point.fromLngLat(
        64.659278,
        47.153569)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(
      Point.fromLngLat(
        70.807219,
        46.649501)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(
      Point.fromLngLat(
        76.053462,
        49.973477)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(
      Point.fromLngLat(
        68.757905,
        43.940044)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(
      Point.fromLngLat(
        68.757905,
        30.776557)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(
      Point.fromLngLat(
        59.740925,
        29.714372)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(
      Point.fromLngLat(
        66.216756,
        34.772613)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(
      Point.fromLngLat(
        64.659278,
        50.497793)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(
      Point.fromLngLat(
        70.561301,
        52.137028)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(
      Point.fromLngLat(
        54.576655,
        49.443387)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(
      Point.fromLngLat(
        65.233086,
        38.263068)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(
      Point.fromLngLat(
        80.561952,
        48.202166)));
    symbolLayerIconFeatureList.add(Feature.fromGeometry(
      Point.fromLngLat(
        58.593309,
        41.101246)));
    return symbolLayerIconFeatureList;
  }
}
