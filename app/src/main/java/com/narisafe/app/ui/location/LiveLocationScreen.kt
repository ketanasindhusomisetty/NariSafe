package com.narisafe.app.ui.location

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.firebase.firestore.FirebaseFirestore
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.geometry.LatLng

@Composable
fun LiveLocationScreen(
    sosId: String
) {

    val context = LocalContext.current

    MapLibre.getInstance(context)

    val db = FirebaseFirestore.getInstance()

    val mapView = remember {

        val options =
            MapLibreMapOptions
                .createFromAttributes(context)
                .textureMode(true)

        MapView(
            context,
            options
        )
    }

    LaunchedEffect(sosId) {

        db.collection("liveLocations")
            .whereEqualTo("sosId", sosId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    return@addSnapshotListener
                }

                val latestDocument =
                    snapshot.documents.maxByOrNull { document ->

                        document.getLong("timestamp") ?: 0L
                    }

                val latitude =
                    latestDocument
                        ?.getDouble("latitude")

                val longitude =
                    latestDocument
                        ?.getDouble("longitude")

                if (
                    latitude != null &&
                    longitude != null
                ) {

                    mapView.getMapAsync { map ->

                        val location =
                            LatLng(
                                latitude,
                                longitude
                            )

                        map.animateCamera(
                            org.maplibre.android.camera.CameraUpdateFactory
                                .newLatLngZoom(
                                    location,
                                    15.0
                                )
                        )
                    }
                }
            }
    }

    AndroidView(

        factory = {

            mapView.apply {

                onCreate(null)

                getMapAsync { map ->

                    map.setStyle(
                        "https://demotiles.maplibre.org/pmtiles/raster/style-imagery.json"
                    ) {

                        map.cameraPosition =
                            org.maplibre.android.camera.CameraPosition.Builder()
                                .target(
                                    org.maplibre.android.geometry.LatLng(
                                        17.3850,
                                        78.4867
                                    )
                                )
                                .zoom(12.0)
                                .build()
                    }
                }
            }
        },

        modifier = Modifier.fillMaxSize()
    )

    DisposableEffect(Unit) {

        mapView.onStart()
        mapView.onResume()

        onDispose {

            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }
}