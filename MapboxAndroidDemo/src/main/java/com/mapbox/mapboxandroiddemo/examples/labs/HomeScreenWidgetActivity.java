package com.mapbox.mapboxandroiddemo.examples.labs;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.mapboxandroiddemo.R;

/**
 * A placeholder activity and XML layout, which describe instruction on installing and viewing
 * the demo app's home screen widget. The widget retrieves the device's location,
 * makes a geocoding request to the Mapbox Geocoding API, and displays the real-world
 * address that's associated with the device's location.
 *
 * Visit the {@link com.mapbox.mapboxandroiddemo.utils.DemoAppHomeScreenAddressWidget}
 * class to see the internal workings of the widget.
 */
public class HomeScreenWidgetActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_homescreen_geocoding_widget);
  }
}