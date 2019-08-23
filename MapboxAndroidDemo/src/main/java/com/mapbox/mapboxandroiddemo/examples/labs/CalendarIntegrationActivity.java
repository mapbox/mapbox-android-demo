package com.mapbox.mapboxandroiddemo.examples.labs;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.widget.Toast;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;

/**
 * Use the Android system's calendar content provider and the Mapbox Geocoding API to show
 * past calendar events on the map.
 */
public class CalendarIntegrationActivity extends AppCompatActivity implements
    OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener {

  private static final int MY_CAL_REQ = 0;
  private static final int TITLE_INDEX = 1;
  private static final int EVENT_LOCATION_INDEX = 2;
  private static final String MARKER_IMAGE_ID = "MARKER_IMAGE_ID";
  private static final String MARKER_LAYER_ID = "MARKER_LAYER_ID";
  private static final String PROPERTY_TITLE = "title";
  private static final String PROPERTY_LOCATION = "location";
  private String geojsonSourceId = "geojsonSourceId";
  private MapView mapView;
  private MapboxMap mapboxMap;
  private List<Feature> featureList;
  private FeatureCollection featureCollection;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_calendar_content_provider);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull final MapboxMap mapboxMap) {
    CalendarIntegrationActivity.this.mapboxMap = mapboxMap;
    mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        featureCollection = FeatureCollection.fromFeatures(new Feature[]{});
        getCalendarData(style);
        mapboxMap.addOnMapClickListener(CalendarIntegrationActivity.this);
      }
    });
  }

  @Override
  public boolean onMapClick(@NonNull LatLng point) {
    return handleClickIcon(mapboxMap.getProjection().toScreenLocation(point));
  }

  /**
   * This method handles click events for SymbolLayer symbols.
   *
   * @param screenPoint the point on screen clicked
   */
  private boolean handleClickIcon(PointF screenPoint) {
    List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, MARKER_LAYER_ID);
    if (!features.isEmpty()) {
      String calendarEventTitle = features.get(0).getStringProperty(PROPERTY_TITLE);
      String calendarEventLocation = features.get(0).getStringProperty(PROPERTY_LOCATION);
      Toast.makeText(this, calendarEventTitle + " – " + calendarEventLocation, Toast.LENGTH_SHORT).show();
      return true;
    }
    return false;
  }

  /**
   * Using a Calendar content provider (https://developer.android.com/guide/topics/providers/calendar-provider),
   * retrieve the title and location of calendar events that are from the main account signed in on the device.
   */
  public void getCalendarData(@NonNull Style style) {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
        != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]
        { Manifest.permission.READ_CALENDAR}, MY_CAL_REQ);
    } else {
      Uri calendarUri;
      calendarUri = CalendarContract.Events.CONTENT_URI;
      Calendar startTime = Calendar.getInstance();
      startTime.set(2018, 2, 1, 0, 0);

      Calendar endTime = Calendar.getInstance();
      endTime.set(2018, 6, 1, 0, 0);

      String selection = "(( " + CalendarContract.Events.DTSTART + " >= "
          + startTime.getTimeInMillis() + " )" + " AND ( " + CalendarContract.Events.DTSTART
          + " <= " + endTime.getTimeInMillis() + " ))";

      String[] projection = new String[] {
        CalendarContract.Events.CALENDAR_ID, CalendarContract.Events.TITLE,
        CalendarContract.Events.EVENT_LOCATION,
        CalendarContract.Events.DTSTART };

      Cursor cur = this.getContentResolver().query(calendarUri, projection, selection, null, null);

      featureList = new ArrayList<>();
      int index = 0;
      if (cur != null) {
        if (!deviceHasInternetConnection()) {
          Toast.makeText(this, R.string.no_connectivity, Toast.LENGTH_LONG).show();
          Timber.d("No internet connectivity");
        } else {
          while (cur.moveToNext()) {
            if (index <= 80) {
              if (cur.getString(EVENT_LOCATION_INDEX) != null && !cur.getString(EVENT_LOCATION_INDEX).isEmpty()) {
                makeMapboxGeocodingRequest(cur.getString(TITLE_INDEX), cur.getString(EVENT_LOCATION_INDEX));
              } else {
                Timber.d("getCalendarData: location is null or empty");
              }
              index++;
            }
          }
          setUpData(style);
        }
      }
    }
  }

  /**
   * Use the Mapbox Java SDK's wrapper for making a Mapbox Geocoding API request. The text in the calendar event's
   * location field is used to make a geocoding request.
   *
   * @param eventTitle    title of the calendar event so that it can be added as a Feature property. The title is
   *                      eventually displayed in a Toast when a calendar event icon is tapped on.
   * @param eventLocation the event's location. This text is used in the Mapbox geocoding search
   */
  private void makeMapboxGeocodingRequest(final String eventTitle, final String eventLocation) {
    try {
      // Build a Mapbox geocoding request
      MapboxGeocoding client = MapboxGeocoding.builder()
          .accessToken(getString(R.string.access_token))
          .query(eventLocation)
          .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
          .mode(GeocodingCriteria.MODE_PLACES)
          .build();
      client.enqueueCall(new Callback<GeocodingResponse>() {
        @Override
        public void onResponse(Call<GeocodingResponse> call,
                               Response<GeocodingResponse> response) {
          List<CarmenFeature> results = response.body().features();
          if (results.size() > 0) {
            // Get the first Feature from the successful geocoding response
            CarmenFeature feature = results.get(0);
            if (feature != null) {
              mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                  LatLng featureLatLng = new LatLng(feature.center().latitude(), feature.center().longitude());
                  Feature singleFeature = Feature.fromGeometry(Point.fromLngLat(featureLatLng.getLongitude(),
                      featureLatLng.getLatitude()));
                  singleFeature.addStringProperty(PROPERTY_TITLE, eventTitle);
                  singleFeature.addStringProperty(PROPERTY_LOCATION, eventLocation);
                  featureList.add(singleFeature);
                  featureCollection = FeatureCollection.fromFeatures(featureList);
                  GeoJsonSource source = style.getSourceAs(geojsonSourceId);
                  if (source != null) {
                    source.setGeoJson(featureCollection);
                  } else {
                    Timber.d("onResponse: listOfCalendarEvents == null");
                  }
                }
              });
            }
          }
        }

        @Override
        public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
          Timber.d("Geocoding Failure: %s", throwable.getMessage());
        }
      });

    } catch (ServicesException servicesException) {
      Timber.d("Error geocoding: %s", servicesException.toString());
      servicesException.printStackTrace();
    }
  }

  /**
   * Sets up all of the sources and layers needed for this example
   *
   * @param style style
   */
  public void setUpData(@NonNull Style style) {
    setupSource(style);
    setUpImage(style);
    setUpCalendarIconLayer(style);
    Toast.makeText(CalendarIntegrationActivity.this, R.string.click_on_calendar_icon_instruction,
        Toast.LENGTH_SHORT).show();
  }

  /**
   * Adds the GeoJSON source to the map
   */
  private void setupSource(@NonNull Style style) {
    style.addSource(new GeoJsonSource(geojsonSourceId, featureCollection));
  }

  /**
   * Adds the marker image to the map for use as a SymbolLayer icon
   */
  private void setUpImage(@NonNull Style style) {
    Bitmap icon = BitmapFactory.decodeResource(
        this.getResources(), R.drawable.calendar_event_icon);
    style.addImage(MARKER_IMAGE_ID, icon);
  }

  /**
   * Setup a layer with a calendar icon representing the location of each calendar event.
   */
  private void setUpCalendarIconLayer(@NonNull Style style) {
    SymbolLayer eventSymbolLayer = new SymbolLayer(MARKER_LAYER_ID, geojsonSourceId);
    eventSymbolLayer.withProperties(
        iconImage(MARKER_IMAGE_ID),
        iconSize(1.8f),
        iconAllowOverlap(true),
        iconIgnorePlacement(true)
    );
    style.addLayer(eventSymbolLayer);
  }

  @Override
  public void onExplanationNeeded(List<String> permissionsToExplain) {
    Toast.makeText(this, R.string.user_calendar_permission_explanation, Toast.LENGTH_LONG).show();
  }

  @Override
  public void onPermissionResult(boolean granted) {
    // Left empty on purpose
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                         int[] grantResults) {
    if (requestCode == MY_CAL_REQ) {
      if (grantResults.length > 0
          && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        if (mapboxMap != null) {
          mapboxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
              getCalendarData(style);
            }
          });
        }
      } else {
        Toast.makeText(this, R.string.user_calendar_permission_explanation, Toast.LENGTH_LONG).show();
      }
    }
  }

  public boolean deviceHasInternetConnection() {
    ConnectivityManager connectivityManager = (ConnectivityManager)
        getApplicationContext().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
    return activeNetwork != null && activeNetwork.isConnected();
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
