package com.mapbox.mapboxandroiddemo.examples.dds

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.mapboxandroiddemo.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import kotlinx.android.synthetic.main.activity_dds_filter_feature.*

/**
 * Use filters to toggle the visibility of specific features. In this example, specific
 * country labels are filtered out from a single layer.
 */
class KotlinFilterFeaturesActivity : AppCompatActivity() {

    private var specificCountryLabelsHidden = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token))

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_dds_filter_feature)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync { mapboxMap ->

            mapboxMap.uiSettings.setAllGesturesEnabled(false)

            mapboxMap.setStyle(Style.MAPBOX_STREETS) {

                toggle_feature_filter_visibility_fab.setOnClickListener {

                    mapboxMap.getStyle {
                        if (it.getLayer("country-label") != null) {
                            it.getLayerAs<SymbolLayer>("country-label")?.setFilter(
                                    if (specificCountryLabelsHidden) all() else
                                        match(
                                                get("name_en"),
                                                all(
                                                        literal("Libya"),
                                                        literal("Austria"),
                                                        literal("Spain"),
                                                        literal("Mali"),
                                                        literal("Syria"),
                                                        literal("Tunisia"),
                                                        literal("Denmark")
                                                ),
                                                literal(false), literal(true)
                                        )
                            )
                            specificCountryLabelsHidden = !specificCountryLabelsHidden
                            Toast.makeText(this, if (specificCountryLabelsHidden)
                                R.string.filtering_countries else
                                R.string.showing_all_country_labels, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
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