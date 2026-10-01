package com.narisafe.app

import android.app.Activity
import android.os.Bundle
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapView

class MapTestActivity : Activity() {

    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapLibre.getInstance(this)

        setContentView(R.layout.map_test)

        mapView = findViewById(R.id.mapView)

        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync { map ->

            mapView.addOnDidFinishLoadingMapListener {
                android.util.Log.d(
                    "NARI_MAP",
                    "✅ MAP FINISHED LOADING"
                )
            }

            mapView.addOnDidFailLoadingMapListener {
                android.util.Log.e(
                    "NARI_MAP",
                    "❌ MAP FAILED TO LOAD"
                )
            }

            mapView.addOnDidFinishLoadingStyleListener {
                android.util.Log.d(
                    "NARI_MAP",
                    "✅ STYLE LOADED"
                )
            }

            map.setStyle(
                "https://demotiles.maplibre.org/pmtiles/raster/style-imagery.json"
            )
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
        mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mapView.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }
}