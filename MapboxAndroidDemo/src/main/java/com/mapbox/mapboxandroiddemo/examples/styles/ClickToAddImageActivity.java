package com.mapbox.mapboxandroiddemo.examples.styles;

// #-code-snippet: click-to-add-image-activity full-java

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngQuad;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.ImageSource;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
  private MapView mapView;
  private MapboxMap mapboxMap;
  private LatLngQuad quad;
  private List<Feature> boundsFeatureList;
  private List<Point> boundsCirclePointList;
  private static int PHOTO_PICK_CODE = 4;
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
  public void onMapReady(MapboxMap mapboxMap) {
    boundsFeatureList = new ArrayList<>();
    boundsCirclePointList = new ArrayList<>();
    this.mapboxMap = mapboxMap;
    mapboxMap.addOnMapClickListener(this);
    imageCountIndex = 0;
    initCircleSource();
    initCircleLayer();
    Toast.makeText(this, R.string.tap_instructions, Toast.LENGTH_LONG).show();
  }

  @Override
  public void onMapClick(@NonNull LatLng point) {

    // Reset the lists once enough LatLngQuad points have been tapped
    if (boundsFeatureList.size() == 4) {
      boundsFeatureList = new ArrayList<>();
      boundsCirclePointList = new ArrayList<>();
    }

    boundsFeatureList.add(Feature.fromGeometry(Point.fromLngLat(point.getLongitude(), point.getLatitude())));

    // Add the click point to the circle layer and update the display of the circle layer data
    boundsCirclePointList.add(Point.fromLngLat(point.getLongitude(), point.getLatitude()));
    GeoJsonSource circleSource = mapboxMap.getSourceAs(CIRCLE_SOURCE_ID);
    if (circleSource != null) {
      circleSource.setGeoJson(FeatureCollection.fromFeatures(boundsFeatureList));
    }

    // Add to latLngList to eventually add a Polygon
    List<LatLng> latLngList = new ArrayList<>();
    for (Point singlePoint : boundsCirclePointList) {
      latLngList.add(new LatLng(singlePoint.latitude(), singlePoint.longitude()));
    }

    // Once the 4 LatLngQuad points have been set for where the image will placed...
    if (boundsCirclePointList.size() == 4) {

      // Add polygon
      mapboxMap.addPolygon(new PolygonOptions()
        .addAll(latLngList)
        .alpha(.3f)
        .fillColor(Color.parseColor("#d004d3")));

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
  }

  /**
   * Set up the CircleLayer source for showing LatLngQuad map click points
   */
  private void initCircleSource() {
    FeatureCollection circleFeatureCollection = FeatureCollection.fromFeatures(new Feature[] {});
    GeoJsonSource circleGeoJsonSource = new GeoJsonSource(CIRCLE_SOURCE_ID, circleFeatureCollection);
    mapboxMap.addSource(circleGeoJsonSource);
  }

  /**
   * Set up the CircleLayer for showing LatLngQuad map click points
   */
  private void initCircleLayer() {
    CircleLayer circleLayer = new CircleLayer("circle-layer-bounds-corner-id",
      CIRCLE_SOURCE_ID);
    circleLayer.setProperties(
      circleRadius(8f),
      circleColor(Color.parseColor("#d004d3"))
    );
    mapboxMap.addLayer(circleLayer);
  }

  /**
   * Calling onActivityResult() to handle the return to the example from the device's image galleyr picker
   */
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == PHOTO_PICK_CODE && resultCode == Activity.RESULT_OK) {
      if (data == null) {
        //Display an error
        Log.d("ClickToAddImageActivity", "data == null");
        return;
      }
      Uri selectedImage = data.getData();
      InputStream imageStream = null;
      try {
        imageStream = getContentResolver().openInputStream(selectedImage);

        Bitmap bitmapOfSelectedImage = BitmapFactory.decodeStream(imageStream);

        // Create an ImageSource object
        ImageSource imageSource = new ImageSource(ID_IMAGE_SOURCE + imageCountIndex, quad, bitmapOfSelectedImage);

        // Add the imageSource to the map
        mapboxMap.addSource(imageSource);

        // Create a raster layer and use the imageSource's ID as the layer's data
        RasterLayer layer = new RasterLayer(ID_IMAGE_LAYER + imageCountIndex,
          ID_IMAGE_SOURCE + imageCountIndex);

        // Add the layer to the map
        mapboxMap.addLayer(layer);

        // Reset lists in preparation for adding more images
        boundsFeatureList = new ArrayList<>();
        boundsCirclePointList = new ArrayList<>();

        imageCountIndex++;

        // Clear circles from CircleLayer
        GeoJsonSource circleSource = mapboxMap.getSourceAs(CIRCLE_SOURCE_ID);
        if (circleSource != null) {
          circleSource.setGeoJson(FeatureCollection.fromFeatures(boundsFeatureList));
        }

        mapboxMap.clear();

      } catch (FileNotFoundException exception) {
        exception.printStackTrace();
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
// #-end-code-snippet: click-to-add-image-activity full-java