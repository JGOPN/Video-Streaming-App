package com.pdm.streamingapp.ui.auth

import androidx.lifecycle.ViewModel
import com.pdm.streamingapp.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.Period

data class UserEntryState(
    val userEntry : User = User(0,"","","",false, LocalDate.of(2000,1,1)),
    val isDialogOpen : Boolean = false
)

class RegisterUserViewModel() : ViewModel() {
    private val _userEntryState = MutableStateFlow(UserEntryState())
    val userEntryState: StateFlow<UserEntryState> = _userEntryState.asStateFlow()

    fun updateUserEntryState(
        userEntry: User = _userEntryState.value.userEntry
    ) {
        _userEntryState.update {
            UserEntryState(userEntry)
        }
    }

    fun toggleConfirmationDialog() {
        _userEntryState.update {
                currentState -> currentState.copy(isDialogOpen = !currentState.isDialogOpen)
        }
    }

    fun validateUserEntry() : List<String>{
        val errorList = mutableListOf<String>()
        val userEntry = _userEntryState.value.userEntry

        if (userEntry.username.isBlank())
            errorList.add("Username cannot be blank.")

        if (userEntry.username.length < 6)
            errorList.add("Username must be at least 6 characters long.")

        // Validate password
        if (userEntry.password.length < 8)
            errorList.add("Password must be at least 8 characters long.")
        if (!userEntry.password.any { it.isDigit() })
            errorList.add("Password must contain at least one number.")
        if (!userEntry.password.any { it.isUpperCase() })
            errorList.add("Password must contain at least one uppercase letter.")

        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        if (!emailRegex.matches(userEntry.email))
            errorList.add("Email is not valid.")

        val age = Period.between(userEntry.birthdate, LocalDate.now()).years
        if (age < 13) errorList.add("User must be at least 13 years old to create an account.")
        if (age > 120) errorList.add("Please input a valid age.")

        return errorList
    }
}