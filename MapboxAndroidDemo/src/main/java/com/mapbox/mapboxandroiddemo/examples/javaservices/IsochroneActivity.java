package com.mapbox.mapboxandroiddemo.examples.javaservices;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
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
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.net.URL;

import okhttp3.HttpUrl;

import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.geometryType;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

/**
 * See how far a car can travel in certain time periods by requesting information from the Mapbox
 * Isochrone API (https://www.mapbox.com/api-documentation/#isochrone)
 */
public class IsochroneActivity extends AppCompatActivity implements MapboxMap.OnMapClickListener {

  private static final String GEOJSON_SOURCE_ID = "GEOJSON_SOURCE_ID";
  private static final String ISOCHRONE_FILL_LAYER = "ISOCHRONE_FILL_LAYER";
  private static final String ISOCHRONE_LINE_LAYER = "ISOCHRONE_LINE_LAYER";
  private MapView mapView;
  private MapboxMap mapboxMap;
  private GeoJsonSource isochroneGeoJsonSource;
  private LatLng lastSelectedLatLng;
  private String randomNumForLayerId;
  private boolean layersShown;
  private boolean usePolygon;

  /**
   * A utils class to conveniently access isochrone profiles within this example
   */
  private class IsochroneProfile {
    private static final String DRIVING = "driving";
    private static final String WALKING = "walking";
    private static final String CYCLING = "cycling";

    public IsochroneProfile() {
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    layersShown = false;
    usePolygon = false;

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_isochrone);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        IsochroneActivity.this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            mapboxMap.addOnMapClickListener(IsochroneActivity.this);

            initIsochroneCenterSymbolLayer(style);

