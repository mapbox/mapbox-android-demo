package com.mapbox.mapboxandroiddemo.examples.javaservices;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.matrix.v1.MapboxMatrix;
import com.mapbox.api.matrix.v1.models.MatrixResponse;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.turf.TurfConversion;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;


/**
 * Use the Mapbox Java SDK's Matrix API to retrieve travel times between many points.
 */
public class MatrixApiActivity extends AppCompatActivity implements MapboxMap.OnMapClickListener {

  private static final String ICON_ID = "ICON_ID";
  private static final String STATION_NAME_PROPERTY = "Station_Name";
  private static final String SOURCE_ID = "SOURCE_ID";
  private static final String LAYER_ID = "LAYER_ID";
  private List<Point> pointList;
  private List<SingleRecyclerViewMatrixLocation> matrixLocationList;
  private MapView mapView;
  private MapboxMap mapboxMap;
  private FeatureCollection featureCollection;
  private MatrixApiLocationRecyclerViewAdapter matrixApiLocationRecyclerViewAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_matrix_api);

    // Create a FeatureCollection via local GeoJSON file
    initFeatureCollection();

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        MatrixApiActivity.this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/cj8gg22et19ot2rnz65958fkn")
            // Add the SymbolLayer icon image to the map style
            .withImage(ICON_ID, BitmapFactory.decodeResource(
              MatrixApiActivity.this.getResources(), R.drawable.lightning_bolt))

            // Adding a GeoJson source for the SymbolLayer icons.
            .withSource(new GeoJsonSource(SOURCE_ID, featureCollection))

            // Adding the actual SymbolLayer to the map style.
            .withLayer(new SymbolLayer(LAYER_ID, SOURCE_ID)
              .withProperties(
                iconImage(ICON_ID),
                iconAllowOverlap(true),
                iconIgnorePlacement(true))
            ),
          new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

              // Set up list of locations to pass to the recyclerview
              initMatrixLocationListForRecyclerView();

              // Set up the recyclerview of charging station cards
              initRecyclerView();

              mapboxMap.addOnMapClickListener(MatrixApiActivity.this);

              Toast.makeText(MatrixApiActivity.this, R.string.click_on_marker_instruction_toast,
                Toast.LENGTH_SHORT).show();
            }
          });
      }
    });
  }

  @Override
  public boolean onMapClick(@NonNull LatLng point) {
    List<Feature> renderedStationFeatures = mapboxMap.queryRenderedFeatures(
      mapboxMap.getProjection().toScreenLocation(point), LAYER_ID);
    if (!renderedStationFeatures.isEmpty()) {
      Point pointOfSelectedStation = (Point) renderedStationFeatures.get(0).geometry();
      if (pointOfSelectedStation != null) {
        String selectedBoltFeatureName = renderedStationFeatures.get(0).getStringProperty(STATION_NAME_PROPERTY);
        List<Feature> featureList = featureCollection.features();
        for (int i = 0; i < featureList.size(); i++) {
          if (featureList.get(i).getStringProperty(STATION_NAME_PROPERTY).equals(selectedBoltFeatureName)) {
            makeMapboxMatrixApiCall(i);
          }
        }
      }
    }
    return true;
  }

  /**
   * Create a {@link FeatureCollection} from a locally stored asset file.
   */
  private void initFeatureCollection() {
    // Get GeoJSON features from GeoJSON file in the assets folder
    featureCollection = FeatureCollection.fromJson(loadGeoJsonFromAsset("boston_charge_stations.geojson"));

    // Initialize List<Position> for eventual use in the Matrix API call
    pointList = new ArrayList<>();

    // Get the position of each GeoJSON feature and build the list of Position
    // objects for eventual use in the Matrix API call
    if (featureCollection != null && featureCollection.features() != null) {
      for (Feature singleLocation : featureCollection.features()) {
        pointList.add((Point) singleLocation.geometry());
      }
    }
  }

  /**
   * Set up the RecyclerView, which will display the travel distances to each charge station.
   */
  private void initRecyclerView() {
    matrixApiLocationRecyclerViewAdapter = new MatrixApiLocationRecyclerViewAdapter(this,
      matrixLocationList);
    RecyclerView recyclerView = findViewById(R.id.matrix_api_recyclerview);
    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
      LinearLayoutManager.HORIZONTAL, true));
    recyclerView.setItemAnimator(new DefaultItemAnimator());
    recyclerView.setAdapter(matrixApiLocationRecyclerViewAdapter);
    SnapHelper snapHelper = new LinearSnapHelper();
    snapHelper.attachToRecyclerView(recyclerView);
  }

  /**
   * Make a call to the Mapbox Matrix API to get the travel distances to each charge station.
   *
   * @param markerPositionInList the position of the tapped bolt icon {@link Feature} in the FeatureCollection.
   */
  private void makeMapboxMatrixApiCall(final int markerPositionInList) {

    // Build Mapbox Matrix API parameters
    MapboxMatrix directionsMatrixClient = MapboxMatrix.builder()
      .accessToken(getString(R.string.access_token))
      .profile(DirectionsCriteria.PROFILE_DRIVING)
      .coordinates(pointList)
      .build();

    // Handle the API response
    directionsMatrixClient.enqueueCall(new Callback<MatrixResponse>() {
      @Override
      public void onResponse(Call<MatrixResponse> call,
                             Response<MatrixResponse> response) {
        if (response.body() != null) {
          List<Double[]> durationsToAllOfTheLocationsFromTheOrigin = response.body().durations();
          if (durationsToAllOfTheLocationsFromTheOrigin != null) {
            for (int x = 0; x < durationsToAllOfTheLocationsFromTheOrigin.size(); x++) {
              String finalConvertedFormattedDistance = String.valueOf(new DecimalFormat("#.##").format(
                TurfConversion.convertLength(
                  durationsToAllOfTheLocationsFromTheOrigin.get(markerPositionInList)[x],
                  "meters", "miles")));
              matrixLocationList.get(x).setDistanceFromOrigin(finalConvertedFormattedDistance);
              if (x != markerPositionInList) {
                matrixApiLocationRecyclerViewAdapter.notifyDataSetChanged();
              }
            }
          }
        }
      }

      @Override
      public void onFailure(Call<MatrixResponse> call, Throwable throwable) {
        Toast.makeText(MatrixApiActivity.this, R.string.call_error,
          Toast.LENGTH_SHORT).show();
        Timber.d("onResponse onFailure");
      }
    });
  }

  private void addMarkers() {
    Icon lightningBoltIcon = IconFactory.getInstance(MatrixApiActivity.this)
      .fromResource(R.drawable.lightning_bolt);
    if (featureCollection.features() != null) {
      for (Feature feature : featureCollection.features()) {
        mapboxMap.addMarker(new MarkerOptions()
          .position(new LatLng(feature.getProperty("Latitude").getAsDouble(),
            feature.getProperty("Longitude").getAsDouble()))
          .snippet(feature.getStringProperty("Station_Name"))
          .icon(lightningBoltIcon));
      }
    }
  }

  private String loadGeoJsonFromAsset(String filename) {
    try {
      // Load GeoJSON file from local asset folder
      InputStream is = getAssets().open(filename);
      int size = is.available();
      byte[] buffer = new byte[size];
      is.read(buffer);
      is.close();
      return new String(buffer,  Charset.forName("UTF-8"));
    } catch (Exception exception) {
      Timber.d(exception.toString(), "Exception Loading GeoJSON: ");
      exception.printStackTrace();
      return null;
    }
  }

  /**
   * Create a list of {@link SingleRecyclerViewMatrixLocation} objects to eventually use in the RecyclerView.
   */
  private void initMatrixLocationListForRecyclerView() {
    matrixLocationList = new ArrayList<>();
    if (featureCollection != null && featureCollection.features() != null) {
      for (Feature feature : featureCollection.features()) {
        SingleRecyclerViewMatrixLocation singleRecyclerViewLocation = new SingleRecyclerViewMatrixLocation();
        singleRecyclerViewLocation.setName(feature.getStringProperty(STATION_NAME_PROPERTY));
        matrixLocationList.add(singleRecyclerViewLocation);
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
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  /**
   * POJO model class for a single location in the recyclerview
   */
  class SingleRecyclerViewMatrixLocation {

    private String name;
    private LatLng locationLatLng;
    private String distanceFromOrigin;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getDistanceFromOrigin() {
      return distanceFromOrigin;
    }

    public void setDistanceFromOrigin(String distanceFromOrigin) {
      this.distanceFromOrigin = distanceFromOrigin;
    }
  }

  /**
   * The adapter for this example's RecyclerView.
   */
  static class MatrixApiLocationRecyclerViewAdapter extends
    RecyclerView.Adapter<MatrixApiLocationRecyclerViewAdapter.MyViewHolder> {

    private List<SingleRecyclerViewMatrixLocation> matrixLocationList;
    private Context context;

    public MatrixApiLocationRecyclerViewAdapter(Context context,
                                                List<SingleRecyclerViewMatrixLocation> matrixLocationList) {
      this.matrixLocationList = matrixLocationList;
      this.context = context;
    }

    @Override
    public MatrixApiLocationRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View itemView = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.rv_matrix_card, parent, false);
      return new MatrixApiLocationRecyclerViewAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MatrixApiLocationRecyclerViewAdapter.MyViewHolder holder, int position) {
      SingleRecyclerViewMatrixLocation singleRecyclerViewLocation = matrixLocationList.get(position);
      holder.name.setText(singleRecyclerViewLocation.getName());

      String finalDistance = singleRecyclerViewLocation.getDistanceFromOrigin()
        == null ? "" : String.format(context.getString(R.string.miles_distance),
        singleRecyclerViewLocation.getDistanceFromOrigin());
      holder.distance.setText(finalDistance);
    }

    @Override
    public int getItemCount() {
      return matrixLocationList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
      TextView name;
      TextView distance;
      CardView singleCard;

      MyViewHolder(View view) {
        super(view);
        name = view.findViewById(R.id.boston_matrix_api_location_title_tv);
        distance = view.findViewById(R.id.boston_matrix_api_location_distance_tv);
        singleCard = view.findViewById(R.id.single_location_cardview);
      }
    }
  }
}