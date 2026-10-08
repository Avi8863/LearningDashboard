package com.example.learningdashboard.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val emailPattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun login(
        email: String,
        password: String,
    ) {
        val trimmedEmail = email.trim()

        if (trimmedEmail.isBlank()) {
            _uiState.value = LoginUiState.Error("Email is required")
            return
        }

        if (!emailPattern.matches(trimmedEmail)) {
            _uiState.value = LoginUiState.Error("Enter a valid email address")
            return
        }

        if (password.isBlank()) {
            _uiState.value = LoginUiState.Error("Password is required")
            return
        }

        if (password.length < 6) {
            _uiState.value = LoginUiState.Error("Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            delay(1000)

            if (trimmedEmail == "test@example.com" && password == "password123") {
                _uiState.value = LoginUiState.Success
            } else {
                _uiState.value = LoginUiState.Error("Invalid email or password")
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}
