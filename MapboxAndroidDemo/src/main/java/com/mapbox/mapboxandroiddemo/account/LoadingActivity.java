package com.mapbox.mapboxandroiddemo.account;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;

/**
 * Full-screen loading activity that's displayed while Mapbox Account is being retrieved.
 */
public class LoadingActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getSupportActionBar().setDisplayShowTitleEnabled(false);
    setContentView(R.layout.activity_loading);
  }
}