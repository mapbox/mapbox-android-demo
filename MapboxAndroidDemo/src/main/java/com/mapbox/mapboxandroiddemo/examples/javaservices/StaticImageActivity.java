package com.mapbox.mapboxandroiddemo.examples.javaservices;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.squareup.picasso.Picasso;

/**
 * Use the Mapbox Static Image API found inside the Java Services SDK to create a URL. After receiving the newly
 * created URL, use Picasso, a third party image loading library, to download and load the static map image into
 * the ImageView.
 */
public class StaticImageActivity extends AppCompatActivity implements
  OnMapReadyCallback, RadioGroup.OnCheckedChangeListener {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private ImageView staticMapImageView;
  private Button createStaticImageButton;
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
  public void onMapReady(MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    staticMapImageView = findViewById(R.id.static_map_imageview);
    createStaticImageButton = findViewById(R.id.create_static_image_button);
    mapStyleRadioGroup = findViewById(R.id.map_style_radio_group);

    mapStyleRadioGroup.setOnCheckedChangeListener(this);
    createStaticImageButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (mapStyleRadioGroup.getCheckedRadioButtonId() == -1) {
          Toast.makeText(StaticImageActivity.this, R.string.select_a_style, Toast.LENGTH_SHORT).show();
        } else {
          // one of the radio buttons is checked
          Picasso.with(StaticImageActivity.this).load(takeSnapshot(
              mapboxMap.getCameraPosition().zoom,
              mapboxMap.getStyleUrl().equals("mapbox://styles/mapbox/dark-v9") ? StaticMapCriteria.DARK_STYLE :
                StaticMapCriteria.STREET_STYLE,
              mapboxMap.getCameraPosition().target,
              findViewById(R.id.static_map_imageview).getMeasuredWidth(),
              findViewById(R.id.static_map_imageview).getMeasuredHeight())
              .url().toString()).into(staticMapImageView);
        }
      }
    });
  }

  @Override
  public void onCheckedChanged(RadioGroup radioGroup, int selectedButtonInt) {
    switch (selectedButtonInt) {
      case 2131296642:
        // Selected Streets style
        mapboxMap.setStyle(Style.MAPBOX_STREETS);
        break;
      case 2131296351:
        // Selected Dark style
        mapboxMap.setStyle(Style.DARK);
        break;
      default:
        mapboxMap.setStyle(Style.MAPBOX_STREETS);
        break;
    }
  }

  private MapboxStaticMap takeSnapshot(double imageZoom, String styleUrl, LatLng imageTarget, int width,
                                       int height) {
    return MapboxStaticMap.builder()
      .accessToken(getString(R.string.access_token))
      .styleId(styleUrl)
      .cameraPoint(Point.fromLngLat(imageTarget.getLongitude(), imageTarget.getLatitude()))
      .cameraZoom(imageZoom)
      .width(width)
      .height(height)
      .retina(true)
      .build();
  }
}