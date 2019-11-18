package com.mapbox.mapboxandroiddemo.examples

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxandroiddemo.R
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.MapboxMapOptions
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.china.constants.ChinaStyle
import com.mapbox.mapboxsdk.plugins.china.maps.ChinaMapView
import com.mapbox.mapboxsdk.plugins.china.shift.ChinaBoundsChecker

class ChinaBoundsCheckerActivity : AppCompatActivity(), OnMapReadyCallback,
        LocationEngineCallback<LocationEngineResult>, PermissionsListener {

    private lateinit var mapboxMap: MapboxMap
    private var savedInstanceState: Bundle? = null
    private var deviceInChina: Boolean? = null
    private var chinaMapView: ChinaMapView? = null
    private var locationComponent: LocationComponent? = null
    private var permissionsManager: PermissionsManager? = null
    private var locationEngine: LocationEngine? = null
    private val defaultIntervalInMilliseconds = 1000L
    private val defaultMaxWaitTime = defaultIntervalInMilliseconds * 5

    // Adjust the Styles below to see various China and non-China styles used in this example
    private val chinaStyleToUse: String = ChinaStyle.MAPBOX_DARK_CHINESE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.savedInstanceState = savedInstanceState
        locationPermissionCheckAndStart()
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap

        Toast.makeText(this, R.string.china_style_with_english_labels_warning_toast, Toast.LENGTH_LONG).show()

        mapboxMap.setStyle(Style.Builder().fromUri(

                // TODO: Because of privacy reasons, this file isn't actually included
                //  in the app. Add the file to an assets folder.
                //  Please email Mapbox at apac-bd@mapbox.com if you're
                //  interested in this file and/or have questions about
                //  this general functionality.
                if (deviceInChina!!) chinaStyleToUse else
                    "asset://cn_style_with_english_labels.json")) {

            Toast.makeText(this@ChinaBoundsCheckerActivity,
                    String.format(getString(R.string.device_location),
                            if (deviceInChina!!) "is" else "isn't"), Toast.LENGTH_SHORT).show()

            initLocationComponent(it)
        }
    }

    override fun onSuccess(result: LocationEngineResult?) {
        val lastLocation = result?.lastLocation
        if (deviceInChina == null) {
            deviceInChina = ChinaBoundsChecker.locationIsInChina(
                    this@ChinaBoundsCheckerActivity, result?.lastLocation)
            initMap(
                    MapboxMapOptions.createFromAttributes(this, null)
                            .camera(
                                    CameraPosition.Builder()
                                            .target(LatLng(lastLocation?.latitude!!,
                                                    lastLocation.longitude))
                                            .zoom(10.0)
                                            .build()),
                    savedInstanceState)
        }
        locationComponent?.forceLocationUpdate(lastLocation)
    }

    override fun onFailure(exception: Exception) {
        Toast.makeText(this, String.format("get location failed: %s",
                exception.localizedMessage), Toast.LENGTH_SHORT).show()
    }

    private fun locationPermissionCheckAndStart() {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            initLocationEngine()
        } else {
            permissionsManager = PermissionsManager(this).apply {
            requestLocationPermissions(this@ChinaBoundsCheckerActivity)
            }
        }
    }

    private fun initMap(
      mapboxMapOptions: MapboxMapOptions,
      savedInstanceState: Bundle?
    ) {
        chinaMapView = ChinaMapView(this, mapboxMapOptions).apply {
            onCreate(savedInstanceState)
            getMapAsync(this@ChinaBoundsCheckerActivity)
            setContentView(this)
        }
    }

    @SuppressWarnings("MissingPermission")
    fun initLocationComponent(@NonNull fullyLoadedStyle: Style) {
        locationComponent = mapboxMap.locationComponent
        locationComponent?.apply {
            // Activate the LocationComponent with LocationComponentActivationOptions
            activateLocationComponent(LocationComponentActivationOptions.builder(
                    this@ChinaBoundsCheckerActivity,
                    fullyLoadedStyle).build())

            // Enable to make the LocationComponent visible
            isLocationComponentEnabled = true

            // Set the LocationComponent's camera mode
            cameraMode = CameraMode.NONE

            // Set the LocationComponent's render mode
            renderMode = RenderMode.NORMAL
        }
    }

    @SuppressLint("MissingPermission")
    private fun initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this).apply {
            requestLocationUpdates(LocationEngineRequest.Builder(defaultIntervalInMilliseconds)
                    .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                    .setMaxWaitTime(defaultMaxWaitTime).build(),
                    this@ChinaBoundsCheckerActivity, mainLooper)
            getLastLocation(this@ChinaBoundsCheckerActivity)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            locationPermissionCheckAndStart()
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        chinaMapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        chinaMapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        chinaMapView?.onStop()
    }

    public override fun onPause() {
        super.onPause()
        chinaMapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        chinaMapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        chinaMapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        chinaMapView?.onSaveInstanceState(outState)
    }
}