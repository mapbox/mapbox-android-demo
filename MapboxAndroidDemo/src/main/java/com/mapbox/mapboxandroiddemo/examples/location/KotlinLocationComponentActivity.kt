package com.mapbox.mapboxandroiddemo.examples.location

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.annotation.NonNull
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxandroiddemo.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.activity_location_component.*

/**
 * Use the LocationComponent to easily add a device location "puck" to a Mapbox map.
 */
class KotlinLocationComponentActivity : AppCompatActivity(), OnMapReadyCallback, PermissionsListener {

  private var permissionsManager: PermissionsManager = PermissionsManager(this)
  private lateinit var mapboxMap: MapboxMap

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token))

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_location_component)

    mapView.onCreate(savedInstanceState)
    mapView.getMapAsync(this)
  }

  override fun onMapReady(mapboxMap: MapboxMap) {
    this.mapboxMap = mapboxMap
    mapboxMap.setStyle(Style.Builder().fromUrl(
            "mapbox://styles/mapbox/cjerxnqt3cgvp2rmyuxbeqme7")) {

      // Map is set up and the style has loaded. Now you can add data or make other map adjustments
      enableLocationComponent(it)
    }
  }

  @SuppressLint("MissingPermission")
  private fun enableLocationComponent(loadedMapStyle: Style) {
    // Check if permissions are enabled and if not request
    if (PermissionsManager.areLocationPermissionsGranted(this)) {

      val options = LocationComponentOptions.builder(this)
              .trackingGesturesManagement(true)
              .accuracyColor(ContextCompat.getColor(this, R.color.mapboxGreen))
              .build()

      // Get an instance of the component
      val locationComponent = mapboxMap.locationComponent

      // Activate the component
      locationComponent.activateLocationComponent(this, loadedMapStyle)

      // Apply the options to the LocationComponent
      locationComponent.applyStyle(options)

      // Enable to make component visible
      locationComponent.isLocationComponentEnabled = true

      // Set the component's camera mode
      locationComponent.cameraMode = CameraMode.TRACKING
      locationComponent.renderMode = RenderMode.COMPASS


    } else {
      permissionsManager = PermissionsManager(this)
      permissionsManager.requestLocationPermissions(this)
    }
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
  }

  override fun onExplanationNeeded(permissionsToExplain: List<String>) {
    Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show()
  }

  override fun onPermissionResult(granted: Boolean) {
    if (granted) {
        enableLocationComponent(mapboxMap.style!!)
    } else {
      Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show()
      finish()
    }
  }

  override fun onStart() {
    super.onStart()
    mapView.onStart()
  }

  override fun onResume() {
    super.onResume()
    mapView.onResume()
  }

  override fun onPause() {
    super.onPause()
    mapView.onPause()
  }

  override fun onStop() {
    super.onStop()
    mapView.onStop()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    mapView.onSaveInstanceState(outState)
  }

  override fun onDestroy() {
    super.onDestroy()
    mapView.onDestroy()
  }

  override fun onLowMemory() {
    super.onLowMemory()
    mapView.onLowMemory()
  }
}