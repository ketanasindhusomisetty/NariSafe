package com.narisafe.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    userName: String,
    onLogout: () -> Unit,
    onContactsClick: () -> Unit,
    onSOSClick: () -> Unit,
    onEmergencyContactViewClick: () -> Unit,
    onSafetyClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = "🛡️ NariSafe"
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Welcome, $userName! 👋"
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // EMERGENCY SOS
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "🚨 EMERGENCY SOS"
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Button(
                    onClick = onSOSClick,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        text = "🚨 SOS"
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

// CONTACTS
        Button(
            onClick = onContactsClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "👥 Contacts"
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

// SAFETY
        Button(
            onClick = onSafetyClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "🛡️ Safety"
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Button(
            onClick = onEmergencyContactViewClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("👥 Emergency Contact View")
        }

        // EMERGENCY ASSISTANCE
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "🚑 Emergency Assistance"
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Text(
                    text = "Police  •  Ambulance  •  Fire"
                )
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // LOGOUT
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                text = "Logout"
            )
        }
    }
}