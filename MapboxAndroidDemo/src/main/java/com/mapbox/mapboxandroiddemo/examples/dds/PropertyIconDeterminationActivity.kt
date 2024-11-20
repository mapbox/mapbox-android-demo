package com.mapbox.mapboxandroiddemo.examples.dds

import android.graphics.BitmapFactory
import com.mapbox.mapboxandroiddemo.R
import android.graphics.PointF
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource


class PropertyIconDeterminationActivity : AppCompatActivity(),  MapboxMap.OnMapClickListener {


    private val SOURCE_ID = "SOURCE_ID"
    private val RED_ICON_ID = "RED_ICON_ID"
    private val YELLOW_ICON_ID = "YELLOW_ICON_ID"
    private val LAYER_ID = "LAYER_ID"
    private val ICON_PROPERTY = "ICON_PROPERTY"
    private var mapView: MapView? = null
    private lateinit var mapboxMap: MapboxMap
    private lateinit var featureInfoTextView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.access_token))
        setContentView(R.layout.activity_dds_property_icon_switch)

        mapView = findViewById(R.id.mapView)
        featureInfoTextView = findViewById(R.id.feature_info)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync { mapboxMap ->

            mapboxMap.setStyle(Style.Builder().fromUri("mapbox://styles/mapbox/cj44mfrt20f082snokim4ungi") // Add the SymbolLayer icon image to the map style
                    .withImage(RED_ICON_ID, BitmapFactory.decodeResource(
                            this@PropertyIconDeterminationActivity.getResources(), R.drawable.red_marker))
                    .withImage(YELLOW_ICON_ID, BitmapFactory.decodeResource(
                            this@PropertyIconDeterminationActivity.getResources(), R.drawable.yellow_marker)) // Adding a GeoJson source for the SymbolLayer icons.
                    .withSource(GeoJsonSource(SOURCE_ID,
                            FeatureCollection.fromFeatures(initCoordinateData()!!))) // Adding the actual SymbolLayer to the map style. The match expression will check the
                    // ICON_PROPERTY property key and then use the partner value for the actual icon id.
                    .withLayer(SymbolLayer(LAYER_ID, SOURCE_ID)
                            .withProperties(iconImage(match(
                                    get(ICON_PROPERTY), literal(RED_ICON_ID),
                                    stop(YELLOW_ICON_ID, YELLOW_ICON_ID),
                                    stop(RED_ICON_ID, RED_ICON_ID))),
                                    iconAllowOverlap(true),
                                    iconAnchor(Property.ICON_ANCHOR_BOTTOM))
                    )) { // Map is set up and the style has loaded. Now you can add additional data or make other map adjustments.
                this@PropertyIconDeterminationActivity.mapboxMap = mapboxMap
                mapboxMap.addOnMapClickListener(this@PropertyIconDeterminationActivity)
                Toast.makeText(this@PropertyIconDeterminationActivity, "marker tapped",
                        Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onMapClick(point: LatLng): Boolean {
        return handleClickIcon(mapboxMap.projection.toScreenLocation(point))
    }


    /**
     * This method handles click events for SymbolLayer symbols.
     *
     * @param screenPoint the point on screen clicked
     */
    private fun handleClickIcon(screenPoint: PointF): Boolean {
        val features = mapboxMap.queryRenderedFeatures(screenPoint, LAYER_ID)
        return if (!features.isEmpty()) {
            // Show the Feature in the TextView to show that the icon is based on the ICON_PROPERTY key/value

            featureInfoTextView.text = features[0].toJson()
            true
        } else {
            false
        }
    }

    private fun initCoordinateData(): List<Feature>? {
        val singleFeatureOne = Feature.fromGeometry(
                Point.fromLngLat(72.88055419921875,
                        19.05822387777432))
        singleFeatureOne.addStringProperty(ICON_PROPERTY, RED_ICON_ID)
        val singleFeatureTwo = Feature.fromGeometry(
                Point.fromLngLat(77.22015380859375,
                        28.549544699103865))
        singleFeatureTwo.addStringProperty(ICON_PROPERTY, YELLOW_ICON_ID)
        val singleFeatureThree = Feature.fromGeometry(
                Point.fromLngLat(88.36647033691406,
                        22.52016858599439))
        singleFeatureThree.addStringProperty(ICON_PROPERTY, RED_ICON_ID)

// Not adding a ICON_PROPERTY property to fourth and fifth features in order to show off the default
// nature of the match expression used in the example up above
        val singleFeatureFour = Feature.fromGeometry(
                Point.fromLngLat(78.42315673828125,
                        17.43320034474222))
        val singleFeatureFive = Feature.fromGeometry(
                Point.fromLngLat(80.16448974609375,
                        12.988500396985364))
        val symbolLayerIconFeatureList: MutableList<Feature> = ArrayList()
        symbolLayerIconFeatureList.add(singleFeatureOne)
        symbolLayerIconFeatureList.add(singleFeatureTwo)
        symbolLayerIconFeatureList.add(singleFeatureThree)
        symbolLayerIconFeatureList.add(singleFeatureFour)
        symbolLayerIconFeatureList.add(singleFeatureFive)
        return symbolLayerIconFeatureList
    }


    override fun onResume() {
        super.onResume()
        mapView!!.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView!!.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView!!.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView!!.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView!!.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mapboxMap != null) {
            mapboxMap.removeOnMapClickListener(this)
        }
        mapView!!.onDestroy()
    }


}
