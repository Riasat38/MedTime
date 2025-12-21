package com.example.medtime.data

object UserSession {
    private var currentUser: User? = null

    fun setUser(user: User) {
        currentUser = user
    }

    fun getUser(): User? = currentUser

    fun clearUser() {
        currentUser = null
    }

    fun isLoggedIn(): Boolean = currentUser != null
}

