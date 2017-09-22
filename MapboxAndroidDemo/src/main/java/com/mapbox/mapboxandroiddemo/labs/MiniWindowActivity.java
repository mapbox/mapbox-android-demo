package com.mapbox.mapboxandroiddemo.labs;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.utils.MapFragmentUtils;

public class MiniWindowActivity extends AppCompatActivity implements MapboxMap.OnCameraMoveListener {

  private MapView mapView;
  private MapboxMap mainLargeMapboxMap;
  private static final LatLngBounds AUSTRALIA_BOUNDS = new LatLngBounds.Builder()
    .include(new LatLng(-9.136343, 109.372126))
    .include(new LatLng(-44.640488, 158.590484))
    .build();
  private OnMapMovedFragmentInterface onMapMovedFragmentInterfaceListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_mini_window);

    mapView = (MapView) findViewById(R.id.main_mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {
        MiniWindowActivity.this.mainLargeMapboxMap = mapboxMap;

        mainLargeMapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
          @Override
          public void onMapClick(@NonNull LatLng point) {
            Log.d("MiniWindowActivity", "Clicked on map");
          }
        });

        mainLargeMapboxMap.setOnCameraMoveListener(new MapboxMap.OnCameraMoveListener() {
          @Override
          public void onCameraMove() {
            Log.d("MiniWindowActivity", "Camera moving");
            onMapMovedFragmentInterfaceListener.onMapMoved(mainLargeMapboxMap.getCameraPosition());
          }
        });

        mainLargeMapboxMap.setOnCameraIdleListener(new MapboxMap.OnCameraIdleListener() {
          @Override
          public void onCameraIdle() {
            Log.d("MiniWindowActivity", "Camera stopped moving");

          }
        });
      }
    });


    /* Custom version of the regular Mapbox SupportMapFragment class.A custom one is being built here
    so that the interface call backs can be used in the appropriate places so that the example eventually
    works*/
    CustomSupportMapFragment customSupportMapFragment;
    if (savedInstanceState == null) {

      // Create fragment
      final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

      // Build mapboxMap
      MapboxMapOptions options = new MapboxMapOptions();
      options.styleUrl(Style.MAPBOX_STREETS);
      options.camera(new CameraPosition.Builder()
        .target(new LatLng(-26.145, 134.312))
        .zoom(1)
        .build());

      // Create map fragment
      customSupportMapFragment = CustomSupportMapFragment.newInstance(options);

      // Add map fragment to parent container
      transaction.add(R.id.mini_map_fragment_container, customSupportMapFragment, "com.mapbox.map");
      transaction.commit();

    } else {
      customSupportMapFragment = (CustomSupportMapFragment) getSupportFragmentManager().findFragmentByTag("com.mapbox.map");
    }

    customSupportMapFragment.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {

        // Customize map with markers, polylines, etc.

        PolygonOptions polygonArea = new PolygonOptions()
          .add(AUSTRALIA_BOUNDS.getNorthWest())
          .add(AUSTRALIA_BOUNDS.getNorthEast())
          .add(AUSTRALIA_BOUNDS.getSouthEast())
          .add(AUSTRALIA_BOUNDS.getSouthWest());
        polygonArea.alpha(0.25f);
        polygonArea.fillColor(Color.parseColor("#ff9a00"));
        mapboxMap.addPolygon(polygonArea);
      }
    });

  }

  public interface OnMapMovedFragmentInterface {
    public void onMapMoved(CameraPosition cameraPosition);
    void onMapMoved(CameraPosition cameraPosition);
  }

  @Override
  public void onCameraMove() {
    Log.d("MiniWindowActivity", "Camera moving");
    onMapMovedFragmentInterfaceListener.onMapMoved(mainLargeMapboxMap.getCameraPosition());
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


  /**
   * Custom version of the regular Mapbox SupportMapFragment class. A custom one is being built here
   * so that the interface call backs can be used in the appropriate places so that the example eventually
   * works
   *
   * @see #getMapAsync(OnMapReadyCallback)
   */
  public static class CustomSupportMapFragment extends Fragment implements
    MiniWindowActivity.OnMapMovedFragmentInterface{
    MiniWindowActivity.OnMapMovedFragmentInterface {

    private MapView map;
    private OnMapReadyCallback onMapReadyCallback;

    /**
     * Creates a default CustomSupportMapFragment instance
     *
     * @return MapFragment created
     */
    public static CustomSupportMapFragment newInstance() {
      return new CustomSupportMapFragment();
    }

    /**
     * Creates a CustomSupportMapFragment instance
     *
     * @param mapboxMapOptions The configuration options to be used.
     * @return CustomSupportMapFragment created.
     */
    public static CustomSupportMapFragment newInstance(@Nullable MapboxMapOptions mapboxMapOptions) {
      CustomSupportMapFragment mapFragment = new CustomSupportMapFragment();
      mapFragment.setArguments(MapFragmentUtils.createFragmentArgs(mapboxMapOptions));
      return mapFragment;
    }

    @Override
    public void onMapMoved(CameraPosition cameraPosition) {
      Log.d("MiniWindowActivity", "camera lat position = " + cameraPosition.target.getLatitude() + " and long = "
        + cameraPosition.target.getLongitude());

      // TODO: Move fragment map camera in this method?
    }

    /**
     * Creates the fragment view hierarchy.
     *
     * @param inflater           Inflater used to inflate content.
     * @param container          The parent layout for the map fragment.
     * @param savedInstanceState The saved instance state for the map fragment.
     * @return The view created
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      super.onCreateView(inflater, container, savedInstanceState);
      Context context = inflater.getContext();
      return map = new MapView(context, MapFragmentUtils.resolveArgs(context, getArguments()));
    }

    /**
     * Called when the fragment view hierarchy is created.
     *
     * @param view               The content view of the fragment
     * @param savedInstanceState THe saved instance state of the framgnt
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      map.onCreate(savedInstanceState);
    }

    /**
     * Called when the fragment is visible for the users.
     */
    @Override
    public void onStart() {
      super.onStart();
      map.onStart();
      map.getMapAsync(onMapReadyCallback);
    }

    /**
     * Called when the fragment is ready to be interacted with.
     */
    @Override
    public void onResume() {
      super.onResume();
      map.onResume();
    }

    /**
     * Called when the fragment is pausing.
     */
    @Override
    public void onPause() {
      super.onPause();
      map.onPause();
    }

    /**
     * Called when the fragment state needs to be saved.
     *
     * @param outState The saved state
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
      super.onSaveInstanceState(outState);
      map.onSaveInstanceState(outState);
    }

    /**
     * Called when the fragment is no longer visible for the user.
     */
    @Override
    public void onStop() {
      super.onStop();
      map.onStop();
    }

    /**
     * Called when the fragment receives onLowMemory call from the hosting Activity.
     */
    @Override
    public void onLowMemory() {
      super.onLowMemory();
      map.onLowMemory();
    }

    /**
     * Called when the fragment is view hiearchy is being destroyed.
     */
    @Override
    public void onDestroyView() {
      super.onDestroyView();
      map.onDestroy();
    }

    /**
     * Sets a callback object which will be triggered when the MapboxMap instance is ready to be used.
     *
     * @param onMapReadyCallback The callback to be invoked.
     */
    public void getMapAsync(@NonNull final OnMapReadyCallback onMapReadyCallback) {
      this.onMapReadyCallback = onMapReadyCallback;
    }
  }
}

