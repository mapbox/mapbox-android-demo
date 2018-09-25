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
import com.mapbox.mapboxandroiddemo.examples.annotations.DrawCustomMarkerActivity;
import com.mapbox.mapboxandroiddemo.examples.annotations.DrawGeojsonLineActivity;
import com.mapbox.mapboxandroiddemo.examples.annotations.DrawMarkerActivity;
import com.mapbox.mapboxandroiddemo.examples.annotations.DrawPolygonActivity;
import com.mapbox.mapboxandroiddemo.examples.annotations.PolygonHolesActivity;
import com.mapbox.mapboxandroiddemo.examples.basics.MapboxMapOptionActivity;
import com.mapbox.mapboxandroiddemo.examples.basics.SimpleMapViewActivity;
import com.mapbox.mapboxandroiddemo.examples.basics.SupportMapFragmentActivity;
import com.mapbox.mapboxandroiddemo.examples.camera.AnimateMapCameraActivity;
import com.mapbox.mapboxandroiddemo.examples.camera.BoundingBoxCameraActivity;
import com.mapbox.mapboxandroiddemo.examples.camera.RestrictCameraActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.AddRainFallStyleActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.BathymetryActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.ChoroplethJsonVectorMixActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.ChoroplethZoomChangeActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.CircleLayerClusteringActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.CreateHotspotsActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.HeatmapActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.ImageClusteringActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.InfoWindowSymbolLayerActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.MultipleGeometriesActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.MultipleHeatmapStylingActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.SatelliteLandSelectActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.StyleCirclesCategoricallyActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.StyleLineIdentityPropertyActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.ExpressionIntegrationActivity;
import com.mapbox.mapboxandroiddemo.examples.extrusions.AdjustExtrusionLightActivity;
import com.mapbox.mapboxandroiddemo.examples.extrusions.Indoor3DMapActivity;
import com.mapbox.mapboxandroiddemo.examples.extrusions.MarathonExtrusionActivity;
import com.mapbox.mapboxandroiddemo.examples.extrusions.PopulationDensityExtrusionActivity;
import com.mapbox.mapboxandroiddemo.examples.extrusions.RotationExtrusionActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.DirectionsActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.GeocodingActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.MapMatchingActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.MatrixApiActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.OptimizationActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.SimplifyPolylineActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.StaticImageActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.AnimatedImageGifActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.DashedLineDirectionsPickerActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.IndoorMapActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.InsetMapActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.LocationPickerActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.PulsingLayerOpacityColorActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.MarkerFollowingRouteActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.PictureInPictureActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.RecyclerViewOnMapActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.SnakingDirectionsRouteActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.SpaceStationLocationActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.SymbolLayerMapillaryActivity;
import com.mapbox.mapboxandroiddemo.examples.offline.OfflineManagerActivity;
import com.mapbox.mapboxandroiddemo.examples.offline.SimpleOfflineMapActivity;
import com.mapbox.mapboxandroiddemo.examples.plugins.BuildingPluginActivity;
import com.mapbox.mapboxandroiddemo.examples.plugins.LocalizationPluginActivity;
import com.mapbox.mapboxandroiddemo.examples.plugins.LocationPluginActivity;
import com.mapbox.mapboxandroiddemo.examples.plugins.LocationPluginFragmentActivity;
import com.mapbox.mapboxandroiddemo.examples.plugins.PlaceSelectionPluginActivity;
import com.mapbox.mapboxandroiddemo.examples.plugins.PlacesPluginActivity;
import com.mapbox.mapboxandroiddemo.examples.plugins.TrafficPluginActivity;
import com.mapbox.mapboxandroiddemo.examples.query.BuildingOutlineActivity;
import com.mapbox.mapboxandroiddemo.examples.query.ClickOnLayerActivity;
import com.mapbox.mapboxandroiddemo.examples.query.FeatureCountActivity;
import com.mapbox.mapboxandroiddemo.examples.query.HighlightedLineActivity;
import com.mapbox.mapboxandroiddemo.examples.query.QueryFeatureActivity;
import com.mapbox.mapboxandroiddemo.examples.query.RedoSearchInAreaActivity;
import com.mapbox.mapboxandroiddemo.examples.query.SelectBuildingActivity;
import com.mapbox.mapboxandroiddemo.examples.snapshot.SnapshotNotificationActivity;
import com.mapbox.mapboxandroiddemo.examples.snapshot.SnapshotShareActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.AddWmsSourceActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.AdjustLayerOpacityActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.BasicSymbolLayerActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.ColorSwitcherActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.DefaultStyleActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.GeojsonLayerInStackActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.HillShadeActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.ImageSourceActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.ImageSourceTimeLapseActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.LanguageSwitchActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.LineLayerActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.LocalStyleSourceActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.MapboxStudioStyleActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.ShowHideLayersActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.StyleFadeSwitchActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.TransparentBackgroundActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.VectorSourceActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.ZoomDependentFillColorActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.CalendarIntegrationActivity;
import com.mapbox.mapboxandroiddemo.model.ExampleItemModel;
import com.mapbox.mapboxandroiddemo.utils.ItemClickSupport;
import com.mapbox.mapboxandroiddemo.utils.SettingsDialogView;

