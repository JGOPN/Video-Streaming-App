package com.pdm.streamingapp.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.pdm.streamingapp.model.User
import com.pdm.streamingapp.network.StreamingAppAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.yearsUntil
import retrofit2.HttpException
import java.io.IOException

data class UserEntryState(
    val userEntry : User = User(0,"","", LocalDate(2000,1,1),false),
    val isDialogOpen : Boolean = false,
    val password : String = "",
    val confirmPassword : String = ""
)

class RegisterViewModel() : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val _isUserLoggedIn = MutableStateFlow(firebaseAuth.currentUser != null)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    init {
        firebaseAuth.addAuthStateListener { auth ->
            _isUserLoggedIn.value = auth.currentUser != null
        }
    }

    private val _userEntryState = MutableStateFlow(UserEntryState())
    val userEntryState: StateFlow<UserEntryState> = _userEntryState.asStateFlow()


    fun updateUserEntryState(userEntry: User = _userEntryState.value.userEntry) {
        _userEntryState.update {
                currentState -> currentState.copy(userEntry = userEntry)
        }
    }

    fun updateConfirmPassword(pwd: String){
        _userEntryState.update {
                currentState -> currentState.copy(confirmPassword = pwd)
        }
    }

    fun updatePassword(pwd: String){
        _userEntryState.update {
                currentState -> currentState.copy(password = pwd)
        }
    }

    fun toggleConfirmationDialog(isOpen: Boolean? = null) {
        _userEntryState.update { currentState ->
            currentState.copy(isDialogOpen = isOpen ?: !currentState.isDialogOpen)
        }
    }

    fun validateUserEntry() : List<String>{
        val errorList = mutableListOf<String>()
        val userEntry = _userEntryState.value.userEntry
        val password = _userEntryState.value.password

        if (userEntry.username.isBlank())
            errorList.add("Username cannot be blank.")

        if (userEntry.username.length < 6)
            errorList.add("Username must be at least 6 characters long.")

        // Validate password
        if (password.length < 6)
            errorList.add("Password must be at least 6 characters long.")
        if (!password.any { it.isDigit() })
            errorList.add("Password must contain at least one number.")
        if (!password.any { it.isUpperCase() })
            errorList.add("Password must contain at least one uppercase letter.")

        if(password != _userEntryState.value.confirmPassword)
            errorList.add("Passwords don't match.")

        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        if (!emailRegex.matches(userEntry.email))
            errorList.add("Email is not valid.")

        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val age = userEntry.birthdate.yearsUntil(currentDate)
        if (age < 13) errorList.add("User must be at least 13 years old to create an account.")
        if (age > 120) errorList.add("Please input a valid age.")

        return errorList
    }

    private suspend fun signUp(email: String, password: String): Result<Unit> {
        return try {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: FirebaseAuthException) {
            Result.failure(e) // Handle Firebase-specific errors
        } catch (e: Exception) {
            Result.failure(e) // Handle general exceptions
        }
    }

    fun signUpUser(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            signUp(email, password).onSuccess {
                onSuccess()
            }.onFailure { exception ->
                val errorMessage = when (exception) {
                    is FirebaseAuthUserCollisionException -> when (exception.errorCode) {
                        "ERROR_EMAIL_ALREADY_IN_USE" -> "Email is already in use by a different account."
                        "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> "Email address in use by another account."
                        "ERROR_CREDENTIAL_ALREADY_IN_USE" -> "Credentials already in use."
                        else -> "Signup failed. Try again."
                    }
                    else -> "An unexpected error occurred."
                }
                onFailure(errorMessage)
            }
        }
    }

    fun addUser(onResult: (Boolean) -> Unit){
        val userEntry = _userEntryState.value.userEntry
        Log.d("MainActivity","adding user ${userEntry.username}")
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { StreamingAppAPI.retrofitService.addUser(userEntry) }
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    Log.d("MainActivity", "User added successfully")
                    onResult(true)
                } else {
                    Log.e("MainActivity", "Failed to add user: ${response.code()} ${response.message()}")
                    onResult(false)
                }
            }.onFailure { exception ->
                handleExceptions(exception)
                onResult(false)
            }
        }
    }


    fun handleExceptions(exception: Throwable){
        when (exception) {
            is IOException -> Log.e("Network", "Network error: ${exception.message}")
            is HttpException -> Log.e("Network", "HTTP error: ${exception.message}")
            else -> Log.e("Network", "Unexpected error: ${exception.message}")
        }
    }


}