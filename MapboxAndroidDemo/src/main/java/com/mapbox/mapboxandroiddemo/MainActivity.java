package com.mapbox.mapboxandroiddemo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.firebase.perf.metrics.AddTrace;
import com.mapbox.mapboxandroiddemo.adapter.ExampleAdapter;
import com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker;
import com.mapbox.mapboxandroiddemo.commons.FirstTimeRunChecker;
import com.mapbox.mapboxandroiddemo.examples.annotations.AnimatedMarkerActivity;
import com.mapbox.mapboxandroiddemo.examples.annotations.BasicMarkerViewActivity;
import com.mapbox.mapboxandroiddemo.examples.annotations.CustomInfoWindowActivity;
import com.mapbox.mapboxandroiddemo.examples.annotations.DrawCustomMarkerActivity;
import com.mapbox.mapboxandroiddemo.examples.annotations.DrawGeojsonLineActivity;
import com.mapbox.mapboxandroiddemo.examples.annotations.DrawMarkerActivity;
import com.mapbox.mapboxandroiddemo.examples.annotations.DrawPolygonActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.MultipleGeometriesActivity;
import com.mapbox.mapboxandroiddemo.examples.annotations.PolygonHolesActivity;
import com.mapbox.mapboxandroiddemo.examples.basics.MapboxMapOptionActivity;
import com.mapbox.mapboxandroiddemo.examples.basics.SimpleMapViewActivity;
import com.mapbox.mapboxandroiddemo.examples.basics.SupportMapFragmentActivity;
import com.mapbox.mapboxandroiddemo.examples.camera.AnimateMapCameraActivity;
import com.mapbox.mapboxandroiddemo.examples.camera.BoundingBoxCameraActivity;
import com.mapbox.mapboxandroiddemo.examples.camera.RestrictCameraActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.AddRainFallStyleActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.ChoroplethJsonVectorMixActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.ChoroplethZoomChangeActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.StyleCirclesCategoricallyActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.StyleLineIdentityPropertyActivity;
import com.mapbox.mapboxandroiddemo.examples.extrusions.AdjustExtrusionLightActivity;
import com.mapbox.mapboxandroiddemo.examples.extrusions.Indoor3DMapActivity;
import com.mapbox.mapboxandroiddemo.examples.extrusions.MarathonExtrusionActivity;
import com.mapbox.mapboxandroiddemo.examples.extrusions.PopulationDensityExtrusionActivity;
import com.mapbox.mapboxandroiddemo.examples.extrusions.RotationExtrusionActivity;
import com.mapbox.mapboxandroiddemo.examples.mas.DirectionsActivity;
import com.mapbox.mapboxandroiddemo.examples.mas.GeocodingActivity;
import com.mapbox.mapboxandroiddemo.examples.mas.MapMatchingActivity;
import com.mapbox.mapboxandroiddemo.examples.mas.OptimizationActivity;
import com.mapbox.mapboxandroiddemo.examples.mas.SimplifyPolylineActivity;
import com.mapbox.mapboxandroiddemo.examples.mas.StaticImageActivity;
import com.mapbox.mapboxandroiddemo.examples.offline.OfflineManagerActivity;
import com.mapbox.mapboxandroiddemo.examples.offline.SimpleOfflineMapActivity;
import com.mapbox.mapboxandroiddemo.examples.plugins.BuildingPluginActivity;
import com.mapbox.mapboxandroiddemo.examples.plugins.GeoJsonPluginActivity;
import com.mapbox.mapboxandroiddemo.examples.plugins.LocationPluginActivity;
import com.mapbox.mapboxandroiddemo.examples.plugins.TrafficPluginActivity;
import com.mapbox.mapboxandroiddemo.examples.query.ClickOnLayerActivity;
import com.mapbox.mapboxandroiddemo.examples.query.FeatureCountActivity;
import com.mapbox.mapboxandroiddemo.examples.query.QueryFeatureActivity;
import com.mapbox.mapboxandroiddemo.examples.query.SelectBuildingActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.AddWmsSourceActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.AdjustLayerOpacityActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.ColorSwitcherActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.CreateHeatmapPointsActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.DefaultStyleActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.GeoJsonClusteringActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.GeojsonLayerInStackActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.LanguageSwitchActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.LineLayerActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.LocalStyleSourceActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.MapboxStudioStyleActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.ShowHideLayersActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.SymbolLayerActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.VectorSourceActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.ZoomDependentFillColorActivity;
import com.mapbox.mapboxandroiddemo.labs.IndoorMapActivity;
import com.mapbox.mapboxandroiddemo.labs.LocationPickerActivity;
import com.mapbox.mapboxandroiddemo.labs.LosAngelesTourismActivity;
import com.mapbox.mapboxandroiddemo.labs.MapillaryActivity;
import com.mapbox.mapboxandroiddemo.labs.MarkerFollowingRouteActivity;
import com.mapbox.mapboxandroiddemo.labs.PictureInPictureActivity;
import com.mapbox.mapboxandroiddemo.labs.RecyclerViewOnMapActivity;
import com.mapbox.mapboxandroiddemo.labs.SpaceStationLocationActivity;
import com.mapbox.mapboxandroiddemo.model.ExampleItemModel;
import com.mapbox.mapboxandroiddemo.utils.ItemClickSupport;
import com.mapbox.mapboxandroiddemo.utils.SettingsDialogView;

