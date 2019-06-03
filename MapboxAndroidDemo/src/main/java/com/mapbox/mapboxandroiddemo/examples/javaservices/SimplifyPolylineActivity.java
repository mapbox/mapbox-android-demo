package com.mapbox.mapboxandroiddemo.examples.javaservices;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.utils.PolylineUtils;
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

import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

/**
 * Using the polylines utility, simplify a polyline at a
 * given tolerance to reduce the number of coordinates in that polyline.
 */
public class SimplifyPolylineActivity extends AppCompatActivity {

  private static final String TAG = "SimplifyLineActivity";

  private MapView mapView;
  private MapboxMap map;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_simplify_polyline);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull MapboxMap mapboxMap) {
        map = mapboxMap;
        mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            new DrawGeoJson(SimplifyPolylineActivity.this).execute();
          }
        });
      }
    });
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
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  private static class DrawGeoJson extends AsyncTask<Void, Void, FeatureCollection> {

    private WeakReference<SimplifyPolylineActivity> weakReference;

    DrawGeoJson(SimplifyPolylineActivity activity) {
      this.weakReference = new WeakReference<>(activity);
    }

    @Override
    protected FeatureCollection doInBackground(Void... voids) {
      try {
        SimplifyPolylineActivity activity = weakReference.get();
        if (activity != null) {
          InputStream inputStream = activity.getAssets().open("matched_route.geojson");
          return FeatureCollection.fromJson(convertStreamToString(inputStream));
        }
      } catch (Exception exception) {
        Timber.e("Exception loading GeoJSON: %s", exception.toString());
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
      SimplifyPolylineActivity activity = weakReference.get();
      if (activity != null && featureCollection != null) {
        activity.drawLines(featureCollection);
      }
    }
  }

  private void drawLines(@NonNull FeatureCollection featureCollection) {
    List<Feature> features = featureCollection.features();
    if (features != null && features.size() > 0) {
      Feature feature = features.get(0);
      drawBeforeSimplify(feature);
      drawSimplify(feature);
    }
  }

  private void drawBeforeSimplify(@NonNull Feature lineStringFeature) {
    addLine("rawLine", lineStringFeature, "#8a8acb");
  }

  private void drawSimplify(@NonNull Feature feature) {
    List<Point> points = ((LineString) Objects.requireNonNull(feature.geometry())).coordinates();
    List<Point> after = PolylineUtils.simplify(points, 0.001);
    addLine("simplifiedLine", Feature.fromGeometry(LineString.fromLngLats(after)), "#3bb2d0");
  }

  private void addLine(String layerId, Feature feature, String lineColorHex) {
    map.getStyle(style -> {
      style.addSource(new GeoJsonSource(layerId, feature));
      style.addLayer(new LineLayer(layerId, layerId).withProperties(
        lineColor(ColorUtils.colorToRgbaString(Color.parseColor(lineColorHex))),
        lineWidth(4f)
      ));
    });
  }
}
