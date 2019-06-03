package com.mapbox.mapboxandroiddemo.examples.dds;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.turf.TurfMeta;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

public class RevealedPolygonHoleOutlineActivity extends AppCompatActivity implements OnMapReadyCallback {

  private static final String FILL_SOURCE_ID = "SOURCE-ID";
  private static final String LINE_SOURCE_ID = "LINE_SOURCE_ID";
  private static final String FILL_LAYER_ID = "FILL-LAYER-ID";
  private static final String LINE_LAYER_ID = "LINE-LAYER-ID";
  private static final float FILL_OPACITY = .7f;
  private static final float LINE_WIDTH = 5f;
  private static final int GREY_COLOR = Color.parseColor("#c2c2c2");
  private static final int RED_COLOR = Color.parseColor("#BF544C");
  private MapView mapView;
  private MapboxMap mapboxMap;
  private static final List<Point> OUTER_POLYGON_COORDINATES = new ArrayList<Point>() {
    {
      add(Point.fromLngLat(-121.9921875, 37.27787748952485));
      add(Point.fromLngLat(-121.79580688476562, 37.27787748952485));
      add(Point.fromLngLat(-121.79580688476562, 37.40452830389465));
      add(Point.fromLngLat(-121.9921875, 37.40452830389465));
      add(Point.fromLngLat(-121.9921875, 37.27787748952485));
    }
  };
  private static final LatLngBounds RESTRICTED_BOUNDS_AREA = new LatLngBounds.Builder()
    .include(new LatLng(37.27787748952485, -121.9921875))
    .include(new LatLng(37.40452830389465, -121.79580688476562))
    .build();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_polygon_holes_outline_revealed);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(final MapboxMap map) {
    this.mapboxMap = map;
    // Set the boundary area for the map camera
    mapboxMap.setLatLngBoundsForCameraTarget(RESTRICTED_BOUNDS_AREA);

    map.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        new LoadGeoJson(RevealedPolygonHoleOutlineActivity.this).execute();
      }
    });
  }

  private void addStyling(@NonNull List<Point> pointList) {
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style loadedStyle) {
        List<LineString> innerList = new ArrayList<>();
        LineString innerLineString = LineString.fromLngLats(pointList);
        innerList.add(innerLineString);

        loadedStyle.addSource(new GeoJsonSource(FILL_SOURCE_ID,
          Polygon.fromOuterInner(LineString.fromLngLats(OUTER_POLYGON_COORDINATES), innerList)));

        loadedStyle.addSource(new GeoJsonSource(LINE_SOURCE_ID, innerLineString));

        loadedStyle.addLayerBelow(new FillLayer(FILL_LAYER_ID, FILL_SOURCE_ID).withProperties(
          fillColor(GREY_COLOR),
          fillOpacity(FILL_OPACITY)), "road-street");

        loadedStyle.addLayer(new LineLayer(LINE_LAYER_ID, LINE_SOURCE_ID).withProperties(
          lineColor(RED_COLOR),
          lineWidth(LINE_WIDTH),
          lineCap(Property.LINE_CAP_ROUND),
          lineJoin(Property.LINE_JOIN_ROUND)
        ));
      }
    });
  }

  private static class LoadGeoJson extends AsyncTask<Void, Void, List<Point>> {

    private WeakReference<RevealedPolygonHoleOutlineActivity> weakReference;

    LoadGeoJson(RevealedPolygonHoleOutlineActivity activity) {
      this.weakReference = new WeakReference<>(activity);
    }

    @Override
    protected List<Point> doInBackground(Void... voids) {
      try {
        RevealedPolygonHoleOutlineActivity activity = weakReference.get();
        if (activity != null) {
          InputStream inputStream = activity.getAssets().open("downtown_san_jose_hole.geojson");
          Feature holePolygonFeature = Feature.fromJson(convertStreamToString(inputStream));
          return TurfMeta.coordAll((Polygon) holePolygonFeature.geometry(), false);
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
    protected void onPostExecute(@Nullable List<Point> pointList) {
      super.onPostExecute(pointList);
      RevealedPolygonHoleOutlineActivity activity = weakReference.get();
      if (activity != null && pointList != null) {
        activity.addStyling(pointList);
      }
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }
}