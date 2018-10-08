package com.mapbox.mapboxandroiddemo.examples.plugins

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxandroiddemo.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode
import kotlinx.android.synthetic.main.activity_location_plugin.*

/**
 * Use the Location Layer plugin to easily add a device location "puck" to a Mapbox map.
 */
class KotlinLocationPluginActivity : AppCompatActivity(), OnMapReadyCallback, PermissionsListener {

  private val permissionsManager: PermissionsManager = PermissionsManager(this)
  private lateinit var mapboxMap: MapboxMap

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token))

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_location_plugin)

    mapView.onCreate(savedInstanceState)
    mapView.getMapAsync(this)
  }

  override fun onMapReady(mapboxMap: MapboxMap) {
    this.mapboxMap = mapboxMap
    enableLocationPlugin()
  }

  private fun enableLocationPlugin() {
    // Check if permissions are enabled and if not request
    if (PermissionsManager.areLocationPermissionsGranted(this)) {

      // Create an instance of the plugin. Adding in LocationLayerOptions is also an optional
      // parameter
      val locationLayerPlugin = LocationLayerPlugin(mapView, mapboxMap)

      // Set the plugin's camera mode
      locationLayerPlugin.cameraMode = CameraMode.TRACKING
      lifecycle.addObserver(locationLayerPlugin)
    } else {
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
      enableLocationPlugin()
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