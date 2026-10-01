package com.narisafe.app.ui.contacts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


data class EmergencyContact(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val relationship: String = "",
    val isPrimary: Boolean = false,
    val hasNariSafeAccount: Boolean = false
)


@Composable
fun EmergencyContactsScreen(
    onBack: () -> Unit
) {

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val contacts = remember {
        mutableStateListOf<EmergencyContact>()
    }


    // LOAD CONTACTS
    fun loadContacts() {

        val userId = auth.currentUser?.uid ?: return

        db.collection("emergencyContacts")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->

                contacts.clear()

                for (document in result.documents) {

                    contacts.add(
                        EmergencyContact(
                            id = document.id,

                            name =
                                document.getString("name") ?: "",

                            phone =
                                document.getString("phone") ?: "",

                            relationship =
                                document.getString("relationship") ?: "",

                            isPrimary =
                                document.getBoolean("isPrimary")
                                    ?: false,

                            hasNariSafeAccount =
                                document.getBoolean(
                                    "hasNariSafeAccount"
                                ) ?: false
                        )
                    )
                }
            }
            .addOnFailureListener { error ->

                message =
                    error.message ?: "Failed to load contacts"
            }
    }


    LaunchedEffect(Unit) {
        loadContacts()
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),

        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = "🚨 Emergency Contacts"
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )


        // CONTACT NAME
        OutlinedTextField(
            value = name,

            onValueChange = {
                name = it
            },

            label = {
                Text("Contact Name")
            },

            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )


        // PHONE NUMBER
        OutlinedTextField(
            value = phone,

            onValueChange = { newValue ->

                val digitsOnly =
                    newValue.filter { it.isDigit() }

                if (digitsOnly.length <= 10) {
                    phone = digitsOnly
                }
            },

            label = {
                Text("Phone Number")
            },

            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )


        // RELATIONSHIP
        OutlinedTextField(
            value = relationship,

            onValueChange = {
                relationship = it
            },

            label = {
                Text("Relationship")
            },

            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // ADD CONTACT
        Button(
            onClick = {

                if (
                    name.isBlank() ||
                    phone.isBlank() ||
                    relationship.isBlank()
                ) {

                    message =
                        "Please fill all fields"

                } else if (phone.length != 10) {

                    message =
                        "Phone number must contain exactly 10 digits"

                } else {

                    val currentUserId =
                        auth.currentUser?.uid

                    if (currentUserId == null) {

                        message =
                            "User is not logged in"

                    } else {

                        /*
                         * Check whether this phone number
                         * belongs to an existing NariSafe user.
                         */
                        db.collection("users")
                            .whereEqualTo("phone", phone)
                            .limit(1)
                            .get()
                            .addOnSuccessListener { result ->

                                var contactUserId: String? = null
                                var hasNariSafeAccount = false

                                if (
                                    result.documents.isNotEmpty()
                                ) {

                                    val contactDocument =
                                        result.documents[0]

                                    contactUserId =
                                        contactDocument.id

                                    hasNariSafeAccount = true
                                }


                                /*
                                 * Create emergency contact.
                                 */
                                val contactData =
                                    hashMapOf<String, Any>(

                                        "userId" to currentUserId,

                                        "name" to name,

                                        "phone" to phone,

                                        "relationship" to relationship,

                                        "isPrimary" to false,

                                        "hasNariSafeAccount"
                                                to hasNariSafeAccount,

                                        "createdAt"
                                                to System.currentTimeMillis()
                                    )


                                /*
                                 * Only add contactUserId
                                 * if the contact has a NariSafe account.
                                 */
                                if (
                                    contactUserId != null
                                ) {

                                    contactData[
                                        "contactUserId"
                                    ] = contactUserId
                                }


                                db.collection(
                                    "emergencyContacts"
                                )
                                    .add(contactData)

                                    .addOnSuccessListener {

                                        if (
                                            hasNariSafeAccount
                                        ) {

                                            message =
                                                "$name is a NariSafe user! ✅"

                                        } else {

                                            message =
                                                "$name added as an emergency contact. 📞"
                                        }


                                        name = ""
                                        phone = ""
                                        relationship = ""

                                        loadContacts()
                                    }

                                    .addOnFailureListener { error ->

                                        message =
                                            error.message
                                                ?: "Failed to save contact"
                                    }
                            }

                            .addOnFailureListener { error ->

                                message =
                                    error.message
                                        ?: "Could not check NariSafe account"
                            }
                    }
                }
            },

            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                "Add Emergency Contact"
            )
        }


        Spacer(
            modifier = Modifier.height(12.dp)
        )


        // MESSAGE
        if (message.isNotBlank()) {

            Text(
                text = message
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )
        }


        Text(
            text = "Saved Emergency Contacts"
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )


        // CONTACT LIST
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {

            items(contacts) { contact ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = contact.name
                        )

                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )

                        Text(
                            text = "📞 ${contact.phone}"
                        )

                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )

                        Text(
                            text =
                                "Relationship: ${contact.relationship}"
                        )

                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )


                        // ACCOUNT STATUS
                        if (
                            contact.hasNariSafeAccount
                        ) {

                            Text(
                                text =
                                    "🟢 NariSafe User"
                            )

                        } else {

                            Text(
                                text =
                                    "📱 SMS Contact"
                            )
                        }


                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )


                        // PRIMARY CONTACT
                        if (contact.isPrimary) {

                            Text(
                                text =
                                    "⭐ Primary Emergency Contact"
                            )

                        } else {

                            Button(
                                onClick = {

                                    val batch =
                                        db.batch()


                                    // Remove primary status
                                    // from all contacts
                                    contacts.forEach {

                                            existingContact ->

                                        val reference =
                                            db.collection(
                                                "emergencyContacts"
                                            )
                                                .document(
                                                    existingContact.id
                                                )

                                        batch.update(
                                            reference,
                                            "isPrimary",
                                            false
                                        )
                                    }


                                    // Make selected contact primary
                                    val selectedReference =
                                        db.collection(
                                            "emergencyContacts"
                                        )
                                            .document(
                                                contact.id
                                            )

                                    batch.update(
                                        selectedReference,
                                        "isPrimary",
                                        true
                                    )


                                    batch.commit()

                                        .addOnSuccessListener {

                                            message =
                                                "${contact.name} is now the Primary Contact ⭐"

                                            loadContacts()
                                        }

                                        .addOnFailureListener { error ->

                                            message =
                                                error.message
                                                    ?: "Failed to set primary contact"
                                        }
                                },

                                modifier =
                                    Modifier.fillMaxWidth()
                            ) {

                                Text(
                                    "⭐ Make Primary"
                                )
                            }
                        }


                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )


                        // DELETE CONTACT
                        OutlinedButton(
                            onClick = {

                                db.collection(
                                    "emergencyContacts"
                                )
                                    .document(contact.id)
                                    .delete()

                                    .addOnSuccessListener {

                                        message =
                                            "${contact.name} deleted successfully 🗑️"

                                        loadContacts()
                                    }

                                    .addOnFailureListener { error ->

                                        message =
                                            error.message
                                                ?: "Failed to delete contact"
                                    }
                            },

                            modifier =
                                Modifier.fillMaxWidth()
                        ) {

                            Text(
                                "🗑️ Delete Contact"
                            )
                        }
                    }
                }
            }
        }


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // BACK BUTTON
        Button(
            onClick = onBack,

            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                "← Back to Home"
            )
        }
    }
}