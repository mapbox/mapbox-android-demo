package com.mapbox.mapboxandroiddemo.examples.mas;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxandroiddemo.labs.
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.api.directions.v5.DirectionsCriteria;
import com.mapbox.services.api.directionsmatrix.v1.MapboxDirectionsMatrix;
import com.mapbox.services.api.directionsmatrix.v1.models.DirectionsMatrixResponse;
import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.geojson.FeatureCollection;
import com.mapbox.services.commons.models.Position;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DirectionsMatrixApiActivity extends AppCompatActivity {

  private static final LatLngBounds BOSTON_BOUNDS = new LatLngBounds.Builder()
    .include(new LatLng(42.363581, -71.097695))
    .include(new LatLng(42.350399, -71.040765))
    .build();

  private MapView mapView;
  private MapboxMap mapboxMap;
  private List<Position> positionList;
  private FeatureCollection featureCollection;
  private String TAG = "DirectionsMatrixApiActivity";
  public static final String MARKER_SOURCE = "marker-source";
  public static final String MARKER_LAYER = "marker-layer";
  public static final String LIGHTING_BOLT_IMAGE = "bolt-image";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_matrix_api);

    setUpPositionList();

    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(final MapboxMap mapboxMap) {
        DirectionsMatrixApiActivity.this.mapboxMap = mapboxMap;
        mapboxMap.setLatLngBoundsForCameraTarget(BOSTON_BOUNDS);
        addMarkers();

        mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
          @Override
          public boolean onMarkerClick(@NonNull Marker marker) {

            Log.d(TAG, "onMarkerClick: Clicked on marker");

            makeMatrixApiCall(Position.fromCoordinates(
              marker.getPosition().getLongitude(),
              marker.getPosition().getLatitude()));

            for (Marker singleMarker : mapboxMap.getMarkers()) {
              if (singleMarker != marker) {
                singleMarker.showInfoWindow(mapboxMap, mapView);
              }
            }

            return false;
          }
        });

      }
    });
  }

  private void makeMatrixApiCall(Position positionOfClickedMarker) {
    MapboxDirectionsMatrix directionsMatrixClient = new MapboxDirectionsMatrix.Builder()
      .setAccessToken(getString(R.string.access_token))
      .setProfile(DirectionsCriteria.PROFILE_DRIVING)
      .setOrigin(positionOfClickedMarker)
      .setCoordinates(positionList)
      .build();

    // Handle the API response
    directionsMatrixClient.enqueueCall(new Callback<DirectionsMatrixResponse>() {
      @Override
      public void onResponse(Call<DirectionsMatrixResponse> call, Response<DirectionsMatrixResponse> response) {

        double[][] array = response.body().getDurations();

        Log.d(TAG, "onResponse: String.valueOf(array[0][0]) = " + String.valueOf(array[0][0]));
        Log.d(TAG, "onResponse: String.valueOf(array[0][1]) = " + String.valueOf(array[0][1]));
        Log.d(TAG, "onResponse: String.valueOf(array[0][2]) = " + String.valueOf(array[0][2]));
        Log.d(TAG, "onResponse: String.valueOf(array[0][3]) = " + String.valueOf(array[0][3]));
        Log.d(TAG, "onResponse: String.valueOf(array[0][4]) = " + String.valueOf(array[0][4]));
        Log.d(TAG, "onResponse: String.valueOf(array[0][5]) = " + String.valueOf(array[0][5]));
        Log.d(TAG, "onResponse: String.valueOf(array[0][6]) = " + String.valueOf(array[0][6]));
        Log.d(TAG, "onResponse: String.valueOf(array[0][7]) = " + String.valueOf(array[0][7]));
        Log.d(TAG, "onResponse: String.valueOf(array[0][8]) = " + String.valueOf(array[0][8]));

      }

      @Override
      public void onFailure(Call<DirectionsMatrixResponse> call, Throwable throwable) {
        Toast.makeText(DirectionsMatrixApiActivity.this, R.string.call_error, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onResponse onFailure");

      }
    });
  }

  private void addMarkers() {
    Icon icon = IconFactory.getInstance(DirectionsMatrixApiActivity.this).fromResource(R.drawable.lightning_bolt);
    for (Feature feature : featureCollection.getFeatures()) {
      mapboxMap.addMarker(new MarkerOptions()
        .position(new LatLng(feature.getProperty("Latitude").getAsDouble(),
          feature.getProperty("Longitude").getAsDouble()))
        .snippet(feature.getStringProperty("Station_Name"))
        .icon(icon));
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
      Log.e(TAG, "Exception Loading GeoJSON: " + exception.toString());
      exception.printStackTrace();
      return null;
    }
  }

  private void setUpPositionList() {

    // Get GeoJSON features from GeoJSON file in the assets folder
    featureCollection = FeatureCollection.fromJson(loadGeoJsonFromAsset("boston_charge_stations.geojson"));

    // Initialize List<Position> for eventual use in the Matrix API call
    positionList = new ArrayList<>();

    // Get the position of each GeoJSON feature and build the list of Position
    // objects for eventual use in the Matrix API call
    for (int x = 0; x < featureCollection.getFeatures().size(); x++) {
      Feature singleLocation = featureCollection.getFeatures().get(x);
      Position singleLocationPosition = (Position) singleLocation.getGeometry().getCoordinates();
      positionList.add(singleLocationPosition);
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
  class SingleRecyclerViewLocation {

    private String name;
    private String bedInfo;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getBedInfo() {
      return bedInfo;
    }

    public void setBedInfo(String bedInfo) {
      this.bedInfo = bedInfo;
    }

    public LatLng getLocationCoordinates() {
      return locationCoordinates;
    }

    public void setLocationCoordinates(LatLng locationCoordinates) {
      this.locationCoordinates = locationCoordinates;
    }
  }

  static class MatrixApiLocationRecyclerViewAdapter extends
    RecyclerView.Adapter<MatrixApiLocationRecyclerViewAdapter.MyViewHolder> {

    private List<SingleRecyclerViewLocation> locationList;
    private MapboxMap map;

    public MatrixApiLocationRecyclerViewAdapter(List<SingleRecyclerViewLocation> locationList, MapboxMap mapBoxMap) {
      this.locationList = locationList;
      this.map = mapBoxMap;
    }

    @Override
    public MatrixApiLocationRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View itemView = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.rv_on_top_of_map_card, parent, false);
      return new MatrixApiLocationRecyclerViewAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MatrixApiLocationRecyclerViewAdapter.MyViewHolder holder, int position) {
      SingleRecyclerViewLocation singleRecyclerViewLocation = locationList.get(position);
      holder.name.setText(singleRecyclerViewLocation.getName());
      holder.numOfBeds.setText(singleRecyclerViewLocation.getBedInfo());
      holder.setClickListener(new RecyclerViewOnMapActivity.ItemClickListener() {
        @Override
        public void onClick(View view, int position) {
          LatLng selectedLocationLatLng = locationList.get(position).getLocationCoordinates();
          CameraPosition newCameraPosition = new CameraPosition.Builder()
            .target(selectedLocationLatLng)
            .build();

          map.addMarker(new MarkerOptions()
            .setPosition(selectedLocationLatLng)
            .setTitle(locationList.get(position).getName()))
            .setSnippet(locationList.get(position).getBedInfo());

          map.easeCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition));
        }
      });
    }

    @Override
    public int getItemCount() {
      return locationList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
      TextView name;
      TextView numOfBeds;
      CardView singleCard;
      ItemClickListener clickListener;

      MyViewHolder(View view) {
        super(view);
        name = (TextView) view.findViewById(R.id.location_title_tv);
        numOfBeds = (TextView) view.findViewById(R.id.location_num_of_beds_tv);
        singleCard = (CardView) view.findViewById(R.id.single_location_cardview);
        singleCard.setOnClickListener(this);
      }

      public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
      }

      @Override
      public void onClick(View view) {
        clickListener.onClick(view, getLayoutPosition());
      }
    }
  }



}
