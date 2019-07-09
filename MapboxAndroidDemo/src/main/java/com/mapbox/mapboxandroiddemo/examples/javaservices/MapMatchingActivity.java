package com.mapbox.mapboxandroiddemo.examples.javaservices;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.api.matching.v5.MapboxMapMatching;
import com.mapbox.api.matching.v5.models.MapMatchingMatching;
import com.mapbox.api.matching.v5.models.MapMatchingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.ColorUtils;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.api.directions.v5.DirectionsCriteria.PROFILE_DRIVING;
import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

/**
 * Match raw GPS points to the map so they align with roads and pathways.
 */
public class MapMatchingActivity extends AppCompatActivity {

  private static final String TAG = "MapMatchingActivity";

  private MapView mapView;
  private MapboxMap map;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_javaservices_map_matching);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull MapboxMap mapboxMap) {
        map = mapboxMap;
        map.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            new LoadGeoJson(MapMatchingActivity.this).execute();
          }
        });
      }
    });
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
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
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

  private static class LoadGeoJson extends AsyncTask<Void, Void, FeatureCollection> {

    private WeakReference<MapMatchingActivity> weakReference;

    LoadGeoJson(MapMatchingActivity activity) {
      this.weakReference = new WeakReference<>(activity);
    }

    @Override
    protected FeatureCollection doInBackground(Void... voids) {
      try {
        MapMatchingActivity activity = weakReference.get();
        if (activity != null) {
          InputStream inputStream = activity.getAssets().open("trace.geojson");
          return FeatureCollection.fromJson(convertStreamToString(inputStream));
        }
      } catch (Exception exception) {
        Timber.e("Exception Loading GeoJSON: %s", exception.toString());
      }
      return null;
    }

    static String convertStreamToString(InputStream is) {
      Scanner scanner = new Scanner(is).useDelimiter("\\A");
      return scanner.hasNext() ? scanner.next() : "";
    }

    @Override
    protected void onPostExecute(@Nullable FeatureCollection featureCollection) {
      super.onPostExecute(featureCollection);
      MapMatchingActivity activity = weakReference.get();
      if (activity != null && featureCollection != null) {
        activity.drawLines(featureCollection);
      }
    }
  }

  private void drawLines(@NonNull FeatureCollection featureCollection) {
    List<Feature> features = featureCollection.features();
    if (features != null && features.size() > 0) {
      Feature feature = features.get(0);
      drawBeforeMapMatching(feature);
      requestMapMatched(feature);
    }
  }

  private void drawBeforeMapMatching(Feature feature) {
    map.getStyle(style -> {
      style.addSource(new GeoJsonSource("pre-matched-source-id", feature));
      style.addLayer(new LineLayer("pre-matched-layer-id", "pre-matched-source-id").withProperties(
        lineColor(ColorUtils.colorToRgbaString(Color.parseColor("#c14a00"))),
        lineWidth(6f),
        lineOpacity(1f)
      ));
    });
  }

  private void requestMapMatched(Feature feature) {
    List<Point> points = ((LineString) Objects.requireNonNull(feature.geometry())).coordinates();

    try {
      // Setup the request using a client.
      MapboxMapMatching client = MapboxMapMatching.builder()
        .accessToken(Objects.requireNonNull(Mapbox.getAccessToken()))
        .profile(PROFILE_DRIVING)
        .coordinates(points)
        .build();

      // Execute the API call and handle the response.
      client.enqueueCall(new Callback<MapMatchingResponse>() {
        @Override
        public void onResponse(@NonNull Call<MapMatchingResponse> call,
                               @NonNull Response<MapMatchingResponse> response) {
          if (response.code() == 200) {
            drawMapMatched(Objects.requireNonNull(response.body()).matchings());
          } else {
            // If the response code does not response "OK" an error has occurred.
            Timber.e("MapboxMapMatching failed with %s", response.code());
          }
        }

        @Override
        public void onFailure(Call<MapMatchingResponse> call, Throwable throwable) {
          Timber.e(throwable, "MapboxMapMatching error");
        }
      });
    } catch (ServicesException servicesException) {
      Timber.e(servicesException, "MapboxMapMatching error");
    }
  }

  private void drawMapMatched(@NonNull List<MapMatchingMatching> matchings) {
    Style style = map.getStyle();
    if (style != null && !matchings.isEmpty()) {
      style.addSource(new GeoJsonSource("matched-source-id", Feature.fromGeometry(LineString.fromPolyline(
        Objects.requireNonNull(matchings.get(0).geometry()), PRECISION_6)))
      );
      style.addLayer(new LineLayer("matched-layer-id", "matched-source-id")
        .withProperties(
          lineColor(ColorUtils.colorToRgbaString(Color.parseColor("#3bb2d0"))),
          lineWidth(6f))
      );
    }
  }
}
