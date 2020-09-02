package com.mapbox.mapboxandroiddemo.examples.extrusions

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.geojson.Feature
import com.mapbox.mapboxandroiddemo.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.expressions.Expression.color
import com.mapbox.mapboxsdk.style.expressions.Expression.get
import com.mapbox.mapboxsdk.style.expressions.Expression.id
import com.mapbox.mapboxsdk.style.expressions.Expression.literal
import com.mapbox.mapboxsdk.style.expressions.Expression.match
import com.mapbox.mapboxsdk.style.expressions.Expression.stop
import com.mapbox.mapboxsdk.style.layers.FillExtrusionLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionColor
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionHeight
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionOpacity
import com.mapbox.mapboxsdk.style.layers.PropertyValue
import kotlinx.android.synthetic.main.activity_extrusion_color_expression.mapView

/**
 * Use runtime styling to make a [FillExtrusionLayer]'s opacity based on the
 * map's zoom level. The 3D building extrusions will be come less opaque as the
 * camera moves closer to the buildings.
 */
class ExtrusionColorExpressionKotlinActivity : AppCompatActivity(), MapboxMap.OnMapClickListener {
    private var mapboxMap: MapboxMap? = null
    private var lastQueryLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token))

        setContentView(R.layout.activity_extrusion_color_expression)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap ->
            this.mapboxMap = mapboxMap
            mapboxMap.setStyle(Style.DARK) { style ->
                lastQueryLatLng = mapboxMap.cameraPosition.target
                addExtrusionLayerToMap(style)
                mapboxMap.addOnMapClickListener(this)
                Toast.makeText(this,
                        R.string.tap_on_building_to_highlight,
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapClick(mapTapLatLng: LatLng): Boolean {
        adjustUiForSelectedBuilding(mapTapLatLng)
        return true
    }

    /**
     * Adjusts the example's UI for the selected building in the [FillExtrusionLayer].
     * Depending on whether all buildings are shown or not, the entire extrusion will move
     * or just the color of the selected extrusion will change.
     *
     * @param clickLatLng The [LatLng] of wherever the map was tapped.
     */
    private fun adjustUiForSelectedBuilding(clickLatLng: LatLng) {
        lastQueryLatLng = clickLatLng
        mapboxMap?.getStyle { style ->
            val buildingExtrusionLayer = style.getLayerAs<FillExtrusionLayer>(EXTRUSION_BUILDING_LAYER_ID)
            buildingExtrusionLayer?.setProperties(getFillExtrusionColorProperty(lastQueryLatLng))
        }
    }

    /**
     * Returns a [fillExtrusionColor] statement so that the selected building is colored
     * appropriately.
     */
    private fun getFillExtrusionColorProperty(queryLatLng: LatLng?): PropertyValue<Expression> {
        return fillExtrusionColor(match(
                Expression.toString(id()),
                color(Color.parseColor(EXTRUSION_COLOR_HEX)),
                stop(literal(getBuildingId(queryLatLng)), color(Color.parseColor(SELECTED_EXTRUSION_COLOR_HEX)))))
    }

    /**
     * Gets the [Feature.id] of the building [Feature] that has the queryLatLng
     * within its footprint. This ID is then used in the filter that's applied to the
     * [FillExtrusionLayer].
     *
     * @param queryLatLng the [LatLng] to use for querying the [MapboxMap] to eventually
     * get the building ID.
     * @return the selected building's ID
     */
    private fun getBuildingId(queryLatLng: LatLng?): String {
        mapboxMap?.let {
            val renderedBuildingFootprintFeatures = it.queryRenderedFeatures(
                    it.projection.toScreenLocation(queryLatLng!!), BUILDING_LAYER_ID)
            if (renderedBuildingFootprintFeatures.isNotEmpty()) {
                return renderedBuildingFootprintFeatures[0].id()!!
            }
        }
        return DEFAULT_BUILDING_ID
    }

    /**
     * Adds a [FillExtrusionLayer] to the map.
     *
     * @param loadedMapStyle A loaded [Style] on the [MapboxMap].
     */
    private fun addExtrusionLayerToMap(loadedMapStyle: Style) {
        FillExtrusionLayer(EXTRUSION_BUILDING_LAYER_ID, COMPOSITE_SOURCE_ID).also {
            it.sourceLayer = BUILDING_LAYER_ID
            it.setProperties(
                    getFillExtrusionColorProperty(lastQueryLatLng),
                    fillExtrusionHeight(get("height")),
                    fillExtrusionOpacity(EXTRUSION_OPACITY))
            loadedMapStyle.addLayer(it)
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

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapboxMap?.removeOnMapClickListener(this)
        mapView.onDestroy()
    }

    companion object {
        private const val EXTRUSION_OPACITY = 0.6f
        private const val EXTRUSION_COLOR_HEX = "#ecca02"
        private const val SELECTED_EXTRUSION_COLOR_HEX = "#e700d2"
        private const val DEFAULT_BUILDING_ID = "0"
        private const val BUILDING_LAYER_ID = "building"
        private const val COMPOSITE_SOURCE_ID = "composite"
        private const val EXTRUSION_BUILDING_LAYER_ID = "EXTRUSION_BUILDING_LAYER_ID"
    }
}