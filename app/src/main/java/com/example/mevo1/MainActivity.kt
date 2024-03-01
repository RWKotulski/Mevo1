package com.example.mevo1

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.circleLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    data class WellingtonVehiclesResponse(
        val type: String,
        val features: List<VehicleFeature>
    )

    data class VehicleFeature(
        val type: String,
        val properties: VehicleProperties,
        val geometry: VehicleGeometry
    )

    data class VehicleProperties(
        val iconUrl: String
    )

    data class VehicleGeometry(
        val type: String,
        val coordinates: List<Double>
    )


    private fun fetchWellingtonVehicles(style: Style) = lifecycleScope.launch {
        try {
            val response = createRetrofitService().getWellingtonVehicles()
            if (response.isSuccessful) {
                // Convert the response body to a string. This is safe to call on the main thread
                // because the network call has already completed asynchronously.
                val geoJsonString = response.body()?.string() ?: ""

                // Check if the GeoJSON string is not empty
                if (geoJsonString.isNotEmpty()) {
                    // Use the GeoJSON string to create a source and add it to the map
                    val source = geoJsonSource("source-id") { data(geoJsonString) }
                    style.addSource(source)
                    style.addLayer(circleLayer("layer-id", "source-id") {
                        circleColor(Color.parseColor("#007cbf"))
                        circleRadius(8.0)
                    })
                }
            } else {
                Log.e("FETCH FAILED", "Fetch failed: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("NETWORK ERROR", "There was a network error: ${e.localizedMessage}")
        }
    }






    private fun createRetrofitService(): ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.mevo.co.nz/public/vehicles/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }


    private lateinit var mapboxMap: MapboxMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mapView = MapView(this)
        setContentView(mapView)
        mapboxMap = mapView.mapboxMap.apply {
            setCamera(
                CameraOptions.Builder()
                    .center(Point.fromLngLat(174.777969, -41.276825))
                    .zoom(16.0)
                    .build()
            )
            loadStyle(Style.STANDARD) { fetchWellingtonVehicles(it) }
        }
    }


}