import java.util.ArrayList;

import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.CLICKED_ON_INFO_DIALOG_NOT_NOW;
import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.CLICKED_ON_INFO_DIALOG_START_LEARNING;
import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.CLICKED_ON_INFO_MENU_ITEM;
import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.CLICKED_ON_SETTINGS_IN_NAV_DRAWER;
import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.OPENED_APP;
import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.SKIPPED_ACCOUNT_CREATION;
import static com.mapbox.mapboxandroiddemo.commons.StringConstants.SKIPPED_KEY;
import static com.mapbox.mapboxandroiddemo.commons.StringConstants.TOKEN_SAVED_KEY;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

  private ArrayList<ExampleItemModel> exampleItemModel;
  private ExampleAdapter adapter;
  private int currentCategory = R.id.nav_basics;
  private RecyclerView recyclerView;
  private Switch analyticsOptOutSwitch;
  private boolean loggedIn;
  private Toolbar toolbar;

  private AnalyticsTracker analytics;

  @Override
  @AddTrace(name = "onCreateMainActivity")
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    analytics = AnalyticsTracker.getInstance(this, false);

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
      @AddTrace(name = "onItemClicked")
      public void onItemClicked(RecyclerView recyclerView, int position, View view) {
        if (currentCategory == R.id.nav_lab && position == 0) {
          return;
        } else if (currentCategory == R.id.nav_mas && position == 0) {
          return;
        } else if (currentCategory == R.id.nav_query_map && position == 0) {
          return;
        }
        startActivity(exampleItemModel.get(position).getActivity());

        analytics.clickedOnIndividualExample(getString(exampleItemModel.get(position).getTitle()), loggedIn);
        analytics.viewedScreen(getString(exampleItemModel.get(position)
          .getTitle()), loggedIn);
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

    loggedIn = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
      .getBoolean(TOKEN_SAVED_KEY, false);

    if (loggedIn) {
      analytics.setMapboxUsername();
      analytics.viewedScreen(MainActivity.class.getSimpleName(), loggedIn);
      checkForFirstTimeOpen();
    } else {
      analytics.trackEvent(SKIPPED_ACCOUNT_CREATION, loggedIn);
      PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
        .putBoolean(SKIPPED_KEY, true)
        .apply();
    }
    analytics.trackEvent(OPENED_APP, loggedIn);
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
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    // Handle navigation view item clicks here.
    int id = item.getItemId();

    if (id == R.id.settings_in_nav_drawer) {
      buildSettingsDialog();
    }

    if (id == R.id.share_app_in_nav_drawer) {
      shareApp();
    }

    if (id != currentCategory && id != R.id.settings_in_nav_drawer) {
      listItems(id);
      toolbar.setTitle(item.getTitle());
      analytics.clickedOnNavDrawerSection(
        item.getTitle().toString(), loggedIn);
    }
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (drawer != null) {
      drawer.closeDrawer(GravityCompat.START);
    }
    return true;
  }

  @AddTrace(name = "listItems")
  private void listItems(int id) {
    exampleItemModel.clear();
    switch (id) {
      case R.id.nav_styles:
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_styles_default_title,
          R.string.activity_styles_default_description,
          new Intent(MainActivity.this, DefaultStyleActivity.class),
          R.string.activity_styles_default_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_styles_symbol_layer_title,
          R.string.activity_styles_symbol_layer_description,
          new Intent(MainActivity.this, SymbolLayerActivity.class),
          R.string.activity_styles_symbol_layer_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_styles_data_clusters_title,
          R.string.activity_styles_create_data_cluster_description,
          new Intent(MainActivity.this, GeoJsonClusteringActivity.class),
          R.string.activity_styles_create_cluster_data_points_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_styles_line_layer_title,
          R.string.activity_styles_line_layer_description,
          new Intent(MainActivity.this, LineLayerActivity.class),
          R.string.activity_styles_line_layer_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_styles_color_switcher_title,
          R.string.activity_styles_color_switcher_description,
          new Intent(MainActivity.this, ColorSwitcherActivity.class),
          R.string.activity_styles_color_switcher_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_styles_vector_source_title,
          R.string.activity_styles_vector_source_description,
          new Intent(MainActivity.this, VectorSourceActivity.class),
          R.string.activity_styles_vector_source_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_styles_add_wms_source_title,
          R.string.activity_styles_add_wms_source_description,
          new Intent(MainActivity.this, AddWmsSourceActivity.class),
          R.string.activity_styles_add_wms_source_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_styles_geojson_layer_in_stack_title,
          R.string.activity_styles_geojson_layer_in_stack_description,
          new Intent(MainActivity.this, GeojsonLayerInStackActivity.class),
          R.string.activity_styles_geojson_layer_in_stack_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_styles_adjust_layer_opacity_title,
          R.string.activity_styles_adjust_layer_opacity_description,
          new Intent(MainActivity.this, AdjustLayerOpacityActivity.class),
          R.string.activity_styles_adjust_layer_opacity_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_styles_zoom_dependent_fill_color_title,
          R.string.activity_styles_zoom_dependent_fill_color_description,
          new Intent(MainActivity.this, ZoomDependentFillColorActivity.class),
          R.string.activity_styles_zoom_dependent_fill_color_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_styles_language_switch_title,
          R.string.activity_styles_language_switch_description,
          new Intent(MainActivity.this, LanguageSwitchActivity.class),
          R.string.activity_styles_language_switch_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_styles_show_hide_layer_title,
          R.string.activity_styles_show_hide_layer_description,
          new Intent(MainActivity.this, ShowHideLayersActivity.class),
          R.string.activity_styles_show_hide_layer_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_styles_mapbox_studio_title,
          R.string.activity_styles_mapbox_studio_description,
          new Intent(MainActivity.this, MapboxStudioStyleActivity.class),
          R.string.activity_styles_mapbox_studio_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_styles_local_style_or_raster_source_title,
          R.string.activity_styles_local_style_or_raster_source_description,
          new Intent(MainActivity.this, LocalStyleSourceActivity.class),
          R.string.activity_styles_local_style_or_raster_source_url
        ));
        currentCategory = R.id.nav_styles;
        break;
      case R.id.nav_extrusions:
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_extrusions_population_density_extrusions_title,
          R.string.activity_extrusions_population_density_extrusions_description,
          new Intent(MainActivity.this, PopulationDensityExtrusionActivity.class),
          R.string.activity_extrusions_population_density_extrusions_url, false
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_extrusions_catalina_marathon_extrusions_title,
          R.string.activity_extrusions_catalina_marathon_extrusions_description,
          new Intent(MainActivity.this, MarathonExtrusionActivity.class),
          R.string.activity_extrusions_catalina_marathon_extrusions_url, false
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_extrusions_adjust_extrusions_title,
          R.string.activity_extrusions_adjust_extrusions_description,
          new Intent(MainActivity.this, AdjustExtrusionLightActivity.class),
          R.string.activity_extrusions_adjust_extrusions_url, false
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_extrusions_indoor_3d_title,
          R.string.activity_extrusions_indoor_3d_description,
          new Intent(MainActivity.this, Indoor3DMapActivity.class),
          R.string.activity_extrusions_indoor_3d_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_extrusions_rotate_extrusions_title,
          R.string.activity_extrusions_rotate_extrusions_description,
          new Intent(MainActivity.this, RotationExtrusionActivity.class),
          R.string.activity_extrusions_rotate_extrusions_url, false
        ));
        currentCategory = R.id.nav_extrusions;
        break;

      case R.id.nav_plugins:
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_plugins_traffic_plugin_title,
          R.string.activity_plugins_traffic_plugin_description,
          new Intent(MainActivity.this, TrafficPluginActivity.class),
          R.string.activity_plugins_traffic_plugin_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_plugins_building_plugin_title,
          R.string.activity_plugins_building_plugin_description,
          new Intent(MainActivity.this, BuildingPluginActivity.class),
          R.string.activity_plugins_building_plugin_url, false
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_plugins_location_plugin_title,
          R.string.activity_plugins_location_plugin_description,
          new Intent(MainActivity.this, LocationPluginActivity.class),
          R.string.activity_plugins_location_plugin_url, false
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_plugins_geojson_plugin_title,
          R.string.activity_plugins_geojson_plugin_description,
          new Intent(MainActivity.this, GeoJsonPluginActivity.class),
          R.string.activity_plugins_geojson_plugin_url, true
        ));
        currentCategory = R.id.nav_plugins;
        break;

      case R.id.nav_annotations:
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_annotation_marker_title,
          R.string.activity_annotation_custom_marker_description,
          new Intent(MainActivity.this, DrawMarkerActivity.class),
          R.string.activity_annotation_marker_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_annotation_custom_marker_title,
          R.string.activity_annotation_custom_marker_description,
          new Intent(MainActivity.this, DrawCustomMarkerActivity.class),
          R.string.activity_annotation_custom_marker_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_annotation_geojson_line_title,
          R.string.activity_annotation_geojson_line_description,
          new Intent(MainActivity.this, DrawGeojsonLineActivity.class),
          R.string.activity_annotation_geojson_line_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_annotation_polygon_title,
          R.string.activity_annotation_polygon_description,
          new Intent(MainActivity.this, DrawPolygonActivity.class),
          R.string.activity_annotation_polygon_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_annotation_polygon_holes_title,
          R.string.activity_annotation_polygon_holes_description,
          new Intent(MainActivity.this, PolygonHolesActivity.class),
          R.string.activity_annotation_polygon_holes_url, false
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_annotation_custom_info_window_title,
          R.string.activity_annotation_custom_info_window_description,
          new Intent(MainActivity.this, CustomInfoWindowActivity.class),
          R.string.activity_annotation_custom_info_window_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_annotation_marker_view_title,
          R.string.activity_annotation_marker_view_description,
          new Intent(MainActivity.this, BasicMarkerViewActivity.class),
          R.string.activity_annotation_basic_marker_view_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_annotation_animated_marker_title,
          R.string.activity_annotation_animated_marker_description,
          new Intent(MainActivity.this, AnimatedMarkerActivity.class),
          R.string.activity_annotation_animated_marker_url
        ));
        currentCategory = R.id.nav_annotations;
        break;

      case R.id.nav_camera:
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_camera_animate_title,
          R.string.activity_camera_animate_description,
          new Intent(MainActivity.this, AnimateMapCameraActivity.class),
          R.string.activity_camera_animate_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_camera_bounding_box_title,
          R.string.activity_camera_bounding_box_description,
          new Intent(MainActivity.this, BoundingBoxCameraActivity.class),
          R.string.activity_camera_bounding_box_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_camera_restrict_title,
          R.string.activity_camera_restrict_description,
          new Intent(MainActivity.this, RestrictCameraActivity.class),
          R.string.activity_camera_restrict_url, false
        ));
        currentCategory = R.id.nav_camera;
        break;
      case R.id.nav_offline:
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_offline_simple_title,
          R.string.activity_offline_simple_description,
          new Intent(MainActivity.this, SimpleOfflineMapActivity.class),
          R.string.activity_offline_simple_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_offline_manager_title,
          R.string.activity_offline_manager_description,
          new Intent(MainActivity.this, OfflineManagerActivity.class),
          R.string.activity_offline_manager_url
        ));
        currentCategory = R.id.nav_offline;
        break;
      case R.id.nav_query_map:
        exampleItemModel.add(null);
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_query_select_building_title,
          R.string.activity_query_select_building_description,
          new Intent(MainActivity.this, SelectBuildingActivity.class),
          R.string.activity_query_select_building_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_query_feature_count_title,
          R.string.activity_query_feature_count_description,
          new Intent(MainActivity.this, FeatureCountActivity.class),
          R.string.activity_query_feature_count_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_query_feature_title,
          R.string.activity_query_feature_description,
          new Intent(MainActivity.this, QueryFeatureActivity.class),
          R.string.activity_query_feature_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_query_click_on_layer_title,
          R.string.activity_query_click_on_layer_description,
          new Intent(MainActivity.this, ClickOnLayerActivity.class),
          R.string.activity_query_click_on_layer_url
        ));
        currentCategory = R.id.nav_query_map;
        break;
      case R.id.nav_mas:
        exampleItemModel.add(null);
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_mas_simplify_polyline_title,
          R.string.activity_mas_simplify_polyline_description,
          new Intent(MainActivity.this, SimplifyPolylineActivity.class),
          R.string.activity_mas_simplify_polyline_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_mas_map_matching_title,
          R.string.activity_mas_map_matching_description,
          new Intent(MainActivity.this, MapMatchingActivity.class),
          R.string.activity_mas_map_matching_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_mas_directions_title,
          R.string.activity_mas_directions_description,
          new Intent(MainActivity.this, DirectionsActivity.class),
          R.string.activity_mas_directions_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_mas_optimization_title,
          R.string.activity_mas_optimization_description,
          new Intent(MainActivity.this, OptimizationActivity.class),
          R.string.activity_mas_optimization_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_mas_geocoding_title,
          R.string.activity_mas_geocoding_description,
          new Intent(MainActivity.this, GeocodingActivity.class),
          R.string.activity_mas_geocoding_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_mas_static_image_title,
          R.string.activity_mas_static_image_description,
          new Intent(MainActivity.this, StaticImageActivity.class),
          R.string.activity_mas_static_image_url
        ));
        currentCategory = R.id.nav_mas;
        break;
      case R.id.nav_lab:
        exampleItemModel.add(null);
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_labs_mapillary_title,
          R.string.activity_lab_mapillary_description,
          new Intent(MainActivity.this, MapillaryActivity.class),
          R.string.activity_lab_mapillary_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_lab_los_angeles_tourism_title,
          R.string.activity_lab_los_angeles_tourism_description,
          new Intent(MainActivity.this, LosAngelesTourismActivity.class),
          R.string.activity_lab_los_angeles_tourism_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_lab_indoor_map_title,
          R.string.activity_lab_indoor_map_description,
          new Intent(MainActivity.this, IndoorMapActivity.class),
          R.string.activity_lab_indoor_map_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_lab_location_picker_title,
          R.string.activity_lab_location_picker_description,
          new Intent(MainActivity.this, LocationPickerActivity.class),
          R.string.activity_lab_location_picker_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_lab_marker_following_route_title,
          R.string.activity_lab_marker_following_route_description,
          new Intent(MainActivity.this, MarkerFollowingRouteActivity.class),
          R.string.activity_lab_marker_following_route_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_lab_space_station_location_title,
          R.string.activity_lab_space_station_location_description,
          new Intent(MainActivity.this, SpaceStationLocationActivity.class),
          R.string.activity_lab_space_station_location_url
        ));
        if (Build.VERSION.SDK_INT >= 26) {
          exampleItemModel.add(new ExampleItemModel(
            R.string.activity_lab_picture_in_picture_title,
            R.string.activity_lab_picture_in_picture_description,
            new Intent(MainActivity.this, PictureInPictureActivity.class),
            R.string.activity_lab_picture_in_picture_url, true
          ));
        }
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_lab_rv_on_map_title,
          R.string.activity_lab_rv_on_map_description,
          new Intent(MainActivity.this, RecyclerViewOnMapActivity.class),
          R.string.activity_lab_rv_on_map_url, false
        ));
        currentCategory = R.id.nav_lab;
        break;
      case R.id.nav_dds:
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_dds_style_circle_categorically_title,
          R.string.activity_dds_style_circle_categorically_description,
          new Intent(MainActivity.this, StyleCirclesCategoricallyActivity.class),
          R.string.activity_dds_style_circle_categorically_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_dds_choropleth_zoom_change_title,
          R.string.activity_dds_choropleth_zoom_change_description,
          new Intent(MainActivity.this, ChoroplethZoomChangeActivity.class),
          R.string.activity_dds_choropleth_zoom_change_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_dds_style_line_identity_property_title,
          R.string.activity_dds_style_line_identity_property_description,
          new Intent(MainActivity.this, StyleLineIdentityPropertyActivity.class),
          R.string.activity_dds_style_line_identity_property_url, false
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_dds_create_heatmap_points_title,
          R.string.activity_dds_create_heatmap_points_description,
          new Intent(MainActivity.this, CreateHeatmapPointsActivity.class),
          R.string.activity_dds_create_heatmap_points_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_dds_json_vector_mix_title,
          R.string.activity_dds_json_vector_mix_description,
          new Intent(MainActivity.this, ChoroplethJsonVectorMixActivity.class),
          R.string.activity_dds_json_vector_mix_url,
          false
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_dds_time_lapse_rainfall_points_title,
          R.string.activity_dds_time_lapse_rainfall_points_description,
          new Intent(MainActivity.this, AddRainFallStyleActivity.class),
          R.string.activity_dds_time_lapse_rainfall_url,
          false
        ));
        exampleItemModel.add(new ExampleItemModel(
            R.string.activity_dds_multiple_geometries_title,
            R.string.activity_dds_multiple_geometries_description,
            new Intent(MainActivity.this, MultipleGeometriesActivity.class),
            R.string.activity_dds_multiple_geometries_url, true
        ));
        currentCategory = R.id.nav_dds;
        break;
      default:
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_basic_simple_mapview_title,
          R.string.activity_basic_simple_mapview_description,
          new Intent(MainActivity.this, SimpleMapViewActivity.class),
          R.string.activity_basic_simple_mapview_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_basic_support_map_frag_title,
          R.string.activity_basic_support_map_frag_description,
          new Intent(MainActivity.this, SupportMapFragmentActivity.class),
          R.string.activity_basic_support_map_frag_url
        ));
        exampleItemModel.add(new ExampleItemModel(
          R.string.activity_basic_mapbox_options_title,
          R.string.activity_basic_mapbox_options_description,
          new Intent(MainActivity.this, MapboxMapOptionActivity.class),
          R.string.activity_basic_mapbox_options_url
        ));
        currentCategory = R.id.nav_basics;
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
    if (id == R.id.action_info) {
      analytics.trackEvent(CLICKED_ON_INFO_MENU_ITEM,
        loggedIn);
      new MaterialStyledDialog.Builder(MainActivity.this)
        .setTitle(getString(R.string.info_dialog_title))
        .setDescription(getString(R.string.info_dialog_description))
        .setHeaderColor(R.color.mapboxBlue)
        .withDivider(true)
        .setPositiveText(getString(R.string.info_dialog_positive_button_text))
        .onPositive(new MaterialDialog.SingleButtonCallback() {
          @Override
          public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
            analytics.trackEvent(CLICKED_ON_INFO_DIALOG_START_LEARNING, false);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://mapbox.com/android-sdk"));
            startActivity(intent);
          }
        })
        .setNegativeText(getString(R.string.info_dialog_negative_button_text))
        .onNegative(new MaterialDialog.SingleButtonCallback() {
          @Override
          public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
            analytics.trackEvent(CLICKED_ON_INFO_DIALOG_NOT_NOW, loggedIn);
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

  private void checkForFirstTimeOpen() {
    FirstTimeRunChecker firstTimeRunChecker = new FirstTimeRunChecker(this);
    if (firstTimeRunChecker.firstEverOpen()) {
      analytics.openedAppForFirstTime(getResources().getBoolean(R.bool.isTablet), loggedIn);
    }
    firstTimeRunChecker.updateSharedPrefWithCurrentVersion();
  }

  private void buildSettingsDialog() {
    analytics.trackEvent(CLICKED_ON_SETTINGS_IN_NAV_DRAWER, loggedIn);
    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    final View customView = inflater.inflate(R.layout.settings_dialog_layout, null);
    analyticsOptOutSwitch = (Switch) customView.findViewById(R.id.analytics_opt_out_switch);
    analyticsOptOutSwitch.setChecked(!analytics.isAnalyticsEnabled());

    final SettingsDialogView dialogView = new SettingsDialogView(customView,
      this, analyticsOptOutSwitch, analytics, loggedIn);

    dialogView.buildDialog();

    Button logOutOfMapboxAccountButton = (Button) customView.findViewById(R.id.log_out_of_account_button);

    if (!loggedIn) {
      logOutOfMapboxAccountButton.setVisibility(View.GONE);
    } else {
      logOutOfMapboxAccountButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          dialogView.logOut(loggedIn);
        }
      });
    }
  }

  private void shareApp() {
    try {
      Intent intent = new Intent(Intent.ACTION_SEND);
      intent.setType("text/plain");
      intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_app_subject));
      intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_text));
      startActivity(Intent.createChooser(intent, getString(R.string.share_app_choose_one_instruction)));
    } catch (Exception exception) {
      Log.d("MainActivity", "shareApp: exception = " + exception);
    }
  }
}
