package com.mapbox.mapboxandroiddemo.examples.styles;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.Layer;

import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_BOTTOM;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_BOTTOM_LEFT;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_BOTTOM_RIGHT;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_CENTER;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_LEFT;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_RIGHT;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_TOP;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_TOP_LEFT;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_TOP_RIGHT;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textIgnorePlacement;

/**
 * Use runtime-styling to adjust the state label SymbolLayer's textField anchor position.
 */
public class RotatingTextAnchorPositionActivity extends AppCompatActivity {

  private MapView mapView;
  private Layer stateLabelSymbolLayer;
  private int index = 0;
  private TextView anchorPositionTextView;
  private static final String[] anchorOptions = new String[] {
    TEXT_ANCHOR_CENTER, // The center of the text is placed closest to the anchor.
    TEXT_ANCHOR_LEFT, // The left side of the text is placed closest to the anchor.
    TEXT_ANCHOR_RIGHT, // The right side of the text is placed closest to the anchor.
    TEXT_ANCHOR_TOP, // The top of the text is placed closest to the anchor.
    TEXT_ANCHOR_BOTTOM, // The bottom of the text is placed closest to the anchor.
    TEXT_ANCHOR_TOP_LEFT, // The top left corner of the text is placed closest to the anchor.
    TEXT_ANCHOR_TOP_RIGHT, // The top right corner of the text is placed closest to the anchor.
    TEXT_ANCHOR_BOTTOM_LEFT, // The bottom left corner of the text is placed closest to the anchor.
    TEXT_ANCHOR_BOTTOM_RIGHT, // The bottom right corner of the text is placed closest to the anchor.
  };

  private static final String[] anchorDescriptions = new String[] {
    "The center of the text is placed closest to the anchor.",
    "The left side of the text is placed closest to the anchor.",
    "The right side of the text is placed closest to the anchor.",
    "The top of the text is placed closest to the anchor.",
    "The bottom of the text is placed closest to the anchor.",
    "The top left corner of the text is placed closest to the anchor.",
    "The top right corner of the text is placed closest to the anchor.",
    "The bottom left corner of the text is placed closest to the anchor.",
    "The bottom right corner of the text is placed closest to the anchor."
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_dds_style_rotating_text_anchor);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            stateLabelSymbolLayer = style.getLayer("state-label");
            if (stateLabelSymbolLayer != null) {

              // Use runtime-styling to ignore collision and allow overlap, so that adjusted text in this example,
              // is more easily visible.
              stateLabelSymbolLayer.setProperties(
                textIgnorePlacement(true),
                textAllowOverlap(true)
              );

              anchorPositionTextView = findViewById(R.id.anchor_position_textview);
              setTextView(anchorOptions[index], anchorDescriptions[index]);

              findViewById(R.id.switch_anchor_position_fab).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                  index++;
                  if (index == anchorOptions.length - 1) {
                    index = 0;
                  }
                  // Use runtime styling to adjust the SymbolLayer's textAnchor position
                  stateLabelSymbolLayer.setProperties(textAnchor(anchorOptions[index]));

                  setTextView(anchorOptions[index], anchorDescriptions[index]);
                }
              });
            }
          }
        });
      }
    });
  }

  private void setTextView(String anchorOption, String anchorDescription) {
    anchorPositionTextView.setText(String.format(getString(R.string.position_textview),
      anchorOption, anchorDescription));
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
    super.onDestroy();
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}

