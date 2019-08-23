package com.mapbox.mapboxandroiddemo.examples.styles;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.VideoView;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import timber.log.Timber;

/**
 * Create a transparent render surface and add whatever you want to the background. This example
 * has a video of moving water behind Earth's land.
 */
public class TransparentBackgroundActivity extends AppCompatActivity implements OnMapReadyCallback {

  private MapView mapView;
  private VideoView backgroundWaterVideoView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_style_transparent_render_background);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull MapboxMap mapboxMap) {
    try {
      // Switch the map to a style that has no background
      mapboxMap.setStyle(new Style.Builder().fromJson(readRawResource(this, R.raw.no_bg_style)),
        new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            initVideoView();
          }
        });
    } catch (IOException exception) {
      Timber.e(exception);
    }
  }

  /**
   * Place the video of moving water behind the map
   */
  private void initVideoView() {
    backgroundWaterVideoView = findViewById(R.id.videoView);
    String path = "android.resource://" + getPackageName() + "/" + R.raw.moving_background_water;
    backgroundWaterVideoView.setVideoURI(Uri.parse(path));
    backgroundWaterVideoView.start();
    backgroundWaterVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mediaPlayer) {
        backgroundWaterVideoView.start();
      }
    });
  }

  // Add the mapView lifecycle to the activity's lifecycle methods
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
    if (backgroundWaterVideoView != null) {
      backgroundWaterVideoView.stopPlayback();
    }
    super.onDestroy();
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  /**
   * Get the map style JSON from the raw file in the app's raw folder
   */
  public static String readRawResource(Context context, @RawRes int rawResource) throws IOException {
    String json = "";
    if (context != null) {
      Writer writer = new StringWriter();
      char[] buffer = new char[1024];
      try (InputStream is = context.getResources().openRawResource(rawResource)) {
        Reader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        int numRead;
        while ((numRead = reader.read(buffer)) != -1) {
          writer.write(buffer, 0, numRead);
        }
      }
      json = writer.toString();
    }
    return json;
  }
}
