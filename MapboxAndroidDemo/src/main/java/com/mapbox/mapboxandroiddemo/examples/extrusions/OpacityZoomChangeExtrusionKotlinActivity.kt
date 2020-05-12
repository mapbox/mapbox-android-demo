package com.mapbox.mapboxandroiddemo.examples.extrusions

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.mapboxandroiddemo.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory.zoomTo
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.exponential
import com.mapbox.mapboxsdk.style.expressions.Expression.get
import com.mapbox.mapboxsdk.style.expressions.Expression.interpolate
import com.mapbox.mapboxsdk.style.expressions.Expression.linear
import com.mapbox.mapboxsdk.style.expressions.Expression.literal
import com.mapbox.mapboxsdk.style.expressions.Expression.stop
import com.mapbox.mapboxsdk.style.expressions.Expression.zoom
import com.mapbox.mapboxsdk.style.layers.FillExtrusionLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import kotlinx.android.synthetic.main.activity_zoom_opacity_extrusion.*

/**
 * Use runtime styling to make a [FillExtrusionLayer]'s opacity based on the
 * map's zoom level. The 3D building extrusions will be come less opaque as the
 * camera moves closer to the buildings.
 */
class OpacityZoomChangeExtrusionKotlinActivity : AppCompatActivity(), OnMapReadyCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token))

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_zoom_opacity_extrusion)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        mapboxMap.setStyle(Style.LIGHT) { style ->

            // Add the 3D buildings' FillExtrusionLayer to the map style
            style.addLayer(FillExtrusionLayer("building-layer-id",
                    "composite").apply {
                sourceLayer = "building"
                minZoom = 15.0f
                setProperties(
                        PropertyFactory.fillExtrusionColor(Color.LTGRAY),
                        PropertyFactory.fillExtrusionHeight(
                                interpolate(
                                        exponential(1),
                                        zoom(),
                                        stop(15, literal(0)),
                                        stop(16, get("height"))
                                )
                        ),
                        // Use a runtime styling property to make the opacity
                        // dependent on the camera zoom value
                        PropertyFactory.fillExtrusionOpacity(interpolate(linear(),
                                zoom(),
                                stop(15.3, .1),
                                stop(17.5, 1)))
                )
            })

            // Zoom the camera in and then back out to show the opacity change
            // as the zoom value changes.
            mapboxMap.easeCamera(zoomTo(18.0), 5000,
                    object : MapboxMap.CancelableCallback {
                        override fun onFinish() {
                            mapboxMap.easeCamera(zoomTo(15.4), 5000)
                        }

                        override fun onCancel() {
                            // Empty because not needed in this example
                        }
                    })
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