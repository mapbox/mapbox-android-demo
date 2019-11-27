package com.mapbox.mapboxandroiddemo.examples.javaservices

import android.graphics.Color
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.mapboxandroiddemo.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.turf.TurfConstants.UNIT_KILOMETERS
import com.mapbox.turf.TurfMeta
import com.mapbox.turf.TurfTransformation
import kotlinx.android.synthetic.main.activity_lab_turf_thick_bordered_circle.*

/**
 * Use [TurfTransformation.circle] to eventually create a thick border around a circular-shaped [Polygon].
 */
class KotlinBorderedCircleActivity : AppCompatActivity(), MapboxMap.OnMapClickListener {

    private var mapboxMap: MapboxMap ? = null
    private lateinit var circlePolygonArea: Polygon
    private var lastClickPoint = STARTING_CAMERA_POINT
    private var circleRadiusUnits = UNIT_KILOMETERS
    private var currentSetCircleRadius = 25
    private var currentSetCircleBorderDifference = 2
    private var borderDifferenceUnitSeekbarMax = 200
    private var radiusSeekbarMax = 500
    private var circleOpacity = .7f

    companion object {
        private const val CIRCLE_CENTER_SOURCE_ID = "CIRCLE_CENTER_SOURCE_ID"
        private const val CIRCLE_COLOR = "#2643ff"
        private const val CIRCLE_BORDER_COLOR = "#132287"
        private const val CIRCLE_BORDER_GEOJSON_SOURCE_ID = "CIRCLE_BORDER_GEOJSON_SOURCE_ID"
        private const val CIRCLE_GEOJSON_SOURCE_ID = "CIRCLE_GEOJSON_SOURCE_ID"
        private const val CIRCLE_BORDER_LAYER_ID = "CIRCLE_BORDER_LAYER_ID"
        private const val CIRCLE_LAYER_ID = "CIRCLE_LAYER_ID"
        private const val CIRCLE_STEPS = 360
        private const val RADIUS_SEEKBAR_DIFFERENCE = 1
        private const val BORDER_DIFFERENCE_SEEKBAR_DIFFERENCE = 1
        private val STARTING_CAMERA_POINT = Point.fromLngLat(33.22424223, 34.99415092)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token))

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_lab_turf_thick_bordered_circle)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.Builder().fromUri(Style.MAPBOX_STREETS)
                    .withSource(GeoJsonSource(CIRCLE_CENTER_SOURCE_ID,
                            Feature.fromGeometry(STARTING_CAMERA_POINT)))
                    .withSource(GeoJsonSource(CIRCLE_BORDER_GEOJSON_SOURCE_ID))
                    .withSource(GeoJsonSource(CIRCLE_GEOJSON_SOURCE_ID))
                    .withLayer(FillLayer(CIRCLE_BORDER_LAYER_ID, CIRCLE_BORDER_GEOJSON_SOURCE_ID).withProperties(
                            fillColor(Color.parseColor(CIRCLE_BORDER_COLOR))
                    ))
                    .withLayer(FillLayer(CIRCLE_LAYER_ID, CIRCLE_GEOJSON_SOURCE_ID).withProperties(
                            fillColor(Color.parseColor(CIRCLE_COLOR)),
                            fillOpacity(circleOpacity)
                    ))) {

                this@KotlinBorderedCircleActivity.mapboxMap = mapboxMap

                drawPolygonCircle(lastClickPoint)

                drawCircleBorder(lastClickPoint)

                mapboxMap.addOnMapClickListener(this@KotlinBorderedCircleActivity)

                Toast.makeText(this@KotlinBorderedCircleActivity,
                        getString(R.string.tap_on_map_to_move_bordered_circle), Toast.LENGTH_SHORT).show()

                initRadiusSeekbar()

                initBorderDifferenceSeekbar()
            }
        }
    }

    /**
     * Initialize the seekbar slider to adjust the circle radius
     */
    private fun initRadiusSeekbar() {
        thick_bordered_circle_radius_seekbar?.apply {
            max = radiusSeekbarMax + RADIUS_SEEKBAR_DIFFERENCE
            incrementProgressBy(RADIUS_SEEKBAR_DIFFERENCE)
            progress = radiusSeekbarMax / 2

            thick_bordered_circle_radius_textview?.text = String.format(getString(
                    R.string.thick_bordered_circle_radius), progress)

            this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    adjustCircleRadius(thick_bordered_circle_radius_textview, seekBar.progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    // Not needed in this example.
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    adjustCircleRadius(thick_bordered_circle_radius_textview, seekBar.progress)
                }
            })
        }
    }

    /**
     * Initialize the seekbar slider to adjust the distance that represents the circle border size
     */
    private fun initBorderDifferenceSeekbar() {
        thick_bordered_circle_border_difference_seekbar?.apply {
            max = borderDifferenceUnitSeekbarMax + BORDER_DIFFERENCE_SEEKBAR_DIFFERENCE
            incrementProgressBy(BORDER_DIFFERENCE_SEEKBAR_DIFFERENCE)
            progress = borderDifferenceUnitSeekbarMax / 2

            thick_bordered_circle_border_difference_textview?.text = String.format(getString(
                    R.string.thick_bordered_circle_border_difference), progress)

            this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    adjustCircleBorderDifference(thick_bordered_circle_border_difference_textview,
                            seekBar.progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    // Not needed in this example.
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    adjustCircleBorderDifference(thick_bordered_circle_border_difference_textview,
                            seekBar.progress)
                }
            })
        }
    }

    /**
     * Make the necessary GeoJSON adjustments to account for a new circle radius
     *
     * @param textView the TextView to update
     * @param progress the new progress value to add to the TextView
     */
    private fun adjustCircleRadius(textView: TextView, progress: Int) {
        adjustSeekBarProgressTextView(R.string.thick_bordered_circle_radius, textView, progress)
        currentSetCircleRadius = progress
        drawPolygonCircle(lastClickPoint)
        drawCircleBorder(lastClickPoint)
    }

    /**
     * Make the necessary GeoJSON adjustments to account for a new circle border size
     *
     * @param textView the TextView to update
     * @param progress the new progress value to add to the TextView
     */
    private fun adjustCircleBorderDifference(textView: TextView, progress: Int) {
        adjustSeekBarProgressTextView(R.string.thick_bordered_circle_border_difference, textView, progress)
        currentSetCircleBorderDifference = progress
        drawCircleBorder(lastClickPoint)
    }

    /**
     * Adjust the textView
     *
     * @param string the string resource to update
     * @param textView the TextView to update
     * @param progress the new progress value to add to the TextView
     */
    private fun adjustSeekBarProgressTextView(string: Int, textView: TextView, progress: Int) {
        var newProgress = progress
        newProgress /= 1
        newProgress *= 1
        textView.text = String.format(getString(string), newProgress)
    }

    /**
     * Make necessary camera and GeoJSON adjustments when the map is tapped on.
     * @param mapClickLatLng where the map was tapped
     */
    override fun onMapClick(mapClickLatLng: LatLng): Boolean {
        mapboxMap?.easeCamera(CameraUpdateFactory.newLatLng(mapClickLatLng))
        lastClickPoint = Point.fromLngLat(mapClickLatLng.longitude, mapClickLatLng.latitude)
        drawPolygonCircle(lastClickPoint)
        drawCircleBorder(lastClickPoint)
        return true
    }

    /**
     * Update the [FillLayer] based on the GeoJSON retrieved via [getTurfPolygon].
     *
     * @param newCircleCenter the center coordinate to be used in the Turf calculation.
     */
    private fun drawPolygonCircle(newCircleCenter: Point) {
        mapboxMap?.getStyle { style ->
            // Use Turf to calculate the coordinates for the circular-shaped Polygon
            circlePolygonArea = getTurfPolygon(newCircleCenter, currentSetCircleRadius.toDouble())

            // Set the source with the circle's GeoJSON
            val polygonCircleSource = style.getSourceAs<GeoJsonSource>(CIRCLE_GEOJSON_SOURCE_ID)
            polygonCircleSource?.setGeoJson(Polygon.fromOuterInner(
                    LineString.fromLngLats(TurfMeta.coordAll(circlePolygonArea, false))))
        }
    }

    /**
     * Update the [FillLayer] based on the GeoJSON retrieved via [getTurfPolygon].
     *
     * @param newCircleCenter the center coordinate to be used in the Turf calculation.
     */
    private fun drawCircleBorder(newCircleCenter: Point) {
        mapboxMap?.getStyle {
            // Use Turf to calculate the coordinates for the circular-shaped Polygon's border
            val circleBorderPolygon = getTurfPolygon(newCircleCenter,
                    currentSetCircleRadius.toDouble() - currentSetCircleBorderDifference)

            // Set the source with the border's GeoJSON
            val circleBorderSource = it.getSourceAs<GeoJsonSource>(CIRCLE_BORDER_GEOJSON_SOURCE_ID)
            circleBorderSource?.setGeoJson(Polygon.fromOuterInner(
                    // Create outer LineString
                    LineString.fromLngLats(TurfMeta.coordAll(circleBorderPolygon, false)),
                    // Create inter LineString
                    LineString.fromLngLats(TurfMeta.coordAll(circlePolygonArea, false))
            ))
        }
    }

    /**
     * Use the Turf library [TurfTransformation.circle] method to
     * retrieve a [Polygon].
     *
     * @param centerPoint a [Point] which the circle will center around
     * @param radius the radius of the circle
     * @return a [Polygon] which represents the newly created circle
     */
    private fun getTurfPolygon(centerPoint: Point, radius: Double): Polygon {
        return TurfTransformation.circle(centerPoint, radius, CIRCLE_STEPS, circleRadiusUnits)
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    public override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    public override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapboxMap?.removeOnMapClickListener(this)
        mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
}