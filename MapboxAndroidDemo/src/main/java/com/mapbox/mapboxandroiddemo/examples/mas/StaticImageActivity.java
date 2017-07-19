package com.mapbox.mapboxandroiddemo.examples.mas;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.services.Constants;
import com.mapbox.services.api.staticimage.v1.MapboxStaticImage;
import com.squareup.picasso.Picasso;

/**
 * This example uses the static image API found inside the Mapbox Services SDK to create an API URL. Using the newly
 * created URL, Picasso, a third party image loading library, is used to download and loading in the image.
 */
public class StaticImageActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_mas_static_image);

    ImageView veniceImageView = (ImageView) findViewById(R.id.veniceImageView);
    ImageView parisImageView = (ImageView) findViewById(R.id.parisImageView);
    ImageView londonImageView = (ImageView) findViewById(R.id.londonImageView);

    MapboxStaticImage veniceStaticImage = new MapboxStaticImage.Builder()
      .setAccessToken(getString(R.string.access_token))
      .setStyleId(Constants.MAPBOX_STYLE_LIGHT)
      .setLat(45.4338) // Image center Latitude
      .setLon(12.3378) // Image center longitude
      .setZoom(13)
      .setWidth(320) // Image width
      .setHeight(320) // Image height
      .setRetina(true) // Retina 2x image will be returned
      .build();

    Picasso.with(this).load(veniceStaticImage.getUrl().toString()).into(veniceImageView);

    MapboxStaticImage parisStaticImage = new MapboxStaticImage.Builder()
      .setAccessToken(getString(R.string.access_token))
      .setStyleId(Constants.MAPBOX_STYLE_OUTDOORS)
      .setLat(48.85826)
      .setLon(2.29450)
      .setZoom(16)
      .setPitch(20)
      .setBearing(60)
      .setWidth(320)
      .setHeight(320)
      .setRetina(true)
      .build();

    Picasso.with(this).load(parisStaticImage.getUrl().toString()).into(parisImageView);

    MapboxStaticImage londonStaticImage = new MapboxStaticImage.Builder()
      .setAccessToken(getString(R.string.access_token))
      .setStyleId(Constants.MAPBOX_STYLE_STREETS)
      .setLat(51.5062)
      .setLon(-0.0756)
      .setZoom(14)
      .setWidth(320)
      .setHeight(320)
      .setRetina(true)
      .build();

    Picasso.with(this).load(londonStaticImage.getUrl().toString()).into(londonImageView);
  }
}