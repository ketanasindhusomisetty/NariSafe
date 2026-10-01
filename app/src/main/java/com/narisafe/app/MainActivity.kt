package com.narisafe.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.narisafe.app.ui.auth.AuthScreen
import com.narisafe.app.ui.contacts.EmergencyContactsScreen
import com.narisafe.app.ui.home.HomeScreen
import com.narisafe.app.ui.location.LiveLocationScreen
import com.narisafe.app.ui.sos.SOSScreen
import com.narisafe.app.ui.theme.NariSafeTheme
import com.narisafe.app.ui.contactview.EmergencyContactViewScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            NariSafeTheme {

                val auth =
                    FirebaseAuth.getInstance()

                val db =
                    FirebaseFirestore.getInstance()

                // -------------------------------------------------
                // LOGIN STATE
                // -------------------------------------------------

                var isLoggedIn by remember {

                    mutableStateOf(
                        auth.currentUser != null
                    )
                }

                // -------------------------------------------------
                // USER NAME
                // -------------------------------------------------

                var userName by remember {

                    mutableStateOf("User")
                }

                // -------------------------------------------------
                // CURRENT SCREEN
                // -------------------------------------------------

                var currentScreen by remember {



                    mutableStateOf("home")
                }

                // -------------------------------------------------
                // ACTIVE SOS ID
                // -------------------------------------------------

                var activeSosId by remember {

                    mutableStateOf<String?>(null)
                }

                var viewingSosId by remember {

                    mutableStateOf<String?>(null)
                }

                // -------------------------------------------------
                // LOAD USER DATA + ACTIVE SOS
                // -------------------------------------------------

                LaunchedEffect(isLoggedIn) {

                    if (isLoggedIn) {

                        val user =
                            auth.currentUser

                        if (user != null) {

                            val userId =
                                user.uid

                            // -------------------------------------
                            // LOAD USER NAME
                            // -------------------------------------

                            db.collection("users")
                                .document(userId)
                                .get()
                                .addOnSuccessListener { document ->

                                    if (document.exists()) {

                                        userName =
                                            document.getString("name")
                                                ?: "User"
                                    }
                                }

                            // -------------------------------------
                            // CHECK FOR ACTIVE SOS
                            // -------------------------------------

                            db.collection("sosAlerts")
                                .whereEqualTo(
                                    "userId",
                                    userId
                                )
                                .whereEqualTo(
                                    "status",
                                    "ACTIVE"
                                )
                                .limit(1)
                                .get()
                                .addOnSuccessListener { result ->

                                    if (
                                        result.documents.isNotEmpty()
                                    ) {

                                        activeSosId =
                                            result.documents[0].id
                                    }
                                }
                        }
                    }
                }

                // -------------------------------------------------
                // USER NOT LOGGED IN
                // -------------------------------------------------

                if (!isLoggedIn) {

                    AuthScreen(

                        onLoginSuccess = {

                            isLoggedIn = true

                            currentScreen =
                                "home"
                        }
                    )

                } else {

                    // -------------------------------------------------
                    // USER LOGGED IN
                    // -------------------------------------------------

                    when (currentScreen) {

                        // =============================================
                        // HOME
                        // =============================================

                        "home" -> {

                            HomeScreen(

                                onEmergencyContactViewClick = {
                                    currentScreen = "contactview"
                                },

                                userName =
                                    userName,

                                onLogout = {

                                    auth.signOut()

                                    isLoggedIn =
                                        false

                                    currentScreen =
                                        "home"

                                    userName =
                                        "User"

                                    activeSosId =
                                        null
                                },

                                onContactsClick = {

                                    currentScreen =
                                        "contacts"
                                },

                                onSOSClick = {

                                    currentScreen =
                                        "sos"
                                },

                                onLocationClick = {

                                    if (activeSosId != null) {

                                        viewingSosId = null

                                        currentScreen =
                                            "location"
                                    }
                                }
                            )
                        }

                        // =============================================
                        // EMERGENCY CONTACTS
                        // =============================================

                        "contacts" -> {

                            EmergencyContactsScreen(

                                onBack = {

                                    currentScreen =
                                        "home"
                                }
                            )
                        }

                        "contactview" -> {

                            EmergencyContactViewScreen(

                                onBack = {
                                    currentScreen = "home"
                                },

                                onViewLocation = { sosId ->

                                    val intent =
                                        android.content.Intent(
                                            this@MainActivity,
                                            LiveLocationActivity::class.java
                                        )

                                    intent.putExtra(
                                        "sosId",
                                        sosId
                                    )

                                    startActivity(intent)
                                }
                            )
                        }

                        // =============================================
                        // SOS
                        // =============================================

                        "sos" -> SOSScreen(
                            onBack = {
                                currentScreen = "home"
                            },

                            onSosActivated = { sosId ->
                                activeSosId = sosId
                            },

                            onSosEnded = {
                                activeSosId = null
                            },

                            activeSosId = activeSosId
                        )

                        // =============================================
                        // LIVE LOCATION
                        // =============================================

                        "location" -> {

                            val sosIdToView =
                                viewingSosId ?: activeSosId

                            sosIdToView?.let { sosId ->

                                LiveLocationScreen(

                                    sosId = sosId
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}