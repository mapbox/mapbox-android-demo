package com.mapbox.mapboxandroiddemo.examples.query;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.turf.TurfJoins;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_ROUND;
import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

/**
 * Use the Android system {@link android.view.View.OnTouchListener} to draw
 * an area and perform a search for features in that area.
 */
public class FingerDrawActivity extends AppCompatActivity {

  private static final String SEARCH_DATA_SYMBOL_LAYER_SOURCE_ID = "SEARCH_DATA_SYMBOL_LAYER_SOURCE_ID";
  private static final String FREEHAND_DRAW_LINE_LAYER_SOURCE_ID = "FREEHAND_DRAW_LINE_LAYER_SOURCE_ID";
  private static final String MARKER_SYMBOL_LAYER_SOURCE_ID = "MARKER_SYMBOL_LAYER_SOURCE_ID";
  private static final String FREEHAND_DRAW_FILL_LAYER_SOURCE_ID = "FREEHAND_DRAW_FILL_LAYER_SOURCE_ID";
  private static final String FREEHAND_DRAW_LINE_LAYER_ID = "FREEHAND_DRAW_LINE_LAYER_ID";
  private static final String FREEHAND_DRAW_FILL_LAYER_ID = "FREEHAND_DRAW_FILL_LAYER_ID";
  private static final String SEARCH_DATA_SYMBOL_LAYER_ID = "SEARCH_DATA_SYMBOL_LAYER_ID";
  private static final String SEARCH_DATA_MARKER_ID = "SEARCH_DATA_MARKER_ID";
  private MapView mapView;
  private MapboxMap mapboxMap;
  private FeatureCollection searchPointFeatureCollection;
  private GeoJsonSource drawLineSource;
  private GeoJsonSource fillPolygonSource;
  private List<Point> freehandDrawLineLayerPointList;
  private List<List<Point>> polygonList;

  /**
   * Customize search UI with these booleans
   */
  private boolean fillSearchAreaWithPolygonWhileDrawing = true;
  private boolean fillSearchAreaWithPolygon = true;
  private boolean closePolygonSearchAreaOnceDrawingIsDone = true;
  private boolean showSearchDataLocations = true;

