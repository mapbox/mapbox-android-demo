package com.mapbox.mapboxandroiddemo.examples.javaservices;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.turf.TurfMeta;
import com.mapbox.turf.TurfTransformation;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newLatLngBounds;
import static com.mapbox.mapboxsdk.style.expressions.Expression.within;
import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;
import static com.mapbox.turf.TurfConstants.UNIT_KILOMETERS;
import static com.mapbox.turf.TurfConstants.UNIT_MILES;

/**
 * Use {@link TurfTransformation#circle(Point, double, int, String)} to draw a
 * circle at a center coordinate with its radius specified in physical units (i.e. "miles").
 * The circle is actually a {@link Polygon} with 360 sides. More about information
 * about {@link TurfTransformation} can be found at
 * https://github.com/mapbox/mapbox-java/blob/ master/services-turf/src/main/java/
 * com/mapbox/turf/TurfTransformation.java and at http://turfjs.org/docs/#circle.
 * <p>
 * Then use the {@link Expression#within(Polygon)} expression in a layer filter
 * to only show POIs that are inside of the circular polygon area.
 */
public class TurfCirclePoiWithinFilterActivity extends AppCompatActivity implements MapboxMap.OnMapClickListener,
    AdapterView.OnItemSelectedListener {

  private static final String TAG = "FilterActivity";
  private static final String TURF_CALCULATION_FILL_LAYER_GEOJSON_SOURCE_ID
      = "TURF_CALCULATION_FILL_LAYER_GEOJSON_SOURCE_ID";
  private static final String TURF_CALCULATION_FILL_LAYER_ID = "TURF_CALCULATION_FILL_LAYER_ID";
  private static final int RADIUS_SEEKBAR_DIFFERENCE = 1;
  private static final int RADIUS_SEEKBAR_MAX = 10;
  private static final Point DOWNTOWN_MUNICH_START_LOCATION =
      Point.fromLngLat(11.5753822, 48.1371079);
  private Point lastClickPoint;
  private MapView mapView;
  private MapboxMap mapboxMap;
  private String circleUnit = UNIT_KILOMETERS;
  private int circleRadius = RADIUS_SEEKBAR_MAX / 2;
  private TextView circleRadiusTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_lab_turf_circle_poi_within_filter);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull MapboxMap mapboxMap) {
        mapboxMap.setStyle(new Style.Builder().fromUri(Style.MAPBOX_STREETS)
                .withSource(new GeoJsonSource(TURF_CALCULATION_FILL_LAYER_GEOJSON_SOURCE_ID)),
            new Style.OnStyleLoaded() {
              @Override
              public void onStyleLoaded(@NonNull Style style) {

                TurfCirclePoiWithinFilterActivity.this.mapboxMap = mapboxMap;

                hideLayers();

                initPolygonCircleFillLayer();

                // Set up the seekbar so that the circle's radius can be adjusted
                final SeekBar circleRadiusSeekbar = findViewById(R.id.circle_radius_seekbar);
                circleRadiusSeekbar.setMax(RADIUS_SEEKBAR_MAX);
                circleRadiusSeekbar.incrementProgressBy(RADIUS_SEEKBAR_DIFFERENCE / 10);
                circleRadiusSeekbar.setProgress(RADIUS_SEEKBAR_MAX / 2);

                circleRadiusTextView = findViewById(R.id.circle_radius_textview);
                circleRadiusTextView.setText(String.format(getString(
                    R.string.polygon_circle_transformation_circle_radius),
                    String.format(".%s", String.valueOf(RADIUS_SEEKBAR_MAX / 2))));

                // Draw the initial circle around the starting location
                drawPolygonCircle(DOWNTOWN_MUNICH_START_LOCATION);

                circleRadiusSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                  @Override
                  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    makeNewRadiusAdjustments(seekBar.getProgress());
                  }

                  @Override
                  public void onStartTrackingTouch(SeekBar seekBar) {
                    // Not needed in this example.
                  }

                  @Override
                  public void onStopTrackingTouch(SeekBar seekBar) {
                    makeNewRadiusAdjustments(seekBar.getProgress());
                  }
                });

                mapboxMap.addOnMapClickListener(TurfCirclePoiWithinFilterActivity.this);

                initDistanceUnitSpinner();

                Toast.makeText(TurfCirclePoiWithinFilterActivity.this,
                    getString(R.string.polygon_circle_transformation_click_map_instruction),
                    Toast.LENGTH_SHORT).show();
              }
            });
      }
    });
  }

  /**
   * Remove other types of label layers from the style in order to highlight the POI label layer.
   */
  private void hideLayers() {
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        SymbolLayer roadLabelLayer = style.getLayerAs("road-label");
        if (roadLabelLayer != null) {
          roadLabelLayer.setProperties(visibility(NONE));
        }
        SymbolLayer transitLabelLayer = style.getLayerAs("transit-label");
        if (transitLabelLayer != null) {
          transitLabelLayer.setProperties(visibility(NONE));
        }
        SymbolLayer roadNumberShieldLayer = style.getLayerAs("road-number-shield");
        if (roadNumberShieldLayer != null) {
          roadNumberShieldLayer.setProperties(visibility(NONE));
        }
      }
    });
  }

  private void makeNewRadiusAdjustments(int progress) {
    String amount;
    if (progress == 0) {
      amount = "0";
    } else if (progress == RADIUS_SEEKBAR_MAX) {
      amount = "1";
    } else {
      amount = String.format(".%s", String.valueOf(progress));
    }
    circleRadiusTextView.setText(String.format(getString(
        R.string.polygon_circle_transformation_circle_radius), amount));

    circleRadius = progress;
    drawPolygonCircle(lastClickPoint != null ? lastClickPoint : DOWNTOWN_MUNICH_START_LOCATION);
  }

  private void initDistanceUnitSpinner() {
    Spinner spinner = findViewById(R.id.circle_units_spinner);
    spinner.setOnItemSelectedListener(this);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        R.array.within_poi_filter_circle_distance_units_array, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);
  }

  @Override
  public void onItemSelected(AdapterView<?> parentAdapterView, View view, int position, long id) {
    String selectedUnitInSpinnerMenu = String.valueOf(parentAdapterView.getItemAtPosition(position));
    if ("Miles".equals(selectedUnitInSpinnerMenu)) {
      circleUnit = UNIT_MILES;
    } else {
      circleUnit = UNIT_KILOMETERS;
    }
    drawPolygonCircle(lastClickPoint != null ? lastClickPoint : DOWNTOWN_MUNICH_START_LOCATION);
  }

  @Override
  public void onNothingSelected(AdapterView<?> adapterView) {
    // Empty on purpose. Not used in this example.
  }

  @Override
  public boolean onMapClick(@NonNull LatLng mapClickLatLng) {
    mapboxMap.easeCamera(CameraUpdateFactory.newLatLng(mapClickLatLng));
    lastClickPoint = Point.fromLngLat(mapClickLatLng.getLongitude(), mapClickLatLng.getLatitude());
    drawPolygonCircle(lastClickPoint);
    return true;
  }

  /**
   * Update the {@link FillLayer} based on the GeoJSON retrieved via
   * {@link this#getTurfPolygon(Point, double, String)}.
   *
   * @param circleCenter the center coordinate to be used in the Turf calculation.
   */
  private void drawPolygonCircle(Point circleCenter) {
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        // Use Turf to calculate the Polygon's coordinates
        Polygon polygonArea = getTurfPolygon(circleCenter, circleRadius, circleUnit);

        List<Point> pointList = TurfMeta.coordAll(polygonArea, false);

        // Update the source's GeoJSON to draw a new circle
        GeoJsonSource polygonCircleSource = style.getSourceAs(TURF_CALCULATION_FILL_LAYER_GEOJSON_SOURCE_ID);
        if (polygonCircleSource != null) {
          polygonCircleSource.setGeoJson(Polygon.fromOuterInner(
              LineString.fromLngLats(pointList)));
        }

        // Show new places of interest (POIs)
        filterPlacesOfInterest(polygonArea);

        // Adjust camera bounds to include entire circle
        List<LatLng> latLngList = new ArrayList<>(pointList.size());
        for (Point singlePoint : pointList) {
          latLngList.add(new LatLng((singlePoint.latitude()), singlePoint.longitude()));
        }
        mapboxMap.easeCamera(newLatLngBounds(
            new LatLngBounds.Builder()
                .includes(latLngList)
                .build(), 75), 1000);
      }
    });
  }

  /**
   * Show only POI labels inside geometry using within expression
   *
   * @param polygonArea the polygon area (a circle in this example) to show POIs in.
   */
  private void filterPlacesOfInterest(Polygon polygonArea) {
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        SymbolLayer poiLabelLayer = style.getLayerAs("poi-label");
        if (poiLabelLayer != null) {
          poiLabelLayer.setFilter(within(polygonArea));
        }
      }
    });
  }

  /**
   * Use the Turf library {@link TurfTransformation#circle(Point, double, int, String)} method to
   * retrieve a {@link Polygon} .
   *
   * @param centerPoint a {@link Point} which the circle will center around
   * @param radius      the radius of the circle
   * @param units       one of the units found inside {@link com.mapbox.turf.TurfConstants}
   * @return a {@link Polygon} which represents the newly created circle
   */
  private Polygon getTurfPolygon(@NonNull Point centerPoint, double radius,
                                 @NonNull String units) {
    return TurfTransformation.circle(centerPoint, radius / 10, 360, units);
  }

  /**
   * Add a {@link FillLayer} to display a {@link Polygon} in a the shape of a circle.
   */
  private void initPolygonCircleFillLayer() {
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        // Create and style a FillLayer based on information that will come from the Turf calculation
        FillLayer fillLayer = new FillLayer(TURF_CALCULATION_FILL_LAYER_ID,
            TURF_CALCULATION_FILL_LAYER_GEOJSON_SOURCE_ID);
        fillLayer.setProperties(
            fillColor(Color.parseColor("#f5425d")),
            fillOpacity(.5f));
        style.addLayerBelow(fillLayer, "poi-label");
      }
    });
  }

  // Add the mapView lifecycle to the activity's lifecycle methods
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
    if (mapboxMap != null) {
      mapboxMap.removeOnMapClickListener(this);
    }
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}