package com.mapbox.mapboxandroiddemo.examples.styles;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngQuad;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.ImageSource;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;

/**
 * Tap the map in four locations to set the bounds for an image that is selected from the device's gallery
 * and then added to the map.
 */
public class ClickToAddImageActivity extends AppCompatActivity implements
  OnMapReadyCallback, MapboxMap.OnMapClickListener {

  private static final String ID_IMAGE_SOURCE = "source-id";
  private static final String CIRCLE_SOURCE_ID = "circle-source-id";
  private static final String ID_IMAGE_LAYER = "layer-id";
  private static int PHOTO_PICK_CODE = 4;
  private MapView mapView;
  private MapboxMap mapboxMap;
  private LatLngQuad quad;
  private List<Feature> boundsFeatureList;
  private List<Point> boundsCirclePointList;
  private int imageCountIndex;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_click_to_add_image);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
    mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        boundsFeatureList = new ArrayList<>();
        boundsCirclePointList = new ArrayList<>();
        ClickToAddImageActivity.this.mapboxMap = mapboxMap;
        mapboxMap.addOnMapClickListener(ClickToAddImageActivity.this);
        imageCountIndex = 0;
        initCircleSource(style);
        initCircleLayer(style);
        Toast.makeText(ClickToAddImageActivity.this, R.string.tap_instructions, Toast.LENGTH_LONG).show();
      }
    });
  }

  @Override
  public boolean onMapClick(@NonNull LatLng point) {

    // Reset the lists once enough LatLngQuad points have been tapped
    if (boundsFeatureList.size() == 4) {
      boundsFeatureList = new ArrayList<>();
      boundsCirclePointList = new ArrayList<>();
    }

    boundsFeatureList.add(Feature.fromGeometry(Point.fromLngLat(point.getLongitude(), point.getLatitude())));

    // Add the click point to the CircleLayer and update the display of the CircleLayer data
    boundsCirclePointList.add(Point.fromLngLat(point.getLongitude(), point.getLatitude()));

    Style style = mapboxMap.getStyle();
    if (style != null) {
      GeoJsonSource circleSource = style.getSourceAs(CIRCLE_SOURCE_ID);
      if (circleSource != null) {
        circleSource.setGeoJson(FeatureCollection.fromFeatures(boundsFeatureList));
      }
    }

    // Once the 4 LatLngQuad points have been set for where the image will placed...
    if (boundsCirclePointList.size() == 4) {

      // Create the LatLng objects to use in the LatLngQuad
      LatLng latLng1 = new LatLng(boundsCirclePointList.get(0).latitude(),
        boundsCirclePointList.get(0).longitude());
      LatLng latLng2 = new LatLng(boundsCirclePointList.get(1).latitude(),
        boundsCirclePointList.get(1).longitude());
      LatLng latLng3 = new LatLng(boundsCirclePointList.get(2).latitude(),
        boundsCirclePointList.get(2).longitude());
      LatLng latLng4 = new LatLng(boundsCirclePointList.get(3).latitude(),
        boundsCirclePointList.get(3).longitude());
      quad = new LatLngQuad(latLng1, latLng2, latLng3, latLng4);

      // Launch the intent to open the device's image gallery picker
      Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK,
        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
      pickPhotoIntent.setType("image/*");
      startActivityForResult(pickPhotoIntent, PHOTO_PICK_CODE);
    }
    return true;
  }

  /**
   * Set up the CircleLayer source for showing LatLngQuad map click points
   */
  private void initCircleSource(@NonNull Style loadedMapStyle) {
    loadedMapStyle.addSource(
      new GeoJsonSource(CIRCLE_SOURCE_ID, FeatureCollection.fromFeatures(new Feature[] {}))
    );
  }

  /**
   * Set up the CircleLayer for showing LatLngQuad map click points
   */
  private void initCircleLayer(@NonNull Style loadedMapStyle) {
    loadedMapStyle.addLayer(new CircleLayer("circle-layer-bounds-corner-id",
      CIRCLE_SOURCE_ID).withProperties(
      circleRadius(8f),
      circleColor(Color.parseColor("#d004d3"))
    ));
  }

  /**
   * Calling onActivityResult() to handle the return to the example from the device's image galleyr picker
   */
  @Override
  public void onActivityResult(int requestCode, int resultCode, final Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == PHOTO_PICK_CODE && resultCode == Activity.RESULT_OK) {
      if (data == null) {
        //Display an error
        Timber.d("data == null");
        return;
      }

      if (mapboxMap != null) {
        mapboxMap.getStyle(new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            Uri selectedImage = data.getData();
            InputStream imageStream;
            try {
              if (selectedImage != null) {
                imageStream = getContentResolver().openInputStream(selectedImage);

                Bitmap bitmapOfSelectedImage = BitmapFactory.decodeStream(imageStream);

                // Add the imageSource to the map
                style.addSource(
                  new ImageSource(ID_IMAGE_SOURCE + imageCountIndex, quad, bitmapOfSelectedImage));

                // Create a raster layer and use the imageSource's ID as the layer's data// Add the layer to the map
                style.addLayer(new RasterLayer(ID_IMAGE_LAYER + imageCountIndex,
                  ID_IMAGE_SOURCE + imageCountIndex));

                // Reset lists in preparation for adding more images
                boundsFeatureList = new ArrayList<>();
                boundsCirclePointList = new ArrayList<>();

                imageCountIndex++;

                // Clear circles from CircleLayer
                GeoJsonSource circleSource = style.getSourceAs(CIRCLE_SOURCE_ID);
                if (circleSource != null) {
                  circleSource.setGeoJson(FeatureCollection.fromFeatures(boundsFeatureList));
                }
              }
            } catch (FileNotFoundException exception) {
              exception.printStackTrace();
            }
          }
        });
      }

    }
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
    if (mapboxMap != null) {
      mapboxMap.removeOnMapClickListener(this);
    }
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}