  private View.OnTouchListener customOnTouchListener = new View.OnTouchListener() {
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

      LatLng latLngTouchCoordinate = mapboxMap.getProjection().fromScreenLocation(
        new PointF(motionEvent.getX(), motionEvent.getY()));

      Point touchPoint = Point.fromLngLat(latLngTouchCoordinate.getLongitude(), latLngTouchCoordinate.getLatitude());

      if (freehandDrawLineLayerPointList != null) {

        // Draw the line as drawing on the map happens
        freehandDrawLineLayerPointList.add(touchPoint);
        drawLineSource = mapboxMap.getStyle().getSourceAs(FREEHAND_DRAW_LINE_LAYER_SOURCE_ID);
        drawLineSource.setGeoJson(LineString.fromLngLats(freehandDrawLineLayerPointList));

        if (fillSearchAreaWithPolygonWhileDrawing) {
          drawSearchFillArea();
        }

        // Take certain actions when the drawing is done
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
          if (closePolygonSearchAreaOnceDrawingIsDone) {
            freehandDrawLineLayerPointList.add(freehandDrawLineLayerPointList.get(0));
          }

          if (!fillSearchAreaWithPolygonWhileDrawing && fillSearchAreaWithPolygon) {
            drawSearchFillArea();
          }

          if (closePolygonSearchAreaOnceDrawingIsDone) {
            FeatureCollection pointsInSearchAreaFeatureCollection =
              TurfJoins.pointsWithinPolygon(searchPointFeatureCollection,
                FeatureCollection.fromFeature(Feature.fromGeometry(
                  Polygon.fromLngLats(polygonList))));
            if (VISIBLE.equals(mapboxMap.getStyle().getLayer(
              SEARCH_DATA_SYMBOL_LAYER_ID).getVisibility().getValue())) {
              Toast.makeText(FingerDrawActivity.this, String.format(
                getString(R.string.search_result_size),
                pointsInSearchAreaFeatureCollection.features().size()), Toast.LENGTH_SHORT).show();
            }
          }
          enableMapMovement();
        }
      }
      return true;
    }
  };

  private void drawSearchFillArea() {
    // Create and show a FillLayer polygon where the search area is
    fillPolygonSource = mapboxMap.getStyle().getSourceAs(FREEHAND_DRAW_FILL_LAYER_SOURCE_ID);
    polygonList = new ArrayList<>();
    polygonList.add(freehandDrawLineLayerPointList);
    fillPolygonSource.setGeoJson(Polygon.fromLngLats(polygonList));
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_finger_drag_draw);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {

            FingerDrawActivity.this.mapboxMap = mapboxMap;

            if (showSearchDataLocations) {
              new LoadGeoJson(FingerDrawActivity.this).execute();
            } else {
              setUpExample(null);
            }

            findViewById(R.id.clear_map_for_new_draw_fab)
              .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                  // Reset ArrayLists
                  polygonList = new ArrayList<>();
                  freehandDrawLineLayerPointList = new ArrayList<>();

                  // Add empty Feature array to the sources
                  drawLineSource = mapboxMap.getStyle().getSourceAs(FREEHAND_DRAW_LINE_LAYER_SOURCE_ID);
                  if (drawLineSource != null) {
                    drawLineSource.setGeoJson(FeatureCollection.fromFeatures(new Feature[]{}));
                  }

                  fillPolygonSource = mapboxMap.getStyle().getSourceAs(FREEHAND_DRAW_FILL_LAYER_SOURCE_ID);
                  if (fillPolygonSource != null) {
                    fillPolygonSource.setGeoJson(FeatureCollection.fromFeatures(new Feature[]{}));
                  }

                  // Reset camera position to default location
                  mapboxMap.easeCamera(CameraUpdateFactory
                    .newCameraPosition(new CameraPosition.Builder()
                      .target(new LatLng(35.087497, -106.651261))
                      .zoom(11.679132)
                      .tilt(0)
                      .bearing(0)
                      .build()));

                  enabledMapDrawing();
                }
              });
          }
        });
      }
    });
  }

  /**
   * Enable moving the map
   */
  private void enableMapMovement() {
    mapView.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
      }
    });
    mapboxMap.getUiSettings().setAllGesturesEnabled(true);
  }

  /**
   * Enable drawing on the map by setting the custom touch listener on the {@link MapView}
   */
  private void enabledMapDrawing() {
    mapView.setOnTouchListener(customOnTouchListener);
    mapboxMap.getUiSettings().setAllGesturesEnabled(false);
  }

  private void setUpExample(FeatureCollection searchDataFeatureCollection) {

    searchPointFeatureCollection = searchDataFeatureCollection;

    Style style = mapboxMap.getStyle();

    if (style != null) {
      freehandDrawLineLayerPointList = new ArrayList<>();

      style.addImage(SEARCH_DATA_MARKER_ID, BitmapFactory.decodeResource(
        FingerDrawActivity.this.getResources(), R.drawable.blue_marker_view));

      // Add sources to the map
      style.addSource(new GeoJsonSource(SEARCH_DATA_SYMBOL_LAYER_SOURCE_ID, searchDataFeatureCollection));
      style.addSource(new GeoJsonSource(FREEHAND_DRAW_LINE_LAYER_SOURCE_ID));
      style.addSource(new GeoJsonSource(MARKER_SYMBOL_LAYER_SOURCE_ID));
      style.addSource(new GeoJsonSource(FREEHAND_DRAW_FILL_LAYER_SOURCE_ID));

      style.addLayer(new SymbolLayer(SEARCH_DATA_SYMBOL_LAYER_ID, SEARCH_DATA_SYMBOL_LAYER_SOURCE_ID).withProperties(
        iconImage(SEARCH_DATA_MARKER_ID),
        iconAllowOverlap(true),
        iconOffset(new Float[]{0f, -8f}),
        iconIgnorePlacement(true))
      );

      // Add freehand draw LineLayer to the map
      style.addLayerBelow(new LineLayer(FREEHAND_DRAW_LINE_LAYER_ID, FREEHAND_DRAW_LINE_LAYER_SOURCE_ID).withProperties(
        lineWidth(5f),
        lineJoin(LINE_JOIN_ROUND),
        lineOpacity(1f),
        lineColor(Color.parseColor("#a0861c"))), SEARCH_DATA_SYMBOL_LAYER_ID
      );

      // Add freehand draw polygon FillLayer to the map
      style.addLayerBelow(new FillLayer(FREEHAND_DRAW_FILL_LAYER_ID, FREEHAND_DRAW_FILL_LAYER_SOURCE_ID).withProperties(
        fillColor(Color.RED),
        fillOpacity(.4f)), FREEHAND_DRAW_LINE_LAYER_ID
      );

      enabledMapDrawing();

      if (showSearchDataLocations) {
        findViewById(R.id.show_search_data_points_fab).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {

            // Toggle the visibility of the fake data point SymbolLayer icons
            Layer dataLayer = mapboxMap.getStyle().getLayer(SEARCH_DATA_SYMBOL_LAYER_ID);
            if (dataLayer != null) {
              dataLayer.setProperties(
                VISIBLE.equals(dataLayer.getVisibility().getValue()) ? visibility(NONE) : visibility(VISIBLE));
            }
          }
        });
      }

      Toast.makeText(FingerDrawActivity.this,
        getString(R.string.draw_instruction), Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * Use an AsyncTask to retrieve GeoJSON data from a file in the assets folder.
   */
  private static class LoadGeoJson extends AsyncTask<Void, Void, FeatureCollection> {

    private WeakReference<FingerDrawActivity> weakReference;

    LoadGeoJson(FingerDrawActivity activity) {
      this.weakReference = new WeakReference<>(activity);
    }

    @Override
    protected FeatureCollection doInBackground(Void... voids) {
      try {
        FingerDrawActivity activity = weakReference.get();
        if (activity != null) {
          InputStream inputStream = activity.getAssets().open("albuquerque_locations.geojson");
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
      FingerDrawActivity activity = weakReference.get();
      if (activity != null && featureCollection != null) {
        activity.setUpExample(featureCollection);
      }
    }
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
