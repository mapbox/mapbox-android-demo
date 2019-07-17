package com.mapbox.mapboxandroiddemo.examples.dds

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.mapboxandroiddemo.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius
import com.mapbox.mapboxsdk.style.sources.VectorSource
import kotlinx.android.synthetic.main.activity_dds_kotlin_style_circles_categorically.*

/**
 * Kotlin example of using data-driven styling to set circles' colors based on imported vector data.
 */
class KotlinStyleCirclesCategoricallyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token))

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_dds_kotlin_style_circles_categorically)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap ->

            mapboxMap.setStyle(Style.LIGHT) {
                val vectorSource = VectorSource(
                        "ethnicity-source",
                        "http://api.mapbox.com/v4/examples.8fgz4egr.json?access_token=" + Mapbox.getAccessToken()!!
                )
                it.addSource(vectorSource)

                val circleLayer = CircleLayer("population", "ethnicity-source")
                circleLayer.sourceLayer = "sf2010"
                circleLayer.withProperties(
                        circleRadius(
                                interpolate(
                                        exponential(1.75f),
                                        zoom(),
                                        stop(12, 2f),
                                        stop(22, 180f)
                                )),
                        circleColor(
                                match(get("ethnicity"), rgb(0, 0, 0),
                                        stop("white", rgb(251, 176, 59)),
                                        stop("Black", rgb(34, 59, 83)),
                                        stop("Hispanic", rgb(229, 94, 94)),
                                        stop("Asian", rgb(59, 178, 208)),
                                        stop("Other", rgb(204, 204, 204)))))

                it.addLayer(circleLayer)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    public override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    public override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
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