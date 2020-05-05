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

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.mapbox.turf.TurfMeta;
import com.mapbox.turf.TurfTransformation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.turf.TurfConstants.UNIT_DEGREES;
import static com.mapbox.turf.TurfConstants.UNIT_KILOMETERS;
import static com.mapbox.turf.TurfConstants.UNIT_MILES;
import static com.mapbox.turf.TurfConstants.UNIT_RADIANS;

/**
 * Use {@link TurfTransformation#circle(Point, double, int, String)} to draw a circle
 * at a center coordinate with its radius specified in physical units (i.e. "miles").
 * Default number of steps is 64 and default unit of distance is kilometers.
 * More information can be found at https://github.com/mapbox/mapbox-java/blob/
 * master/services-turf/src/main/java/com/mapbox/turf/TurfTransformation.java and
 * at http://turfjs.org/docs/#circle.
 */
public class TurfPhysicalCircleActivity extends AppCompatActivity implements MapboxMap.OnMapClickListener,
  AdapterView.OnItemSelectedListener {

  private static final String TURF_CALCULATION_FILL_LAYER_GEOJSON_SOURCE_ID
    = "TURF_CALCULATION_FILL_LAYER_GEOJSON_SOURCE_ID";
  private static final String TURF_CALCULATION_FILL_LAYER_ID = "TURF_CALCULATION_FILL_LAYER_ID";
  private static final String CIRCLE_CENTER_SOURCE_ID = "CIRCLE_CENTER_SOURCE_ID";
  private static final String CIRCLE_CENTER_ICON_ID = "CIRCLE_CENTER_ICON_ID";
  private static final String CIRCLE_CENTER_LAYER_ID = "CIRCLE_CENTER_LAYER_ID";
  private static final Point DOWNTOWN_KATHMANDU = Point.fromLngLat(85.323283875, 27.7014884022);
  private static final int RADIUS_SEEKBAR_DIFFERENCE = 1;
  private static final int STEPS_SEEKBAR_DIFFERENCE = 1;
  private static final int STEPS_SEEKBAR_MAX = 360;
  private static final int RADIUS_SEEKBAR_MAX = 500;

  // Min is 4 because LinearRings need to be made up of 4 or more coordinates.
  private static final int MINIMUM_CIRCLE_STEPS = 4;
  private Point lastClickPoint = DOWNTOWN_KATHMANDU;
  private MapView mapView;
  private MapboxMap mapboxMap;

  // Not static final because they will be adjusted by the seekbars and spinner menu
  private String circleUnit = UNIT_KILOMETERS;
  private int circleSteps = 180;
  private int circleRadius = 100;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_lab_turf_circle_physical_units);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull MapboxMap mapboxMap) {
        mapboxMap.setStyle(new Style.Builder().fromUri(Style.MAPBOX_STREETS)
          .withImage(CIRCLE_CENTER_ICON_ID, BitmapUtils.getBitmapFromDrawable(
            getResources().getDrawable(R.drawable.red_marker)))
          .withSource(new GeoJsonSource(CIRCLE_CENTER_SOURCE_ID,
            Feature.fromGeometry(DOWNTOWN_KATHMANDU)))
          .withSource(new GeoJsonSource(TURF_CALCULATION_FILL_LAYER_GEOJSON_SOURCE_ID))
          .withLayer(new SymbolLayer(CIRCLE_CENTER_LAYER_ID,
            CIRCLE_CENTER_SOURCE_ID).withProperties(
            iconImage(CIRCLE_CENTER_ICON_ID),
            iconIgnorePlacement(true),
            iconAllowOverlap(true),
            iconOffset(new Float[] {0f, -4f})
          )), new Style.OnStyleLoaded() {
              @Override
              public void onStyleLoaded(@NonNull Style style) {

                TurfPhysicalCircleActivity.this.mapboxMap = mapboxMap;

                initPolygonCircleFillLayer();

                final SeekBar circleStepsSeekbar = findViewById(R.id.circle_steps_seekbar);
                circleStepsSeekbar.setMax(STEPS_SEEKBAR_MAX);
                circleStepsSeekbar.incrementProgressBy(STEPS_SEEKBAR_DIFFERENCE);
                circleStepsSeekbar.setProgress(STEPS_SEEKBAR_MAX / 2);

                final SeekBar circleRadiusSeekbar = findViewById(R.id.circle_radius_seekbar);
                circleRadiusSeekbar.setMax(RADIUS_SEEKBAR_MAX + RADIUS_SEEKBAR_DIFFERENCE);
                circleRadiusSeekbar.incrementProgressBy(RADIUS_SEEKBAR_DIFFERENCE);
                circleRadiusSeekbar.setProgress(RADIUS_SEEKBAR_MAX / 2);

                final TextView circleStepsTextview = findViewById(R.id.circle_steps_textview);
                circleStepsTextview.setText(String.format(getString(
                  R.string.polygon_circle_transformation_circle_steps),
                  circleStepsSeekbar.getProgress()));

                final TextView circleRadiusTextView = findViewById(R.id.circle_radius_textview);
                circleRadiusTextView.setText(String.format(getString(
                  R.string.polygon_circle_transformation_circle_radius),
                  circleRadiusSeekbar.getProgress()));

                drawPolygonCircle(lastClickPoint);

                circleStepsSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                  @Override
                  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress < MINIMUM_CIRCLE_STEPS) {
                      seekBar.setProgress(MINIMUM_CIRCLE_STEPS);
                    }
                    adjustSteps(R.string.polygon_circle_transformation_circle_steps, circleStepsTextview,
                      progress < MINIMUM_CIRCLE_STEPS ? MINIMUM_CIRCLE_STEPS : progress, STEPS_SEEKBAR_DIFFERENCE);
                  }

                  @Override
                  public void onStartTrackingTouch(SeekBar seekBar) {
                    // Not needed in this example.
                  }

                  @Override
                  public void onStopTrackingTouch(SeekBar seekBar) {
                    if (seekBar.getProgress() < MINIMUM_CIRCLE_STEPS) {
                      seekBar.setProgress(MINIMUM_CIRCLE_STEPS);
                    }
                    adjustSteps(R.string.polygon_circle_transformation_circle_steps, circleStepsTextview,
                      seekBar.getProgress(), STEPS_SEEKBAR_DIFFERENCE);
                  }
                });

                circleRadiusSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                  @Override
                  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    adjustRadius(R.string.polygon_circle_transformation_circle_radius, circleRadiusTextView,
                      seekBar.getProgress(), STEPS_SEEKBAR_DIFFERENCE);
                  }

                  @Override
                  public void onStartTrackingTouch(SeekBar seekBar) {
                    // Not needed in this example.
                  }

                  @Override
                  public void onStopTrackingTouch(SeekBar seekBar) {
                    adjustRadius(R.string.polygon_circle_transformation_circle_radius, circleRadiusTextView,
                      seekBar.getProgress(), STEPS_SEEKBAR_DIFFERENCE);
                  }
                });
                mapboxMap.addOnMapClickListener(TurfPhysicalCircleActivity.this);
                initDistanceUnitSpinner();
                Toast.makeText(TurfPhysicalCircleActivity.this,
                  getString(R.string.polygon_circle_transformation_click_map_instruction), Toast.LENGTH_SHORT).show();
              }
          });
      }
    });
  }

  private void adjustRadius(int string, TextView textView, int progress, int difference) {
    adjustTextView(string, textView, progress, difference);
    circleRadius = progress;
    drawPolygonCircle(lastClickPoint);
  }

  private void adjustSteps(int string, TextView textView, int progress, int difference) {
    adjustTextView(string, textView, progress, difference);
    circleSteps = progress;
    drawPolygonCircle(lastClickPoint);
  }

  private void initDistanceUnitSpinner() {
    Spinner spinner = findViewById(R.id.circle_units_spinner);
    spinner.setOnItemSelectedListener(this);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
      R.array.polygon_circle_transformation_circle_distance_units_array, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);
  }

  @Override
  public void onItemSelected(AdapterView<?> parentAdapterView, View view, int position, long id) {
    String selectedUnitInSpinnerMenu = String.valueOf(parentAdapterView.getItemAtPosition(position));
    switch (selectedUnitInSpinnerMenu) {
      case "Kilometers":
        circleUnit = UNIT_KILOMETERS;
        break;
      case "Miles":
        circleUnit = UNIT_MILES;
        break;
      case "Degrees":
        circleUnit = UNIT_DEGREES;
        break;
      case "Radians":
        circleUnit = UNIT_RADIANS;
        break;
      default:
        circleUnit = UNIT_KILOMETERS;
    }
    drawPolygonCircle(lastClickPoint);
  }

  @Override
  public void onNothingSelected(AdapterView<?> adapterView) {
    // Empty on purpose. Not used in this example.
  }

  private void adjustTextView(int string, TextView textView, int progress, int difference) {
    progress = progress / difference;
    progress = progress * difference;
    textView.setText(String.format(getString(string), progress));
  }

  @Override
  public boolean onMapClick(@NonNull LatLng mapClickLatLng) {
    mapboxMap.easeCamera(CameraUpdateFactory.newLatLng(mapClickLatLng));
    lastClickPoint = Point.fromLngLat(mapClickLatLng.getLongitude(), mapClickLatLng.getLatitude());
    moveCircleCenterMarker(lastClickPoint);
    drawPolygonCircle(lastClickPoint);
    return true;
  }

  /**
   * Move the red marker icon to wherever the map was tapped on.
   *
   * @param circleCenter where the red marker icon will be moved to.
   */
  private void moveCircleCenterMarker(Point circleCenter) {
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        // Use Turf to calculate the Polygon's coordinates
        GeoJsonSource markerSource = style.getSourceAs(CIRCLE_CENTER_SOURCE_ID);
        if (markerSource != null) {
          markerSource.setGeoJson(circleCenter);
        }
      }
    });
  }

  /**
   * Update the {@link FillLayer} based on the GeoJSON retrieved via
   * {@link #getTurfPolygon(Point, double, int, String)}.
   *
   * @param circleCenter the center coordinate to be used in the Turf calculation.
   */
  private void drawPolygonCircle(Point circleCenter) {
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        // Use Turf to calculate the Polygon's coordinates
        Polygon polygonArea = getTurfPolygon(circleCenter, circleRadius, circleSteps, circleUnit);
        GeoJsonSource polygonCircleSource = style.getSourceAs(TURF_CALCULATION_FILL_LAYER_GEOJSON_SOURCE_ID);
        if (polygonCircleSource != null) {
          polygonCircleSource.setGeoJson(Polygon.fromOuterInner(
            LineString.fromLngLats(TurfMeta.coordAll(polygonArea, false))));
        }
      }
    });
  }

  /**
   * Use the Turf library {@link TurfTransformation#circle(Point, double, int, String)} method to
   * retrieve a {@link Polygon} .
   *
   * @param centerPoint a {@link Point} which the circle will center around
   * @param radius the radius of the circle
   * @param steps  number of steps which make up the circle parameter
   * @param units  one of the units found inside {@link com.mapbox.turf.TurfConstants}
   * @return a {@link Polygon} which represents the newly created circle
   */
  private Polygon getTurfPolygon(@NonNull Point centerPoint, @NonNull double radius,
                                 @NonNull int steps, @NonNull String units) {
    return TurfTransformation.circle(centerPoint, radius, steps, units);
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
          fillOpacity(.7f));
        style.addLayerBelow(fillLayer, CIRCLE_CENTER_LAYER_ID);
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