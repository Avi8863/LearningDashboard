package com.example.learningdashboard.presentation.login

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun login_blankEmail_returnsError() {
        viewModel.login("", "password123")

        val state = viewModel.uiState.value
        assertTrue(state is LoginUiState.Error)
        assertEquals("Email is required", (state as LoginUiState.Error).message)
    }

    @Test
    fun login_invalidEmailFormat_returnsError() {
        viewModel.login("invalid-email", "password123")

        val state = viewModel.uiState.value
        assertTrue(state is LoginUiState.Error)
        assertEquals("Enter a valid email address", (state as LoginUiState.Error).message)
    }

    @Test
    fun login_blankPassword_returnsError() {
        viewModel.login("test@example.com", "")

        val state = viewModel.uiState.value
        assertTrue(state is LoginUiState.Error)
        assertEquals("Password is required", (state as LoginUiState.Error).message)
    }

    @Test
    fun login_shortPassword_returnsError() {
        viewModel.login("test@example.com", "123")

        val state = viewModel.uiState.value
        assertTrue(state is LoginUiState.Error)
        assertEquals("Password must be at least 6 characters", (state as LoginUiState.Error).message)
    }

    @Test
    fun login_validCredentials_setsSuccessState() = runTest {
        viewModel.login("test@example.com", "password123")

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is LoginUiState.Success)
    }

    @Test
    fun login_invalidCredentials_returnsError() = runTest {
        viewModel.login("test@example.com", "wrongpassword")

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is LoginUiState.Error)
        assertEquals("Invalid email or password", (state as LoginUiState.Error).message)
    }
}
