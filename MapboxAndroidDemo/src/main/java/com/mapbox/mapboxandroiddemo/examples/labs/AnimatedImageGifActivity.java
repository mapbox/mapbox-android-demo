package com.mapbox.mapboxandroiddemo.examples.labs;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngQuad;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
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

    map.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        // Use the RefreshImageRunnable class and runnable to quickly display images for a GIF/video UI experience
        InputStream gifInputStream = getResources().openRawResource(R.raw.waving_bear);
        runnable = new RefreshImageRunnable(style, Movie.decodeStream(gifInputStream), handler = new Handler());
        handler.postDelayed(runnable, 100);
      }
    });
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
    if (handler != null) {
      handler.removeCallbacks(runnable);
    }
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
    private Style style;
    private Movie movie;
    private Handler handler;
    private long movieStart;
    private Bitmap bitmap;
    private Canvas canvas;

    RefreshImageRunnable(Style style, Movie movie, Handler handler) {
      this.style = style;
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

      if (imageSource == null) {
        // Set the bounds/size of the gif. Then create an image source object with a unique id,
        // the bounds, and drawable image
        imageSource = new ImageSource(ID_IMAGE_SOURCE,
          new LatLngQuad(
            new LatLng(46.437, -80.425),
            new LatLng(46.437, -71.516),
            new LatLng(37.936, -71.516),
            new LatLng(37.936, -80.425)), bitmap);

        // Add the source to the map
        style.addSource(imageSource);

        // Create an raster layer with a unique id and the image source created above. Then add the layer to the map.
        style.addLayer(new RasterLayer(ID_IMAGE_LAYER, ID_IMAGE_SOURCE));
      }

      imageSource.setImage(bitmap);
      handler.postDelayed(this, 50);
    }
  }
}
