package com.mapbox.mapboxandroiddemo.examples.javaservices;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.mapbox.api.staticmap.v1.MapboxStaticMap;
import com.mapbox.api.staticmap.v1.StaticMapCriteria;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.utils.MathUtils;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Use the Mapbox Static Image API found inside the Mapbox Java SDK to create a URL. After receiving the newly
 * created URL, use Picasso, a third party image loading library, to download and load the static map image into
 * the ImageView.
 */
public class StaticImageActivity extends AppCompatActivity implements
  OnMapReadyCallback, RadioGroup.OnCheckedChangeListener {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private ImageView staticMapImageView;
  private RadioGroup mapStyleRadioGroup;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_javaservices_static_image);
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    mapboxMap.setStyle(Style.MAPBOX_STREETS);
    initViews();
  }

  private void initViews() {
    staticMapImageView = findViewById(R.id.static_map_imageview);
    Button createStaticImageButton = findViewById(R.id.create_static_image_button);
    createStaticImageButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (mapStyleRadioGroup.getCheckedRadioButtonId() == -1) {
          Toast.makeText(StaticImageActivity.this, R.string.select_a_style, Toast.LENGTH_SHORT).show();
        } else {
          mapboxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
              // one of the radio buttons is checked
              Picasso.with(StaticImageActivity.this).load(takeSnapshot(
                mapboxMap.getCameraPosition(),
                style.getUri().contains("mapbox/dark-v")
                  ? StaticMapCriteria.DARK_STYLE :
                  StaticMapCriteria.STREET_STYLE,
                (int) MathUtils.clamp(findViewById(R.id.static_map_imageview).getMeasuredWidth(), 0, 1280),
                (int) MathUtils.clamp(findViewById(R.id.static_map_imageview).getMeasuredHeight(), 0, 1280))
                .url().toString()).into(staticMapImageView);
            }
          });
        }
      }
    });
    mapStyleRadioGroup = findViewById(R.id.map_style_radio_group);
    mapStyleRadioGroup.setOnCheckedChangeListener(StaticImageActivity.this);
  }

  @Override
  public void onCheckedChanged(RadioGroup radioGroup, int selectedButtonInt) {
    switch (selectedButtonInt) {
      case R.id.streets_style_select_radio_button:
        mapboxMap.setStyle(Style.MAPBOX_STREETS);
        break;
      case R.id.dark_style_select_radio_button:
        mapboxMap.setStyle(Style.DARK);
        break;
      default:
        mapboxMap.setStyle(Style.MAPBOX_STREETS);
        break;
    }
  }

  private MapboxStaticMap takeSnapshot(CameraPosition cameraPosition, String styleUrl, int width,
                                       int height) {
    return MapboxStaticMap.builder()
      .accessToken(getString(R.string.access_token))
      .styleId(styleUrl)
      .cameraPoint(Point.fromLngLat(cameraPosition.target.getLongitude(),
        cameraPosition.target.getLatitude()))
      .cameraZoom(cameraPosition.zoom)
      .cameraPitch(cameraPosition.tilt)
      .cameraBearing(cameraPosition.bearing)
      .width(width)
      .height(height)
      .retina(true)
      .build();
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  protected void onStop() {
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
