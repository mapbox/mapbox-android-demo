package com.mapbox.mapboxandroiddemo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.perf.metrics.AddTrace;
import com.mapbox.mapboxandroiddemo.account.LandingActivity;
import com.mapbox.mapboxandroiddemo.adapter.ExampleAdapter;
import com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker;
import com.mapbox.mapboxandroiddemo.commons.FirstTimeRunChecker;
import com.mapbox.mapboxandroiddemo.examples.basics.KotlinSimpleMapViewActivity;
import com.mapbox.mapboxandroiddemo.examples.basics.KotlinSupportMapFragmentActivity;
import com.mapbox.mapboxandroiddemo.examples.basics.MapboxMapOptionActivity;
import com.mapbox.mapboxandroiddemo.examples.basics.SimpleMapViewActivity;
import com.mapbox.mapboxandroiddemo.examples.basics.SupportMapFragmentActivity;
import com.mapbox.mapboxandroiddemo.examples.camera.AnimateMapCameraActivity;
import com.mapbox.mapboxandroiddemo.examples.camera.BoundingBoxCameraActivity;
import com.mapbox.mapboxandroiddemo.examples.camera.RestrictCameraActivity;
import com.mapbox.mapboxandroiddemo.examples.camera.SlowlyRotatingCameraActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.AddRainFallStyleActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.BathymetryActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.ChoroplethJsonVectorMixActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.ChoroplethZoomChangeActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.CircleLayerClusteringActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.CircleRadiusActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.CircleToIconTransitionActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.KotlinFilterFeaturesActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.PropertyIconDeterminationActivity;
import com.mapbox.mapboxandroiddemo.examples.camera.ZoomToShowClusterLeavesActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.WithinExpressionActivity;
import com.mapbox.mapboxandroiddemo.examples.extrusions.OpacityZoomChangeExtrusionKotlinActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.DirectionsProfileToggleActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.KotlinBorderedCircleActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.CreateHotspotsActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.DrawGeojsonLineActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.DrawPolygonActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.ExpressionIntegrationActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.HeatmapActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.ImageClusteringActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.InfoWindowSymbolLayerActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.KotlinStyleCirclesCategoricallyActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.LineGradientActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.MultipleGeometriesActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.MultipleHeatmapStylingActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.PolygonHolesActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.PolygonSelectToggleActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.RevealedPolygonHoleOutlineActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.SatelliteLandSelectActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.StyleCirclesCategoricallyActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.StyleLineIdentityPropertyActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.SymbolCollisionDetectionActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.SymbolSwitchOnZoomActivity;
import com.mapbox.mapboxandroiddemo.examples.extrusions.AdjustExtrusionLightActivity;
import com.mapbox.mapboxandroiddemo.examples.extrusions.Indoor3DMapActivity;
import com.mapbox.mapboxandroiddemo.examples.extrusions.MarathonExtrusionActivity;
import com.mapbox.mapboxandroiddemo.examples.extrusions.PopulationDensityExtrusionActivity;
import com.mapbox.mapboxandroiddemo.examples.extrusions.RotationExtrusionActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.DirectionsActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.DirectionsGradientLineActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.ElevationQueryActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.GeocodingActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.IsochroneActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.IsochroneSeekbarActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.MapMatchingActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.MatrixApiActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.MultipleGeometriesDirectionsRouteActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.OptimizationActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.SimplifyPolylineActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.StaticImageActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.StraightLineDistanceMapMovementActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.StaticImageNotificationActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.TilequeryActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.TurfCirclePoiWithinFilterActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.TurfLineDistanceActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.TurfPhysicalCircleActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.TurfRingActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.TwoPersonMeetupLocationIsochroneWithinActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.AnimatedImageGifActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.AnimatedMarkerActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.BaseballSprayChartActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.BiometricFingerprintLayerUnlockActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.CalendarIntegrationActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.ChangeAttributionColorActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.DashedLineDirectionsPickerActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.HomeScreenWidgetActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.IndoorMapActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.InsetMapActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.LocationPickerActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.MagicWindowKotlinActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.MapFogBackgroundActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.MarkerFollowingRouteActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.MovingIconWithTrailingLineActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.PictureInPictureActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.PulsingLayerOpacityColorActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.RecyclerViewDirectionsActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.RecyclerViewOnMapActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.SharedPreferencesActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.SnakingDirectionsRouteActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.SpaceStationLocationActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.SpinningSymbolLayerIconActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.SymbolLayerMapillaryActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.ValueAnimatorIconAnimationActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.VibrateOnPinDropKotlinActivity;
import com.mapbox.mapboxandroiddemo.examples.location.KotlinLocationComponentActivity;
import com.mapbox.mapboxandroiddemo.examples.location.LocationChangeListeningActivity;
import com.mapbox.mapboxandroiddemo.examples.location.LocationComponentActivity;
import com.mapbox.mapboxandroiddemo.examples.location.LocationComponentBasicPulsingActivity;
import com.mapbox.mapboxandroiddemo.examples.location.LocationComponentCameraOptionsActivity;
import com.mapbox.mapboxandroiddemo.examples.location.LocationComponentCustomPulsingActivity;
import com.mapbox.mapboxandroiddemo.examples.location.LocationComponentFragmentActivity;
import com.mapbox.mapboxandroiddemo.examples.location.LocationComponentOptionsActivity;
import com.mapbox.mapboxandroiddemo.examples.offline.CacheManagementActivity;
import com.mapbox.mapboxandroiddemo.examples.offline.OfflineManagerActivity;
import com.mapbox.mapboxandroiddemo.examples.offline.SimpleOfflineMapActivity;
import com.mapbox.mapboxandroiddemo.examples.plugins.BuildingPluginActivity;
import com.mapbox.mapboxandroiddemo.examples.plugins.LocalizationPluginActivity;
import com.mapbox.mapboxandroiddemo.examples.plugins.MarkerViewPluginActivity;
import com.mapbox.mapboxandroiddemo.examples.plugins.PlaceSelectionPluginActivity;
import com.mapbox.mapboxandroiddemo.examples.plugins.PlacesPluginActivity;
import com.mapbox.mapboxandroiddemo.examples.plugins.ScalebarPluginActivity;
import com.mapbox.mapboxandroiddemo.examples.plugins.SymbolListenerActivity;
import com.mapbox.mapboxandroiddemo.examples.plugins.TrafficPluginActivity;
import com.mapbox.mapboxandroiddemo.examples.query.BuildingOutlineActivity;
import com.mapbox.mapboxandroiddemo.examples.query.ClickOnLayerActivity;
import com.mapbox.mapboxandroiddemo.examples.query.FeatureCountActivity;
import com.mapbox.mapboxandroiddemo.examples.query.FingerDrawQueryActivity;
import com.mapbox.mapboxandroiddemo.examples.query.HighlightedLineActivity;
import com.mapbox.mapboxandroiddemo.examples.query.QueryFeatureActivity;
import com.mapbox.mapboxandroiddemo.examples.query.RedoSearchInAreaActivity;
import com.mapbox.mapboxandroiddemo.examples.query.SelectBuildingActivity;
import com.mapbox.mapboxandroiddemo.examples.snapshot.SnapshotNotificationActivity;
import com.mapbox.mapboxandroiddemo.examples.snapshot.SnapshotShareActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.AddWmsSourceActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.AdjustLayerOpacityActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.BasicSymbolLayerActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.ClickToAddImageActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.ColorSwitcherActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.DefaultStyleActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.GeojsonLayerInStackActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.HillShadeActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.CircleIconToggleOnClickActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.IconSizeChangeOnClickActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.ImageSourceActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.ImageSourceTimeLapseActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.LanguageSwitchActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.LineLayerActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.LocalStyleSourceActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.MapboxStudioStyleActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.MissingIconActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.RotatingTextAnchorPositionActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.RuntimeStylingActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.SatelliteOpacityOnZoomActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.ShowHideLayersActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.TextFieldFormattingActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.TextFieldMultipleFormatsActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.TransparentBackgroundActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.VariableLabelPlacementActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.VectorSourceActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.KotlinWorldviewSwitchActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.ZoomDependentFillColorActivity;
import com.mapbox.mapboxandroiddemo.model.ExampleItemModel;
import com.mapbox.mapboxandroiddemo.utils.ItemClickSupport;
import com.mapbox.mapboxandroiddemo.utils.SettingsDialogView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.CLICKED_ON_INFO_DIALOG_NOT_NOW;
import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.CLICKED_ON_INFO_DIALOG_START_LEARNING;
import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.CLICKED_ON_INFO_MENU_ITEM;
import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.CLICKED_ON_SETTINGS_IN_NAV_DRAWER;
import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.OPENED_APP;
import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.SKIPPED_ACCOUNT_CREATION;
import static com.mapbox.mapboxandroiddemo.commons.StringConstants.FROM_LOGIN_SCREEN_MENU_ITEM_KEY;
import static com.mapbox.mapboxandroiddemo.commons.StringConstants.SKIPPED_KEY;
import static com.mapbox.mapboxandroiddemo.commons.StringConstants.TOKEN_SAVED_KEY;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
  ItemClickSupport.OnItemClickListener {
  // Used to track internal navigation to the Snapshotter section
  private static final String EXTRA_NAV = "EXTRA_NAV";
  private static final String STATE_CURRENT_CATEGORY = "STATE_CURRENT_CATEGORY";
  private static final String STATE_TOOLBAR_TITLE = "STATE_TOOLBAR_TITLE";
  private static final String STATE_SHOW_JAVA = "STATE_SHOW_JAVA";
  private static final String TAG = "MainActivity";

  private final ArrayList<ExampleItemModel> exampleItemModels = new ArrayList<>();

  private Toolbar toolbar;
  private String categoryTitleForToolbar;

  private AnalyticsTracker analytics;

  private ExampleAdapter adapter;
  private RecyclerView recyclerView;
  private TextView noExamplesTv;
  private ItemClickSupport itemClickSupport;

  private boolean loggedIn;
  private int currentCategory = R.id.nav_basics;
  private boolean showJavaExamples = true;

  @Override
  @AddTrace(name = "onCreateMainActivity")
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    loggedIn = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
      .getBoolean(TOKEN_SAVED_KEY, false);

    toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    noExamplesTv = findViewById(R.id.no_examples_tv);

    analytics = AnalyticsTracker.getInstance(this, false);

    initializeModels();

    // Create the adapter to convert the array to views
    adapter = new ExampleAdapter(this);
    // Attach the adapter to a ListView
    recyclerView = findViewById(R.id.details_list);
    if (recyclerView != null) {
      recyclerView.setHasFixedSize(true);
      recyclerView.setLayoutManager(new LinearLayoutManager(this));
      recyclerView.setAdapter(adapter);
    }
    if (savedInstanceState != null) {
      currentCategory = savedInstanceState.getInt(STATE_CURRENT_CATEGORY);
      categoryTitleForToolbar = savedInstanceState.getString(STATE_TOOLBAR_TITLE);
      showJavaExamples = savedInstanceState.getBoolean(STATE_SHOW_JAVA);
      toolbar.setTitle(categoryTitleForToolbar);
      listItems(currentCategory);
    } else if (getIntent().getIntExtra(EXTRA_NAV, -1) == R.id.nav_snapshot_image_generator) {
      currentCategory = R.id.nav_snapshot_image_generator;
      listItems(R.id.nav_snapshot_image_generator);
    } else {
      listItems(R.id.nav_basics);
    }

    // Item click listener
    itemClickSupport = ItemClickSupport.addTo(recyclerView);

    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
      this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    if (drawer != null) {
      drawer.addDrawerListener(toggle);
    }
    toggle.syncState();

    NavigationView navigationView = findViewById(R.id.nav_view);
    if (navigationView != null) {
      navigationView.setNavigationItemSelectedListener(this);
      navigationView.setCheckedItem(R.id.nav_basics);
      if (loggedIn) {
        navigationView.getMenu().findItem(R.id.show_login_screen_in_nav_drawer).setVisible(false);
      }
    }


    if (loggedIn) {
      analytics.setMapboxUsername();
      analytics.viewedScreen(MainActivity.class.getSimpleName(), loggedIn);
      checkForFirstTimeOpen();
    } else {
      analytics.trackEvent(SKIPPED_ACCOUNT_CREATION, false);
      PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
        .putBoolean(SKIPPED_KEY, true)
        .apply();
    }
    analytics.trackEvent(OPENED_APP, loggedIn);
  }

  @Override
  public void onItemClicked(RecyclerView recyclerView, int position, View view) {
    ExampleItemModel model = adapter.getItemAt(position);

    // in case it's an info tile
    if (model != null) {

      if (showJavaExamples) {
        startActivity(model.getJavaActivity());
      } else {
        startActivity(model.getKotlinActivity());
      }

      analytics.clickedOnIndividualExample(getString(model.getTitle()), loggedIn);
      analytics.viewedScreen(getString(model.getTitle()), loggedIn);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    itemClickSupport.setOnItemClickListener(this);
  }

  @Override
  protected void onPause() {
    super.onPause();
    itemClickSupport.setOnItemClickListener(null);
  }

  @Override
  public void onBackPressed() {
    DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
    if (id == R.id.show_login_screen_in_nav_drawer) {
      Intent intent = new Intent(this, LandingActivity.class);
      intent.putExtra(FROM_LOGIN_SCREEN_MENU_ITEM_KEY, true);
      this.startActivity(intent);
    }
    if (id == R.id.settings_in_nav_drawer) {
      buildSettingsDialog();
    }

    if (id == R.id.share_app_in_nav_drawer) {
      shareApp();
    }

    if (id != currentCategory && id != R.id.settings_in_nav_drawer) {
      currentCategory = id;
      listItems(id);
      categoryTitleForToolbar = item.getTitle().toString();
      toolbar.setTitle(categoryTitleForToolbar);
      analytics.clickedOnNavDrawerSection(
        item.getTitle().toString(), loggedIn);
    }

    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    if (drawer != null) {
      drawer.closeDrawer(GravityCompat.START);
    }
    return true;
  }

  @AddTrace(name = "listItems")
  private void listItems(int id) {
    List<ExampleItemModel> models = new ArrayList<>();
    for (ExampleItemModel model : exampleItemModels) {
      if (model.getCategoryId() == id && verifySdkVersion(model)) {
        if ((showJavaExamples && model.getJavaActivity() != null)
          || !showJavaExamples && model.getKotlinActivity() != null) {
          models.add(model);
        }
      }

    }

    adapter.updateDataSet(models, currentCategory);

    // Scrolls recycler view back to top.
    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
    layoutManager.scrollToPositionWithOffset(0, 0);

    noExamplesTv.setVisibility(models.size() == 0 ? View.VISIBLE : View.GONE);
  }

  private boolean verifySdkVersion(ExampleItemModel model) {
    return model == null || Build.VERSION.SDK_INT >= model.getMinSdkVersion();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate toolbar items
    getMenuInflater().inflate(R.menu.menu_activity_main, menu);
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    MenuItem item = menu.findItem(R.id.action_show_other_language);
    if (showJavaExamples) {
      item.setTitle(R.string.examples_language_kotlin);
    } else {
      item.setTitle(R.string.examples_language_java);
    }
    return super.onPrepareOptionsMenu(menu);
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
    } else if (id == R.id.action_show_other_language) {
      if (showJavaExamples) {
        setExamplesLanguage(false);
        item.setTitle(R.string.examples_language_java);
      } else {
        setExamplesLanguage(true);
        item.setTitle(R.string.examples_language_kotlin);
      }
    }
    return super.onOptionsItemSelected(item);
  }

  private void setExamplesLanguage(boolean showJava) {
    showJavaExamples = showJava;
    listItems(currentCategory);
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
    Switch analyticsOptOutSwitch = customView.findViewById(R.id.analytics_opt_out_switch);
    Switch alwaysShowLandingSwitch = customView.findViewById(R.id.login_or_create_account_switch);
    analyticsOptOutSwitch.setChecked(!analytics.isAnalyticsEnabled());

    final SettingsDialogView dialogView = new SettingsDialogView(customView,
      this, analyticsOptOutSwitch, alwaysShowLandingSwitch, analytics, loggedIn);

    dialogView.buildDialog();

    Button logOutOfMapboxAccountButton = customView.findViewById(R.id.log_out_of_account_button);

    if (!loggedIn) {
      logOutOfMapboxAccountButton.setVisibility(View.GONE);
    } else {
      logOutOfMapboxAccountButton.setOnClickListener(view -> {
        dialogView.logOut(loggedIn);
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
      Timber.d(exception, "shareApp: exception = %s", exception.getMessage());
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(STATE_CURRENT_CATEGORY, currentCategory);
    outState.putString(STATE_TOOLBAR_TITLE, categoryTitleForToolbar);
    outState.putBoolean(STATE_SHOW_JAVA, showJavaExamples);
  }

  private void initializeModels() {
    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_styles,
      R.string.activity_styles_default_title,
      R.string.activity_styles_default_description,
      new Intent(MainActivity.this, DefaultStyleActivity.class),
      null,
      R.string.activity_styles_default_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_styles,
      R.string.activity_styles_basic_symbol_layer_title,
      R.string.activity_styles_basic_symbol_layer_description,
      new Intent(MainActivity.this, BasicSymbolLayerActivity.class),
      null,
      R.string.activity_styles_symbol_layer_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_styles,
      R.string.activity_styles_symbol_icon_onclick_size_change_title,
      R.string.activity_styles_symbol_icon_onclick_size_change_description,
      new Intent(MainActivity.this, IconSizeChangeOnClickActivity.class),
      null,
      R.string.activity_styles_symbol_icon_onclick_size_change_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_styles,
      R.string.activity_styles_line_layer_title,
      R.string.activity_styles_line_layer_description,
      new Intent(MainActivity.this, LineLayerActivity.class),
      null,
      R.string.activity_styles_line_layer_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_styles,
      R.string.activity_styles_color_switcher_title,
      R.string.activity_styles_color_switcher_description,
      new Intent(MainActivity.this, ColorSwitcherActivity.class),
      null,
      R.string.activity_styles_color_switcher_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_styles,
      R.string.activity_styles_vector_source_title,
      R.string.activity_styles_vector_source_description,
      new Intent(MainActivity.this, VectorSourceActivity.class),
      null,
      R.string.activity_styles_vector_source_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_styles,
      R.string.activity_styles_add_wms_source_title,
      R.string.activity_styles_add_wms_source_description,
      new Intent(MainActivity.this, AddWmsSourceActivity.class),
      null,
      R.string.activity_styles_add_wms_source_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_styles,
      R.string.activity_styles_geojson_layer_in_stack_title,
      R.string.activity_styles_geojson_layer_in_stack_description,
      new Intent(MainActivity.this, GeojsonLayerInStackActivity.class),
      null,
      R.string.activity_styles_geojson_layer_in_stack_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_styles,
      R.string.activity_styles_adjust_layer_opacity_title,
      R.string.activity_styles_adjust_layer_opacity_description,
      new Intent(MainActivity.this, AdjustLayerOpacityActivity.class),
      null,
      R.string.activity_styles_adjust_layer_opacity_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_styles,
      R.string.activity_styles_zoom_dependent_fill_color_title,
      R.string.activity_styles_zoom_dependent_fill_color_description,
      new Intent(MainActivity.this, ZoomDependentFillColorActivity.class),
      null,
      R.string.activity_styles_zoom_dependent_fill_color_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_styles,
      R.string.activity_styles_language_switch_title,
      R.string.activity_styles_language_switch_description,
      new Intent(MainActivity.this, LanguageSwitchActivity.class),
      null,
      R.string.activity_styles_language_switch_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_styles,
      R.string.activity_styles_show_hide_layer_title,
      R.string.activity_styles_show_hide_layer_description,
      new Intent(MainActivity.this, ShowHideLayersActivity.class),
      null,
      R.string.activity_styles_show_hide_layer_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_styles,
      R.string.activity_styles_mapbox_studio_title,
      R.string.activity_styles_mapbox_studio_description,
      new Intent(MainActivity.this, MapboxStudioStyleActivity.class),
      null,
      R.string.activity_styles_mapbox_studio_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_styles,
      R.string.activity_styles_local_style_or_raster_source_title,
      R.string.activity_styles_local_style_or_raster_source_description,
      new Intent(MainActivity.this, LocalStyleSourceActivity.class),
      null,
      R.string.activity_styles_local_style_or_raster_source_url, false, BuildConfig.MIN_SDK_VERSION
    ));
    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_styles,
      R.string.activity_style_image_source_title,
      R.string.activity_style_image_source_description,
      new Intent(MainActivity.this, ImageSourceActivity.class),
      null,
      R.string.activity_style_image_source_url, false, BuildConfig.MIN_SDK_VERSION
    ));
    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_styles,
      R.string.activity_style_image_source_time_lapse_title,
      R.string.activity_style_image_source_time_lapse_description,
      new Intent(MainActivity.this, ImageSourceTimeLapseActivity.class),
      null,
      R.string.activity_style_image_source_time_lapse_url, false, BuildConfig.MIN_SDK_VERSION
    ));
    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_styles,
      R.string.activity_styles_hillshade_title,
      R.string.activity_style_hillshade_description,
      new Intent(MainActivity.this, HillShadeActivity.class),
      null,
      R.string.activity_style_hillshade_url, false, BuildConfig.MIN_SDK_VERSION
    ));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_styles,
      R.string.activity_styles_text_field_multiple_formats_title,
      R.string.activity_styles_text_field_multiple_formats_description,
      new Intent(MainActivity.this, TextFieldMultipleFormatsActivity.class),
      null,
      R.string.activity_styles_text_field_multiple_formats_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_styles,
      R.string.activity_styles_transparent_background_title,
      R.string.activity_styles_transparent_background_description,
      new Intent(MainActivity.this, TransparentBackgroundActivity.class),
      null,
      R.string.activity_styles_transparent_background_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_styles,
      R.string.activity_styles_click_to_add_image_title,
      R.string.activity_styles_click_to_add_image_description,
      new Intent(MainActivity.this, ClickToAddImageActivity.class),
      null,
      R.string.activity_styles_click_to_add_image_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_styles,
      R.string.activity_styles_rotating_anchor_text_title,
      R.string.activity_styles_rotating_anchor_text_description,
      new Intent(MainActivity.this, RotatingTextAnchorPositionActivity.class),
      null,
      R.string.activity_styles_rotating_anchor_text_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_styles,
      R.string.activity_styles_satellite_opacity_on_zoom_title,
      R.string.activity_style_satellite_opacity_on_zoom_description,
      new Intent(MainActivity.this, SatelliteOpacityOnZoomActivity.class),
      null,
      R.string.activity_style_satellite_opacity_on_zoom_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_styles,
      R.string.activity_styles_text_field_formatting_title,
      R.string.activity_styles_text_field_formatting_description,
      new Intent(MainActivity.this, TextFieldFormattingActivity.class),
      null,
      R.string.activity_styles_text_field_formatting_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_styles,
      R.string.activity_styles_missing_icon_title,
      R.string.activity_styles_missing_icon_description,
      new Intent(MainActivity.this, MissingIconActivity.class),
      null,
      R.string.activity_styles_missing_icon_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_styles,
      R.string.activity_styles_variable_label_placement_title,
      R.string.activity_styles_variable_label_placement_description,
      new Intent(MainActivity.this, VariableLabelPlacementActivity.class),
            null,
       R.string.activity_styles_variable_label_placement_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_styles,
      R.string.activity_styles_worldview_switch_title,
      R.string.activity_styles_worldview_switch_description,
      null,
      new Intent(MainActivity.this, KotlinWorldviewSwitchActivity.class),
      R.string.activity_styles_worldview_switch_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_styles,
      R.string.activity_styles_runtime_styling_title,
      R.string.activity_styles_runtime_styling_description,
      new Intent(MainActivity.this, RuntimeStylingActivity.class),
      null,
      R.string.activity_styles_runtime_styling_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_extrusions,
      R.string.activity_extrusions_population_density_extrusions_title,
      R.string.activity_extrusions_population_density_extrusions_description,
      new Intent(MainActivity.this, PopulationDensityExtrusionActivity.class),
      null,
      R.string.activity_extrusions_population_density_extrusions_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_extrusions,
      R.string.activity_extrusions_catalina_marathon_extrusions_title,
      R.string.activity_extrusions_catalina_marathon_extrusions_description,
      new Intent(MainActivity.this, MarathonExtrusionActivity.class),
      null,
      R.string.activity_extrusions_catalina_marathon_extrusions_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_extrusions,
      R.string.activity_extrusions_adjust_extrusions_title,
      R.string.activity_extrusions_adjust_extrusions_description,
      new Intent(MainActivity.this, AdjustExtrusionLightActivity.class),
      null,
      R.string.activity_extrusions_adjust_extrusions_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_extrusions,
      R.string.activity_extrusions_indoor_3d_title,
      R.string.activity_extrusions_indoor_3d_description,
      new Intent(MainActivity.this, Indoor3DMapActivity.class),
      null,
      R.string.activity_extrusions_indoor_3d_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_extrusions,
      R.string.activity_extrusions_rotate_extrusions_title,
      R.string.activity_extrusions_rotate_extrusions_description,
      new Intent(MainActivity.this, RotationExtrusionActivity.class),
      null,
      R.string.activity_extrusions_rotate_extrusions_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
        R.id.nav_extrusions,
        R.string.activity_extrusions_zoom_opacity_change_title,
        R.string.activity_extrusions_zoom_opacity_change_description,
        null,
        new Intent(MainActivity.this, OpacityZoomChangeExtrusionKotlinActivity.class),
        R.string.activity_extrusions_zoom_opacity_change_url, true, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_plugins,
      R.string.activity_plugins_traffic_plugin_title,
      R.string.activity_plugins_traffic_plugin_description,
      new Intent(MainActivity.this, TrafficPluginActivity.class),
      null,
      R.string.activity_plugins_traffic_plugin_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_plugins,
      R.string.activity_plugins_building_plugin_title,
      R.string.activity_plugins_building_plugin_description,
      new Intent(MainActivity.this, BuildingPluginActivity.class),
      null,
      R.string.activity_plugins_building_plugin_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_plugins,
      R.string.activity_plugins_places_plugin_title, R.string.activity_plugins_places_plugin_description,
      new Intent(MainActivity.this, PlacesPluginActivity.class),
      null,
      R.string.activity_plugins_places_plugin_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_plugins,
      R.string.activity_plugins_symbol_listener_title, R.string.activity_plugins_symbol_listener_description,
      new Intent(MainActivity.this, SymbolListenerActivity.class),
      null,
      R.string.activity_plugins_symbol_listener_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_plugins,
      R.string.activity_plugins_localization_plugin_title,
      R.string.activity_plugins_localization_plugin_description,
      new Intent(MainActivity.this, LocalizationPluginActivity.class),
      null,
      R.string.activity_plugins_localization_plugin_url, false, BuildConfig.MIN_SDK_VERSION)
    );

    exampleItemModels.add(new ExampleItemModel(
        R.id.nav_plugins,
        R.string.activity_plugins_place_picker_plugin_title,
        R.string.activity_plugins_place_picker_plugin_description,
        new Intent(MainActivity.this, PlaceSelectionPluginActivity.class),
        null,
        R.string.activity_plugins_place_picker_plugin_url, false, BuildConfig.MIN_SDK_VERSION)
    );

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_plugins,
      R.string.activity_plugins_markerview_plugin_title,
      R.string.activity_plugins_markerview_plugin_description,
      new Intent(MainActivity.this, MarkerViewPluginActivity.class),
      null,
      R.string.activity_plugins_markerview_plugin_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_plugins,
      R.string.activity_plugins_scalebar_plugin_title,
      R.string.activity_plugins_scalebar_plugin_description,
      new Intent(MainActivity.this, ScalebarPluginActivity.class),
      null,
      R.string.activity_plugins_scalebar_plugin_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_dds,
      R.string.activity_dds_polygon_title,
      R.string.activity_dds_polygon_description,
      new Intent(MainActivity.this, DrawPolygonActivity.class),
      null,
      R.string.activity_dds_polygon_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_location,
      R.string.activity_location_location_component_title,
      R.string.activity_location_location_component_description,
      new Intent(MainActivity.this, LocationComponentActivity.class),
      new Intent(MainActivity.this, KotlinLocationComponentActivity.class),
      R.string.activity_location_location_component_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_location,
      R.string.activity_location_user_location_map_frag_title,
      R.string.activity_location_user_location_map_frag_plugin_description,
      new Intent(MainActivity.this, LocationComponentFragmentActivity.class),
      null,
      R.string.activity_location_user_location_fragment_plugin_url, false, BuildConfig.MIN_SDK_VERSION)
    );

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_location,
      R.string.activity_location_location_component_options_title,
      R.string.activity_location_location_component_options_description,
      new Intent(MainActivity.this, LocationComponentOptionsActivity.class),
      null,
      R.string.activity_location_location_component_options_url, false, BuildConfig.MIN_SDK_VERSION)
    );

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_location,
      R.string.activity_location_location_component_camera_options_title,
      R.string.activity_location_location_component_camera_options_description,
      new Intent(MainActivity.this, LocationComponentCameraOptionsActivity.class),
      null,
      R.string.activity_location_location_component_camera_options_url, false, BuildConfig.MIN_SDK_VERSION)
    );

    exampleItemModels.add(new ExampleItemModel(
        R.id.nav_location,
        R.string.activity_location_location_change_listening_title,
        R.string.activity_location_location_change_listening_description,
        new Intent(MainActivity.this, LocationChangeListeningActivity.class),
        null,
        R.string.activity_location_location_change_listening_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
        R.id.nav_location,
        R.string.activity_location_location_basic_pulsing_title,
        R.string.activity_location_location_basic_pulsing_description,
        new Intent(MainActivity.this, LocationComponentBasicPulsingActivity.class),
        null,
        R.string.activity_location_location_basic_pulsing_url, true, BuildConfig.MIN_SDK_VERSION)
    );

    exampleItemModels.add(new ExampleItemModel(
        R.id.nav_location,
        R.string.activity_location_location_custom_pulsing_title,
        R.string.activity_location_location_custom_pulsing_description,
        new Intent(MainActivity.this, LocationComponentCustomPulsingActivity.class),
        null,
        R.string.activity_location_location_custom_pulsing_url, true, BuildConfig.MIN_SDK_VERSION)
    );

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_camera,
      R.string.activity_camera_animate_title,
      R.string.activity_camera_animate_description,
      new Intent(MainActivity.this, AnimateMapCameraActivity.class),
      null,
      R.string.activity_camera_animate_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_camera,
      R.string.activity_camera_bounding_box_title,
      R.string.activity_camera_bounding_box_description,
      new Intent(MainActivity.this, BoundingBoxCameraActivity.class),
      null,
      R.string.activity_camera_bounding_box_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_camera,
      R.string.activity_camera_restrict_title,
      R.string.activity_camera_restrict_description,
      new Intent(MainActivity.this, RestrictCameraActivity.class),
      null,
      R.string.activity_camera_restrict_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_camera,
      R.string.activity_camera_slowly_rotating_title,
      R.string.activity_camera_slowly_rotating_description,
      new Intent(MainActivity.this, SlowlyRotatingCameraActivity.class),
      null,
      R.string.activity_camera_slowly_rotating_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_camera,
      R.string.activity_camera_zoom_to_show_cluster_leaves_title,
      R.string.activity_camera_zoom_to_show_cluster_leaves_description,
      new Intent(MainActivity.this, ZoomToShowClusterLeavesActivity.class),
      null,
      R.string.activity_camera_zoom_to_show_cluster_leaves_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_offline,
      R.string.activity_offline_simple_title,
      R.string.activity_offline_simple_description,
      new Intent(MainActivity.this, SimpleOfflineMapActivity.class),
      null,
      R.string.activity_offline_simple_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_offline,
      R.string.activity_offline_manager_title,
      R.string.activity_offline_manager_description,
      new Intent(MainActivity.this, OfflineManagerActivity.class),
      null,
      R.string.activity_offline_manager_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_offline,
      R.string.activity_offline_cache_management_title,
      R.string.activity_offline_cache_management_description,
      new Intent(MainActivity.this, CacheManagementActivity.class),
      null,
      R.string.activity_offline_cache_management_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_query_map,
      R.string.activity_query_select_building_title,
      R.string.activity_query_select_building_description,
      new Intent(MainActivity.this, SelectBuildingActivity.class),
      null,
      R.string.activity_query_select_building_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_query_map,
      R.string.activity_query_feature_count_title,
      R.string.activity_query_feature_count_description,
      new Intent(MainActivity.this, FeatureCountActivity.class),
      null,
      R.string.activity_query_feature_count_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_query_map,
      R.string.activity_query_feature_title,
      R.string.activity_query_feature_description,
      new Intent(MainActivity.this, QueryFeatureActivity.class),
      null,
      R.string.activity_query_feature_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_query_map,
      R.string.activity_query_click_on_layer_title,
      R.string.activity_query_click_on_layer_description,
      new Intent(MainActivity.this, ClickOnLayerActivity.class),
      null,
      R.string.activity_query_click_on_layer_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_query_map,
      R.string.activity_query_redo_search_in_area_title,
      R.string.activity_query_redo_search_in_area_description,
      new Intent(MainActivity.this, RedoSearchInAreaActivity.class),
      null,
      R.string.activity_query_redo_search_in_area_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_query_map,
      R.string.activity_query_building_outline_title,
      R.string.activity_query_building_outline_description,
      new Intent(MainActivity.this, BuildingOutlineActivity.class),
      null,
      R.string.activity_query_building_outline_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_query_map,
      R.string.activity_query_highlighted_line_title,
      R.string.activity_query_highlighted_line_description,
      new Intent(MainActivity.this, HighlightedLineActivity.class),
      null,
      R.string.activity_query_highlighted_line_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_query_map,
      R.string.activity_lab_drag_draw_title,
      R.string.activity_lab_drag_draw_description,
      new Intent(MainActivity.this, FingerDrawQueryActivity.class),
      null,
      R.string.activity_lab_drag_draw_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_java_services,
      R.string.activity_java_services_simplify_polyline_title,
      R.string.activity_java_services_simplify_polyline_description,
      new Intent(MainActivity.this, SimplifyPolylineActivity.class),
      null,
      R.string.activity_java_services_simplify_polyline_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_java_services,
      R.string.activity_java_services_map_matching_title,
      R.string.activity_java_services_map_matching_description,
      new Intent(MainActivity.this, MapMatchingActivity.class),
      null,
      R.string.activity_java_services_map_matching_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_java_services,
      R.string.activity_java_services_directions_title,
      R.string.activity_java_services_directions_description,
      new Intent(MainActivity.this, DirectionsActivity.class),
      null,
      R.string.activity_java_services_directions_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_java_services,
      R.string.activity_java_services_optimization_title,
      R.string.activity_java_services_optimization_description,
      new Intent(MainActivity.this, OptimizationActivity.class),
      null,
      R.string.activity_java_services_optimization_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_java_services,
      R.string.activity_java_services_static_image_title,
      R.string.activity_java_services_static_image_description,
      new Intent(MainActivity.this, StaticImageActivity.class),
      null,
      R.string.activity_java_services_static_image_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_java_services,
      R.string.activity_java_services_maxtrix_api_title,
      R.string.activity_java_services_matrix_api_description,
      new Intent(MainActivity.this, MatrixApiActivity.class),
      null,
      R.string.activity_java_services_matrix_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_java_services,
      R.string.activity_java_services_geocoding_title,
      R.string.activity_java_services_geocoding_description,
      new Intent(MainActivity.this, GeocodingActivity.class),
      null,
      R.string.activity_java_services_geocoding_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_java_services,
      R.string.activity_java_services_isochrone_title,
      R.string.activity_java_services_isochrone_description,
      new Intent(MainActivity.this, IsochroneActivity.class),
      null,
      R.string.activity_java_services_isochrone_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_java_services,
      R.string.activity_java_services_isochrone_with_seekbar_title,
      R.string.activity_java_services_isochrone_with_seekbar_description,
      new Intent(MainActivity.this, IsochroneSeekbarActivity.class),
      null,
      R.string.activity_java_services_isochrone_with_seekbar_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_java_services,
      R.string.activity_java_services_tilequery_title,
      R.string.activity_java_services_tilequery_description,
      new Intent(MainActivity.this, TilequeryActivity.class),
      null,
      R.string.activity_java_services_tilequery_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_java_services,
      R.string.activity_java_services_turf_ring_title,
      R.string.activity_java_services_turf_ring_description,
      new Intent(MainActivity.this, TurfRingActivity.class),
      null,
      R.string.activity_java_services_turf_ring_url, false, BuildConfig.MIN_SDK_VERSION
    ));
    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_java_services,
      R.string.activity_java_services_turf_physical_circle_title,
      R.string.activity_java_services_turf_physical_circle_description,
      new Intent(MainActivity.this, TurfPhysicalCircleActivity.class),
      null,
      R.string.activity_java_services_turf_physical_circle_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
        R.id.nav_java_services,
        R.string.activity_java_services_turf_elevation_query_title,
        R.string.activity_java_services_turf_elevation_query_description,
        new Intent(MainActivity.this, ElevationQueryActivity.class),
        null,
        R.string.activity_java_services_turf_elevation_query_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_java_services,
      R.string.activity_java_services_turf_line_distance_title,
      R.string.activity_java_services_turf_line_distance_description,
      new Intent(MainActivity.this, TurfLineDistanceActivity.class),
      null,
      R.string.activity_java_services_turf_line_distance_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_java_services,
      R.string.activity_java_services_directions_gradient_title,
      R.string.activity_java_services_directions_gradient_description,
      new Intent(MainActivity.this, DirectionsGradientLineActivity.class),
      null,
      R.string.activity_java_services_directions_gradient_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_java_services,
      R.string.activity_java_services_multiple_geometries_from_directions_route_title,
      R.string.activity_java_services_multiple_geometries_from_directions_route_description,
      new Intent(MainActivity.this, MultipleGeometriesDirectionsRouteActivity.class),
      null,
      R.string.activity_java_services_multiple_geometries_from_directions_route_url,
      false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_java_services,
      R.string.activity_java_services_bordered_circle_title,
      R.string.activity_java_services_bordered_circle_description,
      null,
      new Intent(MainActivity.this, KotlinBorderedCircleActivity.class),
      R.string.activity_java_services_bordered_circle_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_java_services,
      R.string.activity_java_services_straight_line_distance_title,
      R.string.activity_java_services_straight_line_distance_description,
      new Intent(MainActivity.this, StraightLineDistanceMapMovementActivity.class),
      null,
      R.string.activity_java_services_straight_line_distance_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
        R.id.nav_java_services,
      R.string.activity_java_services_static_image_notification_title,
      R.string.activity_java_services_static_image_notification_description,
      new Intent(MainActivity.this, StaticImageNotificationActivity.class),
      null,
      R.string.activity_java_services_static_image_notification_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
        R.id.nav_java_services,
        R.string.activity_java_services_directions_profile_toggle_title,
        R.string.activity_java_services_directions_profile_toggle_description,
        new Intent(MainActivity.this, DirectionsProfileToggleActivity.class),
        null,
        R.string.activity_java_services_directions_profile_toggle_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
        R.id.nav_java_services,
        R.string.activity_java_services_turf_circle_poi_within_filter_title,
        R.string.activity_java_services_turf_circle_poi_within_filter_description,
        new Intent(MainActivity.this, TurfCirclePoiWithinFilterActivity.class),
        null,
        R.string.activity_java_services_turf_circle_poi_within_filter_url, true, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
        R.id.nav_java_services,
        R.string.activity_java_services_two_person_meetup_isochrone_within_title,
        R.string.activity_java_services_two_person_meetup_isochrone_within_description,
        new Intent(MainActivity.this, TwoPersonMeetupLocationIsochroneWithinActivity.class),
        null,
        R.string.activity_java_services_two_person_meetup_isochrone_within_url, true, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_snapshot_image_generator,
      R.string.activity_image_generator_snapshot_notification_title,
      R.string.activity_image_generator_snapshot_notification_description,
      new Intent(MainActivity.this, SnapshotNotificationActivity.class),
      null,
      R.string.activity_image_generator_snapshot_notification_url, false, BuildConfig.MIN_SDK_VERSION
    ));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_snapshot_image_generator,
      R.string.activity_image_generator_snapshot_share_title,
      R.string.activity_image_generator_snapshot_share_description,
      new Intent(MainActivity.this, SnapshotShareActivity.class),
      null,
      R.string.activity_image_generator_snapshot_share_url, false, BuildConfig.MIN_SDK_VERSION
    ));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_lab,
      R.string.activity_lab_animated_marker_title,
      R.string.activity_lab_animated_marker_description,
      new Intent(MainActivity.this, AnimatedMarkerActivity.class),
      null,
      R.string.activity_lab_animated_marker_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_lab,
      R.string.activity_lab_symbol_layer_and_mapillary_on_map_title,
      R.string.activity_lab_symbol_layer_and_mapillary_on_map_description,
      new Intent(MainActivity.this, SymbolLayerMapillaryActivity.class),
      null,
      R.string.activity_lab_symbol_layer_on_map_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_lab,
      R.string.activity_lab_pulsing_layer_opacity_color_title,
      R.string.activity_lab_pulsing_layer_opacity_color_description,
      new Intent(MainActivity.this, PulsingLayerOpacityColorActivity.class),
      null,
      R.string.activity_lab_pulsing_layer_opacity_color_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_lab,
      R.string.activity_lab_indoor_map_title,
      R.string.activity_lab_indoor_map_description,
      new Intent(MainActivity.this, IndoorMapActivity.class),
      null,
      R.string.activity_lab_indoor_map_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_lab,
      R.string.activity_lab_location_picker_title,
      R.string.activity_lab_location_picker_description,
      new Intent(MainActivity.this, LocationPickerActivity.class),
      null,
      R.string.activity_lab_location_picker_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_lab,
      R.string.activity_lab_marker_following_route_title,
      R.string.activity_lab_marker_following_route_description,
      new Intent(MainActivity.this, MarkerFollowingRouteActivity.class),
      null,
      R.string.activity_lab_marker_following_route_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_lab,
      R.string.activity_lab_space_station_location_title,
      R.string.activity_lab_space_station_location_description,
      new Intent(MainActivity.this, SpaceStationLocationActivity.class),
      null,
      R.string.activity_lab_space_station_location_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_lab,
      R.string.activity_lab_picture_in_picture_title,
      R.string.activity_lab_picture_in_picture_description,
      new Intent(MainActivity.this, PictureInPictureActivity.class),
      null,
      R.string.activity_lab_picture_in_picture_url, false, Build.VERSION_CODES.O));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_lab,
      R.string.activity_lab_rv_on_map_title,
      R.string.activity_lab_rv_on_map_description,
      new Intent(MainActivity.this, RecyclerViewOnMapActivity.class),
      null,
      R.string.activity_lab_rv_on_map_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_lab,
      R.string.activity_labs_inset_map_title,
      R.string.activity_labs_inset_map_description,
      new Intent(MainActivity.this, InsetMapActivity.class),
      null,
      R.string.activity_labs_inset_map_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_lab,
      R.string.activity_labs_gif_on_map_title,
      R.string.activity_labs_gif_on_map_description,
      new Intent(MainActivity.this, AnimatedImageGifActivity.class),
      null,
      R.string.activity_labs_gif_on_map_url, false, BuildConfig.MIN_SDK_VERSION
    ));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_lab,
      R.string.activity_lab_fog_background_title,
      R.string.activity_lab_fog_background_description,
      new Intent(MainActivity.this, MapFogBackgroundActivity.class),
      null,
      R.string.activity_lab_fog_background_url, false, BuildConfig.MIN_SDK_VERSION
    ));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_lab,
      R.string.activity_lab_dashed_line_directions_picker_title,
      R.string.activity_lab_dashed_line_directions_picker_description,
      new Intent(MainActivity.this, DashedLineDirectionsPickerActivity.class),
      null,
      R.string.activity_lab_dashed_line_directions_picker_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_lab,
      R.string.activity_lab_calendar_integration_title,
      R.string.activity_lab_calendar_integration_description,
      new Intent(MainActivity.this, CalendarIntegrationActivity.class),
      null,
      R.string.activity_lab_calendar_integration_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_lab,
      R.string.activity_lab_magic_window_title,
      R.string.activity_lab_magic_window_description,
      null,
      new Intent(MainActivity.this, MagicWindowKotlinActivity.class),
      R.string.activity_lab_magic_window_image_url, true, Build.VERSION_CODES.O));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_lab,
      R.string.activity_labs_snaking_directions_route_title,
      R.string.activity_labs_snaking_directions_route_description,
      new Intent(MainActivity.this, SnakingDirectionsRouteActivity.class),
      null,
      R.string.activity_labs_snaking_directions_route_url, false, BuildConfig.MIN_SDK_VERSION
    ));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_lab,
      R.string.activity_lab_home_screen_widget_title,
      R.string.activity_lab_home_screen_widget_description,
      new Intent(MainActivity.this, HomeScreenWidgetActivity.class),
      null,
      R.string.activity_lab_home_screen_widget_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_lab,
      R.string.activity_lab_animated_interpolator_icon_drop_title,
      R.string.activity_lab_animated_interpolator_icon_drop_description,
      new Intent(MainActivity.this, ValueAnimatorIconAnimationActivity.class),
      null,
      R.string.activity_lab_animated_interpolator_icon_drop_url, false, BuildConfig.MIN_SDK_VERSION
    ));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_lab,
      R.string.activity_lab_moving_icon_with_trailing_line_title,
      R.string.activity_lab_moving_icon_with_trailing_line_description,
      new Intent(MainActivity.this, MovingIconWithTrailingLineActivity.class),
      null,
      R.string.activity_lab_moving_icon_with_trailing_line_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
        R.id.nav_lab,
        R.string.activity_lab_rv_directions_title,
        R.string.activity_lab_rv_directions_description,
        new Intent(MainActivity.this, RecyclerViewDirectionsActivity.class),
        null,
        R.string.activity_lab_rv_directions_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_lab,
      R.string.activity_lab_spinning_icon_title,
      R.string.activity_lab_spinning_icon_description,
      new Intent(MainActivity.this, SpinningSymbolLayerIconActivity.class),
      null,
      R.string.activity_lab_spinning_icon_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_lab,
      R.string.activity_lab_change_attribution_color_title,
      R.string.activity_lab_change_attribution_color_description,
      new Intent(MainActivity.this, ChangeAttributionColorActivity.class),
      null,
      R.string.activity_lab_change_attribution_color_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
        R.id.nav_lab,
        R.string.activity_lab_shared_preferences_title,
        R.string.activity_lab_shared_preferences_description,
        new Intent(MainActivity.this, SharedPreferencesActivity.class),
        null,
        R.string.activity_lab_shared_preferences_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_lab,
      R.string.activity_lab_biometric_fingerprint_title,
      R.string.activity_lab_biometric_fingerprint_description,
      new Intent(MainActivity.this, BiometricFingerprintLayerUnlockActivity.class),
      null,
      R.string.activity_lab_biometric_fingerprint_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
        R.id.nav_lab,
        R.string.activity_lab_baseball_spray_chart_title,
        R.string.activity_lab_baseball_spray_chart_description,
        new Intent(MainActivity.this, BaseballSprayChartActivity.class),
        null,
        R.string.activity_lab_baseball_spray_chart_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
        R.id.nav_lab,
        R.string.activity_lab_vibrate_on_pin_drop_title,
        R.string.activity_lab_vibrate_on_pin_drop_description,
        null,
        new Intent(MainActivity.this, VibrateOnPinDropKotlinActivity.class),
        R.string.activity_lab_vibrate_on_pin_drop_url, true, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_dds,
      R.string.activity_dds_geojson_line_title,
      R.string.activity_dds_geojson_line_description,
      new Intent(MainActivity.this, DrawGeojsonLineActivity.class),
      null,
      R.string.activity_dds_geojson_line_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_dds,
      R.string.activity_dds_polygon_holes_title,
      R.string.activity_dds_polygon_holes_description,
      new Intent(MainActivity.this, PolygonHolesActivity.class),
      null,
      R.string.activity_dds_polygon_holes_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_dds,
      R.string.activity_dds_heatmap_title,
      R.string.activity_dds_heatmap_description,
      new Intent(MainActivity.this, HeatmapActivity.class),
      null,
      R.string.activity_dds_heatmap_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_dds,
      R.string.activity_dds_multiple_heatmap_styling_title,
      R.string.activity_dds_multiple_heatmap_styling_description,
      new Intent(MainActivity.this, MultipleHeatmapStylingActivity.class),
      null,
      R.string.activity_dds_multiple_heatmap_styling_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_dds,
      R.string.activity_dds_bathymetry_title,
      R.string.activity_dds_bathymetry_description,
      new Intent(MainActivity.this, BathymetryActivity.class),
      null,
      R.string.activity_dds_bathymetry_url, false, BuildConfig.MIN_SDK_VERSION
    ));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_dds,
      R.string.activity_styles_dds_geojson_circle_layer_clusters_title,
      R.string.activity_styles_dds_geojson_circle_layer_clusters_description,
      new Intent(MainActivity.this, CircleLayerClusteringActivity.class),
      null,
      R.string.activity_styles_dds_geojson_circle_layer_clusters_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_dds,
      R.string.activity_dds_image_clustering_title,
      R.string.activity_dds_image_clustering_description,
      new Intent(MainActivity.this, ImageClusteringActivity.class),
      null,
      R.string.activity_dds_image_clustering_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_dds,
      R.string.activity_dds_style_circle_categorically_title,
      R.string.activity_dds_style_circle_categorically_description,
      new Intent(MainActivity.this, StyleCirclesCategoricallyActivity.class),
      null,
      R.string.activity_dds_style_circle_categorically_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_dds,
      R.string.activity_dds_choropleth_zoom_change_title,
      R.string.activity_dds_choropleth_zoom_change_description,
      new Intent(MainActivity.this, ChoroplethZoomChangeActivity.class),
      null,
      R.string.activity_dds_choropleth_zoom_change_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_dds,
      R.string.activity_dds_style_line_identity_property_title,
      R.string.activity_dds_style_line_identity_property_description,
      new Intent(MainActivity.this, StyleLineIdentityPropertyActivity.class),
      null,
      R.string.activity_dds_style_line_identity_property_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_dds,
      R.string.activity_dds_line_gradient_title,
      R.string.activity_dds_line_gradient_description,
      new Intent(MainActivity.this, LineGradientActivity.class),
      null,
      R.string.activity_dds_line_gradient_url, false, BuildConfig.MIN_SDK_VERSION));


    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_dds,
      R.string.activity_dds_create_hotspots_points_title,
      R.string.activity_dds_create_hotspots_points_description,
      new Intent(MainActivity.this, CreateHotspotsActivity.class),
      null,
      R.string.activity_dds_create_hotspots_points_url, false, BuildConfig.MIN_SDK_VERSION
    ));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_dds,
      R.string.activity_dds_json_vector_mix_title,
      R.string.activity_dds_json_vector_mix_description,
      new Intent(MainActivity.this, ChoroplethJsonVectorMixActivity.class),
      null,
      R.string.activity_dds_json_vector_mix_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_dds,
      R.string.activity_dds_multiple_geometries_title,
      R.string.activity_dds_multiple_geometries_description,
      new Intent(MainActivity.this, MultipleGeometriesActivity.class),
      null,
      R.string.activity_dds_multiple_geometries_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_dds,
      R.string.activity_dds_info_window_symbol_layer_title,
      R.string.activity_dds_info_window_symbol_layer_description,
      new Intent(MainActivity.this, InfoWindowSymbolLayerActivity.class),
      null,
      R.string.activity_dds_info_window_symbol_layer_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_dds,
      R.string.activity_dds_time_lapse_rainfall_points_title,
      R.string.activity_dds_time_lapse_rainfall_points_description,
      new Intent(MainActivity.this, AddRainFallStyleActivity.class),
      null,
      R.string.activity_dds_time_lapse_rainfall_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_dds,
      R.string.activity_dds_expression_integration_title,
      R.string.activity_dds_expression_integration_description,
      new Intent(MainActivity.this, ExpressionIntegrationActivity.class),
      null,
      R.string.activity_dds_expression_integration_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_dds,
      R.string.activity_dds_style_kotlin_circle_categorically_title,
      R.string.activity_dds_kotlin_style_circle_categorically_description,
      null,
      new Intent(MainActivity.this, KotlinStyleCirclesCategoricallyActivity.class),
      R.string.activity_dds_style_circle_categorically_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_dds,
      R.string.activity_dds_satellite_land_select_title,
      R.string.activity_dds_satellite_land_select_description,
      new Intent(MainActivity.this, SatelliteLandSelectActivity.class),
      null,
      R.string.activity_dds_satellite_land_select_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
        R.id.nav_dds,
        R.string.activity_dds_symbol_zoom_switch_title,
        R.string.activity_dds_symbol_zoom_switch_description,
        new Intent(MainActivity.this, SymbolSwitchOnZoomActivity.class),
        null,
        R.string.activity_dds_symbol_zoom_switch_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_dds,
      R.string.activity_dds_polygon_select_toggle_title,
      R.string.activity_dds_polygon_select_toggle_description,
      null,
      new Intent(MainActivity.this, PolygonSelectToggleActivity.class),
      R.string.activity_dds_polygon_select_toggle_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_dds,
      R.string.activity_dds_symbol_collision_detection_title,
      R.string.activity_dds_symbol_collision_detection_description,
      new Intent(MainActivity.this, SymbolCollisionDetectionActivity.class),
      null,
      R.string.activity_dds_symbol_collision_detection_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_dds,
      R.string.activity_dds_polygon_revealed_hole_outline_title,
      R.string.activity_dds_polygon_revealed_hole_outline_description,
      new Intent(MainActivity.this, RevealedPolygonHoleOutlineActivity.class),
      null,
      R.string.activity_dds_polygon_revealed_hole_outline_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_dds,
      R.string.activity_dds_circle_radius_title,
      R.string.activity_dds_circle_radius_description,
      new Intent(MainActivity.this, CircleRadiusActivity.class),
      null,
      R.string.activity_dds_circle_radius_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_dds,
      R.string.activity_dds_circle_to_icon_smooth_transition_title,
      R.string.activity_dds_circle_to_icon_smooth_transition_description,
      new Intent(MainActivity.this, CircleToIconTransitionActivity.class),
      null,
      R.string.activity_dds_circle_to_icon_smooth_transition_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_dds,
      R.string.activity_dds_property_icon_switch_title,
      R.string.activity_dds_property_icon_switch_description,
      new Intent(MainActivity.this, PropertyIconDeterminationActivity.class),
      null,
      R.string.activity_dds_property_icon_switch_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_dds,
      R.string.activity_dds_circle_icon_toggle_on_click_title,
      R.string.activity_dds_circle_icon_toggle_on_click_description,
      new Intent(MainActivity.this, CircleIconToggleOnClickActivity.class),
      null,
      R.string.activity_dds_circle_icon_toggle_on_click_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
        R.id.nav_dds,
        R.string.activity_dds_within_expression_title,
        R.string.activity_dds_within_expression_description,
        null,
        new Intent(MainActivity.this, WithinExpressionActivity.class),
        R.string.activity_dds_within_expression_url, true, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_dds,
      R.string.activity_dds_filter_features_title,
      R.string.activity_dds_filter_features_description,
      null,
      new Intent(MainActivity.this, KotlinFilterFeaturesActivity.class),
      R.string.activity_dds_filter_features_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_basics,
      R.string.activity_basic_simple_mapview_title,
      R.string.activity_basic_simple_mapview_description,
      new Intent(MainActivity.this, SimpleMapViewActivity.class),
      new Intent(MainActivity.this, KotlinSimpleMapViewActivity.class),
      R.string.activity_basic_simple_mapview_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_basics,
      R.string.activity_basic_support_map_frag_title,
      R.string.activity_basic_support_map_frag_description,
      new Intent(MainActivity.this, SupportMapFragmentActivity.class),
      null,
      R.string.activity_basic_support_map_frag_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_basics,
      R.string.activity_basic_mapbox_options_title,
      R.string.activity_basic_mapbox_options_description,
      new Intent(MainActivity.this, MapboxMapOptionActivity.class),
      null,
      R.string.activity_basic_mapbox_options_url, false, BuildConfig.MIN_SDK_VERSION));

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_basics,
      R.string.activity_basic_kotlin_support_map_frag_title,
      R.string.activity_basic_kotlin_support_map_frag_description,
      null,
      new Intent(MainActivity.this, KotlinSupportMapFragmentActivity.class),
      R.string.activity_basic_kotlin_support_map_frag_url, false, BuildConfig.MIN_SDK_VERSION));
  }
}
