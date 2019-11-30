package com.mapbox.mapboxandroiddemo.examples.labs

import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.matching.v5.MapboxMapMatching
import com.mapbox.api.matching.v5.models.MapMatchingResponse
import com.mapbox.core.constants.Constants
import com.mapbox.core.exceptions.ServicesException
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxandroiddemo.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.activity_lab_drag_to_draw_map_matched_route.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.Objects.requireNonNull

/**
 * Use the Android system [View.OnTouchListener] to draw
 * an polygon and/or a line. Also perform a search for data points within the drawn polygon area.
 */
class DragToDrawMapMatchedRouteActivity : AppCompatActivity() {
    private var mapboxMap: MapboxMap? = null
    private var freehandDrawingPointList: MutableList<Point> = ArrayList()
    private var finalChainedMatchPointList: MutableList<Point> = ArrayList()
    private var index = 0
    private var numberOf100MultiplesInTotalDrawnPoints = 0
    private var remainingLeftoverPoints = 0

    private val customOnTouchListener = OnTouchListener { view, motionEvent ->
        mapboxMap?.projection?.fromScreenLocation(
                PointF(motionEvent.x, motionEvent.y))?.apply {
            freehandDrawingPointList.add(Point.fromLngLat(this.longitude, this.latitude))
        }

        Log.d(TAG,"onStyleLoaded: freehandDrawingPointList.size() = ${freehandDrawingPointList.size}")

        mapboxMap?.getStyle { style: Style ->

            // Draw the line on the map as the finger is dragged along the map
            val drawLineSource = style.getSourceAs<GeoJsonSource>(FREEHAND_DRAW_LINE_LAYER_SOURCE_ID)
            drawLineSource?.setGeoJson(LineString.fromLngLats(freehandDrawingPointList))

            // Take next actions when the on-screen drawing is finished
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                enableMapMovement()

                loading_route_progress_spinner.visibility = View.VISIBLE

                when {
                    freehandDrawingPointList.size < 2 -> {
                        Toast.makeText(this@DragToDrawMapMatchedRouteActivity,
                                R.string.draw_a_longer_route, Toast.LENGTH_SHORT).show()
                    }
                    freehandDrawingPointList.size <= 100 -> {
                        requestMapMatched(freehandDrawingPointList, true,false, false)
                    }
                    else -> {
                        numberOf100MultiplesInTotalDrawnPoints = freehandDrawingPointList.size / 100
                        Log.d(TAG, "onStyleLoaded: numberOf100MultiplesInTotalDrawnPoints = $numberOf100MultiplesInTotalDrawnPoints")

                        remainingLeftoverPoints = freehandDrawingPointList.size - numberOf100MultiplesInTotalDrawnPoints * 100
                        Log.d(TAG,"onStyleLoaded: remainingLeftoverPoints = $remainingLeftoverPoints")
        //                    Log.d(TAG,"onStyleLoaded: freehandDrawingPointList.size()%100 = ${freehandDrawingPointList.size % 100}")
                        makeLinkedCall(index)
                    }
                }
            }
        }
        true
    }

    private fun makeLinkedCall(indexMultiple: Int) {
        val pointListToMatch: List<Point> = freehandDrawingPointList.subList(if (indexMultiple == 0) indexMultiple
                * 100 else indexMultiple * 100 + 1, indexMultiple * 100 + 100)
        Log.d(TAG,"makeLinkedCall: pointListToMatch = ${pointListToMatch.size}")
        requestMapMatched(pointListToMatch, false,true, false)
    }

    private fun makeMatchCallForRemainingPoints() {
        if (freehandDrawingPointList.size % 100 != 0) {
            Log.d(TAG,"makeMatchCallForRemainingPoints: " +
                    "freehandDrawingPointList.size() - numberOf100MultiplesInTotalDrawnPoints * 100 = ${
            (freehandDrawingPointList.size - numberOf100MultiplesInTotalDrawnPoints * 100)}")
            val remainingModuloPointsToMatch: List<Point> = freehandDrawingPointList.subList(
                    numberOf100MultiplesInTotalDrawnPoints * 100 + 1,
                    freehandDrawingPointList.size - numberOf100MultiplesInTotalDrawnPoints * 100)

            Log.d(TAG,"remainingModuloPointsToMatch size = ${remainingModuloPointsToMatch.size}")
            requestMapMatched(remainingModuloPointsToMatch, false, true, true)
        }
    }

    private fun requestMapMatched(points: List<Point>,
                                  singleCallOnly: Boolean,
                                  chainingMatchedPolylineTogether: Boolean,
                                  finalChainedCall: Boolean) {


        val radiusArray = arrayOfNulls<Double>(points.size)

        for (x in points.indices) {
            radiusArray[x] = DESIRED_MATCH_RADIUS
        }

        val client = MapboxMapMatching.builder()
                .accessToken(Mapbox.getAccessToken()!!)
                .profile(DirectionsCriteria.PROFILE_WALKING)
                .coordinates(points)
                .tidy(true)
                .overview(DirectionsCriteria.OVERVIEW_SIMPLIFIED)
                .radiuses(*radiusArray)
                .geometries(DirectionsCriteria.GEOMETRY_POLYLINE6)
                .build()
        try {

            // Execute the API call and handle the response.
            client.enqueueCall(object : Callback<MapMatchingResponse?> {
                override fun onResponse(call: Call<MapMatchingResponse?>,
                                        response: Response<MapMatchingResponse?>) {
                    if (response.isSuccessful && response.body() != null) {

                        try {

                            response.body()?.matchings()?.let {

                                // Matched route from ingle call
                                val lineStringFromApiResponse = LineString.fromPolyline(requireNonNull(
                                        it[0].geometry())!!, Constants.PRECISION_6)


                                if (singleCallOnly) {
                                    displayLineStringOnMap(LineString.fromLngLats(
                                            lineStringFromApiResponse.coordinates()))
                                    Toast.makeText(this@DragToDrawMapMatchedRouteActivity,
                                            R.string.drawn_route_now_matched, Toast.LENGTH_SHORT).show()
                                    setUiAfterMapMatching()
                                    resetAllLists()
                                } else {
                                    enableMapMovement()
                                    finalChainedMatchPointList.addAll(lineStringFromApiResponse.coordinates())
                                    Log.d(TAG,"onResponse: finalChainedMatchPointList size =" +
                                            " ${finalChainedMatchPointList.size}")

                                    Log.d(TAG,"index before = $index")
                                    if (index == numberOf100MultiplesInTotalDrawnPoints) {
                                        // Process the final map matching call
                                        Log.d(TAG,"finalChainedCall = $finalChainedCall")
                                        if (finalChainedCall) {
                                            Log.d(TAG,"finalChainedCall = true &&" +
                                                    "finalChainedMatchPointList size = ${finalChainedMatchPointList.size}")
                                            displayLineStringOnMap(LineString.fromLngLats(finalChainedMatchPointList))
                                            setUiAfterMapMatching()
                                            resetAllLists()
                                        } else {
                                            makeMatchCallForRemainingPoints()
                                        }
                                    } else {
                                        // Keep making match map calls
                                        index++
                                        Log.d(TAG,"onResponse: index = $index")
                                        makeLinkedCall(index)
                                    }
                                }
                            }
                        } catch (nullPointerException: NullPointerException) {
                            Log.d(TAG,"onResponse: nullPointerException = $nullPointerException")
                        }
                    } else { // If the response code does not response "OK" an error has occurred.
                        Log.d(TAG,"MapboxMapMatching failed with ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<MapMatchingResponse?>, throwable: Throwable) {
                    Log.d(TAG,"onFailure() throwable = $throwable")
                    Log.d(TAG,"onFailure() throwable call = ${call.request().url()}")
                    Toast.makeText(this@DragToDrawMapMatchedRouteActivity,
                            R.string.move_map_explore_drawn_matched_route_timeout, Toast.LENGTH_SHORT).show()
                    clear_map_for_new_draw_fab.visibility = View.VISIBLE
                    loading_route_progress_spinner.visibility = View.INVISIBLE
                }
            })
        } catch (servicesException: ServicesException) {
            Log.d(TAG,"servicesException = $servicesException")
        }
    }

    private fun setUiAfterMapMatching() {
        Toast.makeText(this@DragToDrawMapMatchedRouteActivity,
                R.string.move_map_explore_drawn_matched_route, Toast.LENGTH_SHORT).show()
        enableMapMovement()
        clear_map_for_new_draw_fab.visibility = View.VISIBLE
        loading_route_progress_spinner.visibility = View.INVISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.

        Mapbox.getInstance(this, getString(R.string.access_token))

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_lab_drag_to_draw_map_matched_route)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.Builder().fromUri(Style.MAPBOX_STREETS)
                    .withSource(GeoJsonSource(FREEHAND_DRAW_LINE_LAYER_SOURCE_ID))
            ) { style ->

                this@DragToDrawMapMatchedRouteActivity.mapboxMap = mapboxMap

                val drawnAndMatchedLineLayer = LineLayer(FREEHAND_DRAW_LINE_LAYER_ID,
                        FREEHAND_DRAW_LINE_LAYER_SOURCE_ID).withProperties(
                        PropertyFactory.lineWidth(LINE_WIDTH),
                        PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                        PropertyFactory.lineOpacity(LINE_OPACITY),
                        PropertyFactory.lineColor(Color.parseColor(LINE_COLOR)))

                if (style.getLayer("road-label") != null) {
                    style.addLayerBelow(drawnAndMatchedLineLayer, "road-label")
                } else {
                    style.addLayer(drawnAndMatchedLineLayer)
                }

                enableRouteDrawing()

                Toast.makeText(this@DragToDrawMapMatchedRouteActivity,
                        getString(R.string.draw_route_instruction), Toast.LENGTH_SHORT).show()

                clear_map_for_new_draw_fab.setOnClickListener(View.OnClickListener {
                    clear_map_for_new_draw_fab.visibility = View.INVISIBLE
                    resetAllLists()
                    // Add empty Feature array to the sources
                    val drawLineSource = style.getSourceAs<GeoJsonSource>(FREEHAND_DRAW_LINE_LAYER_SOURCE_ID)
                    drawLineSource?.setGeoJson(FeatureCollection.fromFeatures(arrayOf()))
                    enableRouteDrawing()
                })
            }
        }
    }

    private fun resetAllLists() {
        freehandDrawingPointList = ArrayList()
        finalChainedMatchPointList = ArrayList()
    }

    /**
     * Enable moving the map with regular Maps SDK gesture detection.
     */
    private fun enableMapMovement() {
        mapView.setOnTouchListener(null)
    }

    /**
     * Enable drawing on the map by setting the custom touch listener on the [MapView]
     */
    private fun enableRouteDrawing() {
        mapView.setOnTouchListener(customOnTouchListener)
    }

    private fun displayLineStringOnMap(lineString: LineString) {
        mapboxMap?.getStyle { style ->
            style.getSourceAs<GeoJsonSource>(FREEHAND_DRAW_LINE_LAYER_SOURCE_ID)?.
                    setGeoJson(Feature.fromGeometry(lineString))
        }
    }

    // Add the mapView lifecycle to the activity's lifecycle methods
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

    companion object {
        private const val TAG = "DragToDrawMap2"
        private const val FREEHAND_DRAW_LINE_LAYER_SOURCE_ID = "FREEHAND_DRAW_LINE_LAYER_SOURCE_ID"
        private const val FREEHAND_DRAW_LINE_LAYER_ID = "FREEHAND_DRAW_LINE_LAYER_ID"
        // Adjust the static final variables to change the example's UI
        private const val LINE_COLOR = "#e60800"
        private const val LINE_WIDTH = 5f
        private const val LINE_OPACITY = 1f
        private const val DESIRED_MATCH_RADIUS = 30.0 // Must be between 0.0 and 50.0
    }
}