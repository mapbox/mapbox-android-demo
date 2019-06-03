package com.mapbox.mapboxandroiddemo.examples.query;

import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.mapbox.mapboxsdk.maps.Style;
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

  private static final String FILL_LAYER_ID = "FILL_LAYER_ID";
  private static final String GEO_JSON_SOURCE_ID = "GEO_JSON_SOURCE_ID";
  private static final String ID_OF_LAYER_TO_HIGHLIGHT = "landuse";
  private MapView mapView;
  private MapboxMap mapboxMap;
  private GeoJsonSource dataGeoJsonSource;
  private Button redoSearchButton;
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
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    mapboxMap.setStyle(Style.DARK, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull final Style style) {
        mapboxMap.addOnMoveListener(RedoSearchInAreaActivity.this);

        style.addSource(new GeoJsonSource(GEO_JSON_SOURCE_ID,
          FeatureCollection.fromFeatures(new Feature[] {})));

        style.addLayer(new FillLayer(FILL_LAYER_ID,
          GEO_JSON_SOURCE_ID).withProperties(
          fillOpacity(interpolate(exponential(1f), zoom(),
            stop(3, 0f),
            stop(8, .5f),
            stop(15f, 1f))),
          fillColor(Color.parseColor("#00F7FF"))
        ));

        redoSearchButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            if (!moveMapInstructionShown) {
              Toast.makeText(RedoSearchInAreaActivity.this,
                R.string.move_the_map_and_research, Toast.LENGTH_SHORT).show();
              moveMapInstructionShown = true;
            }
            FeatureCollection featureCollection = null;
            if (style.getLayer(ID_OF_LAYER_TO_HIGHLIGHT) != null) {
              featureCollection = FeatureCollection.fromFeatures(getFeaturesInViewport(ID_OF_LAYER_TO_HIGHLIGHT));
            } else {
              Toast.makeText(RedoSearchInAreaActivity.this,
                String.format(getString(R.string.layer_not_found), ID_OF_LAYER_TO_HIGHLIGHT),
                Toast.LENGTH_SHORT).show();
            }
            // Retrieve and update the GeoJSON source used in the FillLayer
            dataGeoJsonSource = style.getSourceAs(GEO_JSON_SOURCE_ID);
            if (dataGeoJsonSource != null && featureCollection != null) {
              dataGeoJsonSource.setGeoJson(featureCollection);
            }
            redoSearchButton.setVisibility(View.INVISIBLE);
          }
        });
      }
    });
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
