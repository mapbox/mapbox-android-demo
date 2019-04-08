package com.mapbox.mapboxandroiddemo.examples.offline;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.mapbox.mapboxandroiddemo.BuildConfig;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.log.Logger;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.offline.OfflineManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Use the Maps SDK's cache management methods to adjust the ambient cache and database.
 * This activity shows how to adjust the cache based on Android system button UI click
 * listening. It also shows how to hook Firebase Remote Config into cache management logic.
 */
public class CacheManagementActivity extends AppCompatActivity {

  // Firebase Remote Config keys
  private static final String INVALIDATE_AMBIENT_CACHE_KEY = "invalidate_ambient_cache_key";
  private static final String SET_MAX_AMBIENT_CACHE_SIZE_BOOLEAN_KEY = "set_max_ambient_cache_size_boolean_key";
  private static final String SET_MAX_AMBIENT_CACHE_SIZE_AMOUNT_KEY = "set_max_ambient_cache_size_amount_key";
  private static final String CLEAR_AMBIENT_CACHE_KEY = "clear_ambient_cache_key";
  private static final String RESET_DATABASE_REMOTE_CONFIG_KEY = "reset_database_remote_config_key";

  private MapView mapView;
  private FirebaseRemoteConfig firebaseRemoteConfig;
  private EditText newMaxAmbientCacheSizeTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);


    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_offline_cache_manipulation);

    newMaxAmbientCacheSizeTextView = findViewById(
        R.id.set_max_ambient_cache_size_button_editText);

    // Disabling Firebase Remote Config usage here to avoid build and compiling issues during
    // development.
    if (!BuildConfig.DEBUG) {

      // Get an instance of Firebase Remote Config.
      firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

      // Sets the minimum interval between successive fetch calls. Fetches less than
      // number of seconds passed in as a parameter, after the last fetch from the Firebase Remote
      // Config server would use values returned during the last fetch.
      // More info at https://bit.ly/2XJdvuT.

      // NOT recommended for apps in production. Uncomment to use for app development and testing
      /*firebaseRemoteConfig.setConfigSettingsAsync(new FirebaseRemoteConfigSettings.Builder()
          .setMinimumFetchIntervalInSeconds(5)
          .build());*/

      // Set default Remote Config parameter values. The app uses the in-app default values, and
      // when you need to adjust those defaults, you set an updated value for only the values you
      // want to change in the Firebase console.
      firebaseRemoteConfig.setDefaults(R.xml.cache_management_remote_config_defaults);

      // Fetch the latest Remote Config values from Firebase and then adjust the cache if needed.
      firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(this,
          new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
              if (task.isSuccessful()) {
                adjustCacheBasedOnRemoteConfigServerSettings();
              } else {
                Logger.d("CacheManagementActivity", "Couldn\'t get Firebase cache "
                    + "management remote config settings from Firebase.");
              }
            }
          });
    }

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull MapboxMap mapboxMap) {
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            findViewById(R.id.invalidate_ambient_cache_button).setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                invalidateAmbientCache();
              }
            });

            findViewById(R.id.set_max_ambient_cache_size_button).setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                try {
                  long newMaxAmbientCacheSize = Long.valueOf(
                      newMaxAmbientCacheSizeTextView.getText().toString());
                  setNewMaxAmbientCache(newMaxAmbientCacheSize);
                } catch (NumberFormatException exception) {
                  Logger.d("CacheManagementActivity", "%s", exception);
                }
              }
            });

            findViewById(R.id.clear_ambient_cache_button).setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                clearAmbientCache();
              }
            });

            findViewById(R.id.reset_database_button).setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                resetDatabase();
              }
            });
          }
        });
      }
    });
  }

  /**
   * Invalidate the ambient cache. See
   * https://docs.mapbox.com/android/maps/overview/offline/#cache-management for more
   * information about this method.
   */
  private void invalidateAmbientCache() {
    OfflineManager fileSource = OfflineManager.getInstance(CacheManagementActivity.this);
    fileSource.invalidateAmbientCache(new OfflineManager.FileSourceCallback() {
      @Override
      public void onSuccess() {
        showToast(getString(R.string.cache_invalidated_toast_confirmation));
      }

      @Override
      public void onError(@NonNull String message) {
        showToast(String.format(getString(
            R.string.cache_invalidated_toast_error), message));
      }
    });
  }

  /**
   * Set a new maximum size of the ambient cache. See
   * https://docs.mapbox.com/android/maps/overview/offline/#cache-management for more
   * information about this method.
   */
  private void setNewMaxAmbientCache(long newMaxAmbientCacheSize) {
    OfflineManager fileSource = OfflineManager.getInstance(CacheManagementActivity.this);
    if (newMaxAmbientCacheSize < 0) {
      showToast(getString(R.string.invalid_max_ambient_cache_size));
      return;
    }

    fileSource.setMaximumAmbientCacheSize(newMaxAmbientCacheSize,
        new OfflineManager.FileSourceCallback() {
          @Override
          public void onSuccess() {
            Toast.makeText(CacheManagementActivity.this, String.format(
                getString(R.string.set_max_ambient_cache_size_toast_confirmation),
                newMaxAmbientCacheSize), Toast.LENGTH_SHORT).show();
          }

          @Override
          public void onError(@NonNull String message) {
            showToast(String.format(getString(
                R.string.set_max_ambient_cache_size_toast_error), message));
          }
        });
  }

  /**
   * Clear the ambient cache. See
   * https://docs.mapbox.com/android/maps/overview/offline/#cache-management for more
   * information about this method.
   */
  private void clearAmbientCache() {
    OfflineManager fileSource = OfflineManager.getInstance(CacheManagementActivity.this);
    fileSource.clearAmbientCache(new OfflineManager.FileSourceCallback() {
      @Override
      public void onSuccess() {
        showToast(getString(R.string.clear_ambient_cache_size_toast_confirmation));
      }

      @Override
      public void onError(@NonNull String message) {
        showToast(String.format(getString(
            R.string.clear_ambient_cache_size_toast_error), message));
      }
    });
  }

  /**
   * Reset the Maps SDK's database. See
   * https://docs.mapbox.com/android/maps/overview/offline/#cache-management for more
   * information about this method.
   */
  private void resetDatabase() {
    OfflineManager fileSource = OfflineManager.getInstance(CacheManagementActivity.this);
    fileSource.resetDatabase(new OfflineManager.FileSourceCallback() {
      @Override
      public void onSuccess() {
        showToast(getString(R.string.reset_database_toast_confirmation));
      }

      @Override
      public void onError(@NonNull String message) {
        showToast(String.format(getString(
            R.string.reset_database_toast_error), message));
      }
    });
  }

  /**
   * Use Remote Config boolean values to make appropriate adjustments to the Maps SDK cache.
   */
  private void adjustCacheBasedOnRemoteConfigServerSettings() {
    if (firebaseRemoteConfig.getBoolean(INVALIDATE_AMBIENT_CACHE_KEY)) {
      invalidateAmbientCache();
    }
    if (firebaseRemoteConfig.getBoolean(SET_MAX_AMBIENT_CACHE_SIZE_BOOLEAN_KEY)) {
      setNewMaxAmbientCache(firebaseRemoteConfig.getLong(SET_MAX_AMBIENT_CACHE_SIZE_AMOUNT_KEY));
    }
    if (firebaseRemoteConfig.getBoolean(CLEAR_AMBIENT_CACHE_KEY)) {
      clearAmbientCache();
    }
    if (firebaseRemoteConfig.getBoolean(RESET_DATABASE_REMOTE_CONFIG_KEY)) {
      resetDatabase();
    }
  }

  private void showToast(String textToShow) {
    CacheManagementActivity.this.runOnUiThread(new Runnable() {
      public void run() {
        Toast.makeText(CacheManagementActivity.this,
            textToShow, Toast.LENGTH_SHORT).show();
      }
    });
  }

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
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}