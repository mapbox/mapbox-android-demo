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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.mapbox.mapboxandroiddemo.adapter.ExampleAdapter;
import com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker;
import com.mapbox.mapboxandroiddemo.commons.FirstTimeRunChecker;
import com.mapbox.mapboxandroiddemo.examples.basics.SimpleMapViewActivity;
import com.mapbox.mapboxandroiddemo.model.ExampleItemModel;
import com.mapbox.mapboxandroiddemo.utils.ItemClickSupport;
import com.mapbox.mapboxandroiddemo.utils.SettingsDialogView;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

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
  private static final String EXTRA_NAV = "EXTRA_NAV";
  private static final String STATE_CURRENT_CATEGORY = "STATE_CURRENT_CATEGORY";
  private static final String STATE_TOOLBAR_TITLE = "STATE_TOOLBAR_TITLE";
  private static final String STATE_SHOW_JAVA = "STATE_SHOW_JAVA";

  private final ArrayList<ExampleItemModel> exampleItemModels = new ArrayList<>();

  private Toolbar toolbar;
  private String categoryTitleForToolbar;

  private AnalyticsTracker analytics;

  private ExampleAdapter adapter;
  private RecyclerView recyclerView;
  private TextView noExamplesTv;

  private boolean loggedIn;
  private int currentCategory = R.id.nav_basics;
  private boolean showJavaExamples = true;
  private boolean isChinaBuild;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    isChinaBuild = BuildConfig.FLAVOR.contains("nogpservices");

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
    ItemClickSupport.addTo(recyclerView).setOnItemClickListener((recyclerView, position, view) -> {
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
    });

    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
      this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    if (drawer != null) {
      drawer.addDrawerListener(toggle);
    }
    toggle.syncState();

    NavigationView navigationView = findViewById(R.id.nav_view);
    if (navigationView != null) {
      if (!isChinaBuild) {
        Menu menu = navigationView.getMenu();
        MenuItem chinaMenuItem = menu.findItem(R.id.nav_china);
        chinaMenuItem.setVisible(false);
      }
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
      analytics.trackEvent(SKIPPED_ACCOUNT_CREATION, false);
      PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
        .putBoolean(SKIPPED_KEY, true)
        .apply();
    }
    analytics.trackEvent(OPENED_APP, loggedIn);
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
        .onPositive((dialog, which) -> {
          analytics.trackEvent(CLICKED_ON_INFO_DIALOG_START_LEARNING, false);
          Intent intent = new Intent(Intent.ACTION_VIEW);
          intent.setData(Uri.parse("https://mapbox.com/android-sdk"));
          startActivity(intent);
        })
        .setNegativeText(getString(R.string.info_dialog_negative_button_text))
        .onNegative((dialog, which) -> analytics.trackEvent(CLICKED_ON_INFO_DIALOG_NOT_NOW, loggedIn))
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
    analyticsOptOutSwitch.setChecked(!analytics.isAnalyticsEnabled());

    final SettingsDialogView dialogView = new SettingsDialogView(customView,
      this, analyticsOptOutSwitch, analytics, loggedIn);

    dialogView.buildDialog();

    Button logOutOfMapboxAccountButton = customView.findViewById(R.id.log_out_of_account_button);

    if (!loggedIn) {
      logOutOfMapboxAccountButton.setVisibility(View.GONE);
    } else {
      logOutOfMapboxAccountButton.setOnClickListener(view -> dialogView.logOut(loggedIn));
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
      R.id.nav_basics,
      R.string.activity_basic_simple_mapview_title,
      R.string.activity_basic_simple_mapview_description,
      new Intent(MainActivity.this, SimpleMapViewActivity.class),
      null,
      R.string.activity_basic_simple_mapview_url, false, BuildConfig.MIN_SDK_VERSION));

  }
}
