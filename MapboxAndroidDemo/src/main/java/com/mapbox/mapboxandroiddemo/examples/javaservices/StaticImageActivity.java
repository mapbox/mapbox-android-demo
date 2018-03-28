package com.mapbox.mapboxandroiddemo.examples.javaservices;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;

import com.mapbox.api.staticmap.v1.MapboxStaticMap;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.MainActivity;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.constants.Style;
import com.squareup.picasso.Picasso;

/**
 * This example uses the static image API found inside the Java Services SDK to create an API URL. Using the newly
 * created URL, Picasso, a third party image loading library, is used to download and loading in the image.
 */
public class StaticImageActivity extends AppCompatActivity {
  CardView banner;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_javaservices_static_image);

    ImageView veniceImageView = (ImageView) findViewById(R.id.veniceImageView);
    ImageView parisImageView = (ImageView) findViewById(R.id.parisImageView);
    ImageView londonImageView = (ImageView) findViewById(R.id.londonImageView);

    MapboxStaticMap veniceStaticImage = MapboxStaticMap.builder()
      .accessToken(getString(R.string.access_token))
      .styleId(Style.LIGHT)
      .cameraPoint(Point.fromLngLat(12.3378, 45.4338))
      .cameraZoom(13)
      .width(320) // Image width
      .height(320) // Image height
      .retina(true) // Retina 2x image will be returned
      .build();

    Picasso.with(this).load(veniceStaticImage.url().toString()).into(veniceImageView);

    MapboxStaticMap parisStaticImage = MapboxStaticMap.builder()
      .accessToken(getString(R.string.access_token))
      .styleId(Style.OUTDOORS)
      .cameraPoint(Point.fromLngLat(2.29450, 48.85826))
      .cameraZoom(16)
      .cameraPitch(20)
      .cameraBearing(60)
      .width(320)
      .height(320)
      .retina(true)
      .build();

    Picasso.with(this).load(parisStaticImage.url().toString()).into(parisImageView);

    MapboxStaticMap londonStaticImage = MapboxStaticMap.builder()
      .accessToken(getString(R.string.access_token))
      .styleId(Style.MAPBOX_STREETS)
      .cameraPoint(Point.fromLngLat(-0.0756, 51.5062))
      .cameraZoom(14)
      .width(320)
      .height(320)
      .retina(true)
      .build();

    Picasso.with(this).load(londonStaticImage.url().toString()).into(londonImageView);
    banner = findViewById(R.id.banner);
    banner.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(StaticImageActivity.this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_NAV, R.id.nav_snapshot_image_generator);
        startActivity(intent);
      }
    });
  }
}