package com.pdm.streamingapp.ui.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.pdm.streamingapp.R
import com.pdm.streamingapp.ui.components.MinimalDialog


@Composable
fun LoginScreen(onLogin: () -> Unit, onSwitch: () -> Unit, modifier: Modifier, loginViewModel : LoginViewModel = viewModel()){
    val authUiState by loginViewModel.authUiState.collectAsState()
    val isUserLoggedIn by loginViewModel.isUserLoggedIn.collectAsState()
    val context = LocalContext.current

    if(isUserLoggedIn){ //checks if already logged in. If so, go straight to MainScreen
        val currentUser = FirebaseAuth.getInstance().currentUser
        Toast.makeText(context, "Already logged in as: ${currentUser?.email}", Toast.LENGTH_LONG).show()
        LaunchedEffect(Unit){
            onLogin()
        }
    }

    if(authUiState.isDialogOpen){
        val errorList = loginViewModel.validateLoginInput()
        if(errorList.isNotEmpty())
            MinimalDialog(
                messageList = errorList,
                onDismissRequest = {loginViewModel.toggleConfirmationDialog()}
            )
        else
            LaunchedEffect(Unit) {
                loginViewModel.logInUser(
                    email = authUiState.email,
                    password = authUiState.password,
                    onSuccess = {
                        onLogin() // Navigate to the main screen
                    },
                    onFailure = { errorMessage ->
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    }
                )
            }
    }

    Box{
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painter = painterResource(R.drawable.logo), colorFilter = ColorFilter.tint(Color.White),
                contentDescription = "logo", contentScale = ContentScale.Fit, modifier = Modifier
                    .size(100.dp)
                    .padding(10.dp))
            TextField(
                value = authUiState.email,
                onValueChange = {loginViewModel.updateUiState(email = it)},
                label = { Text("E-mail") },
                singleLine = true
            )
            TextField(
                value = authUiState.password,
                onValueChange = {loginViewModel.updateUiState(password = it)},
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                FilledTonalButton(
                    onClick = onSwitch,
                    modifier = Modifier.weight(0.3f)
                ) {
                    Text("Sign up", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.size(5.dp))
                Button(
                    onClick = loginViewModel::toggleConfirmationDialog,
                    modifier = Modifier.weight(0.3f),
                    contentPadding = ButtonDefaults.TextButtonContentPadding
                ) {
                    Text("Log In", fontSize = 20.sp)
                }
            }
        }

    }
}

