package com.mapbox.mapboxandroiddemo.labs;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

public class SliderCompareActivity extends AppCompatActivity {

  private MapView mapViewOne;
  private MapView mapViewTwo;
  private SeekBar seekBar;
  private String TAG = "SliderCompareActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_slider_compare);

    mapViewOne = (MapView) findViewById(R.id.mapViewOne);
    mapViewTwo = (MapView) findViewById(R.id.mapViewTwo);
    seekBar = (SeekBar) findViewById(R.id.slider_compare_seek_bar);

    mapViewOne.onCreate(savedInstanceState);
    mapViewTwo.onCreate(savedInstanceState);
    mapViewOne.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {

        findViewById(R.id.slider_compare_container).setOnTouchListener(new View.OnTouchListener() {
          @Override
          public boolean onTouch(View v, MotionEvent event) {
            Log.d(TAG, "onTouch: ");
            mapViewOne.onTouchEvent(event);
            mapViewTwo.onTouchEvent(event);
            return false;
          }
        });
      }
    });

    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
        Log.d(TAG, "onProgressChanged: progressValue = " + progressValue);
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.target_framelayout);
        frameLayout.setLayoutParams(new CoordinatorLayout.LayoutParams(progressValue, frameLayout.getLayoutParams().height));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });
  }

  // Add the mapViewOne lifecycle to the activity's lifecycle methods
  @Override
  public void onResume() {
    super.onResume();
    mapViewOne.onResume();
    mapViewTwo.onResume();
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapViewOne.onStart();
    mapViewTwo.onStart();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapViewOne.onStop();
    mapViewTwo.onStop();
  }

  @Override
  public void onPause() {
    super.onPause();
    mapViewOne.onPause();
    mapViewTwo.onPause();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapViewOne.onLowMemory();
    mapViewTwo.onLowMemory();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapViewOne.onDestroy();
    mapViewTwo.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapViewOne.onSaveInstanceState(outState);
    mapViewTwo.onSaveInstanceState(outState);
  }
}