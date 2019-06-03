package com.mapbox.mapboxandroiddemo.examples.dds;

import android.graphics.Color;
import android.os.Bundle;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static com.mapbox.mapboxsdk.style.expressions.Expression.exponential;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;

/**
 * Set the radii of a CircleLayer's circles based on a data property.
 */
public class CircleRadiusActivity extends AppCompatActivity {

  private MapView mapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Mapbox.getInstance(this, getString(R.string.access_token));
    setContentView(R.layout.activity_circle_radius_activity);
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        mapboxMap.setStyle(Style.DARK, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            // replace examples.8mj5l1r9 with the map ID for the tileset
            // you created by uploading data to your Mapbox account
            style.addSource(new VectorSource("trees-source", "mapbox://examples.8mj5l1r9"));

            CircleLayer circleLayer = new CircleLayer("trees-style", "trees-source");
            // replace street-trees-DC-9gvg5l with the name of your source layer
            circleLayer.setSourceLayer("street-trees-DC-9gvg5l");
            circleLayer.withProperties(
                    circleOpacity(0.6f),
                    circleColor(Color.parseColor("#ffffff")),
                    circleRadius(
                            interpolate(exponential(1.0f), get("DBH"),
                                    stop(0, 0f),
                                    stop(1, 1f),
                                    stop(110, 11f)
                            )
                    )
            );
            style.addLayer(circleLayer);
          }
        });
      }
    });
  }

  // Add the mapView's own lifecycle methods to the activity's lifecycle methods
  @Override
  public void onStart() {
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
  public void onStop() {
    super.onStop();
    mapView.onStop();
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