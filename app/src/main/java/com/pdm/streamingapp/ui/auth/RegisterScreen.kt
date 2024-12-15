package com.pdm.streamingapp.ui.auth

import android.util.Log
import android.widget.Toast
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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.pdm.streamingapp.ui.components.MinimalDialog
import kotlinx.datetime.LocalDate

@Composable
fun RegisterScreen(onRegister: () -> Unit, onSwitch: () -> Unit, modifier: Modifier, registerViewModel : RegisterViewModel = viewModel()){
        val userEntryState by registerViewModel.userEntryState.collectAsState()
        val userEntry = userEntryState.userEntry
        val birthdateStr = remember { mutableStateOf(userEntry.birthdate.toString()) } //birthdate string so we only parse to LocalDate at submit

        val validateInputs = registerViewModel::validateUserEntry
        val onUpdate = registerViewModel::updateUserEntryState
        val onSubmit: ((Boolean) -> Unit) -> Unit = registerViewModel::addUser
        val context = LocalContext.current
        val isUserLoggedIn by registerViewModel.isUserLoggedIn.collectAsState()

        if(isUserLoggedIn){ //checks if already logged in. If so, go straight to MainScreen
            val currentUser = FirebaseAuth.getInstance().currentUser
            Toast.makeText(context, "Logged in as: ${currentUser?.email}", Toast.LENGTH_LONG).show()
            LaunchedEffect(Unit){
                onRegister() //navigate to Main
            }
        }

        //When user clicks submit, open alertDialog if form not valid, otherwise registers user and triggers onSubmit()
        if(userEntryState.isDialogOpen){
            val errorList = validateInputs()
            if(errorList.isNotEmpty())
                MinimalDialog(
                    messageList = errorList,
                    onDismissRequest = {registerViewModel.toggleConfirmationDialog()}
                )
            else {
                registerViewModel.toggleConfirmationDialog()
                LaunchedEffect(Unit) {
                    registerViewModel.signUpUser(//Add user via firebase
                        userEntryState.userEntry.email,
                        userEntryState.password,
                        onSuccess = {
                            Toast.makeText(context, "User added successfully", Toast.LENGTH_LONG).show()
                        },
                        onFailure = { errorMessage ->
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    )
                }
                onSubmit() { isSuccess -> //addUser adds user to application server db
                    if(isSuccess) onRegister() //navigate to MainScreen
                }
            }
        }

    Box{
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
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, capitalization = KeyboardCapitalization.Words)
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
                value = userEntryState.password,
                onValueChange = { registerViewModel.updatePassword(it) },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = userEntryState.confirmPassword,
                onValueChange = { registerViewModel.updateConfirmPassword(it) },
                label = { Text("Confirm password") },
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                FilledTonalButton(
                    onClick = onSwitch,
                    modifier = Modifier.weight(0.3f)
                ) {
                    Text("Sign in", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.size(5.dp))
                Button(
                    onClick = {
                        try {
                            onUpdate(userEntry.copy(birthdate = LocalDate.parse(birthdateStr.value))) //handle parseError on LocalDate
                        } catch (ex : Exception) {
                            Log.d("MainActivity","Birthdate Parse error")
                        }
                        registerViewModel.toggleConfirmationDialog()//show dialog if has errors, submit otherwise
                    },
                    modifier = Modifier.weight(0.3f)
                ) {
                    Text(text = "Submit", fontSize = 20.sp)
                }
            }
        }
    }
}