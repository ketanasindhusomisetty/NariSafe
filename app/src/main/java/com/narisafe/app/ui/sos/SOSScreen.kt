package com.narisafe.app.ui.sos

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.narisafe.app.services.LocationService
import java.net.URLEncoder
import kotlinx.coroutines.delay
import android.content.ActivityNotFoundException

@Composable
fun SOSScreen(
    onBack: () -> Unit,
    onSosActivated: (String) -> Unit,
    onSosEnded: () -> Unit,
    activeSosId: String?
) {

    val context = LocalContext.current

    val db =
        FirebaseFirestore.getInstance()

    val auth =
        FirebaseAuth.getInstance()

    LocationService.initialize(context)

    // ---------------------------------------------------------
    // SMS PERMISSION
    // ---------------------------------------------------------

    val smsPermissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {

                Toast.makeText(
                    context,
                    "SMS permission granted ✅",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Toast.makeText(
                    context,
                    "SMS permission is required for emergency alerts",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    // ---------------------------------------------------------
    // PHONE CALL PERMISSION
    // ---------------------------------------------------------

    val callPermissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {

                Toast.makeText(
                    context,
                    "Phone call permission granted ✅",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Toast.makeText(
                    context,
                    "Phone call permission is required",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    // ---------------------------------------------------------
    // SOS STATE
    // ---------------------------------------------------------

    var sosActivated by remember {

        mutableStateOf(
            activeSosId != null
        )
    }

    var countdown by remember {

        mutableStateOf(5)
    }

    var cancelled by remember {

        mutableStateOf(false)
    }

    var locationMessage by remember {

        mutableStateOf(

            if (activeSosId != null) {

                "🚨 Existing SOS is still active"

            } else {

                ""
            }
        )
    }

    // ---------------------------------------------------------
    // LOCATION PERMISSION
    // ---------------------------------------------------------

    var hasLocationPermission by remember {

        mutableStateOf(

            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||

                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(

            contract =
                ActivityResultContracts.RequestMultiplePermissions()

        ) { permissions ->

            hasLocationPermission =

                permissions[
                    Manifest.permission.ACCESS_FINE_LOCATION
                ] == true ||

                        permissions[
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ] == true
        }

    // ---------------------------------------------------------
    // REQUEST PERMISSIONS
    // ---------------------------------------------------------

    LaunchedEffect(Unit) {

        if (
            activeSosId == null &&
            !hasLocationPermission
        ) {

            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            smsPermissionLauncher.launch(
                Manifest.permission.SEND_SMS
            )
        }

        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            callPermissionLauncher.launch(
                Manifest.permission.CALL_PHONE
            )
        }
    }

    // ---------------------------------------------------------
    // SEND EMERGENCY SMS
    // ---------------------------------------------------------

    fun sendEmergencySMS(
        latitude: Double,
        longitude: Double
    ) {

        val userId =
            auth.currentUser?.uid

        if (userId == null) {

            locationMessage =
                "⚠️ User is not logged in"

            return
        }

        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            smsPermissionLauncher.launch(
                Manifest.permission.SEND_SMS
            )

            return
        }

        db.collection("emergencyContacts")
            .whereEqualTo(
                "userId",
                userId
            )
            .get()
            .addOnSuccessListener { result ->

                if (result.isEmpty) {

                    locationMessage =
                        "⚠️ No emergency contacts found"

                    return@addOnSuccessListener
                }

                val message =

                    "🚨 NariSafe Emergency Alert!\n\n" +
                            "I need help. My SOS has been activated.\n\n" +
                            "📍 My current location:\n" +
                            "https://maps.google.com/?q=$latitude,$longitude"

                val smsManager =
                    SmsManager.getDefault()

                var smsSent =
                    false

                for (
                document in result.documents
                ) {

                    val phone =
                        document.getString("phone")

                    if (
                        !phone.isNullOrBlank()
                    ) {

                        try {

                            smsManager.sendTextMessage(
                                phone,
                                null,
                                message,
                                null,
                                null
                            )

                            smsSent =
                                true

                        } catch (
                            exception: Exception
                        ) {

                            locationMessage =
                                "⚠️ Failed to send SMS to $phone"
                        }
                    }
                }

                if (smsSent) {

                    Toast.makeText(
                        context,
                        "🚨 Emergency SMS sent!",
                        Toast.LENGTH_LONG
                    ).show()

                    locationMessage =
                        "📱 Emergency SMS sent to your contacts"
                }
            }
            .addOnFailureListener { error ->

                locationMessage =
                    error.message
                        ?: "Failed to load emergency contacts"
            }
    }

    // ---------------------------------------------------------
    // CALL PRIMARY EMERGENCY CONTACT
    // ---------------------------------------------------------

    fun callPrimaryEmergencyContact() {

        val userId =
            auth.currentUser?.uid

        if (userId == null) {
            return
        }

        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            callPermissionLauncher.launch(
                Manifest.permission.CALL_PHONE
            )

            return
        }

        db.collection("emergencyContacts")
            .whereEqualTo(
                "userId",
                userId
            )
            .whereEqualTo(
                "isPrimary",
                true
            )
            .get()
            .addOnSuccessListener { result ->

                if (result.isEmpty) {

                    Toast.makeText(
                        context,
                        "⚠️ No Primary Emergency Contact found",
                        Toast.LENGTH_LONG
                    ).show()

                    return@addOnSuccessListener
                }

                val phone =
                    result.documents[0]
                        .getString("phone")

                if (phone.isNullOrBlank()) {

                    Toast.makeText(
                        context,
                        "⚠️ Primary contact has no phone number",
                        Toast.LENGTH_LONG
                    ).show()

                    return@addOnSuccessListener
                }

                try {

                    val callIntent =
                        Intent(
                            Intent.ACTION_CALL,
                            Uri.parse(
                                "tel:$phone"
                            )
                        )

                    context.startActivity(
                        callIntent
                    )

                } catch (
                    exception: Exception
                ) {

                    Toast.makeText(
                        context,
                        "❌ Unable to make emergency call",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnFailureListener { error ->

                Toast.makeText(
                    context,
                    error.message
                        ?: "Failed to find primary contact",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    // ---------------------------------------------------------
    // SHARE SOS ON WHATSAPP
    // ---------------------------------------------------------

// ---------------------------------------------------------
// SHARE SOS ON WHATSAPP
// ---------------------------------------------------------

    fun shareSOSOnWhatsApp(
        latitude: Double,
        longitude: Double
    ) {

        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(
                context,
                "⚠️ User is not logged in",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        db.collection("emergencyContacts")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isPrimary", true)
            .get()
            .addOnSuccessListener { result ->

                if (result.isEmpty) {
                    Toast.makeText(
                        context,
                        "⚠️ No Primary Emergency Contact found",
                        Toast.LENGTH_LONG
                    ).show()

                    return@addOnSuccessListener
                }

                val phone = result.documents[0]
                    .getString("phone")

                if (phone.isNullOrBlank()) {
                    Toast.makeText(
                        context,
                        "⚠️ Primary contact has no phone number",
                        Toast.LENGTH_LONG
                    ).show()

                    return@addOnSuccessListener
                }

                val cleanPhone = phone
                    .replace("+", "")
                    .replace(" ", "")
                    .replace("-", "")
                    .replace("(", "")
                    .replace(")", "")

                // Google Maps location link
                val mapsUrl =
                    "https://www.google.com/maps?q=$latitude,$longitude"

                // WhatsApp message
                val message =
                    "🚨 NariSafe Emergency Alert!\n\n" +
                            "I need help. My SOS is currently active.\n\n" +
                            "📍 My current location:\n" +
                            mapsUrl

                val encodedMessage =
                    URLEncoder.encode(
                        message,
                        "UTF-8"
                    )

                // WhatsApp direct chat URL
                val whatsappUri =
                    Uri.parse(
                        "https://wa.me/$cleanPhone?text=$encodedMessage"
                    )

                try {

                    val whatsappUri = Uri.parse(
                        "whatsapp://send?phone=$cleanPhone&text=$encodedMessage"
                    )

                    val whatsappIntent = Intent(
                        Intent.ACTION_VIEW,
                        whatsappUri
                    ).apply {
                        setPackage("com.whatsapp")
                    }

                    context.startActivity(whatsappIntent)

                } catch (exception: ActivityNotFoundException) {

                    Toast.makeText(
                        context,
                        "⚠️ WhatsApp is not installed on this device",
                        Toast.LENGTH_LONG
                    ).show()

                } catch (exception: Exception) {

                    Toast.makeText(
                        context,
                        "❌ Unable to open WhatsApp: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnFailureListener { error ->

                Toast.makeText(
                    context,
                    error.message
                        ?: "Failed to find primary contact",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    // ---------------------------------------------------------
    // COUNTDOWN + SOS ACTIVATION
    // ---------------------------------------------------------

    LaunchedEffect(
        hasLocationPermission,
        activeSosId
    ) {

        // Existing SOS = DO NOT START COUNTDOWN
        if (
            activeSosId != null ||
            !hasLocationPermission ||
            cancelled
        ) {

            return@LaunchedEffect
        }

        while (
            countdown > 0 &&
            !cancelled
        ) {

            delay(1000)

            countdown--
        }

        if (
            !cancelled &&
            countdown == 0
        ) {

            val userId =
                auth.currentUser?.uid

            if (userId == null) {

                locationMessage =
                    "⚠️ User is not logged in"

                return@LaunchedEffect
            }

            // -------------------------------------------------
            // GET CURRENT LOCATION
            // -------------------------------------------------

            locationMessage =
                "📍 Getting your current location..."

            val fusedLocationClient =
                LocationServices
                    .getFusedLocationProviderClient(
                        context
                    )

            val cancellationTokenSource =
                CancellationTokenSource()

            val currentLocationRequest =
                CurrentLocationRequest
                    .Builder()
                    .setPriority(
                        Priority.PRIORITY_HIGH_ACCURACY
                    )
                    .setMaxUpdateAgeMillis(5000)
                    .build()

            fusedLocationClient
                .getCurrentLocation(
                    currentLocationRequest,
                    cancellationTokenSource.token
                )
                .addOnSuccessListener { location ->

                    if (location == null) {

                        locationMessage =
                            "⚠️ Could not get current location. Please make sure GPS is ON."

                        return@addOnSuccessListener
                    }

                    // -------------------------------------------------
                    // LOCATION SUCCESSFUL
                    // -------------------------------------------------

                    val latitude =
                        location.latitude

                    val longitude =
                        location.longitude

                    locationMessage =
                        "📍 Current location obtained successfully!"

                    val sosData =
                        hashMapOf(

                            "userId" to
                                    userId,

                            "status" to
                                    "ACTIVE",

                            "startedAt" to
                                    System.currentTimeMillis(),

                            "message" to
                                    "Emergency SOS activated",

                            "initialLatitude" to
                                    latitude,

                            "initialLongitude" to
                                    longitude
                        )

                    // -------------------------------------------------
                    // CREATE SOS ONLY AFTER LOCATION EXISTS
                    // -------------------------------------------------

                    db.collection("sosAlerts")
                        .add(sosData)
                        .addOnSuccessListener { documentReference ->

                            val sosId =
                                documentReference.id

                            // NOW SOS REALLY EXISTS
                            sosActivated =
                                true

                            locationMessage =
                                "📍 Location saved successfully!"

                            // Store SOS ID in MainActivity
                            onSosActivated(
                                sosId
                            )

                            // 🚨 SMS
                            sendEmergencySMS(
                                latitude =
                                    latitude,
                                longitude =
                                    longitude
                            )

                            // 📞 PRIMARY CONTACT CALL
                            callPrimaryEmergencyContact()

                            // 📍 LIVE TRACKING
                            LocationService
                                .startLiveTracking(

                                    userId =
                                        userId,

                                    sosId =
                                        sosId
                                )
                        }
                        .addOnFailureListener { error ->

                            locationMessage =
                                error.message
                                    ?: "Failed to save SOS"

                            sosActivated =
                                false
                        }
                }
                .addOnFailureListener { error ->

                    locationMessage =
                        error.message
                            ?: "Failed to get current location"

                    sosActivated =
                        false
                }
        }
    }

    // ---------------------------------------------------------
    // END SOS
    // ---------------------------------------------------------

    fun endSOS() {

        // Stop live location tracking immediately
        LocationService.stopLiveTracking()

        if (activeSosId != null) {

            val updates = hashMapOf<String, Any>(
                "status" to "ENDED",
                "endedAt" to System.currentTimeMillis()
            )

            db.collection("sosAlerts")
                .document(activeSosId)
                .update(updates)
                .addOnSuccessListener {

                    // SOS successfully ended in Firestore
                    sosActivated = false

                    // Clear active SOS in MainActivity
                    onSosEnded()

                    // Return to Home
                    onBack()
                }
                .addOnFailureListener { error ->

                    // Do NOT leave the SOS screen
                    // if Firestore update failed.
                    locationMessage =
                        error.message
                            ?: "Failed to end SOS"

                    Toast.makeText(
                        context,
                        "❌ Failed to end SOS",
                        Toast.LENGTH_LONG
                    ).show()
                }

        } else {

            // No active SOS ID
            sosActivated = false

            onSosEnded()

            onBack()
        }
    }

    // ---------------------------------------------------------
    // UI
    // ---------------------------------------------------------

    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text =
                "🚨 SOS EMERGENCY"
        )

        Spacer(
            modifier =
                Modifier.height(30.dp)
        )

        // =====================================================
        // ACTIVE SOS
        // =====================================================

        if (
            sosActivated ||
            activeSosId != null
        ) {

            Text(
                text =
                    "🚨 SOS ACTIVATED!"
            )

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            Text(
                text =
                    "Your emergency SOS is currently active."
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            if (
                locationMessage.isNotBlank()
            ) {

                Text(
                    text =
                        locationMessage
                )
            }

            Spacer(
                modifier =
                    Modifier.height(30.dp)
            )

            // BACK WITHOUT ENDING SOS
            Button(

                onClick =
                    onBack,

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    text =
                        "← Back to Home"
                )
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            // END SOS
            Button(

                onClick = {
                    endSOS()
                },

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    text =
                        "🛑 END SOS"
                )
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            // WHATSAPP
            Button(

                onClick = {

                    if (
                        activeSosId != null
                    ) {

                        db.collection("sosAlerts")
                            .document(activeSosId)
                            .get()
                            .addOnSuccessListener { document ->

                                val latitude =
                                    document.getDouble(
                                        "initialLatitude"
                                    )

                                val longitude =
                                    document.getDouble(
                                        "initialLongitude"
                                    )

                                if (
                                    latitude != null &&
                                    longitude != null
                                ) {

                                    shareSOSOnWhatsApp(
                                        latitude =
                                            latitude,

                                        longitude =
                                            longitude
                                    )

                                } else {

                                    Toast.makeText(
                                        context,
                                        "⚠️ SOS location is unavailable",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                            .addOnFailureListener {

                                Toast.makeText(
                                    context,
                                    "⚠️ Failed to load SOS location",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                    } else {

                        Toast.makeText(
                            context,
                            "⚠️ No active SOS",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },

                modifier =
                    Modifier.fillMaxWidth()

            ) {

                Text(
                    text =
                        "🟢 Share SOS on WhatsApp"
                )
            }

        }

        // =====================================================
        // COUNTDOWN
        // =====================================================

        else if (
            !cancelled &&
            hasLocationPermission
        ) {

            Text(
                text =
                    "$countdown"
            )

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            Text(
                text =
                    "SOS will be activated soon..."
            )

            Spacer(
                modifier =
                    Modifier.height(30.dp)
            )

            Button(

                onClick = {

                    cancelled =
                        true

                    LocationService
                        .stopLiveTracking()
                },

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    text =
                        "CANCEL SOS"
                )
            }

        }

        // =====================================================
        // LOCATION PERMISSION
        // =====================================================

        else if (
            !hasLocationPermission
        ) {

            Text(
                text =
                    "📍 Location permission is required"
            )

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )

            Button(

                onClick = {

                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            ) {

                Text(
                    text =
                        "Allow Location"
                )
            }

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )

            Button(

                onClick =
                    onBack,

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    text =
                        "← Back to Home"
                )
            }

        }

        // =====================================================
        // CANCELLED
        // =====================================================

        else {

            Text(
                text =
                    "SOS Cancelled"
            )

            Spacer(
                modifier =
                    Modifier.height(30.dp)
            )

            Button(

                onClick =
                    onBack,

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    text =
                        "← Back to Home"
                )
            }
        }
    }
}