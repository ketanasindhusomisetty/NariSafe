package com.narisafe.app.ui.contactview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class AuthorizedSOS(
    val sosId: String = "",
    val userId: String = "",
    val userName: String = ""
)

@Composable
fun EmergencyContactViewScreen(
    onBack: () -> Unit,
    onViewLocation: (String) -> Unit
) {

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    var loading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("") }
    var activeSOS by remember { mutableStateOf<AuthorizedSOS?>(null) }

    LaunchedEffect(Unit) {

        val currentUserId = auth.currentUser?.uid

        if (currentUserId == null) {

            message = "User is not logged in."
            loading = false
            return@LaunchedEffect
        }

        db.collection("emergencyContacts")
            .whereEqualTo("contactUserId", currentUserId)
            .get()
            .addOnSuccessListener { contactsResult ->

                if (contactsResult.isEmpty) {

                    message =
                        "You are not an emergency contact for any NariSafe user."

                    loading = false
                    return@addOnSuccessListener
                }

                val contactDocuments =
                    contactsResult.documents

                var completedChecks = 0
                var sosFound = false

                fun checkFinished() {

                    completedChecks++

                    if (
                        completedChecks == contactDocuments.size &&
                        !sosFound
                    ) {

                        message =
                            "No active SOS alerts right now."

                        loading = false
                    }
                }

                for (contactDocument in contactDocuments) {

                    val ownerUserId =
                        contactDocument.getString("userId")

                    if (ownerUserId == null) {

                        checkFinished()
                        continue
                    }

                    db.collection("sosAlerts")
                        .whereEqualTo(
                            "userId",
                            ownerUserId
                        )
                        .whereEqualTo(
                            "status",
                            "ACTIVE"
                        )
                        .limit(1)
                        .get()
                        .addOnSuccessListener { sosResult ->

                            if (
                                sosResult.documents.isNotEmpty() &&
                                !sosFound
                            ) {

                                sosFound = true

                                val sosDocument =
                                    sosResult.documents[0]

                                db.collection("users")
                                    .document(ownerUserId)
                                    .get()
                                    .addOnSuccessListener { userDocument ->

                                        val userName =
                                            userDocument
                                                .getString("name")
                                                ?: "NariSafe User"

                                        activeSOS =
                                            AuthorizedSOS(
                                                sosId =
                                                    sosDocument.id,

                                                userId =
                                                    ownerUserId,

                                                userName =
                                                    userName
                                            )

                                        loading = false
                                    }
                                    .addOnFailureListener {

                                        activeSOS =
                                            AuthorizedSOS(
                                                sosId =
                                                    sosDocument.id,

                                                userId =
                                                    ownerUserId,

                                                userName =
                                                    "NariSafe User"
                                            )

                                        loading = false
                                    }

                            } else {

                                checkFinished()
                            }
                        }
                        .addOnFailureListener {

                            checkFinished()
                        }
                }
            }
            .addOnFailureListener { error ->

                message =
                    error.message
                        ?: "Failed to check emergency contacts."

                loading = false
            }
    }


    // ---------------------------------------------------------
    // SCREEN UI
    // ---------------------------------------------------------

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),

        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = "🚨 Emergency Contact View"
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )


        if (loading) {

            Text(
                text = "Checking emergency alerts..."
            )

        } else if (activeSOS != null) {

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "🚨 ACTIVE SOS"
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text =
                            "${activeSOS!!.userName} has activated an SOS."
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "You are an authorized emergency contact."
                    )

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    Button(
                        onClick = {

                            onViewLocation(
                                activeSOS!!.sosId
                            )
                        },

                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text = "📍 View SOS Location"
                        )
                    }
                }
            }

        } else {

            Text(
                text = message
            )
        }


        Spacer(
            modifier = Modifier.height(20.dp)
        )


        Button(
            onClick = onBack,

            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                text = "← Back"
            )
        }
    }
}