            // Set the click listener for the floating action button
            findViewById(R.id.switch_isochrone_style_fab).setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                usePolygon = !usePolygon;
                redrawIsochroneData(lastSelectedLatLng);
              }
            });

            Toast.makeText(IsochroneActivity.this,
              getString(R.string.click_on_map_instruction), Toast.LENGTH_SHORT).show();
          }
        });
      }
    });
  }

  @Override
  public boolean onMapClick(@NonNull LatLng point) {
    // Update the click Symbol Layer to move the red marker to wherever the map was clicked on
    lastSelectedLatLng = point;

    if (mapboxMap.getStyle() != null) {
      GeoJsonSource source = mapboxMap.getStyle().getSourceAs("click-source-id");
      if (source != null) {
        source.setGeoJson(Feature.fromGeometry(Point.fromLngLat(point.getLongitude(), point.getLatitude())));
      }

      // Request and redraw the Isochrone information based on the map click point
      redrawIsochroneData(point);
    }
    return true;
  }

  /**
   * Remove the previous isochrone layers and add the data to the map
   *
   * @param point The center point to use for the request to the Isochrone API
   */
  private void redrawIsochroneData(LatLng point) {
    if (layersShown) {
      if (mapboxMap.getStyle() != null) {
        Style style = mapboxMap.getStyle();
        style.removeLayer(ISOCHRONE_FILL_LAYER + randomNumForLayerId);
        style.removeLayer(ISOCHRONE_LINE_LAYER + randomNumForLayerId);
      }
    }
    addDataToMap(point);
  }

  /**
   * Make a request to the Mapbox Isochrone API
   *
   * @param mapClickPoint The center point of the isochrone. It is part of the API request.
   */
  private void addDataToMap(@NonNull LatLng mapClickPoint) {
    try {
      HttpUrl url = new HttpUrl.Builder()
        .scheme("https")
        .host("api.mapbox.com")
        .addPathSegment("isochrone")
        .addPathSegment("v1")
        .addPathSegment("mapbox")
        .addPathSegment(IsochroneProfile.DRIVING) // Walking or cycling are other options besides driving
        .addPathSegment(String.valueOf(mapClickPoint.getLongitude()) + ","
          + String.valueOf(mapClickPoint.getLatitude()))
        .addQueryParameter("polygons", String.valueOf(usePolygon))
        .addQueryParameter("contours_minutes", "5,15,30")
        .addQueryParameter("contours_colors", "6706ce,04e813,4286f4")
        .addQueryParameter("access_token", getString(R.string.access_token))
        .build();

      String randomNum = String.valueOf(Math.random());

      // Create and add a new GeoJsonSource with a unique ID. The source is fed a List of Feature objects via
      // the Isochrone API response.

      if (mapboxMap.getStyle() != null) {
        mapboxMap.getStyle().addSource(new GeoJsonSource(GEOJSON_SOURCE_ID + randomNum, new URL(url.toString())));

        // Create new Fill and Line layers with unique ids.
        randomNumForLayerId = String.valueOf(randomNum);
        initFillLayer(randomNumForLayerId, GEOJSON_SOURCE_ID + randomNum);
        initLineLayer(randomNumForLayerId, GEOJSON_SOURCE_ID + randomNum);

        layersShown = true;
      }

      // Move the camera from the map in case it's too zoomed in. This is here so that the isochrone information
      // can be seen if the camera is too close to the map.
      if (mapboxMap.getCameraPosition().zoom > 14) {
        CameraPosition zoomOut = new CameraPosition.Builder()
          .zoom(mapboxMap.getCameraPosition().zoom - 4.5)
          .build();
        mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(zoomOut), 1500);
      }

    } catch (Throwable throwable) {
      Log.e("IsochroneActivity", "Couldn't add GeoJsonSource to map", throwable);
    }
  }

  /**
   * Add a SymbolLayer to the map so that the map click point has a visual marker. This is where the
   * Isochrone API information radiates from.
   */
  private void initIsochroneCenterSymbolLayer(@NonNull Style loadedMapStyle) {
    loadedMapStyle.addImage("map-click-icon-id", BitmapUtils.getBitmapFromDrawable(
      getResources().getDrawable(R.drawable.red_marker)));

    loadedMapStyle.addSource(new GeoJsonSource("click-source-id",
      FeatureCollection.fromFeatures(new Feature[] {})));

    loadedMapStyle.addLayer(new SymbolLayer("click-layer-id", "click-source-id").withProperties(
      iconImage("map-click-icon-id"),
      iconOffset(new Float[] {0f, -4f})
    ));
  }

  /**
   * Add a FillLayer so that that polygons returned by the Isochrone API response can be displayed
   *
   * @param randomNumForId A random number to append to the layer's ID so that there are no issues
   *                       adding it to the map.
   * @param sourceId       The sourceId of the GeoJsonSource that the layer should depend on
   */
  private void initFillLayer(@NonNull String randomNumForId, @NonNull String sourceId) {
    // Create and style a FillLayer based on information in the Isochrone API response
    FillLayer isochroneFillLayer = new FillLayer(ISOCHRONE_FILL_LAYER + randomNumForId, sourceId);
    isochroneFillLayer.setProperties(
      fillColor(get("color")),
      fillOpacity(get("fillOpacity")));
    isochroneFillLayer.setFilter(eq(geometryType(), literal("Polygon")));
    mapboxMap.getStyle().addLayerBelow(isochroneFillLayer, "click-layer-id");
  }

  /**
   * Add a LineLayer so that that lines returned by the Isochrone API response can be displayed
   *
   * @param randomNumForId A random number to append to the layer's ID so that there are no issues
   *                       adding it to the map.
   * @param sourceId       The sourceId of the GeoJsonSource that the layer should depend on
   */
  private void initLineLayer(@NonNull String randomNumForId, @NonNull String sourceId) {
    // Create and style a LineLayer based on information in the Isochrone API response
    LineLayer isochroneLineLayer = new LineLayer(ISOCHRONE_LINE_LAYER + randomNumForId, sourceId);
    isochroneLineLayer.setProperties(
      lineColor(get("color")),
      lineWidth(5f),
      lineOpacity(get("opacity")));
    isochroneLineLayer.setFilter(eq(geometryType(), literal("LineString")));
    mapboxMap.getStyle().addLayerBelow(isochroneLineLayer, "click-layer-id");
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
}