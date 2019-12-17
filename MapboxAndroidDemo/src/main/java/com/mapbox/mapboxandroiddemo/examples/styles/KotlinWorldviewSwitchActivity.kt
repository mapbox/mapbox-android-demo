package com.mapbox.mapboxandroiddemo.examples.styles

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.mapboxandroiddemo.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.LineLayer
import kotlinx.android.synthetic.main.activity_style_worldview_switch.*

/**
 * Uses the worldview value to adjust administrative boundaries.
 * You can see the worldview options within the worldviews variable in this example.
 *
 * More about Mapbox's worldview can be found at
 * https://docs.mapbox.com/vector-tiles/reference/mapbox-streets-v8/#-worldview-text
 */
class KotlinWorldviewSwitchActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private var possibleWorldViews = mutableListOf("US", "CN", "IN")
    private var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token))

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_style_worldview_switch)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->

                switch_worldview_fab.setOnClickListener {

                    style.layers.forEach {

                        // Apply changes only to certain LineLayers in the style
                        if (it.id == "admin-0-boundary" ||
                                it.id == "admin-1-boundary" ||
                                it.id == "admin-0-boundary-disputed" ||
                                it.id == "admin-1-boundary-bg" ||
                                it.id == "admin-0-boundary-bg") {
                            if (index == possibleWorldViews.size) {
                                index = 0
                            }

                            val layerToBeAdjusted = style.getLayerAs<LineLayer>(it.id)

                            // Use the Maps SDK's expressions to show the corresponding world view
                            layerToBeAdjusted?.setFilter(
                                    match(
                                            get("worldview"),
                                            all(literal(possibleWorldViews[index])),
                                            literal(true), literal(false))
                            )
                        }
                    }

                    when (possibleWorldViews[index]) {
                        "US" -> {
                            Toast.makeText(this, String.format(
                                    getString(R.string.now_viewing_worldview, "US")),
                                    Toast.LENGTH_SHORT).show()
                        }

                        "CN" -> {
                            Toast.makeText(this, String.format(
                                    getString(R.string.now_viewing_worldview, "China")),
                                    Toast.LENGTH_SHORT).show()
                        }

                        "IN" -> {
                            Toast.makeText(this, String.format(
                                    getString(R.string.now_viewing_worldview, "India")),
                                    Toast.LENGTH_SHORT).show()
                        }
                    }

                    index++
                }
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    public override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}
