package com.mapbox.mapboxandroiddemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.mapbox.mapboxandroiddemo.examples.annotations.AnimatedCircleMarkerActivity;
import com.mapbox.mapboxandroiddemo.examples.annotations.AnimatedMarkerActivity;
import com.mapbox.mapboxandroiddemo.examples.annotations.BasicMarkerViewActivity;
import com.mapbox.mapboxandroiddemo.examples.annotations.CustomInfoWindowActivity;
import com.mapbox.mapboxandroiddemo.examples.annotations.DrawCustomMarkerActivity;
import com.mapbox.mapboxandroiddemo.examples.annotations.DrawGeojsonLineActivity;
import com.mapbox.mapboxandroiddemo.examples.annotations.DrawMarkerActivity;
import com.mapbox.mapboxandroiddemo.examples.annotations.DrawPolygonActivity;
import com.mapbox.mapboxandroiddemo.examples.basics.MapboxMapOptionActivity;
import com.mapbox.mapboxandroiddemo.examples.basics.SimpleMapViewActivity;
import com.mapbox.mapboxandroiddemo.examples.basics.SupportMapFragmentActivity;
import com.mapbox.mapboxandroiddemo.examples.camera.AnimateMapCameraActivity;
import com.mapbox.mapboxandroiddemo.examples.camera.BoundingBoxCameraActivity;
import com.mapbox.mapboxandroiddemo.examples.location.AnimatedLocationIconActivity;
import com.mapbox.mapboxandroiddemo.examples.location.BasicUserLocation;
import com.mapbox.mapboxandroiddemo.examples.location.CustomizeUserLocationActivity;
import com.mapbox.mapboxandroiddemo.examples.location.LocationTrackingActivity;
import com.mapbox.mapboxandroiddemo.examples.mas.DirectionsActivity;
import com.mapbox.mapboxandroiddemo.examples.mas.GeocodingActivity;
import com.mapbox.mapboxandroiddemo.examples.mas.MapMatchingActivity;
import com.mapbox.mapboxandroiddemo.examples.mas.SimplifyPolylineActivity;
import com.mapbox.mapboxandroiddemo.examples.mas.StaticImageActivity;
import com.mapbox.mapboxandroiddemo.examples.offline.OfflineManagerActivity;
import com.mapbox.mapboxandroiddemo.examples.offline.SimpleOfflineMapActivity;
import com.mapbox.mapboxandroiddemo.examples.query.FeatureCountActivity;
import com.mapbox.mapboxandroiddemo.examples.query.QueryFeatureActivity;
import com.mapbox.mapboxandroiddemo.examples.query.SelectBuildingActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.AdjustLayerOpacityActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.ColorSwitcherActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.CreateHeatmapPointsActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.CustomRasterStyleActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.DefaultStyleActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.GeojsonLayerInStackActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.LanguageSwitchActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.MapboxStudioStyleActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.SatelliteStyleActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.ShowHideLayersActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.VectorSourceActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.ZoomDependentFillColorActivity;
import com.mapbox.mapboxandroiddemo.labs.LandUseStylingActivity;
import com.mapbox.mapboxandroiddemo.labs.LocationPickerActivity;
import com.mapbox.mapboxandroiddemo.labs.MarkerFollowingRouteActivity;
import com.mapbox.mapboxandroiddemo.labs.OffRouteActivity;
import com.mapbox.mapboxandroiddemo.labs.SpaceStationLocationActivity;
import com.mapbox.mapboxandroiddemo.model.ExampleItemModel;
import com.mapbox.mapboxandroiddemo.utils.ItemClickSupport;
import com.mapbox.mapboxsdk.MapboxAccountManager;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

  private ArrayList<ExampleItemModel> exampleItemModel;
  private ExampleAdapter adapter;
  private int currentCategory = R.id.nav_basics;
  private RecyclerView recyclerView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    // Mapbox access token is configured here.
    MapboxAccountManager.start(this, getString(R.string.access_token));

    exampleItemModel = new ArrayList<>();


    // Create the adapter to convert the array to views
    adapter = new ExampleAdapter(this, exampleItemModel);
    // Attach the adapter to a ListView
    recyclerView = (RecyclerView) findViewById(R.id.details_list);
    if (recyclerView != null) {
      recyclerView.setHasFixedSize(true);
      recyclerView.setLayoutManager(new LinearLayoutManager(this));
      recyclerView.setAdapter(adapter);
    }
    if (savedInstanceState == null) {
      listItems(R.id.nav_basics);
    } else {
      currentCategory = savedInstanceState.getInt("CURRENT_CATEGORY");
      listItems(currentCategory);
    }

    ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
      @Override
      public void onItemClicked(RecyclerView recyclerView, int position, View v) {
        if (currentCategory == R.id.nav_lab && position == 0) return;
        else if (currentCategory == R.id.nav_mas && position == 0) return;
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
        moveTaskToBack(true);
      }
    }
  }

  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    // Handle navigation view item clicks here.
    int id = item.getItemId();

    if (id != currentCategory) {

      listItems(id);


    }

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (drawer != null) {
      drawer.closeDrawer(GravityCompat.START);
    }
    return true;
  }

  private void listItems(int id) {
    exampleItemModel.clear();
    switch (id) {
      case R.id.nav_basics:
        exampleItemModel.add(new ExampleItemModel(R.string.activity_basic_simple_mapview_title, R.string.activity_basic_simple_mapview_description, new Intent(MainActivity.this, SimpleMapViewActivity.class), R.string.activity_basic_simple_mapview_url));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_basic_support_map_frag_title, R.string.activity_basic_support_map_frag_description, new Intent(MainActivity.this, SupportMapFragmentActivity.class), R.string.activity_basic_support_map_frag_url));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_basic_mapbox_options_title, R.string.activity_basic_mapbox_options_description, new Intent(MainActivity.this, MapboxMapOptionActivity.class), R.string.activity_basic_mapbox_options_url));
        currentCategory = R.id.nav_basics;
        break;

      case R.id.nav_styles:
        exampleItemModel.add(new ExampleItemModel(R.string.activity_style_create_heatmap_points_title, R.string.activity_style_create_heatmap_points_description, new Intent(MainActivity.this, CreateHeatmapPointsActivity.class), R.string.activity_style_create_heatmap_points_url, true));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_styles_color_switcher_title, R.string.activity_styles_color_switcher_description, new Intent(MainActivity.this, ColorSwitcherActivity.class), R.string.activity_styles_color_switcher_url, true));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_styles_vector_source_title, R.string.activity_styles_vector_source_description, new Intent(MainActivity.this, VectorSourceActivity.class), R.string.activity_styles_vector_source_url, true));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_styles_geojson_layer_in_stack_title, R.string.activity_styles_geojson_layer_in_stack_description, new Intent(MainActivity.this, GeojsonLayerInStackActivity.class), R.string.activity_styles_geojson_layer_in_stack_url, true));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_style_zoom_dependent_fill_color_title, R.string.activity_style_zoom_dependent_fill_color_description, new Intent(MainActivity.this, ZoomDependentFillColorActivity.class), R.string.activity_style_zoom_dependent_fill_color_url));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_styles_adjust_layer_opacity_title, R.string.activity_styles_adjust_layer_opacity_description, new Intent(MainActivity.this, AdjustLayerOpacityActivity.class), R.string.activity_styles_adjust_layer_opacity_url, true));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_styles_langauge_switch_title, R.string.activity_styles_langauge_switch_description, new Intent(MainActivity.this, LanguageSwitchActivity.class), R.string.activity_styles_langauge_switch_url, true));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_styles_show_hide_layer_title, R.string.activity_styles_show_hide_layer_description, new Intent(MainActivity.this, ShowHideLayersActivity.class), R.string.activity_styles_show_hide_layer_url, true));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_style_mapbox_studio_title, R.string.activity_style_mapbox_studio_description, new Intent(MainActivity.this, MapboxStudioStyleActivity.class), R.string.activity_style_mapbox_studio_url));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_style_satellite_title, R.string.activity_style_satellite_description, new Intent(MainActivity.this, SatelliteStyleActivity.class), R.string.activity_style_satellite_url));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_style_raster_title, R.string.activity_style_raster_description, new Intent(MainActivity.this, CustomRasterStyleActivity.class), R.string.activity_style_raster_url));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_style_default_title, R.string.activity_style_default_description, new Intent(MainActivity.this, DefaultStyleActivity.class), R.string.activity_style_default_url));
        currentCategory = R.id.nav_styles;
        break;
      case R.id.nav_annotations:
        exampleItemModel.add(new ExampleItemModel(R.string.activity_annotation_animated_circle_marker_title, R.string.activity_annotation_animated_circle_marker_description, new Intent(MainActivity.this, AnimatedCircleMarkerActivity.class), R.string.activity_annotation_animated_circle_marker_url, true));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_annotation_marker_title, R.string.activity_annotation_custom_marker_description, new Intent(MainActivity.this, DrawMarkerActivity.class), R.string.activity_annotation_marker_url));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_annotation_custom_marker_title, R.string.activity_annotation_custom_marker_description, new Intent(MainActivity.this, DrawCustomMarkerActivity.class), R.string.activity_annotation_custom_marker_url));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_annotation_geojson_line_title, R.string.activity_annotation_geojson_line_description, new Intent(MainActivity.this, DrawGeojsonLineActivity.class), R.string.activity_annotation_geojson_line_url));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_annotation_polygon_title, R.string.activity_annotation_polygon_description, new Intent(MainActivity.this, DrawPolygonActivity.class), R.string.activity_annotation_polygon_url));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_annotation_custom_info_window_title, R.string.activity_annotation_custom_info_window_description, new Intent(MainActivity.this, CustomInfoWindowActivity.class), R.string.activity_annotation_custom_info_window_url));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_annotation_marker_view_title, R.string.activity_annotation_marker_view_description, new Intent(MainActivity.this, BasicMarkerViewActivity.class), R.string.activity_annotation_basic_marker_view_url));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_annotation_animated_marker_title, R.string.activity_annotation_animated_marker_description, new Intent(MainActivity.this, AnimatedMarkerActivity.class), R.string.activity_annotation_animated_marker_url));
        currentCategory = R.id.nav_annotations;
        break;

      case R.id.nav_camera:
        exampleItemModel.add(new ExampleItemModel(R.string.activity_camera_animate_title, R.string.activity_camera_animate_description, new Intent(MainActivity.this, AnimateMapCameraActivity.class), R.string.activity_camera_animate_url));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_camera_bounding_box_title, R.string.activity_camera_bounding_box_description, new Intent(MainActivity.this, BoundingBoxCameraActivity.class), R.string.activity_camera_bounding_box_url));
        currentCategory = R.id.nav_camera;
        break;
      case R.id.nav_offline:
        exampleItemModel.add(new ExampleItemModel(R.string.activity_offline_simple_title, R.string.activity_offline_simple_description, new Intent(MainActivity.this, SimpleOfflineMapActivity.class), R.string.activity_offline_simple_url));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_offline_manager_title, R.string.activity_offline_manager_description, new Intent(MainActivity.this, OfflineManagerActivity.class), R.string.activity_offline_manager_url));
        currentCategory = R.id.nav_offline;
        break;
      case R.id.nav_query_map:
        exampleItemModel.add(null);
        exampleItemModel.add(new ExampleItemModel(R.string.activity_query_select_building_title, R.string.activity_query_select_building_description, new Intent(MainActivity.this, SelectBuildingActivity.class), R.string.activity_query_select_building_url, true));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_query_feature_count_title, R.string.activity_query_feature_count_description, new Intent(MainActivity.this, FeatureCountActivity.class), R.string.activity_query_feature_count_url, true));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_query_feature_title, R.string.activity_query_feature_description, new Intent(MainActivity.this, QueryFeatureActivity.class), R.string.activity_query_feature_url, true));
        currentCategory = R.id.nav_query_map;
        break;
      case R.id.nav_mas:
        exampleItemModel.add(null);
        exampleItemModel.add(new ExampleItemModel(R.string.activity_mas_simplify_polyline_title, R.string.activity_mas_simplify_polyline_description, new Intent(MainActivity.this, SimplifyPolylineActivity.class), R.string.activity_mas_simplify_polyline_url));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_mas_map_matching_title, R.string.activity_mas_map_matching_description, new Intent(MainActivity.this, MapMatchingActivity.class), R.string.activity_mas_map_matching_url));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_mas_directions_title, R.string.activity_mas_directions_description, new Intent(MainActivity.this, DirectionsActivity.class), R.string.activity_mas_directions_url));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_mas_geocoding_title, R.string.activity_mas_geocoding_description, new Intent(MainActivity.this, GeocodingActivity.class), R.string.activity_mas_geocoding_url));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_mas_static_image_title, R.string.activity_mas_static_image_description, new Intent(MainActivity.this, StaticImageActivity.class), R.string.activity_mas_static_image_url));
        currentCategory = R.id.nav_mas;
        break;
      case R.id.nav_location:
        exampleItemModel.add(new ExampleItemModel(R.string.activity_location_animated_icon_title, R.string.activity_location_animated_icon_description, new Intent(MainActivity.this, AnimatedLocationIconActivity.class), R.string.activity_location_animated_icon_url, true));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_location_customize_user_title, R.string.activity_location_customize_user_description, new Intent(MainActivity.this, CustomizeUserLocationActivity.class), R.string.activity_location_customize_user_url, true));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_location_basic_title, R.string.activity_location_basic_description, new Intent(MainActivity.this, BasicUserLocation.class), R.string.activity_location_basic_image_url));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_location_tracking_title, R.string.activity_location_tracking_description, new Intent(MainActivity.this, LocationTrackingActivity.class), R.string.activity_location_tracking_url));
        currentCategory = R.id.nav_location;
        break;
      case R.id.nav_lab:
        exampleItemModel.add(null);
        exampleItemModel.add(new ExampleItemModel(R.string.activity_lab_land_use_stying_title, R.string.activity_lab_land_use_stying_description, new Intent(MainActivity.this, LandUseStylingActivity.class), R.string.activity_lab_land_use_stying_url, true));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_lab_off_route_title, R.string.activity_lab_off_route_description, new Intent(MainActivity.this, OffRouteActivity.class), R.string.activity_lab_off_route_url, true));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_lab_location_picker_title, R.string.activity_lab_location_picker_description, new Intent(MainActivity.this, LocationPickerActivity.class), R.string.activity_lab_location_picker_url));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_lab_marker_following_route_title, R.string.activity_lab_marker_following_route_description, new Intent(MainActivity.this, MarkerFollowingRouteActivity.class), R.string.activity_lab_marker_following_route_url, true));
        exampleItemModel.add(new ExampleItemModel(R.string.activity_lab_space_station_location_title, R.string.activity_lab_space_station_location_description, new Intent(MainActivity.this, SpaceStationLocationActivity.class), R.string.activity_lab_space_station_location_url));
        currentCategory = R.id.nav_lab;
        break;
    }
    adapter.notifyDataSetChanged();

    // Scrolls recycler view back to top.
    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
    layoutManager.scrollToPositionWithOffset(0, 0);
  }

  public int getCurrentCategory() {
    return currentCategory;
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate toolbar items
    getMenuInflater().inflate(R.menu.menu_activity_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_info) {
      new MaterialStyledDialog(MainActivity.this)
          .setTitle(getString(R.string.info_dialog_title))
          .setDescription(getString(R.string.info_dialog_description))
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
