package com.mapbox.mapboxandroiddemo.examples.annotations;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

/**
 * Use an info window adapter to customize the info window.
 */
public class CustomInfoWindowActivity extends AppCompatActivity {

  private MapView mapView;

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_annotation_custom_info_window);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {

        mapboxMap.addMarker(new MarkerViewOptions()
          .position(new LatLng(40.416717, -3.703771))
          .anchor(0.5f, 0.5f)
          .title(getString(R.string.custom_window_marker_title_spain)));

        mapboxMap.addMarker(new MarkerViewOptions()
          .position(new LatLng(26.794531, 29.781524))
          .anchor(0.5f, 0.5f)
          .title(getString(R.string.custom_window_marker_title_egypt)));

        mapboxMap.addMarker(new MarkerViewOptions()
          .position(new LatLng(50.981488, 10.384677))
          .anchor(0.5f, 0.5f)
          .title(getString(R.string.custom_window_marker_title_germany)));

        mapboxMap.setInfoWindowAdapter(new MapboxMap.InfoWindowAdapter() {
          @Nullable
          @Override
          public View getInfoWindow(@NonNull Marker marker) {

            // The info window layout is created dynamically, parent is the info window
            // container
            LinearLayout parent = new LinearLayout(CustomInfoWindowActivity.this);
            parent.setLayoutParams(new LinearLayout.LayoutParams(
              ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            parent.setOrientation(LinearLayout.VERTICAL);

            // Depending on the marker latitude, the correct image source is used. If you
            // have many markers using different images, extending Marker and
            // baseMarkerOptions, adding additional options such as the image, might be
            // a better choice.
            ImageView countryFlagImage = new ImageView(CustomInfoWindowActivity.this);

            if (TextUtils.equals(marker.getTitle(), getString(R.string.custom_window_marker_title_spain))) {
              countryFlagImage.setImageDrawable(ContextCompat.getDrawable(
                CustomInfoWindowActivity.this, R.drawable.flag_of_spain));
            } else if (TextUtils.equals(marker.getTitle(), getString(R.string.custom_window_marker_title_egypt))) {
              countryFlagImage.setImageDrawable(ContextCompat.getDrawable(
                CustomInfoWindowActivity.this, R.drawable.flag_of_egypt));
            } else {
              // By default all markers without a matching latitude will use the
              // Germany flag
              countryFlagImage.setImageDrawable(ContextCompat.getDrawable(
                CustomInfoWindowActivity.this, R.drawable.flag_of_germany));
            }

            // Set the size of the image
            countryFlagImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(150, 100));

            // add the image view to the parent layout
            parent.addView(countryFlagImage);

            return parent;
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
