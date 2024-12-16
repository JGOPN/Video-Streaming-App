package com.pdm.streamingapp.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SettingsScreen(signOut: () -> Unit, onSignOut: () -> Unit){
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,  modifier = Modifier.fillMaxSize()){
        Text("Signed in as: ${FirebaseAuth.getInstance().currentUser?.email}")
        Spacer(modifier = Modifier.size(8.dp))
        Button(
            onClick = {
                signOut() //sign out on firebase
                onSignOut() //navigate back to login
            }
        ) {
            Text(text = "Sign Out", textAlign = TextAlign.Center)
        }
    }
}