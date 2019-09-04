package com.mapbox.mapboxandroiddemo.examples.dds;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Scanner;

import timber.log.Timber;

/**
 * Draw a polyline by parsing a GeoJSON file with the Mapbox Android SDK.
 */
public class DrawGeojsonLineActivity extends AppCompatActivity implements OnMapReadyCallback {

  private MapView mapView;
  private MapboxMap mapboxMap;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_dds_draw_geojson_line);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        new LoadGeoJson(DrawGeojsonLineActivity.this).execute();
      }
    });
  }

  private void drawLines(@NonNull FeatureCollection featureCollection) {
    if (mapboxMap != null) {
      mapboxMap.getStyle(style -> {
        if (featureCollection.features() != null) {
          if (featureCollection.features().size() > 0) {
            style.addSource(new GeoJsonSource("line-source", featureCollection));

            // The layer properties for our line. This is where we make the line dotted, set the
            // color, etc.
            style.addLayer(new LineLayer("linelayer", "line-source")
              .withProperties(PropertyFactory.lineCap(Property.LINE_CAP_SQUARE),
                PropertyFactory.lineJoin(Property.LINE_JOIN_MITER),
                PropertyFactory.lineOpacity(.7f),
                PropertyFactory.lineWidth(7f),
                PropertyFactory.lineColor(Color.parseColor("#3bb2d0"))));
          }
        }
      });
    }
  }

  private static class LoadGeoJson extends AsyncTask<Void, Void, FeatureCollection> {

    private WeakReference<DrawGeojsonLineActivity> weakReference;

    LoadGeoJson(DrawGeojsonLineActivity activity) {
      this.weakReference = new WeakReference<>(activity);
    }

    @Override
    protected FeatureCollection doInBackground(Void... voids) {
      try {
        DrawGeojsonLineActivity activity = weakReference.get();
        if (activity != null) {
          InputStream inputStream = activity.getAssets().open("example.geojson");
          return FeatureCollection.fromJson(convertStreamToString(inputStream));
        }
      } catch (Exception exception) {
        Timber.e("Exception Loading GeoJSON: %s" , exception.toString());
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
      DrawGeojsonLineActivity activity = weakReference.get();
      if (activity != null && featureCollection != null) {
        activity.drawLines(featureCollection);
      }
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
  public void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }
}
