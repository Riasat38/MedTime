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

class SignUpViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    var signUpState by mutableStateOf<AuthResult<User>?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun signUp(
        name: String,
        email: String,
        password: String,
        gender: String,
        age: String
    ) {
        if (!validateInputs(name, email, password, gender, age)) {
            return
        }

        viewModelScope.launch {
            signUpState = AuthResult.Loading
            errorMessage = null

            val result = repository.signUp(name, email, password, gender, age)
            signUpState = result

            if (result is AuthResult.Error) {
                errorMessage = result.message
            }
        }
    }

    private fun validateInputs(
        name: String,
        email: String,
        password: String,
        gender: String,
        age: String
    ): Boolean {
        when {
            name.isBlank() -> {
                errorMessage = "Please enter your name"
                return false
            }
            email.isBlank() -> {
                errorMessage = "Please enter your email"
                return false
            }
            !email.contains("@") -> {
                errorMessage = "Please enter a valid email"
                return false
            }
            password.isBlank() -> {
                errorMessage = "Please enter a password"
                return false
            }
            password.length < 6 -> {
                errorMessage = "Password must be at least 6 characters"
                return false
            }
            gender.isBlank() -> {
                errorMessage = "Please select your gender"
                return false
            }
            age.isBlank() -> {
                errorMessage = "Please enter your age"
                return false
            }
            age.toIntOrNull() == null || age.toInt() < 1 || age.toInt() > 120 -> {
                errorMessage = "Please enter a valid age"
                return false
            }
        }
        return true
    }

    fun clearError() {
        errorMessage = null
    }

    fun resetState() {
        signUpState = null
        errorMessage = null
    }
}

