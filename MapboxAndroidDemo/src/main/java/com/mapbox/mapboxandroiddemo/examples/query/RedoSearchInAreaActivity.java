package com.mapbox.mapboxandroiddemo.examples.query;

// #-code-snippet: redo-search-activity full-java

import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.List;

import static com.mapbox.mapboxsdk.style.expressions.Expression.exponential;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;

/**
 * Use MapboxMap.queryRenderedFeatures() to find and highlight certain features within the map viewport.
 * The search button re-appears when the map is moved.
 */
public class RedoSearchInAreaActivity extends AppCompatActivity implements OnMapReadyCallback,
  MapboxMap.OnMoveListener {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private FeatureCollection dataFeatureCollection;
  private GeoJsonSource dataGeoJsonSource;
  private FillLayer dataFillLayer;
  private Button redoSearchButton;
  private String desiredMapLayerToShow = "parks";
  private String geoJsonSourceId = "geoJsonSourceId";
  private String fillLayerId = "fillLayerId";
  private boolean moveMapInstructionShown;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_query_redo_search_in_area);

    redoSearchButton = findViewById(R.id.redo_search_button);
    moveMapInstructionShown = false;

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    mapboxMap.addOnMoveListener(this);
    initGeoJsonSource();
    initFillLayer();
    this.redoSearchButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mapboxMap.clear();
        if (!moveMapInstructionShown) {
          Toast.makeText(RedoSearchInAreaActivity.this,
            R.string.move_the_map_and_research, Toast.LENGTH_SHORT).show();
          moveMapInstructionShown = true;
        }
        FeatureCollection featureCollection = null;
        if (mapboxMap.getLayer(desiredMapLayerToShow) != null) {
          featureCollection = FeatureCollection.fromFeatures(getFeaturesInViewport(desiredMapLayerToShow));
        } else {
          Toast.makeText(RedoSearchInAreaActivity.this,
            String.format(getString(R.string.layer_not_found), desiredMapLayerToShow),
            Toast.LENGTH_SHORT).show();
        }
        // Retrieve and update the GeoJSON source used in the FillLayer
        dataGeoJsonSource = mapboxMap.getSourceAs(geoJsonSourceId);
        if (dataGeoJsonSource != null && featureCollection != null) {
          dataGeoJsonSource.setGeoJson(featureCollection);
        }
        redoSearchButton.setVisibility(View.INVISIBLE);
      }
    });
  }

  /**
   * Set up the GeoJsonSource and add it to the map
   */
  private void initGeoJsonSource() {
    dataFeatureCollection = FeatureCollection.fromFeatures(new Feature[] {});
    dataGeoJsonSource = new GeoJsonSource(geoJsonSourceId, dataFeatureCollection);
    mapboxMap.addSource(dataGeoJsonSource);
  }

  /**
   * Set up the FillLayer and add it to the map. This layer will display the rendered features that are
   * a part of the map layer you want to show (i.e. the desiredMapLayerToShow variable)
   */
  private void initFillLayer() {
    dataFillLayer = new FillLayer(fillLayerId,
      geoJsonSourceId);
    dataFillLayer.withProperties(
      fillOpacity(interpolate(exponential(1f), zoom(),
        stop(3, 0f),
        stop(8, .5f),
        stop(15f, 1f))),
      fillColor(Color.parseColor("#00F7FF"))
    );
    mapboxMap.addLayer(dataFillLayer);
  }

  /**
   * Perform feature query within the viewport.
   */
  private List<Feature> getFeaturesInViewport(String layerName) {
    RectF rectF = new RectF(mapView.getLeft(),
      mapView.getTop(), mapView.getRight(), mapView.getBottom());
    return mapboxMap.queryRenderedFeatures(rectF, layerName);
  }

  @Override
  public void onMoveEnd(MoveGestureDetector detector) {
    redoSearchButton.setVisibility(View.VISIBLE);
  }

  @Override
  public void onMoveBegin(MoveGestureDetector detector) {
    // Left empty on purpose
  }

  @Override
  public void onMove(MoveGestureDetector detector) {
    // Left empty on purpose
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
    if (mapboxMap != null) {
      mapboxMap.removeOnMoveListener(this);
    }
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}
// #-end-code-snippet: redo-search-activity full-java