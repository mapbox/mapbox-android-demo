package com.mapbox.mapboxandroiddemo.examples.annotations;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

public class AnimatedCircleMarkerActivity extends Activity {

  private MapView mapView;
  private MarkerView circle;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_annotation_animated_circle_marker);

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(final MapboxMap mapboxMap) {

        IconFactory iconFactory = IconFactory.getInstance(AnimatedCircleMarkerActivity.this);
        Drawable iconDrawable = ContextCompat.getDrawable(AnimatedCircleMarkerActivity.this, R.drawable.circle_icon);
        Icon icon = iconFactory.fromDrawable(iconDrawable);

        circle = mapboxMap.addMarker(new MarkerViewOptions()
            .position(new LatLng(40.73581, -73.99155))
            .anchor(0.5f, 0.5f)
            .icon(icon));

        View view = mapboxMap.getMarkerViewManager().getView(circle);

        ValueAnimator scaleCircleX = ObjectAnimator.ofFloat(view, "scaleX", 1.5f);
        ValueAnimator scaleCircleY = ObjectAnimator.ofFloat(view, "scaleY", 1.5f);
        scaleCircleX.setDuration(3000);
        scaleCircleY.setDuration(3000);
        scaleCircleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleCircleY.setRepeatCount(ValueAnimator.INFINITE);
        scaleCircleX.setRepeatMode(ObjectAnimator.REVERSE);
        scaleCircleY.setRepeatMode(ObjectAnimator.REVERSE);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(scaleCircleX).with(scaleCircleY);
        animatorSet.start();

      }
    });
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
