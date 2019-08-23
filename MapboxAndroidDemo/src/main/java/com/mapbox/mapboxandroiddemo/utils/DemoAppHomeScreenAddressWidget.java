package com.mapbox.mapboxandroiddemo.utils;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.ContextWrapper;
import android.location.Location;
import androidx.annotation.NonNull;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;

import java.lang.ref.WeakReference;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.os.Looper.getMainLooper;

/**
 * A home screen widget. The widget retrieves the device's location,
 * makes a reverse geocoding request to the Mapbox Geocoding API, and displays the real-world
 * address that's associated with the device's location.
 */
public class DemoAppHomeScreenAddressWidget extends AppWidgetProvider implements
  PermissionsListener {

  private Context context;
  private LocationChangeListeningActivityLocationCallback callback;
  private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
  private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    this.context = context;
    this.callback = new LocationChangeListeningActivityLocationCallback(context, appWidgetManager, appWidgetIds[0]);
    checkPermissions(context);
  }

  @Override
  public void onEnabled(Context context) {
    // Add message/UI here if you'd like
  }

  @Override
  public void onPermissionResult(boolean granted) {
    if (granted) {
      initializeLocationEngine(context);
    } else {
      Toast.makeText(context, R.string.user_location_permission_not_granted, Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * Check whether location permissions are granted and then eventually initialize the LocationEngine
   *
   * @param context the application's context
   */
  private void checkPermissions(Context context) {
    // Check if permissions are enabled and if not request
    try {
      if (PermissionsManager.areLocationPermissionsGranted(context)) {
        // Create a location engine instance
        initializeLocationEngine(context);
      } else {
        PermissionsManager permissionsManager = new PermissionsManager(this);
        permissionsManager.requestLocationPermissions(getActivity(context));
      }
    } catch (NullPointerException nullPointerException) {
      Toast.makeText(context, context.getString(R.string.enable_location), Toast.LENGTH_LONG).show();
    }
  }

  /**
   * Initialize the LocationEngine so that the device's current location can be retrieved
   *
   * @param context the application's context
   */
  @SuppressWarnings( {"MissingPermission"})
  private void initializeLocationEngine(Context context) {
    LocationEngine locationEngine = LocationEngineProvider.getBestLocationEngine(context);
    LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
      .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
      .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

    locationEngine.requestLocationUpdates(request, callback, getMainLooper());
    locationEngine.getLastLocation(callback);
  }

  public Activity getActivity(Context context) {
    if (context == null) {
      return null;
    } else if (context instanceof ContextWrapper) {
      if (context instanceof Activity) {
        return (Activity) context;
      } else {
        return getActivity(((ContextWrapper) context).getBaseContext());
      }
    }
    return null;
  }

  @Override
  public void onDisabled(Context context) {
    // Add message/UI here if you'd like
  }


  @Override
  public void onExplanationNeeded(List<String> permissionsToExplain) {
    // Add explanation here if you'd like
    Toast.makeText(context, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
  }

  private static class LocationChangeListeningActivityLocationCallback
    implements LocationEngineCallback<LocationEngineResult> {

    private final WeakReference<Context> activityWeakReference;
    private AppWidgetManager appWidgetManager;
    private int singleWidgetId;

    LocationChangeListeningActivityLocationCallback(Context context, AppWidgetManager appWidgetManager,
                                                    int singleWidgetId) {
      this.activityWeakReference = new WeakReference<>(context);
      this.appWidgetManager = appWidgetManager;
      this.singleWidgetId = singleWidgetId;
    }

    /**
     * The LocationEngineCallback interface's method which fires when the device's location has changed.
     *
     * @param result the LocationEngineResult object which has the last known location within it.
     */
    @Override
    public void onSuccess(LocationEngineResult result) {
      final Context context = activityWeakReference.get();

      if (context != null) {
        Location lastKnownLocation = result.getLastLocation();

        if (lastKnownLocation != null) {
          // Build a Mapbox reverse geocode request
          MapboxGeocoding reverseGeocode = MapboxGeocoding.builder()
            .accessToken(context.getString(R.string.access_token))
            .query(Point.fromLngLat(lastKnownLocation.getLongitude(), lastKnownLocation.getLatitude()))
            .build();

          reverseGeocode.enqueueCall(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
              try {
                if (response.body() != null) {
                  List<CarmenFeature> carmenFeatureList = response.body().features();

                  String textForWidgetTextView;

                  // Check that the reverse geocoding response has a place name to display
                  if (carmenFeatureList.size() > 0 && !carmenFeatureList.get(0).placeName().isEmpty()) {

                    // Parse through the response to get the place name
                    textForWidgetTextView = carmenFeatureList.get(0).placeName();

                    // Construct the RemoteViews object
                    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.demo_app_home_screen_widget);
                    views.setTextViewText(R.id.device_location_textview, textForWidgetTextView);

                    // Instruct the widget manager to update the widget
                    appWidgetManager.updateAppWidget(singleWidgetId, views);

                  } else {
                    textForWidgetTextView = context.getString(R.string.no_place_name_for_home_screen_widget);
                    Timber.d("onResponse: no place name");
                  }

                  // Construct the RemoteViews object
                  RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.demo_app_home_screen_widget);
                  views.setTextViewText(R.id.device_location_textview, textForWidgetTextView);

                  // Instruct the widget manager to update the widget
                  appWidgetManager.updateAppWidget(singleWidgetId, views);
                }
              } catch (NullPointerException exception) {
                Timber.d("onResponse: exception = %s", exception);
              }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
              Timber.d("onFailure: geocoding failure");
              Toast.makeText(context, R.string.reverse_geocode_failure, Toast.LENGTH_LONG).show();
              throwable.printStackTrace();
            }
          });
        }
      }
    }

    /**
     * The LocationEngineCallback interface's method which fires when the device's location can not be captured
     *
     * @param exception the exception message
     */
    @Override
    public void onFailure(@NonNull Exception exception) {
      Context context = activityWeakReference.get();
      if (context != null) {
        Toast.makeText(context, exception.getLocalizedMessage(),
          Toast.LENGTH_SHORT).show();
      }
    }
  }
}