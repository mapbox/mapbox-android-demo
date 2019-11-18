package com.mapbox.mapboxandroiddemo.examples

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.mapboxandroiddemo.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.*
import com.mapbox.mapboxsdk.plugins.china.constants.ChinaStyle
import com.mapbox.mapboxsdk.plugins.china.maps.ChinaMapView
import com.mapbox.mapboxsdk.plugins.china.shift.ChinaBoundsChecker
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource

/**
 * This is an example of showing how to check for device location and then
 * loading a China style or custom .com/global style based on the location.
 */
class MixedChinaAndGlobalStyleActivity : AppCompatActivity(), OnMapReadyCallback,
        LocationEngineCallback<LocationEngineResult>, PermissionsListener {

    private lateinit var mapboxMap: MapboxMap
    private var chinaMapView: ChinaMapView? = null
    private var globalMapView: MapView? = null
    private var savedInstanceState: Bundle? = null
    private var deviceInChina: Boolean? = null
    private var locationComponent: LocationComponent? = null
    private var permissionsManager: PermissionsManager? = null
    private var locationEngine: LocationEngine? = null
    private val defaultIntervalInMilliseconds = 1000L
    private val defaultMaxWaitTime = defaultIntervalInMilliseconds * 5

    // Adjust the Styles below to see various China and global styles used in this example
    private val chinaStyleToUse: String = ChinaStyle.MAPBOX_DARK_CHINESE
    private val globalStyleToUse: String = Style.TRAFFIC_DAY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.savedInstanceState = savedInstanceState

        // Check location permissions
        locationPermissionCheckAndStart()
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        // Set the map style based on whether the device is in or out of China
        mapboxMap.setStyle(Style.Builder().fromUri(
                if (deviceInChina!!) chinaStyleToUse else globalStyleToUse)) {

            // Add the LocationComponent device location puck to the map
            initLocationComponent(it)

            // Add data to the map on top of whatever style is loaded above.
            initSource()
            initLayers()
        }
    }

    /**
     * This callback fires whenever the device location changes. This is where the
     * device location's coordinates are checked against China's borders. The
     * Mapbox token and map type are then set up based on where the device is.
     */
    override fun onSuccess(result: LocationEngineResult?) {
        val lastLocation = result?.lastLocation

        Toast.makeText(this, R.string.china_token_warning_toast, Toast.LENGTH_LONG).show()

        if (deviceInChina == null) {

            // Check to see whether the device location is inside
            // or outside of China's borders
            deviceInChina = ChinaBoundsChecker.locationIsInChina(
                    this@MixedChinaAndGlobalStyleActivity, result?.lastLocation)

            if (deviceInChina!!) {

                // TODO: Make sure that you add the `access_token` and `china_access_token` string
                //  resources found below, to the `developer-config.xml` file.

                // TODO: `developer-config.xml` file instructions can be found at
                //  https://github.com/mapbox/mapbox-android-demo#setting-the-mapbox-access-token

                // TODO: Your global Mapbox token can be retrieved at
                //  https://account.mapbox.com/access-tokens/

                // <string name="access_token" translatable="false">PASTE_GLOBAL_TOKEN_HERE</string>

                // TODO: Contact our sales team via https://www.mapbox.com/contact/sales
                //  to start the process of receiving this special access token:

                // <string name="china_access_token" translatable="false">PASTE_SPECIAL_CHINA_TOKEN_HERE</string>

                // TODO: Uncomment the `Mapbox.setAccessToken()` line below once you've
                //  added `china_access_token` string resource to the `developer-config.xml` file
                /*Mapbox.setAccessToken(getString(R.string.china_access_token))*/
            } else {
                Mapbox.setAccessToken(getString(R.string.access_token))
            }

            initMap(deviceInChina!!,
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

    /**
     * Check location permissions. Start the permissions process if they're not already
     * granted. Initialize the [LocationEngine] if they're already given.
     */
    private fun locationPermissionCheckAndStart() {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            initLocationEngine()
        } else {
            permissionsManager = PermissionsManager(this).apply {
                requestLocationPermissions(this@MixedChinaAndGlobalStyleActivity)
            }
        }
    }

    /**
     * Initialize the map based on whether the device location is in or outside of China.
     */
    private fun initMap(
      deviceInChina: Boolean,
      mapboxMapOptions: MapboxMapOptions,
      savedInstanceState: Bundle?
    ) {
        if (deviceInChina) {
            chinaMapView = ChinaMapView(this, mapboxMapOptions).apply {
                onCreate(savedInstanceState)
                getMapAsync(this@MixedChinaAndGlobalStyleActivity)
                setContentView(this)
            }
        } else {
            globalMapView = MapView(this, mapboxMapOptions).apply {
                onCreate(savedInstanceState)
                getMapAsync(this@MixedChinaAndGlobalStyleActivity)
                setContentView(this)
            }
        }
    }

    /**
     * Initialize the [LocationComponent] to show the device location puck on top of whatever
     * map style is loaded.
     */
    @SuppressWarnings("MissingPermission")
    fun initLocationComponent(@NonNull fullyLoadedStyle: Style) {
        locationComponent = mapboxMap.locationComponent
        mapboxMap.locationComponent.apply {
            // Activate the LocationComponent with LocationComponentActivationOptions
            activateLocationComponent(LocationComponentActivationOptions.builder(
                    this@MixedChinaAndGlobalStyleActivity,
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
    /**
     * Initialize the [LocationEngine] so that location change callbacks happen
     */
    private fun initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this).apply {
            requestLocationUpdates(LocationEngineRequest.Builder(defaultIntervalInMilliseconds)
                    .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                    .setMaxWaitTime(defaultMaxWaitTime).build(),
                    this@MixedChinaAndGlobalStyleActivity, mainLooper)
            getLastLocation(this@MixedChinaAndGlobalStyleActivity)
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

    /**
     * Initialize map source to eventually show line and fill layers.
     */
    private fun initSource() {
        mapboxMap.getStyle {
            val polygonFeature = Feature.fromGeometry(Polygon.fromLngLats(mutableListOf(
                    mutableListOf(Point.fromLngLat(121.474113, 31.230784),
                            Point.fromLngLat(121.481752, 31.213315),
                            Point.fromLngLat(121.495914, 31.212434),
                            Point.fromLngLat(121.498403, 31.224325),
                            Point.fromLngLat(121.487331, 31.235407),
                            Point.fromLngLat(121.474113, 31.230784)))))
            val geojsonSource = GeoJsonSource("source",
                    polygonFeature)
            it.addSource(geojsonSource)
        }
    }

    /**
     * Add line and fill layers to the map to show data on top of whatever style is loaded.
     */
    private fun initLayers() {
        mapboxMap.getStyle {
            LineLayer("line-layer", "source").apply {
                withProperties(
                        PropertyFactory.lineColor(Color.parseColor("#ca59ff")),
                        PropertyFactory.lineWidth(5f)
                )
                it.addLayer(this)
            }
            FillLayer("fill-layer", "source").apply {
                withProperties(
                        PropertyFactory.fillColor(Color.parseColor("#ca59ff")),
                        PropertyFactory.fillOpacity(.6f)
                )
                it.addLayer(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        chinaMapView?.onResume()
        globalMapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        chinaMapView?.onStart()
        globalMapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        chinaMapView?.onStop()
        globalMapView?.onStop()
    }

    public override fun onPause() {
        super.onPause()
        chinaMapView?.onPause()
        globalMapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        chinaMapView?.onLowMemory()
        globalMapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        chinaMapView?.onDestroy()
        globalMapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        chinaMapView?.onSaveInstanceState(outState)
        globalMapView?.onSaveInstanceState(outState)
    }
}