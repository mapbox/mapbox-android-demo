package com.mapbox.mapboxandroiddemo.examples.javaservices;
// #-code-snippet: matrix-api-activity full-java
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
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
import com.mapbox.turf.TurfConversion;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Use the Mapbox Java Services SDK's Matrix API to retrieve travel times between many points.
 */
public class MatrixApiActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private List<Point> pointList;
  private FeatureCollection featureCollection;
  private RecyclerView recyclerView;
  private MatrixApiLocationRecyclerViewAdapter matrixApiLocationRecyclerViewAdapter;
  private ArrayList<SingleRecyclerViewMatrixLocation> matrixLocationList;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_matrix_api);

    recyclerView = findViewById(R.id.matrix_api_recyclerview);

    // Create list of positions from local GeoJSON file
    initPositionListFromGeoJsonFile();

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(final MapboxMap mapboxMap) {
        MatrixApiActivity.this.mapboxMap = mapboxMap;

        // Add markers to the map
        addMarkers();

        // Set up list of locations to pass to the recyclerview
        initMatrixLocationListForRecyclerView();

        // Set up the recyclerview of charging station cards
        initRecyclerView();

        mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
          @Override
          public boolean onMarkerClick(@NonNull Marker marker) {

            // Make a call to the Mapbox Matrix API
            makeMapboxMatrixApiCall(getClickedMarkerNumInPositionList(marker), Point.fromLngLat(
              marker.getPosition().getLongitude(), marker.getPosition().getLatitude()));
            return false;
          }
        });
        Toast.makeText(MatrixApiActivity.this, R.string.click_on_marker_instruction_toast,
          Toast.LENGTH_SHORT).show();
      }
    });
  }

  private int getClickedMarkerNumInPositionList(Marker clickedMarker) {
    int clickedMarkerIndexPositionInList = -1;
    if (clickedMarker != null) {
      for (Marker singleMarker : mapboxMap.getMarkers()) {
        if (singleMarker == clickedMarker) {
          clickedMarkerIndexPositionInList = mapboxMap.getMarkers().indexOf(singleMarker);
        }
      }
      return clickedMarkerIndexPositionInList;
    } else {
      return 0;
    }
  }

  private void initRecyclerView() {
    matrixApiLocationRecyclerViewAdapter = new MatrixApiLocationRecyclerViewAdapter(this,
      matrixLocationList);
    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
      LinearLayoutManager.HORIZONTAL, true));
    recyclerView.setItemAnimator(new DefaultItemAnimator());
    recyclerView.setAdapter(matrixApiLocationRecyclerViewAdapter);
    SnapHelper snapHelper = new LinearSnapHelper();
    snapHelper.attachToRecyclerView(recyclerView);
  }

  private void makeMapboxMatrixApiCall(final int markerPositionInList, Point pointOfClickedMarker) {

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
        List<Double[]> durationsToAllOfTheLocationsFromTheOrigin = response.body().durations();
        for (int x = 0; x < durationsToAllOfTheLocationsFromTheOrigin.size(); x++) {
          String finalConvertedFormattedDistance = String.valueOf(new DecimalFormat("#.##")
            .format(TurfConversion.convertLength(
              durationsToAllOfTheLocationsFromTheOrigin.get(markerPositionInList)[x],
              "meters", "miles")));
          if (x == markerPositionInList) {
            matrixLocationList.get(x).setDistanceFromOrigin(finalConvertedFormattedDistance);
          }
          if (x != markerPositionInList) {
            matrixLocationList.get(x).setDistanceFromOrigin(finalConvertedFormattedDistance);
            matrixApiLocationRecyclerViewAdapter.notifyDataSetChanged();
          }
        }
      }

      @Override
      public void onFailure(Call<MatrixResponse> call, Throwable throwable) {
        Toast.makeText(MatrixApiActivity.this, R.string.call_error,
          Toast.LENGTH_SHORT).show();
        Log.d("MatrixApiActivity", "onResponse onFailure");
      }
    });
  }

  private void addMarkers() {
    Icon lightningBoltIcon = IconFactory.getInstance(MatrixApiActivity.this)
      .fromResource(R.drawable.lightning_bolt);
    for (Feature feature : featureCollection.features()) {
      mapboxMap.addMarker(new MarkerOptions()
        .position(new LatLng(feature.getProperty("Latitude").getAsDouble(),
          feature.getProperty("Longitude").getAsDouble()))
        .snippet(feature.getStringProperty("Station_Name"))
        .icon(lightningBoltIcon));
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
      return new String(buffer, "UTF-8");
    } catch (Exception exception) {
      Log.d("MatrixApiActivity", "Exception Loading GeoJSON: " + exception.toString());
      exception.printStackTrace();
      return null;
    }
  }

  private void initPositionListFromGeoJsonFile() {

    // Get GeoJSON features from GeoJSON file in the assets folder
    featureCollection = FeatureCollection.fromJson(loadGeoJsonFromAsset("boston_charge_stations.geojson"));

    // Initialize List<Position> for eventual use in the Matrix API call
    pointList = new ArrayList<>();

    // Get the position of each GeoJSON feature and build the list of Position
    // objects for eventual use in the Matrix API call
    for (Feature singleLocation : featureCollection.features()) {
      pointList.add((Point) singleLocation.geometry());
    }
  }

  private void initMatrixLocationListForRecyclerView() {
    matrixLocationList = new ArrayList<>();
    for (Feature feature : featureCollection.features()) {
      SingleRecyclerViewMatrixLocation singleRecyclerViewLocation = new SingleRecyclerViewMatrixLocation();
      singleRecyclerViewLocation.setName(feature.getStringProperty("Station_Name"));
      singleRecyclerViewLocation.setLocationLatLng(new LatLng(((Point)
        feature.geometry()).latitude(),
        ((Point) feature.geometry()).longitude()));
      matrixLocationList.add(singleRecyclerViewLocation);
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

    public void setLocationLatLng(LatLng locationLatLng) {
      this.locationLatLng = locationLatLng;
    }
  }

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
// #-end-code-snippet: matrix-api-activity full-java