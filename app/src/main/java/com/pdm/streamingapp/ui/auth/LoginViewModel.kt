package com.pdm.streamingapp.ui.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel( /*private val itemsRepository: ItemsRepository */ ) : ViewModel() {

    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    fun updateUiState(username : String = _loginUiState.value.username, password: String =  _loginUiState.value.password, isDialogOpen: Boolean = _loginUiState.value.isDialogOpen) {
        _loginUiState.update { currentState ->
                currentState.copy(
                    username = username,
                    password = password,
                    isDialogOpen = isDialogOpen
                )
            }
    }

    fun toggleConfirmationDialog() {
        _loginUiState.update {
                currentState -> currentState.copy(isDialogOpen = !currentState.isDialogOpen)
        }
    }

    fun validateLoginInput(): List<String> {
        //Validate input and returns a list of errors (or empty list). To be passed to a MinimalDialog
        val errorList = mutableListOf<String>()

        if (_loginUiState.value.username.isBlank()) errorList.add("Username cannot be blank")

        if (_loginUiState.value.password.isBlank()) errorList.add("Password cannot be blank")

        return errorList
    }

}

data class LoginUiState(
    val username : String = "",
    val password : String = "",
    val isDialogOpen : Boolean = false
)
