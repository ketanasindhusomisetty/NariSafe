package com.narisafe.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit
) {

    var isLogin by remember {
        mutableStateOf(true)
    }

    var name by remember {
        mutableStateOf("")
    }

    var phone by remember {
        mutableStateOf("")
    }

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var message by remember {
        mutableStateOf("")
    }

    val auth =
        FirebaseAuth.getInstance()

    val db =
        FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text =
                if (isLogin)
                    "Welcome to NariSafe"
                else
                    "Create your NariSafe Account"
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        // -----------------------------------------------------
        // SIGN UP FIELDS
        // -----------------------------------------------------

        if (!isLogin) {

            // FULL NAME
            OutlinedTextField(

                value = name,

                onValueChange = {
                    name = it
                },

                label = {
                    Text("Full Name")
                },

                modifier =
                    Modifier.fillMaxWidth()
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
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

                placeholder = {
                    Text("Enter 10-digit phone number")
                },

                modifier =
                    Modifier.fillMaxWidth(),

                singleLine = true
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )
        }

        // -----------------------------------------------------
        // EMAIL
        // -----------------------------------------------------

        OutlinedTextField(

            value = email,

            onValueChange = {
                email = it
            },

            label = {
                Text("Email")
            },

            placeholder = {
                Text("example@gmail.com")
            },

            modifier =
                Modifier.fillMaxWidth(),

            singleLine = true
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        // -----------------------------------------------------
        // PASSWORD
        // -----------------------------------------------------

        OutlinedTextField(

            value = password,

            onValueChange = {
                password = it
            },

            label = {
                Text("Password")
            },

            visualTransformation =
                PasswordVisualTransformation(),

            modifier =
                Modifier.fillMaxWidth(),

            singleLine = true
        )

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        // -----------------------------------------------------
        // LOGIN / SIGNUP BUTTON
        // -----------------------------------------------------

        Button(

            onClick = {

                // =================================================
                // LOGIN
                // =================================================

                if (isLogin) {

                    // Email validation for login
                    if (email.isBlank()) {

                        message =
                            "Please enter your email"

                        return@Button
                    }

                    if (password.isBlank()) {

                        message =
                            "Please enter your password"

                        return@Button
                    }

                    auth.signInWithEmailAndPassword(
                        email.trim(),
                        password
                    )
                        .addOnCompleteListener { task ->

                            if (task.isSuccessful) {

                                message =
                                    "Login successful! 🎉"

                                onLoginSuccess()

                            } else {

                                message =
                                    task.exception?.message
                                        ?: "Login failed"
                            }
                        }

                }

                // =================================================
                // SIGN UP
                // =================================================

                else {

                    // -------------------------------------------------
                    // NAME VALIDATION
                    // -------------------------------------------------

                    if (name.isBlank()) {

                        message =
                            "Please enter your name"

                        return@Button
                    }

                    // -------------------------------------------------
                    // PHONE VALIDATION
                    // -------------------------------------------------

                    val normalizedPhone =
                        phone.trim()

                    if (normalizedPhone.isBlank()) {

                        message =
                            "Please enter your phone number"

                        return@Button
                    }

                    // Exactly 10 digits
                    if (
                        normalizedPhone.length != 10
                    ) {

                        message =
                            "Phone number must contain exactly 10 digits"

                        return@Button
                    }

                    // Digits only
                    if (
                        !normalizedPhone.all {
                            it.isDigit()
                        }
                    ) {

                        message =
                            "Phone number must contain only digits"

                        return@Button
                    }

                    // -------------------------------------------------
                    // GMAIL VALIDATION
                    // -------------------------------------------------

                    val normalizedEmail =
                        email.trim()

                    if (normalizedEmail.isBlank()) {

                        message =
                            "Please enter your email"

                        return@Button
                    }

                    // Must end with @gmail.com
                    if (
                        !normalizedEmail.endsWith(
                            "@gmail.com",
                            ignoreCase = true
                        )
                    ) {

                        message =
                            "Please use a Gmail address ending with @gmail.com"

                        return@Button
                    }

                    // Basic Gmail format check
                    val gmailPattern =
                        Regex(
                            "^[A-Za-z0-9._%+-]+@gmail\\.com$"
                        )

                    if (
                        !gmailPattern.matches(
                            normalizedEmail
                        )
                    ) {

                        message =
                            "Please enter a valid Gmail address"

                        return@Button
                    }

                    // -------------------------------------------------
                    // PASSWORD VALIDATION
                    // -------------------------------------------------

                    if (password.isBlank()) {

                        message =
                            "Please enter your password"

                        return@Button
                    }

                    // -------------------------------------------------
                    // CHECK DUPLICATE PHONE
                    // -------------------------------------------------

                    db.collection("users")
                        .whereEqualTo(
                            "phone",
                            normalizedPhone
                        )
                        .get()
                        .addOnSuccessListener { documents ->

                            if (!documents.isEmpty) {

                                message =
                                    "Phone number already registered. Please use a different number."

                            } else {

                                // -------------------------------------------------
                                // CREATE FIREBASE ACCOUNT
                                // -------------------------------------------------

                                auth.createUserWithEmailAndPassword(
                                    normalizedEmail,
                                    password
                                )
                                    .addOnCompleteListener { task ->

                                        if (task.isSuccessful) {

                                            val userId =
                                                auth.currentUser?.uid

                                            if (userId != null) {

                                                // -------------------------------------------------
                                                // SAVE USER PROFILE
                                                // -------------------------------------------------

                                                val userData =
                                                    hashMapOf(

                                                        "name" to
                                                                name.trim(),

                                                        "phone" to
                                                                normalizedPhone,

                                                        "email" to
                                                                normalizedEmail,

                                                        "safetyStatus" to
                                                                "Safe",

                                                        "createdAt" to
                                                                System.currentTimeMillis()
                                                    )

                                                db.collection("users")
                                                    .document(userId)
                                                    .set(userData)
                                                    .addOnSuccessListener {

                                                        message =
                                                            "Account created successfully! 🎉"

                                                    }
                                                    .addOnFailureListener { exception ->

                                                        message =
                                                            "Account created, but profile save failed: ${exception.message}"
                                                    }

                                            } else {

                                                message =
                                                    "Account created, but user ID was not found"
                                            }

                                        } else {

                                            message =
                                                task.exception?.message
                                                    ?: "Sign up failed"
                                        }
                                    }
                            }
                        }
                        .addOnFailureListener { exception ->

                            message =
                                "Could not check phone number: ${exception.message}"
                        }
                }
            },

            modifier =
                Modifier.fillMaxWidth()

        ) {

            Text(
                text =
                    if (isLogin)
                        "Login"
                    else
                        "Create Account"
            )
        }

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        // -----------------------------------------------------
        // LOGIN / SIGNUP SWITCH
        // -----------------------------------------------------

        TextButton(

            onClick = {

                isLogin =
                    !isLogin

                message =
                    ""
            }
        ) {

            Text(

                text =

                    if (isLogin)

                        "Don't have an account? Sign Up"

                    else

                        "Already have an account? Login"
            )
        }

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        // -----------------------------------------------------
        // MESSAGE
        // -----------------------------------------------------

        if (
            message.isNotEmpty()
        ) {

            Text(
                text =
                    message
            )
        }
    }
}