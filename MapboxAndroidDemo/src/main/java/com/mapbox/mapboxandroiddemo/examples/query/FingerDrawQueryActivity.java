package com.mapbox.mapboxandroiddemo.examples.query;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
 * an polygon and/or a line. Also perform a search for data points within the drawn polygon area.
 */
public class FingerDrawQueryActivity extends AppCompatActivity {

  private static final String SEARCH_DATA_SYMBOL_LAYER_SOURCE_ID = "SEARCH_DATA_SYMBOL_LAYER_SOURCE_ID";
  private static final String FREEHAND_DRAW_LINE_LAYER_SOURCE_ID = "FREEHAND_DRAW_LINE_LAYER_SOURCE_ID";
  private static final String MARKER_SYMBOL_LAYER_SOURCE_ID = "MARKER_SYMBOL_LAYER_SOURCE_ID";
  private static final String FREEHAND_DRAW_FILL_LAYER_SOURCE_ID = "FREEHAND_DRAW_FILL_LAYER_SOURCE_ID";
  private static final String FREEHAND_DRAW_LINE_LAYER_ID = "FREEHAND_DRAW_LINE_LAYER_ID";
  private static final String FREEHAND_DRAW_FILL_LAYER_ID = "FREEHAND_DRAW_FILL_LAYER_ID";
  private static final String SEARCH_DATA_SYMBOL_LAYER_ID = "SEARCH_DATA_SYMBOL_LAYER_ID";
  private static final String SEARCH_DATA_MARKER_ID = "SEARCH_DATA_MARKER_ID";
  private static final String LINE_COLOR = "#a0861c";
  private static final float LINE_WIDTH = 5f;
  private static final float LINE_OPACITY = 1f;
  private static final float FILL_OPACITY = .4f;

  private MapView mapView;
  private MapboxMap mapboxMap;
  private FeatureCollection searchPointFeatureCollection;
  private List<Point> freehandTouchPointListForPolygon = new ArrayList<>();
  private List<Point> freehandTouchPointListForLine = new ArrayList<>();
  private boolean showSearchDataLocations = true;
  private boolean drawSingleLineOnly = false;

  private View.OnTouchListener customOnTouchListener = new View.OnTouchListener() {
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

      LatLng latLngTouchCoordinate = mapboxMap.getProjection().fromScreenLocation(
          new PointF(motionEvent.getX(), motionEvent.getY()));

      Point screenTouchPoint = Point.fromLngLat(latLngTouchCoordinate.getLongitude(),
          latLngTouchCoordinate.getLatitude());

      // Draw the line on the map as the finger is dragged along the map
      freehandTouchPointListForLine.add(screenTouchPoint);
      mapboxMap.getStyle(new Style.OnStyleLoaded() {
        @Override
        public void onStyleLoaded(@NonNull Style style) {
          GeoJsonSource drawLineSource = style.getSourceAs(FREEHAND_DRAW_LINE_LAYER_SOURCE_ID);
          if (drawLineSource != null) {
            drawLineSource.setGeoJson(LineString.fromLngLats(freehandTouchPointListForLine));
          }

          // Draw a polygon area if drawSingleLineOnly == false
          if (!drawSingleLineOnly) {
            if (freehandTouchPointListForPolygon.size() < 2) {
              freehandTouchPointListForPolygon.add(screenTouchPoint);
            } else if (freehandTouchPointListForPolygon.size() == 2) {
              freehandTouchPointListForPolygon.add(screenTouchPoint);
              freehandTouchPointListForPolygon.add(freehandTouchPointListForPolygon.get(0));
            } else {
              freehandTouchPointListForPolygon.remove(freehandTouchPointListForPolygon.size() - 1);
              freehandTouchPointListForPolygon.add(screenTouchPoint);
              freehandTouchPointListForPolygon.add(freehandTouchPointListForPolygon.get(0));
            }
          }

          // Create and show a FillLayer polygon where the search area is
          GeoJsonSource fillPolygonSource = style.getSourceAs(FREEHAND_DRAW_FILL_LAYER_SOURCE_ID);
          List<List<Point>> polygonList = new ArrayList<>();
          polygonList.add(freehandTouchPointListForPolygon);
          Polygon drawnPolygon = Polygon.fromLngLats(polygonList);
          if (fillPolygonSource != null) {
            fillPolygonSource.setGeoJson(drawnPolygon);
          }

          // Take certain actions when the drawing is done
          if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

            // If drawing polygon, add the first screen touch point to the end of
            // the LineLayer list so that it's
            if (!drawSingleLineOnly) {
              freehandTouchPointListForLine.add(freehandTouchPointListForPolygon.get(0));
            }

            if (showSearchDataLocations && !drawSingleLineOnly) {

              // Use Turf to calculate the number of data points within a certain Polygon area
              FeatureCollection pointsInSearchAreaFeatureCollection =
                TurfJoins.pointsWithinPolygon(searchPointFeatureCollection,
                  FeatureCollection.fromFeature(Feature.fromGeometry(
                    drawnPolygon)));

              // Create a Toast which say show many data points within a certain Polygon area
              if (VISIBLE.equals(style.getLayer(
                SEARCH_DATA_SYMBOL_LAYER_ID).getVisibility().getValue())) {
                Toast.makeText(FingerDrawQueryActivity.this, String.format(
                  getString(R.string.search_result_size),
                  pointsInSearchAreaFeatureCollection.features().size()), Toast.LENGTH_SHORT).show();
              }
            }

            if (drawSingleLineOnly) {
              Toast.makeText(FingerDrawQueryActivity.this,
                getString(R.string.move_map_drawn_line), Toast.LENGTH_SHORT).show();
            }
            enableMapMovement();
          }
        }
      });

