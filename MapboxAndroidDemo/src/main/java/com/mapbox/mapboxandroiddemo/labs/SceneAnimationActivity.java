package com.mapbox.mapboxandroiddemo.labs;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

public class SceneAnimationActivity extends AppCompatActivity {

  private MapView mapView;
  private ImageView placeHolderImageView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));


    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_scene_animation);

    mapView = (MapView) findViewById(R.id.mapView);
    BitmapUtils bitmapUtils = new BitmapUtils();
    placeHolderImageView = (ImageView) findViewById(R.id.placeholder_imageView);
    placeHolderImageView.setImageBitmap(bitmapUtils.createBitmapFromView(mapView));


    mapView.setVisibility(View.INVISIBLE);

    //TODO: Get bitmap of map?


    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {

        placeHolderImageView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            mapView.setVisibility(View.VISIBLE);
            placeHolderImageView.setVisibility(View.INVISIBLE);
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
