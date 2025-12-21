package com.example.medtime.auth

import com.example.medtime.data.AuthResult
import com.example.medtime.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun login(email: String, password: String): AuthResult<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return AuthResult.Error("User not found")

            // Fetch user data from Firestore
            val userDoc = firestore.collection("users").document(uid).get().await()
            if (userDoc.exists()) {
                val user = User(
                    uid = uid,
                    name = userDoc.getString("name") ?: "",
                    email = email,
                    gender = userDoc.getString("gender") ?: "",
                    age = userDoc.getString("age") ?: ""
                )
                AuthResult.Success(user)
            } else {
                AuthResult.Error("User data not found")
            }
        } catch (e: Exception) {
            when {
                e.message?.contains("no user record", ignoreCase = true) == true ||
                e.message?.contains("user not found", ignoreCase = true) == true -> {
                    AuthResult.Error("No account found with this email. Please sign up.")
                }
                e.message?.contains("password is invalid", ignoreCase = true) == true ||
                e.message?.contains("wrong password", ignoreCase = true) == true -> {
                    AuthResult.Error("Incorrect password. Please try again.")
                }
                e.message?.contains("network", ignoreCase = true) == true -> {
                    AuthResult.Error("Network error. Please check your connection.")
                }
                else -> {
                    AuthResult.Error(e.message ?: "Login failed. Please try again.")
                }
            }
        }
    }

    suspend fun signUp(
        name: String,
        email: String,
        password: String,
        gender: String,
        age: String
    ): AuthResult<User> {
        return try {

            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return AuthResult.Error("Failed to create user")


            val user = User(
                uid = uid,
                name = name,
                email = email,
                gender = gender,
                age = age
            )

            val userData = hashMapOf(
                "name" to name,
                "email" to email,
                "gender" to gender,
                "age" to age
            )

            firestore.collection("users").document(uid).set(userData).await()

            AuthResult.Success(user)
        } catch (e: Exception) {
            when {
                e.message?.contains("email address is already in use", ignoreCase = true) == true -> {
                    AuthResult.Error("This email is already registered. Please login.")
                }
                e.message?.contains("password", ignoreCase = true) == true -> {
                    AuthResult.Error("Password must be at least 6 characters.")
                }
                e.message?.contains("email", ignoreCase = true) == true -> {
                    AuthResult.Error("Invalid email address.")
                }
                e.message?.contains("network", ignoreCase = true) == true -> {
                    AuthResult.Error("Network error. Please check your connection.")
                }
                else -> {
                    AuthResult.Error(e.message ?: "Sign up failed. Please try again.")
                }
            }
        }
    }

    fun logout() {
        auth.signOut()
    }
}

