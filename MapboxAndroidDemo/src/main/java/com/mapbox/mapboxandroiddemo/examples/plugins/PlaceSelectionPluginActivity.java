package com.mapbox.mapboxandroiddemo.examples.plugins;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.places.picker.PlacePicker;
import com.mapbox.mapboxsdk.plugins.places.picker.model.PlacePickerOptions;

/**
 * Use the place picker functionality inside of the Places Plugin, to show UI for
 * choosing a map location. Once selected, return to the previous location with a
 * CarmenFeature to extract information from for whatever use that you want.
 */
public class PlaceSelectionPluginActivity extends AppCompatActivity {

  private static final int REQUEST_CODE = 5678;
  private TextView selectedLocationTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_place_selection);

    selectedLocationTextView = findViewById(R.id.selected_location_info_textview);
    goToPickerActivity();
  }

  /**
   * Set up the PlacePickerOptions and startActivityForResult
   */
  private void goToPickerActivity() {
    startActivityForResult(
      new PlacePicker.IntentBuilder()
        .accessToken(getString(R.string.access_token))
        .placeOptions(PlacePickerOptions.builder()
          .statingCameraPosition(new CameraPosition.Builder()
            .target(new LatLng(40.7544, -73.9862)).zoom(16).build())
          .build())
        .build(this), REQUEST_CODE);
  }

  /**
   * This fires after a location is selected in the Places Plugin's PlacePickerActivity.
   * @param requestCode code that is a part of the return to this activity
   * @param resultCode code that is a part of the return to this activity
   * @param data the data that is a part of the return to this activity
   */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == RESULT_CANCELED) {
      // Show the button and set the OnClickListener()
      Button goToPickerActivityButton = findViewById(R.id.go_to_picker_button);
      goToPickerActivityButton.setVisibility(View.VISIBLE);
      goToPickerActivityButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          goToPickerActivity();
        }
      });
    } else if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
      // Retrieve the information from the selected location's CarmenFeature
      CarmenFeature carmenFeature = PlacePicker.getPlace(data);

      // Set the TextView text to the entire CarmenFeature. The CarmenFeature
      // also be parsed through to grab and display certain information such as
      // its placeName, text, or coordinates.
      if (carmenFeature != null) {
        selectedLocationTextView.setText(String.format(
          getString(R.string.selected_place_info), carmenFeature.toJson()));
      }
    }
  }
}
