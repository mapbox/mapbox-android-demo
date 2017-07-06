package com.mapbox.mapboxandroiddemo.labs;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewOnMapActivity extends AppCompatActivity {

  private MapView mapView;
  private MapboxMap map;
  private RecyclerView recyclerView;
  private LocationRecyclerViewAdapter locationAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_lab_recycler_view_on_map);

    recyclerView = (RecyclerView) findViewById(R.id.rv_on_top_of_map);
    locationAdapter = new LocationRecyclerViewAdapter(makeListOfLocations());
    recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true));
    recyclerView.setItemAnimator(new DefaultItemAnimator());
    recyclerView.setAdapter(locationAdapter);


    // Initialize the map view
    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;

      }
    });

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

  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        return true;
      default:
        finish();
    }
    return super.onOptionsItemSelected(item);
  }

  private ArrayList<SingleRecyclerViewLocation> makeListOfLocations() {

    ArrayList<SingleRecyclerViewLocation> locationList = new ArrayList<>();

    LatLng[] coordinates = new LatLng[] {new LatLng(-34.6054099, -58.363654800000006),
      new LatLng(-34.6041508, -58.38555650000001), new LatLng(-34.6114412, -58.37808899999999),
      new LatLng(-34.6097604, -58.382064000000014), new LatLng(-34.596636, -58.373077999999964),
      new LatLng(-34.590548, -58.38256609999996),
      new LatLng(-34.5982127, -58.38110440000003)};

    for (int x = 0; x < 5; x++) {
      SingleRecyclerViewLocation singleLocation = new SingleRecyclerViewLocation();
      singleLocation.setName(String.format(getString(R.string.rv_card_name), x));
      singleLocation.setBedInfo(String.format(getString(R.string.rv_card_bed_info), x));
      singleLocation.setLocationCoordinates(coordinates[x]);
      locationList.add(singleLocation);
    }
    return locationList;
  }


  class SingleRecyclerViewLocation {

    private String name;
    private String bedInfo;
    private LatLng locationCoordinates;

    public SingleRecyclerViewLocation() {
    }

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

  static class LocationRecyclerViewAdapter extends
    RecyclerView.Adapter<LocationRecyclerViewAdapter.MyViewHolder> {

    private List<SingleRecyclerViewLocation> locationList;

    public LocationRecyclerViewAdapter(List<SingleRecyclerViewLocation> locationList) {
      this.locationList = locationList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View itemView = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.rv_on_top_of_map_card, parent, false);
      return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
      SingleRecyclerViewLocation singleRecyclerViewLocation = locationList.get(position);
      holder.name.setText(singleRecyclerViewLocation.getName());
      holder.numOfBeds.setText(singleRecyclerViewLocation.getBedInfo());
    }

    @Override
    public int getItemCount() {
      return locationList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
      TextView name;
      TextView numOfBeds;
      CardView singleCard;


      MyViewHolder(View view) {
        super(view);
        name = (TextView) view.findViewById(R.id.location_title_tv);
        numOfBeds = (TextView) view.findViewById(R.id.location_num_of_beds_tv);
        singleCard = (CardView) view.findViewById(R.id.single_location_cardview);

        singleCard.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            Log.d("OnClick 1", "onClick 1: view = " + view);
            Log.d("OnClick 1", "onClick 1: view id = " + view.getId());
          }
        });
      }

      @Override
      public void onClick(View view) {

        Log.d("onClick 2", ": view.getId() = " + view.getId());
      }
    }
  }
}

