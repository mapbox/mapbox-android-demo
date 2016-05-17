package com.mapbox.mapboxandroiddemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.mapbox.mapboxandroiddemo.adapter.ExampleAdapter;
import com.mapbox.mapboxandroiddemo.examples.AnimateMapCameraActivity;
import com.mapbox.mapboxandroiddemo.examples.BoundingBoxCameraActivity;
import com.mapbox.mapboxandroiddemo.examples.CustomRasterStyleActivity;
import com.mapbox.mapboxandroiddemo.examples.DirectionsActivity;
import com.mapbox.mapboxandroiddemo.examples.DrawCustomMarkerActivity;
import com.mapbox.mapboxandroiddemo.examples.DrawGeojsonLineActivity;
import com.mapbox.mapboxandroiddemo.examples.DrawMarkerActivity;
import com.mapbox.mapboxandroiddemo.examples.DrawPolygonActivity;
import com.mapbox.mapboxandroiddemo.examples.GeocodingActivity;
import com.mapbox.mapboxandroiddemo.examples.MapboxStudioStyleActivity;
import com.mapbox.mapboxandroiddemo.examples.OfflineManagerActivity;
import com.mapbox.mapboxandroiddemo.examples.SatelliteStyleActivity;
import com.mapbox.mapboxandroiddemo.examples.SimpleMapViewActivity;
import com.mapbox.mapboxandroiddemo.examples.SimpleOfflineMapActivity;
import com.mapbox.mapboxandroiddemo.examples.StaticImageActivity;
import com.mapbox.mapboxandroiddemo.examples.SupportMapFragmentActivity;
import com.mapbox.mapboxandroiddemo.model.ExampleItemModel;
import com.mapbox.mapboxandroiddemo.utils.ItemClickSupport;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ArrayList<ExampleItemModel> exampleItemModel;
    private ExampleAdapter adapter;
    private int currentCategory = R.id.nav_basics;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        exampleItemModel = new ArrayList<>();


        // Create the adapter to convert the array to views
        adapter = new ExampleAdapter(this, exampleItemModel);
        // Attach the adapter to a ListView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.details_list);
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
        }
        if (savedInstanceState == null) {
            list(R.id.nav_basics);
        } else {
            currentCategory = savedInstanceState.getInt("CURRENT_CATEGORY");
            list(currentCategory);
        }

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {

                startActivity(exampleItemModel.get(position).getActivity());

            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (drawer != null) {
            drawer.addDrawerListener(toggle);
        }
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
            navigationView.setCheckedItem(R.id.nav_basics);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id != currentCategory) {

            list(id);


        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    private void list(int id) {
        switch (id) {
            case R.id.nav_basics:
                exampleItemModel.clear();
                exampleItemModel.add(new ExampleItemModel(getString(R.string.activity_basic_simple_mapview_title), getString(R.string.activity_basic_simple_mapview_description), new Intent(MainActivity.this, SimpleMapViewActivity.class), getString(R.string.activity_basic_simple_mapview_url)));
                exampleItemModel.add(new ExampleItemModel(getString(R.string.activity_basic_support_map_frag_title), getString(R.string.activity_basic_support_map_frag_description), new Intent(MainActivity.this, SupportMapFragmentActivity.class), getString(R.string.activity_basic_support_map_frag_url)));
                adapter.notifyDataSetChanged();
                currentCategory = R.id.nav_basics;
                break;

            case R.id.nav_styles:
                exampleItemModel.clear();
                exampleItemModel.add(new ExampleItemModel(getString(R.string.activity_style_mapbox_studio_title), getString(R.string.activity_style_mapbox_studio_description), new Intent(MainActivity.this, MapboxStudioStyleActivity.class), getString(R.string.activity_style_mapbox_studio_url)));
                exampleItemModel.add(new ExampleItemModel(getString(R.string.activity_style_satellite_title), getString(R.string.activity_style_satellite_description), new Intent(MainActivity.this, SatelliteStyleActivity.class), getString(R.string.activity_style_satellite_url)));
                exampleItemModel.add(new ExampleItemModel(getString(R.string.activity_style_raster_title), getString(R.string.activity_style_raster_description), new Intent(MainActivity.this, CustomRasterStyleActivity.class), getString(R.string.activity_style_raster_url)));
                adapter.notifyDataSetChanged();
                currentCategory = R.id.nav_styles;
                break;
            case R.id.nav_annotations:
                exampleItemModel.clear();
                exampleItemModel.add(new ExampleItemModel(getString(R.string.activity_annotation_marker_title), getString(R.string.activity_annotation_custom_marker_description), new Intent(MainActivity.this, DrawMarkerActivity.class), getString(R.string.activity_annotation_marker_url)));
                exampleItemModel.add(new ExampleItemModel(getString(R.string.activity_annotation_custom_marker_title), getString(R.string.activity_annotation_custom_marker_description), new Intent(MainActivity.this, DrawCustomMarkerActivity.class), getString(R.string.activity_annotation_custom_marker_url)));
                exampleItemModel.add(new ExampleItemModel(getString(R.string.activity_annotation_geojson_line_title), getString(R.string.activity_annotation_geojson_line_description), new Intent(MainActivity.this, DrawGeojsonLineActivity.class), getString(R.string.activity_annotation_geojson_line_url)));
                exampleItemModel.add(new ExampleItemModel(getString(R.string.activity_annotation_polygon_title), getString(R.string.activity_annotation_polygon_description), new Intent(MainActivity.this, DrawPolygonActivity.class), getString(R.string.activity_annotation_polygon_url)));
                adapter.notifyDataSetChanged();
                currentCategory = R.id.nav_annotations;
                break;

            case R.id.nav_camera:
                exampleItemModel.clear();
                exampleItemModel.add(new ExampleItemModel(getString(R.string.activity_camera_animate_title), getString(R.string.activity_camera_animate_description), new Intent(MainActivity.this, AnimateMapCameraActivity.class), getString(R.string.activity_camera_animate_url)));
                exampleItemModel.add(new ExampleItemModel(getString(R.string.activity_camera_bounding_box_title), getString(R.string.activity_camera_bounding_box_description), new Intent(MainActivity.this, BoundingBoxCameraActivity.class), null));
                adapter.notifyDataSetChanged();
                currentCategory = R.id.nav_camera;
                break;
            case R.id.nav_offline:
                exampleItemModel.clear();
                exampleItemModel.add(new ExampleItemModel(getString(R.string.activity_offline_simple_title), getString(R.string.activity_offline_simple_description), new Intent(MainActivity.this, SimpleOfflineMapActivity.class), getString(R.string.activity_offline_simple_url)));
                exampleItemModel.add(new ExampleItemModel(getString(R.string.activity_offline_manager_title), getString(R.string.activity_offline_manager_description), new Intent(MainActivity.this, OfflineManagerActivity.class), getString(R.string.activity_offline_manager_url)));
                adapter.notifyDataSetChanged();
                currentCategory = R.id.nav_offline;
                break;
            case R.id.nav_mas:
                exampleItemModel.clear();
                exampleItemModel.add(new ExampleItemModel(getString(R.string.activity_mas_directions_title), getString(R.string.activity_mas_directions_description), new Intent(MainActivity.this, DirectionsActivity.class), getString(R.string.activity_mas_directions_url)));
                exampleItemModel.add(new ExampleItemModel(getString(R.string.activity_mas_geocoding_title), getString(R.string.activity_mas_geocoding_description), new Intent(MainActivity.this, GeocodingActivity.class), getString(R.string.activity_mas_geocoding_url)));
                exampleItemModel.add(new ExampleItemModel(getString(R.string.activity_mas_static_image_title), getString(R.string.activity_mas_static_image_description), new Intent(MainActivity.this, StaticImageActivity.class), getString(R.string.activity_mas_static_image_url)));
                adapter.notifyDataSetChanged();
                currentCategory = R.id.nav_mas;
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_info) {
            new MaterialStyledDialog(MainActivity.this)
                    .setTitle("Awesome maps?")
                    .setDescription("Do you like this library? Learn how to include it in your app!")
                    .setIcon(R.mipmap.ic_launcher)
                    .setHeaderColor(R.color.mapboxDenim)
                    .withDivider(true)
                    .setPositive("Mapbox", new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse("https://mapbox.com/android-sdk"));
                            startActivity(i);
                        }
                    })
                    .setNegative("Not now", new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        }
                    })

                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("CURRENT_CATEGORY", currentCategory);
    }
}