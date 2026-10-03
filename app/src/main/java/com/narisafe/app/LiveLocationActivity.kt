package com.narisafe.app

import android.app.Activity
import android.os.Bundle
import com.google.firebase.firestore.FirebaseFirestore
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory

class LiveLocationActivity : Activity() {

    private lateinit var mapView: MapView

    private val db =
        FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ---------------------------------------------------------
        // INITIALIZE MAPLIBRE
        // ---------------------------------------------------------

        MapLibre.getInstance(this)

        setContentView(R.layout.map_test)

        mapView =
            findViewById(R.id.mapView)

        mapView.onCreate(savedInstanceState)

        // ---------------------------------------------------------
        // GET SOS ID
        // ---------------------------------------------------------

        val sosId =
            intent.getStringExtra("sosId")

        if (sosId == null) {

            android.util.Log.e(
                "NARI_LOCATION_MAP",
                "❌ SOS ID IS NULL"
            )

            return
        }

        android.util.Log.d(
            "NARI_LOCATION_MAP",
            "📍 Viewing SOS: $sosId"
        )

        // ---------------------------------------------------------
        // LOAD MAP
        // ---------------------------------------------------------

        mapView.getMapAsync { map ->

            android.util.Log.d(
                "NARI_LOCATION_MAP",
                "🗺️ MAP READY"
            )

            // Normal map background
            map.setStyle(
                "https://tiles.openfreemap.org/styles/liberty"
            ){

                android.util.Log.d(
                    "NARI_LOCATION_MAP",
                    "✅ MAP STYLE LOADED"
                )

                // -------------------------------------------------
                // GET SAVED SOS LOCATION
                // -------------------------------------------------

                db.collection("sosAlerts")
                    .document(sosId)
                    .get()
                    .addOnSuccessListener { document ->

                        if (!document.exists()) {

                            android.util.Log.e(
                                "NARI_LOCATION_MAP",
                                "❌ SOS DOCUMENT NOT FOUND"
                            )

                            return@addOnSuccessListener
                        }

                        // -----------------------------------------
                        // GET INITIAL LATITUDE
                        // -----------------------------------------

                        val latitude =
                            document.getDouble(
                                "initialLatitude"
                            )

                        // -----------------------------------------
                        // GET INITIAL LONGITUDE
                        // -----------------------------------------

                        val longitude =
                            document.getDouble(
                                "initialLongitude"
                            )

                        if (
                            latitude == null ||
                            longitude == null
                        ) {

                            android.util.Log.e(
                                "NARI_LOCATION_MAP",
                                "❌ SOS LOCATION NOT FOUND"
                            )

                            return@addOnSuccessListener
                        }

                        android.util.Log.d(
                            "NARI_LOCATION_MAP",
                            "📍 SOS LOCATION: $latitude, $longitude"
                        )

                        // -------------------------------------------------
                        // CREATE LOCATION
                        // -------------------------------------------------

                        val location =
                            LatLng(
                                latitude,
                                longitude
                            )

                        // -------------------------------------------------
                        // CLEAR OLD MARKERS
                        // -------------------------------------------------

                        map.clear()

                        // -------------------------------------------------
                        // ADD RED LOCATION MARKER
                        // -------------------------------------------------

                        map.addMarker(
                            MarkerOptions()
                                .position(location)
                                .title("Emergency SOS Location")
                        )

                        // -------------------------------------------------
                        // MOVE CAMERA TO LOCATION
                        // -------------------------------------------------

                        map.animateCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(
                                    location,
                                    16.0
                                )
                        )

                        android.util.Log.d(
                            "NARI_LOCATION_MAP",
                            "✅ RED LOCATION MARKER SHOWN"
                        )
                    }
                    .addOnFailureListener { error ->

                        android.util.Log.e(
                            "NARI_LOCATION_MAP",
                            "❌ FIRESTORE ERROR: ${error.message}"
                        )
                    }
            }
        }
    }

    // ---------------------------------------------------------
    // MAP LIFECYCLE
    // ---------------------------------------------------------

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