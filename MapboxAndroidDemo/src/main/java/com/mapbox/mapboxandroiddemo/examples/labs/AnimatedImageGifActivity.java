package com.mapbox.mapboxandroiddemo.examples.labs;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngQuad;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.sources.ImageSource;

import java.io.InputStream;

/**
 * Add an animated image (GIF) anywhere on the map
 */
public class AnimatedImageGifActivity extends AppCompatActivity implements OnMapReadyCallback {

  private static final String ID_IMAGE_SOURCE = "animated_image_source";
  private static final String ID_IMAGE_LAYER = "animated_image_layer";

  private MapView mapView;
  private Handler handler;
  private Runnable runnable;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_animated_image_gif);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap map) {

    // Set the bounds/size of the gif
    LatLngQuad quad = new LatLngQuad(
      new LatLng(46.437, -80.425),
      new LatLng(46.437, -71.516),
      new LatLng(37.936, -71.516),
      new LatLng(37.936, -80.425));

    // Create an image source object with a unique id, the bounds, and drawable image
    ImageSource imageSource = new ImageSource(ID_IMAGE_SOURCE, quad, R.drawable.waving_bear_gif);

    // Add the source to the map
    map.addSource(imageSource);

    // Create an raster layer with a unique id and the image source created above
    RasterLayer layer = new RasterLayer(ID_IMAGE_LAYER, ID_IMAGE_SOURCE);

    // Add the layer to the map
    map.addLayer(layer);

    // Use the RefreshImageRunnable class and runnable to quickly display images for a GIF/video UI experience
    InputStream gifInputStream = getResources().openRawResource(R.raw.waving_bear);
    runnable = new RefreshImageRunnable(imageSource, Movie.decodeStream(gifInputStream), handler = new Handler());
    handler.postDelayed(runnable, 100);
  }

  @Override
  protected void onStart() {
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
  protected void onStop() {
    super.onStop();
    mapView.onStop();
    handler.removeCallbacks(runnable);
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

  private static class RefreshImageRunnable implements Runnable {

    private ImageSource imageSource;
    private Movie movie;
    private Handler handler;
    private long movieStart;
    private Bitmap bitmap;
    private Canvas canvas;

    RefreshImageRunnable(ImageSource imageSource, Movie movie, Handler handler) {
      this.imageSource = imageSource;
      this.movie = movie;
      this.handler = handler;
      bitmap = Bitmap.createBitmap(movie.width(), movie.height(), Bitmap.Config.ARGB_8888);
      canvas = new Canvas(bitmap);
    }

    @Override
    public void run() {
      long now = android.os.SystemClock.uptimeMillis();
      if (movieStart == 0) {
        movieStart = now;
      }

      int dur = movie.duration();
      if (dur == 0) {
        dur = 1000;
      }

      movie.setTime((int) ((now - movieStart) % dur));
      movie.draw(canvas, 0, 0);

      imageSource.setImage(bitmap);
      handler.postDelayed(this, 50);
    }
  }
}
