package com.mapbox.mapboxsdk.testapp;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.testapp.action.WaitAction;
import com.mapbox.mapboxsdk.testapp.utils.OnMapReadyIdlingResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResourceTimeoutException;
import androidx.test.rule.ActivityTestRule;
import timber.log.Timber;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public abstract class BaseActivityTest {

  @Rule
  public ActivityTestRule<Activity> rule = new ActivityTestRule<>(getActivityClass());
  private MapboxMap mapboxMap;
  private OnMapReadyIdlingResource idlingResource;

  @Before
  public void beforeTest() {
    try {
      Timber.e("@Before test: register idle resource");
      idlingResource = new OnMapReadyIdlingResource(rule.getActivity());
      IdlingRegistry.getInstance().register(idlingResource);
      checkViewIsDisplayed(R.id.mapView);
      mapboxMap = idlingResource.getMapboxMap();
    } catch (IdlingResourceTimeoutException idlingResourceTimeoutException) {
      Timber.e("Idling resource timed out. Couldn't not validate if map is ready.");
      throw new RuntimeException("Could not start test for " + getActivityClass().getSimpleName() + ".\n"
        + "The ViewHierarchy doesn't contain a view with resource id = R.id.mapView or \n"
        + "the Activity doesn't contain an instance variable with a name equal to mapboxMap.\n"
        + "You can resolve this issue by adding the requirements above or\n add "
        + getActivityClass().getSimpleName() + " to the scripts/exclude-activity-gen.json to blacklist"
        + " the Activity from being generated.\n");
    }
  }

  protected void validateTestSetup() {
    assertTrue("Device is not connected to the Internet.", isConnected(rule.getActivity()));
    checkViewIsDisplayed(R.id.mapView);
    assertNotNull(mapboxMap);
  }

  protected MapboxMap getMapboxMap() {
    return mapboxMap;
  }

  protected abstract Class getActivityClass();

  protected void checkViewIsDisplayed(int id) {
    onView(withId(id)).check(matches(isDisplayed()));
  }

  protected void waitAction() {
    waitAction(500);
  }

  protected void waitAction(long waitTime) {
    onView(withId(R.id.mapView)).perform(new WaitAction(waitTime));
  }

  static boolean isConnected(Context context) {
    ConnectivityManager connectivityManager
      = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
  }

  @After
  public void afterTest() {
    Timber.e("@After test: unregister idle resource");
    IdlingRegistry.getInstance().unregister(idlingResource);
  }
}