import java.util.ArrayList;
import java.util.Iterator;

import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.CLICKED_ON_INFO_DIALOG_NOT_NOW;
import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.CLICKED_ON_INFO_DIALOG_START_LEARNING;
import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.CLICKED_ON_INFO_MENU_ITEM;
import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.CLICKED_ON_SETTINGS_IN_NAV_DRAWER;
import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.OPENED_APP;
import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.SKIPPED_ACCOUNT_CREATION;
import static com.mapbox.mapboxandroiddemo.commons.StringConstants.SKIPPED_KEY;
import static com.mapbox.mapboxandroiddemo.commons.StringConstants.TOKEN_SAVED_KEY;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
  // Used to track internal navigation to the Snapshotter section
  public static String EXTRA_NAV = "EXTRA_NAV";

  private ArrayList<ExampleItemModel> exampleItemModels;
  private ExampleAdapter adapter;
  private int currentCategory = R.id.nav_basics;
  private RecyclerView recyclerView;
  private Switch analyticsOptOutSwitch;
  private boolean loggedIn;
  private Toolbar toolbar;
  private String categoryTitleForToolbar;
  private AnalyticsTracker analytics;

  @Override
  @AddTrace(name = "onCreateMainActivity")
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    toolbar = (Toolbar) findViewById(R.id.toolbar);

    if (savedInstanceState == null) {
      setSupportActionBar(toolbar);
    }

    analytics = AnalyticsTracker.getInstance(this, false);

    exampleItemModels = new ArrayList<>();

    // Create the adapter to convert the array to views
    adapter = new ExampleAdapter(this, exampleItemModels);
    // Attach the adapter to a ListView
    recyclerView = (RecyclerView) findViewById(R.id.details_list);
    if (recyclerView != null) {
      recyclerView.setHasFixedSize(true);
      recyclerView.setLayoutManager(new LinearLayoutManager(this));
      recyclerView.setAdapter(adapter);
    }
    if (savedInstanceState != null) {
      currentCategory = savedInstanceState.getInt("CURRENT_CATEGORY");
      categoryTitleForToolbar = savedInstanceState.getString("CURRENT_CATEGORY_TOOLBAR_TITLE");
      toolbar.setTitle(categoryTitleForToolbar);
      listItems(currentCategory);
    } else if (getIntent().getIntExtra(EXTRA_NAV, -1) == R.id.nav_snapshot_image_generator) {
      currentCategory = R.id.nav_snapshot_image_generator;
      listItems(R.id.nav_snapshot_image_generator);
    } else {
      listItems(R.id.nav_basics);
    }

    ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
      @Override
      @AddTrace(name = "onItemClicked")
      public void onItemClicked(RecyclerView recyclerView, int position, View view) {
        if (currentCategory == R.id.nav_lab && position == 0) {
          return;
        } else if (currentCategory == R.id.nav_java_services && position == 0) {
          return;
        } else if (currentCategory == R.id.nav_query_map && position == 0) {
          return;
        }
        startActivity(exampleItemModels.get(position).getActivity());

        analytics.clickedOnIndividualExample(getString(exampleItemModels.get(position).getTitle()), loggedIn);
        analytics.viewedScreen(getString(exampleItemModels.get(position)
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
      categoryTitleForToolbar = item.getTitle().toString();
      toolbar.setTitle(categoryTitleForToolbar);
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
    exampleItemModels.clear();
    switch (id) {
      case R.id.nav_styles:
        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_styles_default_title,
          R.string.activity_styles_default_description,
          new Intent(MainActivity.this, DefaultStyleActivity.class),
          R.string.activity_styles_default_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_styles_basic_symbol_layer_title,
          R.string.activity_styles_basic_symbol_layer_description,
          new Intent(MainActivity.this, BasicSymbolLayerActivity.class),
          R.string.activity_styles_symbol_layer_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_styles_line_layer_title,
          R.string.activity_styles_line_layer_description,
          new Intent(MainActivity.this, LineLayerActivity.class),
          R.string.activity_styles_line_layer_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_styles_color_switcher_title,
          R.string.activity_styles_color_switcher_description,
          new Intent(MainActivity.this, ColorSwitcherActivity.class),
          R.string.activity_styles_color_switcher_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_styles_vector_source_title,
          R.string.activity_styles_vector_source_description,
          new Intent(MainActivity.this, VectorSourceActivity.class),
          R.string.activity_styles_vector_source_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_styles_add_wms_source_title,
          R.string.activity_styles_add_wms_source_description,
          new Intent(MainActivity.this, AddWmsSourceActivity.class),
          R.string.activity_styles_add_wms_source_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_styles_geojson_layer_in_stack_title,
          R.string.activity_styles_geojson_layer_in_stack_description,
          new Intent(MainActivity.this, GeojsonLayerInStackActivity.class),
          R.string.activity_styles_geojson_layer_in_stack_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_styles_adjust_layer_opacity_title,
          R.string.activity_styles_adjust_layer_opacity_description,
          new Intent(MainActivity.this, AdjustLayerOpacityActivity.class),
          R.string.activity_styles_adjust_layer_opacity_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_styles_zoom_dependent_fill_color_title,
          R.string.activity_styles_zoom_dependent_fill_color_description,
          new Intent(MainActivity.this, ZoomDependentFillColorActivity.class),
          R.string.activity_styles_zoom_dependent_fill_color_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_styles_language_switch_title,
          R.string.activity_styles_language_switch_description,
          new Intent(MainActivity.this, LanguageSwitchActivity.class),
          R.string.activity_styles_language_switch_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_styles_show_hide_layer_title,
          R.string.activity_styles_show_hide_layer_description,
          new Intent(MainActivity.this, ShowHideLayersActivity.class),
          R.string.activity_styles_show_hide_layer_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_styles_mapbox_studio_title,
          R.string.activity_styles_mapbox_studio_description,
          new Intent(MainActivity.this, MapboxStudioStyleActivity.class),
          R.string.activity_styles_mapbox_studio_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_styles_local_style_or_raster_source_title,
          R.string.activity_styles_local_style_or_raster_source_description,
          new Intent(MainActivity.this, LocalStyleSourceActivity.class),
          R.string.activity_styles_local_style_or_raster_source_url, false, BuildConfig.MIN_SDK_VERSION
        ));
        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_style_image_source_title,
          R.string.activity_style_image_source_description,
          new Intent(MainActivity.this, ImageSourceActivity.class),
          R.string.activity_style_image_source_url, false, BuildConfig.MIN_SDK_VERSION
        ));
        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_style_image_source_time_lapse_title,
          R.string.activity_style_image_source_time_lapse_description,
          new Intent(MainActivity.this, ImageSourceTimeLapseActivity.class),
          R.string.activity_style_image_source_time_lapse_url, false, BuildConfig.MIN_SDK_VERSION
        ));
        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_styles_hillshade_title,
          R.string.activity_style_hillshade_description,
          new Intent(MainActivity.this, HillShadeActivity.class),
          R.string.activity_style_hillshade_url, false, BuildConfig.MIN_SDK_VERSION
        ));
        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_styles_fade_switch_title,
          R.string.activity_styles_fade_switch_description,
          new Intent(MainActivity.this, StyleFadeSwitchActivity.class),
          R.string.activity_styles_fade_switch_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_styles_transparent_background_title,
          R.string.activity_styles_transparent_background_description,
          new Intent(MainActivity.this, TransparentBackgroundActivity.class),
          R.string.activity_styles_transparent_background_url, false, BuildConfig.MIN_SDK_VERSION));

        currentCategory = R.id.nav_styles;
        break;
      case R.id.nav_extrusions:
        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_extrusions_population_density_extrusions_title,
          R.string.activity_extrusions_population_density_extrusions_description,
          new Intent(MainActivity.this, PopulationDensityExtrusionActivity.class),
          R.string.activity_extrusions_population_density_extrusions_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_extrusions_catalina_marathon_extrusions_title,
          R.string.activity_extrusions_catalina_marathon_extrusions_description,
          new Intent(MainActivity.this, MarathonExtrusionActivity.class),
          R.string.activity_extrusions_catalina_marathon_extrusions_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_extrusions_adjust_extrusions_title,
          R.string.activity_extrusions_adjust_extrusions_description,
          new Intent(MainActivity.this, AdjustExtrusionLightActivity.class),
          R.string.activity_extrusions_adjust_extrusions_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_extrusions_indoor_3d_title,
          R.string.activity_extrusions_indoor_3d_description,
          new Intent(MainActivity.this, Indoor3DMapActivity.class),
          R.string.activity_extrusions_indoor_3d_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_extrusions_rotate_extrusions_title,
          R.string.activity_extrusions_rotate_extrusions_description,
          new Intent(MainActivity.this, RotationExtrusionActivity.class),
          R.string.activity_extrusions_rotate_extrusions_url, false, BuildConfig.MIN_SDK_VERSION));

        currentCategory = R.id.nav_extrusions;
        break;

      case R.id.nav_plugins:

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_plugins_traffic_plugin_title,
          R.string.activity_plugins_traffic_plugin_description,
          new Intent(MainActivity.this, TrafficPluginActivity.class),
          R.string.activity_plugins_traffic_plugin_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_plugins_building_plugin_title,
          R.string.activity_plugins_building_plugin_description,
          new Intent(MainActivity.this, BuildingPluginActivity.class),
          R.string.activity_plugins_building_plugin_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_plugins_location_plugin_title,
          R.string.activity_plugins_location_plugin_description,
          new Intent(MainActivity.this, LocationPluginActivity.class),
          R.string.activity_plugins_location_plugin_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_plugins_places_plugin_title, R.string.activity_plugins_places_plugin_description,
          new Intent(MainActivity.this, PlacesPluginActivity.class),
          R.string.activity_plugins_places_plugin_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_plugins_localization_plugin_title,
          R.string.activity_plugins_localization_plugin_description,
          new Intent(MainActivity.this, LocalizationPluginActivity.class),
          R.string.activity_plugins_localization_plugin_url, false, BuildConfig.MIN_SDK_VERSION)
        );

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_plugins_place_picker_plugin_title,
          R.string.activity_plugins_place_picker_plugin_description,
          new Intent(MainActivity.this, PlaceSelectionPluginActivity.class),
          R.string.activity_plugins_place_picker_plugin_url, true, BuildConfig.MIN_SDK_VERSION)
        );

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_user_location_map_frag_title,
          R.string.activity_user_location_map_frag_plugin_description,
          new Intent(MainActivity.this, LocationPluginFragmentActivity.class),
          R.string.activity_user_location_fragment_plugin_url, true, BuildConfig.MIN_SDK_VERSION)
        );

        currentCategory = R.id.nav_plugins;
        break;

      case R.id.nav_annotations:
        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_annotation_marker_title,
          R.string.activity_annotation_custom_marker_description,
          new Intent(MainActivity.this, DrawMarkerActivity.class),
          R.string.activity_annotation_marker_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_annotation_custom_marker_title,
          R.string.activity_annotation_custom_marker_description,
          new Intent(MainActivity.this, DrawCustomMarkerActivity.class),
          R.string.activity_annotation_custom_marker_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_annotation_geojson_line_title,
          R.string.activity_annotation_geojson_line_description,
          new Intent(MainActivity.this, DrawGeojsonLineActivity.class),
          R.string.activity_annotation_geojson_line_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_annotation_polygon_title,
          R.string.activity_annotation_polygon_description,
          new Intent(MainActivity.this, DrawPolygonActivity.class),
          R.string.activity_annotation_polygon_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_annotation_polygon_holes_title,
          R.string.activity_annotation_polygon_holes_description,
          new Intent(MainActivity.this, PolygonHolesActivity.class),
          R.string.activity_annotation_polygon_holes_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_annotation_animated_marker_title,
          R.string.activity_annotation_animated_marker_description,
          new Intent(MainActivity.this, AnimatedMarkerActivity.class),
          R.string.activity_annotation_animated_marker_url, false, BuildConfig.MIN_SDK_VERSION));

        currentCategory = R.id.nav_annotations;
        break;

      case R.id.nav_camera:
        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_camera_animate_title,
          R.string.activity_camera_animate_description,
          new Intent(MainActivity.this, AnimateMapCameraActivity.class),
          R.string.activity_camera_animate_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_camera_bounding_box_title,
          R.string.activity_camera_bounding_box_description,
          new Intent(MainActivity.this, BoundingBoxCameraActivity.class),
          R.string.activity_camera_bounding_box_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_camera_restrict_title,
          R.string.activity_camera_restrict_description,
          new Intent(MainActivity.this, RestrictCameraActivity.class),
          R.string.activity_camera_restrict_url, false, BuildConfig.MIN_SDK_VERSION));

        currentCategory = R.id.nav_camera;
        break;
      case R.id.nav_offline:
        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_offline_simple_title,
          R.string.activity_offline_simple_description,
          new Intent(MainActivity.this, SimpleOfflineMapActivity.class),
          R.string.activity_offline_simple_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_offline_manager_title,
          R.string.activity_offline_manager_description,
          new Intent(MainActivity.this, OfflineManagerActivity.class),
          R.string.activity_offline_manager_url, false, BuildConfig.MIN_SDK_VERSION));

        currentCategory = R.id.nav_offline;
        break;
      case R.id.nav_query_map:
        exampleItemModels.add(null);
        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_query_select_building_title,
          R.string.activity_query_select_building_description,
          new Intent(MainActivity.this, SelectBuildingActivity.class),
          R.string.activity_query_select_building_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_query_feature_count_title,
          R.string.activity_query_feature_count_description,
          new Intent(MainActivity.this, FeatureCountActivity.class),
          R.string.activity_query_feature_count_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_query_feature_title,
          R.string.activity_query_feature_description,
          new Intent(MainActivity.this, QueryFeatureActivity.class),
          R.string.activity_query_feature_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_query_click_on_layer_title,
          R.string.activity_query_click_on_layer_description,
          new Intent(MainActivity.this, ClickOnLayerActivity.class),
          R.string.activity_query_click_on_layer_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_query_redo_search_in_area_title,
          R.string.activity_query_redo_search_in_area_description,
          new Intent(MainActivity.this, RedoSearchInAreaActivity.class),
          R.string.activity_query_redo_search_in_area_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_query_building_outline_title,
          R.string.activity_query_building_outline_description,
          new Intent(MainActivity.this, BuildingOutlineActivity.class),
          R.string.activity_query_building_outline_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_query_highlighted_line_title,
          R.string.activity_query_highlighted_line_description,
          new Intent(MainActivity.this, HighlightedLineActivity.class),
          R.string.activity_query_highlighted_line_url, true, BuildConfig.MIN_SDK_VERSION));

        currentCategory = R.id.nav_query_map;
        break;
      case R.id.nav_java_services:
        exampleItemModels.add(null);
        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_java_services_simplify_polyline_title,
          R.string.activity_java_services_simplify_polyline_description,
          new Intent(MainActivity.this, SimplifyPolylineActivity.class),
          R.string.activity_java_services_simplify_polyline_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_java_services_map_matching_title,
          R.string.activity_java_services_map_matching_description,
          new Intent(MainActivity.this, MapMatchingActivity.class),
          R.string.activity_java_services_map_matching_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_java_services_directions_title,
          R.string.activity_java_services_directions_description,
          new Intent(MainActivity.this, DirectionsActivity.class),
          R.string.activity_java_services_directions_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_java_services_optimization_title,
          R.string.activity_java_services_optimization_description,
          new Intent(MainActivity.this, OptimizationActivity.class),
          R.string.activity_java_services_optimization_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_java_services_static_image_title,
          R.string.activity_java_services_static_image_description,
          new Intent(MainActivity.this, StaticImageActivity.class),
          R.string.activity_java_services_static_image_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_java_services_maxtrix_api_title,
          R.string.activity_java_services_matrix_api_description,
          new Intent(MainActivity.this, MatrixApiActivity.class),
          R.string.activity_java_services_matrix_url));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_java_services_geocoding_title,
          R.string.activity_java_services_geocoding_description,
          new Intent(MainActivity.this, GeocodingActivity.class),
          R.string.activity_java_services_geocoding_url));

        currentCategory = R.id.nav_java_services;
        break;
      case R.id.nav_snapshot_image_generator:
        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_image_generator_snapshot_notification_title,
          R.string.activity_image_generator_snapshot_notification_description,
          new Intent(MainActivity.this, SnapshotNotificationActivity.class),
          R.string.activity_image_generator_snapshot_notification_url
        ));
        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_image_generator_snapshot_share_title,
          R.string.activity_image_generator_snapshot_share_description,
          new Intent(MainActivity.this, SnapshotShareActivity.class),
          R.string.activity_image_generator_snapshot_share_url
        ));
        currentCategory = R.id.nav_snapshot_image_generator;
        break;
      case R.id.nav_lab:
        exampleItemModels.add(null);
        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_lab_symbol_layer_and_mapillary_on_map_title,
          R.string.activity_lab_symbol_layer_and_mapillary_on_map_description,
          new Intent(MainActivity.this, SymbolLayerMapillaryActivity.class),
          R.string.activity_lab_symbol_layer_on_map_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_lab_pulsing_layer_opacity_color_title,
          R.string.activity_lab_pulsing_layer_opacity_color_description,
          new Intent(MainActivity.this, PulsingLayerOpacityColorActivity.class),
          R.string.activity_lab_pulsing_layer_opacity_color_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_lab_indoor_map_title,
          R.string.activity_lab_indoor_map_description,
          new Intent(MainActivity.this, IndoorMapActivity.class),
          R.string.activity_lab_indoor_map_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_lab_location_picker_title,
          R.string.activity_lab_location_picker_description,
          new Intent(MainActivity.this, LocationPickerActivity.class),
          R.string.activity_lab_location_picker_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_lab_marker_following_route_title,
          R.string.activity_lab_marker_following_route_description,
          new Intent(MainActivity.this, MarkerFollowingRouteActivity.class),
          R.string.activity_lab_marker_following_route_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_lab_space_station_location_title,
          R.string.activity_lab_space_station_location_description,
          new Intent(MainActivity.this, SpaceStationLocationActivity.class),
          R.string.activity_lab_space_station_location_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_lab_picture_in_picture_title,
          R.string.activity_lab_picture_in_picture_description,
          new Intent(MainActivity.this, PictureInPictureActivity.class),
          R.string.activity_lab_picture_in_picture_url, false, Build.VERSION_CODES.O));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_lab_rv_on_map_title,
          R.string.activity_lab_rv_on_map_description,
          new Intent(MainActivity.this, RecyclerViewOnMapActivity.class),
          R.string.activity_lab_rv_on_map_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_labs_inset_map_title,
          R.string.activity_labs_inset_map_description,
          new Intent(MainActivity.this, InsetMapActivity.class),
          R.string.activity_labs_inset_map_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_labs_gif_on_map_title,
          R.string.activity_labs_gif_on_map_description,
          new Intent(MainActivity.this, AnimatedImageGifActivity.class),
          R.string.activity_labs_gif_on_map_url, false, BuildConfig.MIN_SDK_VERSION
        ));
        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_labs_snaking_directions_route_title,
          R.string.activity_labs_snaking_directions_route_description,
          new Intent(MainActivity.this, SnakingDirectionsRouteActivity.class),
          R.string.activity_labs_snaking_directions_route_url, false, BuildConfig.MIN_SDK_VERSION
        ));
        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_dashed_line_directions_picker_title,
          R.string.activity_dashed_line_directions_picker_description,
          new Intent(MainActivity.this, DashedLineDirectionsPickerActivity.class),
          R.string.activity_dashed_line_directions_picker_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_lab_calendar_integration_title,
          R.string.activity_lab_calendar_integration_description,
          new Intent(MainActivity.this, CalendarIntegrationActivity.class),
          R.string.activity_lab_calendar_integration_url, false, BuildConfig.MIN_SDK_VERSION));
        currentCategory = R.id.nav_lab;
        break;
      case R.id.nav_dds:
        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_dds_heatmap_title,
          R.string.activity_dds_heatmap_description,
          new Intent(MainActivity.this, HeatmapActivity.class),
          R.string.activity_dds_heatmap_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_dds_multiple_heatmap_styling_title,
          R.string.activity_dds_multiple_heatmap_styling_description,
          new Intent(MainActivity.this, MultipleHeatmapStylingActivity.class),
          R.string.activity_dds_multiple_heatmap_styling_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_dds_bathymetry_title,
          R.string.activity_dds_bathymetry_description,
          new Intent(MainActivity.this, BathymetryActivity.class),
          R.string.activity_dds_bathymetry_url, false, BuildConfig.MIN_SDK_VERSION
        ));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_styles_dds_geojson_circle_layer_clusters_title,
          R.string.activity_styles_dds_geojson_circle_layer_clusters_description,
          new Intent(MainActivity.this, CircleLayerClusteringActivity.class),
          R.string.activity_styles_dds_geojson_circle_layer_clusters_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_dds_image_clustering_title,
          R.string.activity_dds_image_clustering_description,
          new Intent(MainActivity.this, ImageClusteringActivity.class),
          R.string.activity_dds_image_clustering_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_dds_style_circle_categorically_title,
          R.string.activity_dds_style_circle_categorically_description,
          new Intent(MainActivity.this, StyleCirclesCategoricallyActivity.class),
          R.string.activity_dds_style_circle_categorically_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_dds_choropleth_zoom_change_title,
          R.string.activity_dds_choropleth_zoom_change_description,
          new Intent(MainActivity.this, ChoroplethZoomChangeActivity.class),
          R.string.activity_dds_choropleth_zoom_change_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_dds_style_line_identity_property_title,
          R.string.activity_dds_style_line_identity_property_description,
          new Intent(MainActivity.this, StyleLineIdentityPropertyActivity.class),
          R.string.activity_dds_style_line_identity_property_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_dds_create_hotspots_points_title,
          R.string.activity_dds_create_hotspots_points_description,
          new Intent(MainActivity.this, CreateHotspotsActivity.class),
          R.string.activity_dds_create_hotspots_points_url
        ));
        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_dds_json_vector_mix_title,
          R.string.activity_dds_json_vector_mix_description,
          new Intent(MainActivity.this, ChoroplethJsonVectorMixActivity.class),
          R.string.activity_dds_json_vector_mix_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_dds_multiple_geometries_title,
          R.string.activity_dds_multiple_geometries_description,
          new Intent(MainActivity.this, MultipleGeometriesActivity.class),
          R.string.activity_dds_multiple_geometries_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_dds_info_window_symbol_layer_title,
          R.string.activity_dds_info_window_symbol_layer_description,
          new Intent(MainActivity.this, InfoWindowSymbolLayerActivity.class),
          R.string.activity_dds_info_window_symbol_layer_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_dds_time_lapse_rainfall_points_title,
          R.string.activity_dds_time_lapse_rainfall_points_description,
          new Intent(MainActivity.this, AddRainFallStyleActivity.class),
          R.string.activity_dds_time_lapse_rainfall_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_dds_expression_integration_title,
          R.string.activity_dds_expression_integration_description,
          new Intent(MainActivity.this, ExpressionIntegrationActivity.class),
          R.string.activity_dds_expression_integration_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_dds_satellite_land_select_title,
          R.string.activity_dds_satellite_land_select_description,
          new Intent(MainActivity.this, SatelliteLandSelectActivity.class),
          R.string.activity_dds_satellite_land_select_url, true, BuildConfig.MIN_SDK_VERSION));

        currentCategory = R.id.nav_dds;
        break;
      default:
        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_basic_simple_mapview_title,
          R.string.activity_basic_simple_mapview_description,
          new Intent(MainActivity.this, SimpleMapViewActivity.class),
          R.string.activity_basic_simple_mapview_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_basic_support_map_frag_title,
          R.string.activity_basic_support_map_frag_description,
          new Intent(MainActivity.this, SupportMapFragmentActivity.class),
          R.string.activity_basic_support_map_frag_url, false, BuildConfig.MIN_SDK_VERSION));

        exampleItemModels.add(new ExampleItemModel(
          R.string.activity_basic_mapbox_options_title,
          R.string.activity_basic_mapbox_options_description,
          new Intent(MainActivity.this, MapboxMapOptionActivity.class),
          R.string.activity_basic_mapbox_options_url, false, BuildConfig.MIN_SDK_VERSION));

        currentCategory = R.id.nav_basics;
        break;
    }

    verifySdkVersion();
    adapter.notifyDataSetChanged();

    // Scrolls recycler view back to top.
    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
    layoutManager.scrollToPositionWithOffset(0, 0);
  }

  private void verifySdkVersion() {
    for (Iterator<ExampleItemModel> iterator = exampleItemModels.iterator(); iterator.hasNext(); ) {
      ExampleItemModel model = iterator.next();
      if (model != null && Build.VERSION.SDK_INT < model.getMinSdkVersion()) {
        iterator.remove();
      }
    }
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
    outState.putString("CURRENT_CATEGORY_TOOLBAR_TITLE", categoryTitleForToolbar);
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
