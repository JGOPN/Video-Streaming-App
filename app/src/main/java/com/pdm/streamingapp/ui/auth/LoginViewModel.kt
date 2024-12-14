package com.pdm.streamingapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthUiState(
    val email : String = "",
    val password : String = "",
    val isDialogOpen : Boolean = false
)

class LoginViewModel() : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val _isUserLoggedIn = MutableStateFlow(firebaseAuth.currentUser != null)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    init {
        firebaseAuth.addAuthStateListener { auth ->
            _isUserLoggedIn.value = auth.currentUser != null
        }
    }

    private val _authUiState = MutableStateFlow(AuthUiState())
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    fun updateUiState(email : String = _authUiState.value.email, password: String =  _authUiState.value.password, isDialogOpen: Boolean = _authUiState.value.isDialogOpen) {
        _authUiState.update { currentState ->
            currentState.copy(
                email = email,
                password = password,
                isDialogOpen = isDialogOpen
            )
        }
    }

    fun toggleConfirmationDialog() {
        _authUiState.update {
                currentState -> currentState.copy(isDialogOpen = !currentState.isDialogOpen)
        }
    }

    private suspend fun logIn(email: String, password: String) : Result<Unit> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: FirebaseAuthException) {
            Result.failure(e) // Handle Firebase-specific errors
        } catch (e: Exception) {
            Result.failure(e) // Handle general exceptions
        }
    }

    fun logInUser(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            logIn(email, password).onSuccess {
                onSuccess()
            }.onFailure { exception ->
                val errorMessage = when (exception) {
                    is FirebaseAuthException -> when (exception.errorCode) {
                        "ERROR_INVALID_EMAIL" -> "Invalid email format."
                        "ERROR_USER_NOT_FOUND" -> "User not found."
                        "ERROR_WRONG_PASSWORD" -> "Incorrect password."
                        else -> "Login failed. Try again."
                    }
                    else -> "An unexpected error occurred."
                }
                onFailure(errorMessage)
            }
        }
    }


    fun validateLoginInput(): List<String> {
        //Validate input and returns a list of errors (or empty list)
        val errorList = mutableListOf<String>()

        if (_authUiState.value.email.isBlank()) errorList.add("E-mail cannot be blank")

        if (_authUiState.value.password.isBlank()) errorList.add("Password cannot be blank")

        return errorList
    }

}