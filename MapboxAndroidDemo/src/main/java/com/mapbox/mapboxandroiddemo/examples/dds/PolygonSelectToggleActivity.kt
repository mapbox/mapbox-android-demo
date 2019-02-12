package com.mapbox.mapboxandroiddemo.examples.dds


import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxandroiddemo.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.activity_dds_polygon_select_toggle.*
import java.lang.ref.WeakReference
import java.util.*

/**
 * Use data-driven styling to style and toggle the colors of various polygons based
 * on user interaction.
 */
class PolygonSelectToggleActivity : AppCompatActivity(), MapboxMap.OnMapClickListener {

    private val BASE_NEIGHBORHOOD_FILL_LAYER_ID = "BASE_NEIGHBORHOOD_FILL_LAYER_ID"
    private val COLORED_FILL_LAYER_ID = "COLORED_FILL_LAYER_ID"
    private val LINE_LAYER_ID = "LINE_LAYER_ID"
    private val NEIGHBORHOOD_POLYGON_SOURCE_ID = "NEIGHBORHOOD_POLYGON_SOURCE_ID"
    private val PROPERTY_SELECTED = "selected"
    private val PROPERTY_FILL_COLOR = "fill_color"
    private val NEIGHBORHOOD_NAME_PROPERTY = "neighborhood_name"
    private var mapboxMap: MapboxMap? = null
    private var featureCollection: FeatureCollection? = null
    private var geoJsonSource: GeoJsonSource? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token))

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_dds_polygon_select_toggle)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.LIGHT) {
                this.mapboxMap = mapboxMap
                LoadGeoJsonDataTask(this).execute()
                mapboxMap.addOnMapClickListener(this)
                Toast.makeText(this, getString(R.string.tap_on_neighborhood),
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapClick(point: LatLng): Boolean {
        return handleClickIcon(mapboxMap?.projection!!.toScreenLocation(point))
    }

    /**
     * Sets up all of the sources and layers needed for this example
     *
     * @param collection the FeatureCollection returned by the data loading AsyncTask.
     */
    fun setUpData(collection: FeatureCollection) {
        if (mapboxMap == null) {
            return
        }

        // Set `featureCollection` equal to the FeatureCollection returned by the
        // the data loading AsyncTask.
        featureCollection = collection

        // Create a GeoJsonSource and add it to the map.
        geoJsonSource = GeoJsonSource(NEIGHBORHOOD_POLYGON_SOURCE_ID, featureCollection)

        mapboxMap?.style?.addSource(geoJsonSource!!)

        // Create a FillLayer which will show a background of all neighborhoods on the map.
        val neighborhoodPolygonBaseFillLayer = FillLayer(BASE_NEIGHBORHOOD_FILL_LAYER_ID,
                NEIGHBORHOOD_POLYGON_SOURCE_ID)
        neighborhoodPolygonBaseFillLayer.withProperties(
                fillOpacity(.2f))

        // Add neighborhood layer below the city label layer
        mapboxMap?.style?.addLayerBelow(neighborhoodPolygonBaseFillLayer, "settlement-label")

        // Add a FillLayer which will only show "selected" features/polygons/neighborhoods.
        val neighborhoodPolygonColoredFillLayer = FillLayer(COLORED_FILL_LAYER_ID,
                NEIGHBORHOOD_POLYGON_SOURCE_ID)
        neighborhoodPolygonColoredFillLayer.withProperties(

                // Use data-driven styling to use each Feature's color property, which was set in
                // `onPostExecute()` of the data loading AsyncTask.
                fillColor(toColor(get(PROPERTY_FILL_COLOR))),
                fillOpacity(.95f))

        // Add a filter to ensure that only "selected" features/polygons/neighborhoods are displayed.
        neighborhoodPolygonColoredFillLayer.withFilter(eq((get(PROPERTY_SELECTED)), literal(true)))
        mapboxMap?.style?.addLayerBelow(neighborhoodPolygonColoredFillLayer, "settlement-label")

        // Add LineLayer to outline the various neighborhoods' areas
        val neighborhoodOutlineLineLayer = LineLayer(LINE_LAYER_ID, NEIGHBORHOOD_POLYGON_SOURCE_ID)
        neighborhoodOutlineLineLayer.withProperties(
                lineColor(Color.GRAY),
                lineWidth(2.2f))
        mapboxMap?.style?.addLayerBelow(neighborhoodOutlineLineLayer, "settlement-label")
    }

    /**
     * This method handles click events for SymbolLayer symbols.
     * When a SymbolLayer icon is clicked, we adjust that feature to the selected state.
     * @param screenPoint the point on screen clicked
     */
    private fun handleClickIcon(screenPoint: PointF): Boolean {
        val features = mapboxMap?.queryRenderedFeatures(screenPoint, BASE_NEIGHBORHOOD_FILL_LAYER_ID)
        if (features?.isEmpty() == false) {
            val name = features[0].getStringProperty(NEIGHBORHOOD_NAME_PROPERTY)
            val featureList = featureCollection?.features()
            for (i in featureList!!.indices) {
                if (featureList[i].getStringProperty(NEIGHBORHOOD_NAME_PROPERTY) == name) {
                    if (featureSelectStatus(i)) {
                        setFeatureSelectState(featureList.get(i), false)
                    } else {
                        setSelected(i)
                    }
                }
            }
            return true
        } else {
            return false
        }
    }

    /**
     * Selects the state of a feature
     *
     * @param feature the feature to be selected.
     */
    private fun setFeatureSelectState(feature: Feature, selectedState: Boolean) {
        feature.properties()!!.addProperty(PROPERTY_SELECTED, selectedState)
        refreshSource()
    }

    /**
     * Checks whether a Feature's boolean "selected" property is true or false
     *
     * @param index the specific Feature's index position in the FeatureCollection's list of Features.
     * @return true if "selected" is true. False if the boolean property is false.
     */
    private fun featureSelectStatus(index: Int): Boolean {
        featureCollection?.features()!![index].getBooleanProperty(PROPERTY_SELECTED)
        return if (featureCollection == null) {
            false
        } else featureCollection?.features()!![index].getBooleanProperty(PROPERTY_SELECTED)
    }

    /**
     * Updates the display of data on the map after the FeatureCollection has been modified
     */
    private fun refreshSource() {
        if (geoJsonSource != null && featureCollection != null) {
            geoJsonSource?.setGeoJson(featureCollection)
        }
    }

    /**
     * Set a feature selected state.
     *
     * @param index the index of selected feature
     */
    private fun setSelected(index: Int) {
        val feature = featureCollection?.features()!![index]
        setFeatureSelectState(feature, true)
        refreshSource()
    }

    /**
     * AsyncTask to load data from a GeoJSON file in the assets folder. Rather than loading from
     * a locally-stored file, you could also use the Mapbox Dataset API or Tileset API to retrieve
     * GeoJSON data.
     */
    private class LoadGeoJsonDataTask internal
    constructor(activity: PolygonSelectToggleActivity) :
            AsyncTask<Void, Void, FeatureCollection>() {

        private val PROPERTY_SELECTED = "selected"
        private val PROPERTY_FILL_COLOR = "fill_color"
        private var randomColorListForPolygons = arrayOf("#20B2AA", "#A52A2A", "#FFB6C1", "#D2B48C",
                "#FAFAD2", "#0000CD", "#FFFFF0", "#C71585", "#9400D3", "#F5F5DC", "#FFFACD", "#FFF5EE",
                "#191970", "#FF8C00", "#000000", "#66CDAA", "#9ACD32", "#FFA07A", "#4B0082", "#E6E6FA"
        )

        private val activityRef: WeakReference<PolygonSelectToggleActivity>

        init {
            this.activityRef = WeakReference(activity)
        }

        override fun doInBackground(vararg params: Void): FeatureCollection? {
            val activity = activityRef.get() ?: return null

            val geoJson = loadGeoJsonFromAsset(activity,
                    "new-orleans-neighborhoods.geojson")
            return FeatureCollection.fromJson(geoJson)
        }

        override fun onPostExecute(featureCollection: FeatureCollection?) {
            super.onPostExecute(featureCollection)
            val activity = activityRef.get()
            if (featureCollection == null || activity == null) {
                return
            }

            for (singleFeature in featureCollection.features()!!) {
                singleFeature.addBooleanProperty(PROPERTY_SELECTED, false)

                // Add a String property to each Feature/neighborhood/polygon. The String value
                // is the hex color value to be used in the `neighborhoodPolygonColoredFillLayer`
                // fill layer.
                singleFeature.addStringProperty(PROPERTY_FILL_COLOR,
                        randomColorListForPolygons.get(Random().nextInt(
                                randomColorListForPolygons.size)))
            }
            activity.setUpData(featureCollection)
        }

        companion object {
            internal fun loadGeoJsonFromAsset(context: Context, filename: String): String {
                try {
                    // Load GeoJSON file
                    val `is` = context.assets.open(filename)
                    val size = `is`.available()
                    val buffer = ByteArray(size)
                    `is`.read(buffer)
                    `is`.close()
                    return String(buffer)
                } catch (exception: Exception) {
                    throw RuntimeException(exception)
                }
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
        mapboxMap?.removeOnMapClickListener(this)
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}