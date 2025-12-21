package com.example.medtime.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

/**
 * SessionManager - Handles persistent user session storage
 * Uses SharedPreferences to store user data across app launches
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREF_NAME,
        Context.MODE_PRIVATE
    )

    private val gson = Gson()

    companion object {
        private const val PREF_NAME = "MedTimeSession"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"

        @Volatile
        private var instance: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    /**
     * Save user session data
     */
    fun saveUserSession(user: User) {
        val editor = prefs.edit()
        val userJson = gson.toJson(user)
        editor.putString(KEY_USER_DATA, userJson)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
    }

    /**
     * Get saved user session data
     */
    fun getUserSession(): User? {
        if (!isLoggedIn()) return null

        val userJson = prefs.getString(KEY_USER_DATA, null) ?: return null
        return try {
            gson.fromJson(userJson, User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Clear user session (logout)
     */
    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}

