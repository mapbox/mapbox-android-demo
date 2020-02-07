package com.mapbox.mapboxandroiddemo.examples.javaservices;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.api.staticmap.v1.MapboxStaticMap;
import com.mapbox.api.tilequery.MapboxTilequery;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.MainActivity;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.app.PendingIntent.getActivity;
import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

/**
 * Use the Static Image API to create a Bitmap and then use the Bitmap in a notification.
 * Update the notification image and text as the map is tapped on. This example could also
 * be used with tracking the device location. For this, see
 * {@link com.mapbox.mapboxandroiddemo.examples.location.LocationChangeListeningActivity}
 */
public class StaticImageNotificationActivity extends AppCompatActivity implements
  OnMapReadyCallback, MapboxMap.OnMapClickListener, SeekBar.OnSeekBarChangeListener {

  private static final String MAP_CLICK_CIRCLE_LAYER_SOURCE_ID = "map-click-circle-layer-source-id";
  private static final String WIFI_LINE_SOURCE_ID = "wifi-line-source-id";
  private static final String MAP_CLICK_CIRCLE_LAYER_ID = "map-click-circle-layer-id";
  private static final String WIFI_LINE_LAYER_ID = "wifi-line-layer-id";
  private static final String NOTIFICATION_CHANNEL_ID = "channel_id";
  private static final String STYLE_ID = "mapbox://styles/appsatmapboxcom/ck65durfd1j861iltx8v4gcm0";
  private static final String LINE_LAYER_COLOR = "#F13B6E";
  private static final float LINE_LAYER_WIDTH = 2;
  private static final double STATIC_IMAGE_ZOOM = 16.5;
  private static final int IMAGE_WIDTH = 400;
  private static final int IMAGE_HEIGHT = 400;
  private static final int NOTIFICATION_ID = 1002;
  private static final Point INITIAL_POINT = Point.fromLngLat(-73.9879401159224, 40.729050870527914);
  private MapView mapView;
  private MapboxMap mapboxMap;
  private NotificationManager notificationManager;
  private Target picassoTarget;
  private NotificationCompat.Builder builder;
  private Point lastMapClickPoint = INITIAL_POINT;
  private TextView tilequerySearchRadiusValueTextView;
  private boolean firstNotificationBuilt = false;
  private int tilequerySearchRadiusFeet = 250;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_static_image_api_notification);

    initSearchRadiusSeekbar();
    tilequerySearchRadiusValueTextView = findViewById(R.id.tilequery_radius_value_textView);
    tilequerySearchRadiusValueTextView.setText(String.format(getString(
      R.string.static_image_api_notification_seekbar_feet), tilequerySearchRadiusFeet));

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);

    // Set a callback for when MapboxMap is ready to be used
    mapView.getMapAsync(this);
  }

  private void initSearchRadiusSeekbar() {
    final SeekBar radiusSeekbar = findViewById(R.id.tilequery_radius_seekbar);
    radiusSeekbar.setProgress(tilequerySearchRadiusFeet);
    radiusSeekbar.setMax(tilequerySearchRadiusFeet * 2);
    radiusSeekbar.setOnSeekBarChangeListener(this);
  }

  private void updateUiBasedOnSeekbarMovement(int progress) {
    tilequerySearchRadiusFeet = progress;
    tilequerySearchRadiusValueTextView.setText(String.format(getString(
      R.string.static_image_api_notification_seekbar_feet), progress));
  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    updateUiBasedOnSeekbarMovement(progress);
  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {

  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
    updateUiBasedOnSeekbarMovement(seekBar.getProgress());
    queryWifiSpotsWithTilequeryApi(new LatLng(lastMapClickPoint.latitude(), lastMapClickPoint.longitude()));
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
    StaticImageNotificationActivity.this.mapboxMap = mapboxMap;
    mapboxMap.setStyle(
      new Style.Builder().fromUri(STYLE_ID), new Style.OnStyleLoaded() {
        @Override
        public void onStyleLoaded(@NonNull Style style) {
          initMapClickCircleLayer();
          initWifiLineLayer();
          mapboxMap.addOnMapClickListener(StaticImageNotificationActivity.this);
          queryWifiSpotsWithTilequeryApi(new LatLng(INITIAL_POINT.latitude(), INITIAL_POINT.longitude()));
          Toast.makeText(StaticImageNotificationActivity.this,
            getString(R.string.click_on_map_instruction), Toast.LENGTH_SHORT).show();
          mapboxMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
              .zoom(16)
              .build()), 5000);
        }
      });
  }

  /**
   * Adjust various UI and make required API calls when the map is tapped on
   *
   * @param mapClickPoint where the map was tapped on
   * @return boolean. True if this click should be consumed and not passed further to other
   * listeners registered afterwards, false otherwise.
   */
  @Override
  public boolean onMapClick(@NonNull LatLng mapClickPoint) {
    // Update the map click point circle's location
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        lastMapClickPoint = Point.fromLngLat(mapClickPoint.getLongitude(), mapClickPoint.getLatitude());

        GeoJsonSource deviceLocationSource = style.getSourceAs(MAP_CLICK_CIRCLE_LAYER_SOURCE_ID);
        if (deviceLocationSource != null) {
          deviceLocationSource.setGeoJson(Point.fromLngLat(
            mapClickPoint.getLongitude(), mapClickPoint.getLatitude()));
        }
        queryWifiSpotsWithTilequeryApi(mapClickPoint);
      }
    });
    return true;
  }

  /**
   * Creates bitmap from given parameters, and creates a notification with that bitmap
   *
   * @param staticImageCenterLatLng coordinates for center of static image
   */
  private void getStaticImageFromApi(LatLng staticImageCenterLatLng, Integer wifiLocationsNearby,
                                     String closestWifiLocation) {
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        picassoTarget = new Target() {
          @Override
          public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            if (!firstNotificationBuilt) {
              createNotification(bitmap, wifiLocationsNearby, closestWifiLocation);
            } else {
              updateNotification(bitmap, wifiLocationsNearby, closestWifiLocation);
            }
          }

          @Override
          public void onBitmapFailed(Drawable errorDrawable) {
            Toast.makeText(StaticImageNotificationActivity.this,
              R.string.static_image_api_notification_bitmap_fail, Toast.LENGTH_SHORT).show();
          }

          @Override
          public void onPrepareLoad(Drawable placeHolderDrawable) {
          }
        };

        Picasso.with(StaticImageNotificationActivity.this).load(
          buildStaticImageUrl(staticImageCenterLatLng)).into(picassoTarget);
      }
    });
  }

  /**
   * Build a URL to request an image from the Mapbox Static Image API
   *
   * @param staticImageCenterLatLng the coordinates that the image should be centered on
   * @return a built and valid URL for the Mapbox Static Image API
   */
  private String buildStaticImageUrl(LatLng staticImageCenterLatLng) {
    return MapboxStaticMap.builder()
      .accessToken(getString(R.string.access_token))

      // Customer use String is needed because a non-Mapbox default style ID is being used
      .user("appsatmapboxcom")

      /*
        Custom style ID is needed because this examples uses a custom style
        rather than a default Mapbox style. If a default Mapbox style were used,
        a constant string from the StaticMapCriteria class, could be passed through
        instead
       */
      .styleId("ck65durfd1j861iltx8v4gcm0")

      .cameraPoint(Point.fromLngLat(staticImageCenterLatLng.getLongitude(), staticImageCenterLatLng.getLatitude()))
      .cameraZoom(STATIC_IMAGE_ZOOM)
      .width(IMAGE_WIDTH)
      .height(IMAGE_HEIGHT)
      .retina(true)
      .build()
      .url()
      .toString();
  }

  /**
   * Creates a notification with given bitmap as a large icon
   *
   * @param bitmap              to add to the notification
   * @param wifiLocationsNearby the number of wifi locations as determined by the Tilequery API
   */
  private void createNotification(Bitmap bitmap, Integer wifiLocationsNearby, String closestWifiLocationAddressText) {
    if (notificationManager == null) {
      notificationManager =
        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      if (notificationManager != null) {
        NotificationChannel notificationChannel = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID);
        if (notificationChannel == null) {
          notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
            "channel_name", NotificationManager.IMPORTANCE_HIGH);
          notificationChannel.setDescription("channel_description");
          notificationManager.createNotificationChannel(notificationChannel);
        }
      }
    }
    Intent intent = new Intent(this, MainActivity.class)
      .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
      .setContentTitle(String.format(getString(R.string.static_image_api_notification_content_title),
        wifiLocationsNearby, tilequerySearchRadiusFeet))
      .setSmallIcon(R.drawable.mapbox_logo_icon)
      .setContentText(String.format(getString(R.string.static_image_api_notification_description),
        closestWifiLocationAddressText))
      .setContentIntent(getActivity(this, 0, intent, 0))
      .setLargeIcon(bitmap)
      .setStyle(new NotificationCompat.BigPictureStyle()
        .bigPicture(bitmap)
        .bigLargeIcon(null));
    Notification notification = builder.build();
    notificationManager.notify(NOTIFICATION_ID, notification);
    firstNotificationBuilt = true;
  }

  /**
   * Update an existing notification with a new image and text
   *
   * @param newBitmapFromStaticImageApi to add to the notification
   * @param wifiLocationsNearby         the number of wifi locations as determined by the Tilequery API
   */
  private void updateNotification(Bitmap newBitmapFromStaticImageApi, Integer wifiLocationsNearby,
                                  String closestWifiLocationAddressText) {

    builder.setLargeIcon(newBitmapFromStaticImageApi);
    builder.setStyle(new NotificationCompat.BigPictureStyle()
      .bigPicture(newBitmapFromStaticImageApi)
      .bigLargeIcon(null));
    builder.setContentTitle(String.format(getString(R.string.static_image_api_notification_content_title),
      wifiLocationsNearby, tilequerySearchRadiusFeet));
    builder.setContentText(String.format(getString(R.string.static_image_api_notification_description),
      closestWifiLocationAddressText));
    notificationManager.notify(NOTIFICATION_ID, builder.build());
  }

  /**
   * Add the CircleLayer to add a circle that represents a certain location. In this example,
   * the circle is moved by tapping somewhere else on the map.
   * Do note that this setup is only for the purposes of this specific example. A real world alternative
   * is for the circle to be replaced by the {@link com.mapbox.mapboxsdk.location.LocationComponent}.
   * <p>
   * See {@link com.mapbox.mapboxandroiddemo.examples.location.LocationChangeListeningActivity}
   * to see how actual device location changes can be listened to.
   */
  private void initMapClickCircleLayer() {
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        style.addSource(new GeoJsonSource(MAP_CLICK_CIRCLE_LAYER_SOURCE_ID, INITIAL_POINT));
        CircleLayer mapClickPointLocationCircleLayer = new CircleLayer(MAP_CLICK_CIRCLE_LAYER_ID,
          MAP_CLICK_CIRCLE_LAYER_SOURCE_ID).withProperties(
          circleColor(Color.parseColor("#F13B6E")),
          circleRadius(6.6f)
        );
        style.addLayer(mapClickPointLocationCircleLayer);
      }
    });
  }

  /**
   * Add the LineLayer to draw small lines from the map click point to the wifi locations
   * found by the Tilequery API
   */
  private void initWifiLineLayer() {
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        style.addSource(new GeoJsonSource(WIFI_LINE_SOURCE_ID));
        LineLayer wifiLineLayer = new LineLayer(WIFI_LINE_LAYER_ID,
          WIFI_LINE_SOURCE_ID).withProperties(
          lineColor(Color.parseColor(LINE_LAYER_COLOR)),
          lineWidth(LINE_LAYER_WIDTH)
        );
        if (style.getLayer("wifi-hotspot-locations") != null) {
          style.addLayerBelow(wifiLineLayer, "wifi-hotspot-locations");
        } else {
          style.addLayer(wifiLineLayer);
        }
      }
    });
  }

  /**
   * Use the Java SDK's MapboxTilequery class to build a API request and use the API response
   *
   * @param clickPoint the center point that the the tilequery will originate from.
   */
  private void queryWifiSpotsWithTilequeryApi(@NonNull LatLng clickPoint) {
    MapboxTilequery.builder()
      .accessToken(getString(R.string.access_token))
      .tilesetIds("appsatmapboxcom.bml2ioc4")
      .query(Point.fromLngLat(clickPoint.getLongitude(), clickPoint.getLatitude()))
      .radius(tilequerySearchRadiusFeet / 3)
      .geometry("point")
      .layers("NYC_Wi-Fi_Hotspot_Locations-8qwm7n")
      .build()
      .enqueueCall(new Callback<FeatureCollection>() {
        @Override
        public void onResponse(Call<FeatureCollection> call, Response<FeatureCollection> response) {
          if (response.body() != null) {
            FeatureCollection responseFeatureCollection = response.body();
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
              @Override
              public void onStyleLoaded(@NonNull Style style) {
                if (responseFeatureCollection.features() != null) {
                  List<Feature> responseFeatureList = responseFeatureCollection.features();
                  GeoJsonSource wifiLineSource = style.getSourceAs(WIFI_LINE_SOURCE_ID);

                  LineLayer nearbyLineLayer = style.getLayerAs(WIFI_LINE_LAYER_ID);

                  if (responseFeatureList.isEmpty()) {
                    Toast.makeText(StaticImageNotificationActivity.this,
                      getString(R.string.static_image_api_notification_no_wifi_nearby),
                      Toast.LENGTH_SHORT).show();

                    if (nearbyLineLayer != null) {
                      if (VISIBLE.equals(nearbyLineLayer.getVisibility().getValue())) {
                        nearbyLineLayer.setProperties(visibility(NONE));
                      }
                    }

                  } else {
                    if (nearbyLineLayer != null) {
                      if (NONE.equals(nearbyLineLayer.getVisibility().getValue())) {
                        nearbyLineLayer.setProperties(visibility(VISIBLE));
                      }
                      if (wifiLineSource != null) {
                        drawLinesFromClickPointToNearbyWifi(responseFeatureList, wifiLineSource);
                      }
                    }

                    getStaticImageFromApi(new LatLng(clickPoint.getLatitude(), clickPoint.getLongitude()),
                      responseFeatureList.size(), responseFeatureList.get(0)
                        .getStringProperty("location").toLowerCase());

                  }
                }
              }
            });
          }
        }

        @Override
        public void onFailure(Call<FeatureCollection> call, Throwable throwable) {
          Timber.d("Request failed: %s", throwable.getMessage());
          Toast.makeText(StaticImageNotificationActivity.this, R.string.api_error,
            Toast.LENGTH_SHORT).show();
        }
      });
  }

  /**
   * Draws a line from the click point to each of the wifi locations that were identified by the
   * Tilequery API query
   *
   * @param featureList    the feature list returned by the Tilequery API repsonse object
   * @param wifiLineSource the source used by the LineLayer
   */
  private void drawLinesFromClickPointToNearbyWifi(List<Feature> featureList, GeoJsonSource wifiLineSource) {

    List<Feature> lineFeatureList = new ArrayList<>();

    for (Feature singleFeature : featureList) {
      List<Point> singleLineStringPointList = new ArrayList<>();

      // Cast the Tilequery response Feature to a Point
      Point singleFeaturePoint = (Point) singleFeature.geometry();

      // Add the Point to the Point list
      singleLineStringPointList.add(singleFeaturePoint);

      // Add the last map click Point to the Point list
      singleLineStringPointList.add(lastMapClickPoint);

      // Create a Feature from the LineString and add the Feature to the
      // Feature list.
      LineString singleLineString = LineString.fromLngLats(singleLineStringPointList);

      lineFeatureList.add(Feature.fromGeometry(singleLineString));
    }

    // Use the Feature list to create a FeatureCollection and update
    // the LineLayer source with the FeatureCollection
    if (wifiLineSource != null) {
      wifiLineSource.setGeoJson(FeatureCollection.fromFeatures(lineFeatureList));
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
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  protected void onDestroy() {
    Picasso.with(this).cancelRequest(picassoTarget);
    if (mapboxMap != null) {
      mapboxMap.removeOnMapClickListener(this);
    }
    mapView.onDestroy();
    super.onDestroy();
  }
}
