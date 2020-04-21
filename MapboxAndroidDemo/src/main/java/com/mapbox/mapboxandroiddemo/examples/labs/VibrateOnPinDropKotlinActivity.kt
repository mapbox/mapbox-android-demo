package com.mapbox.mapboxandroiddemo.examples.labs

import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.geojson.Point
import com.mapbox.mapboxandroiddemo.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import kotlinx.android.synthetic.main.activity_lab_vibrate_on_pin_drop_kotlin.*

/**
 * Vibrate the Android device when a pin icon is dropped on the map.
 */
class VibrateOnPinDropKotlinActivity : AppCompatActivity(), OnMapReadyCallback, MapboxMap.OnMapLongClickListener {

    var mapboxMap: MapboxMap? = null

    companion object {
        private const val SOURCE_ID = "SOURCE_ID"
        private const val ICON_ID = "ICON_ID"
        private const val LAYER_ID = "LAYER_ID"
        private const val VIBRATE_SPEED_ONE_HUNDRED_MILLISECONDS = 100L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token))

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_lab_vibrate_on_pin_drop_kotlin)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        mapboxMap.setStyle(Style.Builder().fromUri(Style.OUTDOORS)

                // Add the SymbolLayer icon image to the map style
                .withImage(ICON_ID, BitmapUtils.getDrawableFromRes(
                        this@VibrateOnPinDropKotlinActivity,
                        R.drawable.mapbox_marker_icon_default)!!)

                // Adding a GeoJson source for the SymbolLayer icons.
                .withSource(GeoJsonSource(SOURCE_ID))

                // Adding the actual SymbolLayer to the map style.
                .withLayer(SymbolLayer(LAYER_ID, SOURCE_ID)
                        .withProperties(
                                iconImage(ICON_ID),
                                iconAllowOverlap(true),
                                iconIgnorePlacement(true))
                )) {
            this.mapboxMap = mapboxMap
            mapboxMap.addOnMapLongClickListener(this@VibrateOnPinDropKotlinActivity)
            Toast.makeText(this@VibrateOnPinDropKotlinActivity,
                    R.string.long_press_to_add_pin, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapLongClick(point: LatLng): Boolean {
        mapboxMap?.getStyle {

            // Update the SymbolLayer icon's source to move the map pin
            val geoJsonSource = it.getSourceAs<GeoJsonSource>(SOURCE_ID)
            geoJsonSource?.setGeoJson(Point.fromLngLat(point.longitude, point.latitude))

            // Vibrate the device
            vibrate()
        }
        return true
    }

    /**
     * Vibrate the device
     */
    private fun vibrate() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(VIBRATE_SPEED_ONE_HUNDRED_MILLISECONDS,
                    VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(VIBRATE_SPEED_ONE_HUNDRED_MILLISECONDS)
        }
    }

    // Add the mapView lifecycle to the activity's lifecycle methods
    public override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    public override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
}