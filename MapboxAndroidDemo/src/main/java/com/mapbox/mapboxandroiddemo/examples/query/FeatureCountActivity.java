package com.mapbox.mapboxandroiddemo.examples.query;

import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.geojson.FeatureCollection;

import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;

/**
 * Get the feature count inside of a bounding box and highlight all of the buildings in the box.
 */
public class FeatureCountActivity extends AppCompatActivity {

  private MapView mapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_query_feature_count);

    // Define our views, ones the center box and the others our view container used for the
    // snackbar.
    final View selectionBox = findViewById(R.id.selection_box);
    final View viewContainer = findViewById(R.id.query_feature_count_map_container);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(final MapboxMap mapboxMap) {

        mapboxMap.addSource(
          new GeoJsonSource("highlighted-shapes-source"));

        // add a layer to the map that highlights the maps buildings inside the bounding box.
        mapboxMap.addLayer(
          new FillLayer("highlighted-shapes-layer", "highlighted-shapes-source")
            .withProperties(fillColor(Color.parseColor("#50667F"))));

        // Toast instructing user to tap on the box
        Toast.makeText(
          FeatureCountActivity.this,
          getString(R.string.tap_on_feature_box_instruction),
          Toast.LENGTH_LONG
        ).show();

        selectionBox.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            // Perform feature query within the selectionBox. The bounding box is
            // calculated using the view but you can also use a map bounding box made up
            // of latitudes and longitudes.
            int top = selectionBox.getTop() - mapView.getTop();
            int left = selectionBox.getLeft() - mapView.getLeft();
            RectF box = new RectF(left, top, left + selectionBox.getWidth(), top + selectionBox.getHeight());
            List<Feature> features = mapboxMap.queryRenderedFeatures(box, "building");

            // Show the features count
            Snackbar.make(
              viewContainer,
              String.format(getString(R.string.feature_count_snackbar_feature_size), features.size()),
              Snackbar.LENGTH_LONG).show();

            GeoJsonSource source = mapboxMap.getSourceAs("highlighted-shapes-source");
            if (source != null) {
              source.setGeoJson(FeatureCollection.fromFeatures(features));
            }
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
}
