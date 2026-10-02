package com.narisafe.app.ui.safety

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@Composable
fun SafetyScreen(
    onBack: () -> Unit
) {

    val context = LocalContext.current

    fun callEmergencyNumber(number: String) {

        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(
                context,
                "📞 Phone call permission is required",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        try {

            val intent = Intent(
                Intent.ACTION_CALL,
                Uri.parse("tel:$number")
            )

            context.startActivity(intent)

        } catch (exception: Exception) {

            Toast.makeText(
                context,
                "❌ Unable to make the call",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = "🛡️ NariSafe Safety"
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // ---------------------------------------------------------
        // EMERGENCY SAFETY
        // ---------------------------------------------------------

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "🚨 Emergency Safety"
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Text(
                    text =
                        "• Activate SOS when you need immediate help.\n" +
                                "• Keep your emergency contacts updated.\n" +
                                "• Stay connected with trusted people."
                )
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // ---------------------------------------------------------
        // PHONE SAFETY
        // ---------------------------------------------------------

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "📱 Phone Safety"
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Text(
                    text =
                        "• Keep your phone charged.\n" +
                                "• Keep Location Services enabled when needed.\n" +
                                "• Keep emergency contacts easily accessible."
                )
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // ---------------------------------------------------------
        // TRAVEL SAFETY
        // ---------------------------------------------------------

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "🚶 Travel Safety"
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Text(
                    text =
                        "• Inform someone you trust about your journey.\n" +
                                "• Avoid isolated areas when possible.\n" +
                                "• Keep your phone accessible."
                )
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // ---------------------------------------------------------
        // EMERGENCY NUMBERS
        // ---------------------------------------------------------

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "📞 Emergency Numbers"
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Button(
                    onClick = {
                        callEmergencyNumber("112")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🚓 Police — 112")
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Button(
                    onClick = {
                        callEmergencyNumber("108")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🚑 Ambulance — 108")
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Button(
                    onClick = {
                        callEmergencyNumber("101")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🚒 Fire — 101")
                }
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // ---------------------------------------------------------
        // BACK
        // ---------------------------------------------------------

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("← Back to Home")
        }
    }
}