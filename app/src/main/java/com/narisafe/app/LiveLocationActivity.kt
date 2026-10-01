package com.narisafe.app

import android.app.Activity
import android.os.Bundle
import com.google.firebase.firestore.FirebaseFirestore
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapView

class LiveLocationActivity : Activity() {

    private lateinit var mapView: MapView

    private val db =
        FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapLibre.getInstance(this)

        setContentView(R.layout.map_test)

        mapView =
            findViewById(R.id.mapView)

        mapView.onCreate(savedInstanceState)

        val sosId =
            intent.getStringExtra("sosId")

        mapView.getMapAsync { map ->

            mapView.addOnDidFinishLoadingMapListener {

                android.util.Log.d(
                    "NARI_LIVE_MAP",
                    "✅ MAP FINISHED LOADING"
                )
            }

            mapView.addOnDidFailLoadingMapListener {

                android.util.Log.e(
                    "NARI_LIVE_MAP",
                    "❌ MAP FAILED TO LOAD"
                )
            }

            mapView.addOnDidFinishLoadingStyleListener {

                android.util.Log.d(
                    "NARI_LIVE_MAP",
                    "✅ STYLE LOADED"
                )
            }

            map.setStyle(
                "https://demotiles.maplibre.org/pmtiles/raster/style-imagery.json"
            )
        }

        if (sosId == null) {

            android.util.Log.e(
                "NARI_LIVE_MAP",
                "❌ SOS ID IS NULL"
            )

            return
        }

        android.util.Log.d(
            "NARI_LIVE_MAP",
            "📍 Viewing SOS: $sosId"
        )

        db.collection("liveLocations")
            .whereEqualTo(
                "sosId",
                sosId
            )
            .addSnapshotListener { snapshot, error ->

                if (error != null) {

                    android.util.Log.e(
                        "NARI_LIVE_MAP",
                        "❌ FIRESTORE ERROR: ${error.message}"
                    )

                    return@addSnapshotListener
                }

                if (
                    snapshot == null ||
                    snapshot.isEmpty
                ) {

                    android.util.Log.d(
                        "NARI_LIVE_MAP",
                        "ℹ️ No live locations found"
                    )

                    return@addSnapshotListener
                }

                val latestDocument =
                    snapshot.documents.maxByOrNull { document ->

                        document.getLong("timestamp")
                            ?: 0L
                    }

                val latitude =
                    latestDocument
                        ?.getDouble("latitude")

                val longitude =
                    latestDocument
                        ?.getDouble("longitude")

                if (
                    latitude == null ||
                    longitude == null
                ) {

                    android.util.Log.e(
                        "NARI_LIVE_MAP",
                        "❌ Invalid location data"
                    )

                    return@addSnapshotListener
                }

                android.util.Log.d(
                    "NARI_LIVE_MAP",
                    "📍 LOCATION: $latitude, $longitude"
                )

                mapView.getMapAsync { map ->

                    val location =
                        org.maplibre.android.geometry.LatLng(
                            latitude,
                            longitude
                        )

                    map.clear()

                    map.addMarker(
                        org.maplibre.android.annotations.MarkerOptions()
                            .position(location)
                            .title("Emergency Location")
                    )

                    map.animateCamera(
                        org.maplibre.android.camera.CameraUpdateFactory
                            .newLatLngZoom(
                                location,
                                16.0
                            )
                    )
                }
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

    override fun onSaveInstanceState(
        outState: Bundle
    ) {

        mapView.onSaveInstanceState(
            outState
        )

        super.onSaveInstanceState(
            outState
        )
    }
}