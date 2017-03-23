package com.mapbox.mapboxandroiddemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WearableRecyclerView;

import com.mapbox.mapboxandroiddemo.adapter.ExampleAdapter;
import com.mapbox.mapboxandroiddemo.examples.LocationTrackingActivity;
import com.mapbox.mapboxandroiddemo.examples.MapFragmentActivity;
import com.mapbox.mapboxandroiddemo.examples.OfflineMapActivity;
import com.mapbox.mapboxandroiddemo.examples.SimpleMapViewActivity;
import com.mapbox.mapboxandroiddemo.model.ExampleItemModel;
import com.mapbox.mapboxandroiddemo.utils.OffsettingHelper;

import java.util.ArrayList;

public class MainActivity extends WearableActivity implements ExampleAdapter.ItemSelectedListener {

  private ArrayList<ExampleItemModel> exampleItemModels;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    WearableRecyclerView wearableRecyclerView = (WearableRecyclerView) findViewById(R.id.recycler_launcher_view);
    wearableRecyclerView.setHasFixedSize(true);

    OffsettingHelper offsettingHelper = new OffsettingHelper();

    wearableRecyclerView.setCenterEdgeItems(true);
    wearableRecyclerView.setOffsettingHelper(offsettingHelper);

    exampleItemModels = new ArrayList<>();
    exampleItemModels.add(new ExampleItemModel(R.string.activity_simple_mapview_title,
      R.drawable.simple_map_view_screen, new Intent(MainActivity.this, SimpleMapViewActivity.class)));
    exampleItemModels.add(new ExampleItemModel(R.string.activity_map_fragment_title,
      R.drawable.simple_map_view_screen, new Intent(MainActivity.this, MapFragmentActivity.class)));
    exampleItemModels.add(new ExampleItemModel(R.string.activity_map_offline_title,
      R.drawable.simple_map_view_screen, new Intent(MainActivity.this, OfflineMapActivity.class)));
    exampleItemModels.add(new ExampleItemModel(R.string.activity_location_tracking_title,
      R.drawable.simple_map_view_screen, new Intent(MainActivity.this, LocationTrackingActivity.class)));

    ExampleAdapter exampleAdapter = new ExampleAdapter(MainActivity.this, exampleItemModels);
    wearableRecyclerView.setAdapter(exampleAdapter);

    exampleAdapter.setListener(this);
  }

  @Override
  public void onItemSelected(int position) {
    startActivity(exampleItemModels.get(position).getActivity());
  }
}
