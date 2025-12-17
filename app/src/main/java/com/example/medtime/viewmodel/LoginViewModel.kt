package com.example.medtime.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medtime.auth.AuthRepository
import com.example.medtime.data.AuthResult
import com.example.medtime.data.User
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    var loginState by mutableStateOf<AuthResult<User>?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Email and password cannot be empty"
            return
        }

        viewModelScope.launch {
            loginState = AuthResult.Loading
            errorMessage = null

            val result = repository.login(email, password)
            loginState = result

            if (result is AuthResult.Error) {
                errorMessage = result.message
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }

    fun resetState() {
        loginState = null
        errorMessage = null
    }
}

