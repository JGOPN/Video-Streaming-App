package com.pdm.streamingapp.ui.auth

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdm.streamingapp.R
import com.pdm.streamingapp.ui.components.MinimalDialog
import com.pdm.streamingapp.ui.theme.StreamingAppTheme
import java.time.LocalDate
import java.time.format.DateTimeParseException


@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = viewModel(),
    onSubmit: () -> Unit, //botao de login
    onSwitch: () -> Unit, //botao que troca para registerScreen
    modifier: Modifier
){
    val loginUiState by loginViewModel.loginUiState.collectAsState()

    //When user clicks submit, open MinimalDialog if form not valid, otherwise triggers onSubmit()
    if(loginUiState.isDialogOpen){
        val errorList = loginViewModel.validateLoginInput()
        if(errorList.isNotEmpty())
            MinimalDialog(
                messageList = errorList,
                onDismissRequest = {loginViewModel.toggleConfirmationDialog()}
            )
        else
            onSubmit()
    }

    Column(modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = "logo", contentScale = ContentScale.Fit, modifier = Modifier
                .size(100.dp)
                .padding(10.dp))
        TextField(
            value = loginUiState.username,
            onValueChange = {loginViewModel.updateUiState(username = it)},
            label = { Text("Username") },
            singleLine = true
        )
        TextField(
            value = loginUiState.password,
            onValueChange = {loginViewModel.updateUiState(password = it)},
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            FilledTonalButton(
                onClick = onSwitch,
                modifier = Modifier.weight(1f),
                contentPadding = ButtonDefaults.TextButtonContentPadding
            ) {
                Text("Register User", fontSize = 20.sp)
            }
            Button(
                onClick = onSubmit,
                modifier = Modifier.weight(1f),
                contentPadding = ButtonDefaults.TextButtonContentPadding
            ) {
                Text("Log In", fontSize = 20.sp)
            }
        }
    }
}

@Composable
fun RegisterScreen(registerUserViewModel: RegisterUserViewModel = viewModel(), onSubmit: () -> Unit, onSwitch: () -> Unit, modifier: Modifier){
    val registerUserState by registerUserViewModel.userEntryState.collectAsState()
    val userEntry = registerUserState.userEntry
    val birthdateStr = remember { mutableStateOf(userEntry.birthdate.toString()) } //birthdate string so we only parse to LocalDate at submit

    val onUpdate = registerUserViewModel::updateUserEntryState

    //When user clicks submit, may open alertDialog if form not valid, otherwise triggers onSubmit()
    if(registerUserState.isDialogOpen){
        val errorList = registerUserViewModel.validateUserEntry()
        if(errorList.isNotEmpty())
            MinimalDialog(
                messageList = errorList,
                onDismissRequest = {registerUserViewModel.toggleConfirmationDialog()}
            )
        else
            onSubmit()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = userEntry.username,
            onValueChange = {  onUpdate(userEntry.copy(username = it)) },
            label = { Text(text="Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = userEntry.email,
            onValueChange = { onUpdate(userEntry.copy(email = it)) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = userEntry.password,
            onValueChange = { onUpdate(userEntry.copy(password = it)) },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = birthdateStr.value,
            onValueChange = { birthdateStr.value = it },
            label = { Text("Birthdate (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            FilledTonalButton(
                onClick = onSwitch,
                modifier = Modifier.weight(1f),
                contentPadding = ButtonDefaults.TextButtonContentPadding
            ) {
                Text("Login", fontSize = 20.sp)
            }
            Button(
                onClick =  {
                    try {
                        onUpdate(userEntry.copy(birthdate = LocalDate.parse(birthdateStr.value))) //handle parseError on LocalDate
                    } catch (ex : DateTimeParseException) {
                        Log.d("MainActivity","Birthdate Parse error")
                    }
                    registerUserViewModel.toggleConfirmationDialog()//show dialog if has errors, submit otherwise
                },
                modifier = Modifier.weight(1f),
                contentPadding = ButtonDefaults.TextButtonContentPadding
            ) {
                Text("Submit", fontSize = 20.sp)
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun LogInPreview() {
    StreamingAppTheme{
            LoginScreen(onSubmit = {}, onSwitch = {}, modifier = Modifier.fillMaxSize())
    }
}