      return true;
    }
  };

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

            FingerDrawQueryActivity.this.mapboxMap = mapboxMap;

            if (showSearchDataLocations) {
              new LoadGeoJson(FingerDrawQueryActivity.this).execute();
            } else {
              setUpExample(null);
            }

            findViewById(R.id.clear_map_for_new_draw_fab)
                .setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View view) {

                    // Reset ArrayLists
                    freehandTouchPointListForPolygon = new ArrayList<>();
                    freehandTouchPointListForLine = new ArrayList<>();

                    // Add empty Feature array to the sources
                    GeoJsonSource drawLineSource = style.getSourceAs(FREEHAND_DRAW_LINE_LAYER_SOURCE_ID);
                    if (drawLineSource != null) {
                      drawLineSource.setGeoJson(FeatureCollection.fromFeatures(new Feature[]{}));
                    }

                    GeoJsonSource fillPolygonSource = style.getSourceAs(FREEHAND_DRAW_FILL_LAYER_SOURCE_ID);
                    if (fillPolygonSource != null) {
                      fillPolygonSource.setGeoJson(FeatureCollection.fromFeatures(new Feature[]{}));
                    }

                    enableMapDrawing();
                  }
                });

            findViewById(R.id.switch_to_single_line_only_fab)
              .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                  drawSingleLineOnly = !drawSingleLineOnly;
                  Toast.makeText(FingerDrawQueryActivity.this, String.format(
                    getString(R.string.now_drawing), drawSingleLineOnly ? getString(R.string.single_line) :
                      getString(R.string.polygon)), Toast.LENGTH_SHORT).show();
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
    mapView.setOnTouchListener(null);
  }

  /**
   * Enable drawing on the map by setting the custom touch listener on the {@link MapView}
   */
  private void enableMapDrawing() {
    mapView.setOnTouchListener(customOnTouchListener);
  }

  private void setUpExample(FeatureCollection searchDataFeatureCollection) {

    searchPointFeatureCollection = searchDataFeatureCollection;

    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style loadedStyle) {
        loadedStyle.addImage(SEARCH_DATA_MARKER_ID, BitmapFactory.decodeResource(
          FingerDrawQueryActivity.this.getResources(), R.drawable.blue_marker_view));

        // Add sources to the map
        loadedStyle.addSource(new GeoJsonSource(SEARCH_DATA_SYMBOL_LAYER_SOURCE_ID,
          searchDataFeatureCollection));
        loadedStyle.addSource(new GeoJsonSource(FREEHAND_DRAW_LINE_LAYER_SOURCE_ID));
        loadedStyle.addSource(new GeoJsonSource(MARKER_SYMBOL_LAYER_SOURCE_ID));
        loadedStyle.addSource(new GeoJsonSource(FREEHAND_DRAW_FILL_LAYER_SOURCE_ID));

        loadedStyle.addLayer(new SymbolLayer(SEARCH_DATA_SYMBOL_LAYER_ID,
          SEARCH_DATA_SYMBOL_LAYER_SOURCE_ID).withProperties(
          iconImage(SEARCH_DATA_MARKER_ID),
          iconAllowOverlap(true),
          iconOffset(new Float[] {0f, -8f}),
          iconIgnorePlacement(true))
        );

        // Add freehand draw LineLayer to the map
        loadedStyle.addLayerBelow(new LineLayer(FREEHAND_DRAW_LINE_LAYER_ID,
          FREEHAND_DRAW_LINE_LAYER_SOURCE_ID).withProperties(
          lineWidth(LINE_WIDTH),
          lineJoin(LINE_JOIN_ROUND),
          lineOpacity(LINE_OPACITY),
          lineColor(Color.parseColor(LINE_COLOR))), SEARCH_DATA_SYMBOL_LAYER_ID
        );

        // Add freehand draw polygon FillLayer to the map
        loadedStyle.addLayerBelow(new FillLayer(FREEHAND_DRAW_FILL_LAYER_ID,
          FREEHAND_DRAW_FILL_LAYER_SOURCE_ID).withProperties(
          fillColor(Color.RED),
          fillOpacity(FILL_OPACITY)), FREEHAND_DRAW_LINE_LAYER_ID
        );

        enableMapDrawing();

        findViewById(R.id.show_search_data_points_fab).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            showSearchDataLocations = !showSearchDataLocations;

            // Toggle the visibility of the fake data point SymbolLayer icons
            Layer dataLayer = loadedStyle.getLayer(SEARCH_DATA_SYMBOL_LAYER_ID);
            if (dataLayer != null) {
              dataLayer.setProperties(
                VISIBLE.equals(dataLayer.getVisibility().getValue()) ? visibility(NONE) : visibility(VISIBLE));
            }
          }
        });

        Toast.makeText(FingerDrawQueryActivity.this,
          getString(R.string.draw_instruction), Toast.LENGTH_SHORT).show();
      }
    });
  }

  /**
   * Use an AsyncTask to retrieve GeoJSON data from a file in the assets folder.
   */
  private static class LoadGeoJson extends AsyncTask<Void, Void, FeatureCollection> {

    private WeakReference<FingerDrawQueryActivity> weakReference;

    LoadGeoJson(FingerDrawQueryActivity activity) {
      this.weakReference = new WeakReference<>(activity);
    }

    @Override
    protected FeatureCollection doInBackground(Void... voids) {
      try {
        FingerDrawQueryActivity activity = weakReference.get();
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
      FingerDrawQueryActivity activity = weakReference.get();
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
