package com.mapbox.mapboxandroidweardemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WearableRecyclerView;

import com.mapbox.mapboxandroidweardemo.adapter.ExampleAdapter;
import com.mapbox.mapboxandroidweardemo.examples.SimpleMapViewActivity;
import com.mapbox.mapboxandroidweardemo.model.ExampleItemModel;

import java.util.ArrayList;

public class MainActivity extends WearableActivity implements ExampleAdapter.ItemSelectedListener {

  private static final String TAG = "MainActivity";
  private WearableRecyclerView wearableRecyclerView;
  private ArrayList<ExampleItemModel> exampleItemModels;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    wearableRecyclerView = (WearableRecyclerView) findViewById(R.id.recycler_launcher_view);
    wearableRecyclerView.setHasFixedSize(true);
    wearableRecyclerView.setLayoutManager(new LinearLayoutManager(this));

//    MyOffsettingHelper myOffsettingHelper = new MyOffsettingHelper();
//
//    wearableRecyclerView.setOffsettingHelper(myOffsettingHelper);

    exampleItemModels = new ArrayList<>();
    exampleItemModels.add(new ExampleItemModel(R.string.activity_simple_mapview_title, R.drawable.simple_map_view_screen, new Intent(MainActivity.this, SimpleMapViewActivity.class)));
    //exampleItemModels.add("Show user location");

    ExampleAdapter exampleAdapter = new ExampleAdapter(MainActivity.this, exampleItemModels);
    wearableRecyclerView.setAdapter(exampleAdapter);

    exampleAdapter.setListener(this);

  }

  @Override
  public void onItemSelected(int position) {
    startActivity(exampleItemModels.get(position).getActivity());
  }
}
