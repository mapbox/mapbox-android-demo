package com.mapbox.mapboxandroiddemo.examples.annotations;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

public class BasicMarkerViewActivity extends AppCompatActivity {

  private MapView mapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_annotation_basic_marker_view);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {

        IconFactory iconFactory = IconFactory.getInstance(BasicMarkerViewActivity.this);
        Drawable iconDrawable = ContextCompat.getDrawable(BasicMarkerViewActivity.this, R.drawable.purple_marker);
        Icon icon = iconFactory.fromDrawable(iconDrawable);

        // The easiest way to add a marker view
        mapboxMap.addMarker(new MarkerViewOptions()
            .position(new LatLng(-37.821629, 144.978535)));

        // marker view using all the different options available
        mapboxMap.addMarker(new MarkerViewOptions()
            .position(new LatLng(-37.822829, 144.981842))
            .icon(icon)
            .rotation(90)
            .anchor(0.5f, 0.5f)
            .alpha(0.5f)
            .title("Hisense Arena")
            .snippet("Olympic Blvd, Melbourne VIC 3001, Australia")
            .infoWindowAnchor(0.5f, 0.5f)
            .flat(true));
      }
    });
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