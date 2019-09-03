package com.mapbox.mapboxandroiddemo.examples.labs;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

/**
 * Animate the marker to a new position on the map.
 */
public class AnimatedMarkerActivity extends AppCompatActivity implements OnMapReadyCallback,
  MapboxMap.OnMapClickListener {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private LatLng currentPosition = new LatLng(64.900932, -18.167040);
  private GeoJsonSource geoJsonSource;
  private ValueAnimator animator;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_lab_animated_marker);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;

    geoJsonSource = new GeoJsonSource("source-id",
      Feature.fromGeometry(Point.fromLngLat(currentPosition.getLongitude(),
        currentPosition.getLatitude())));


    mapboxMap.setStyle(Style.SATELLITE_STREETS, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {

        style.addImage(("marker_icon"), BitmapFactory.decodeResource(
          getResources(), R.drawable.red_marker));

        style.addSource(geoJsonSource);

        style.addLayer(new SymbolLayer("layer-id", "source-id")
          .withProperties(
            PropertyFactory.iconImage("marker_icon"),
            PropertyFactory.iconIgnorePlacement(true),
            PropertyFactory.iconAllowOverlap(true)
          ));

        Toast.makeText(
          AnimatedMarkerActivity.this,
          getString(R.string.tap_on_map_instruction),
          Toast.LENGTH_LONG
        ).show();

        mapboxMap.addOnMapClickListener(AnimatedMarkerActivity.this);

      }
    });
  }

  @Override
  public boolean onMapClick(@NonNull LatLng point) {
    // When the user clicks on the map, we want to animate the marker to that
    // location.
    if (animator != null && animator.isStarted()) {
      currentPosition = (LatLng) animator.getAnimatedValue();
      animator.cancel();
    }

    animator = ObjectAnimator
      .ofObject(latLngEvaluator, currentPosition, point)
      .setDuration(2000);
    animator.addUpdateListener(animatorUpdateListener);
    animator.start();

    currentPosition = point;
    return true;
  }

  private final ValueAnimator.AnimatorUpdateListener animatorUpdateListener =
    new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator valueAnimator) {
        LatLng animatedPosition = (LatLng) valueAnimator.getAnimatedValue();
        geoJsonSource.setGeoJson(Point.fromLngLat(animatedPosition.getLongitude(), animatedPosition.getLatitude()));
      }
    };

  // Class is used to interpolate the marker animation.
  private static final TypeEvaluator<LatLng> latLngEvaluator = new TypeEvaluator<LatLng>() {

    private final LatLng latLng = new LatLng();

    @Override
    public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
      latLng.setLatitude(startValue.getLatitude()
        + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
      latLng.setLongitude(startValue.getLongitude()
        + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
      return latLng;
    }
  };

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
    if (animator != null) {
      animator.cancel();
    }
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
