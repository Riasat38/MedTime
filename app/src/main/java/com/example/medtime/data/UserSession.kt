package com.example.medtime.data

import android.content.Context

object UserSession {
    private var currentUser: User? = null
    private var sessionManager: SessionManager? = null

    fun initialize(context: Context) {
        sessionManager = SessionManager.getInstance(context)
        // Load saved user session if exists
        currentUser = sessionManager?.getUserSession()
    }

    fun setUser(user: User) {
        currentUser = user
        sessionManager?.saveUserSession(user)
    }

    fun getUser(): User? = currentUser

    fun clearUser() {
        currentUser = null
        sessionManager?.clearSession()
    }

    fun isLoggedIn(): Boolean {
        // Check both in-memory and persisted session
        if (currentUser != null) return true
        sessionManager?.let {
            if (it.isLoggedIn()) {
                currentUser = it.getUserSession()
                return currentUser != null
            }
        }
        return false
    }
}

