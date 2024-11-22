package com.pdm.streamingapp.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.pdm.streamingapp.ui.theme.StreamingAppTheme

@Composable
fun MinimalDialog(messageList : List<String>, onDismissRequest: () -> Unit) {
    /* Esta funcao serve para mostrar um card por cima da tela que a chamou
    *  Recebe uma lista de mensagens para mostrar e uma funcao para quando o usuario
    * toca fora da tela (geralmente algo tipo: xyzViewModel.toggleConfirmationDialog() )*/
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(10.dp)){
                Text(text = "Validation Error", style = MaterialTheme.typography.titleMedium)
                for (error in messageList){
                    Text(
                        text = error,
                        modifier = Modifier,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ConfirmationDialog(isVisible : Boolean = false, selectedItem: String?, onDismissRequest: () -> Unit, onAcceptRequest: () -> Unit) {
    /*Mostrar um card por cima da tela que a chamou, para pedir a confirmacao do usuario. Nao sei onde seria usada no streaming app.
    * Para perceber como funciona, ver MainScreen do CMSApp.  */
    if (isVisible) {
        Log.d("MainActivity","Dialog open. $selectedItem")
        Dialog(onDismissRequest = onDismissRequest) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(10.dp)){
                    Text(text = "Confirmation Dialog", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Are you sure you want to remove $selectedItem?",
                            modifier = Modifier,
                            textAlign = TextAlign.Center
                        )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        FilledTonalButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Cancel")
                        }

                        Button(
                            onClick = onAcceptRequest,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Yes")
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun confPreview(){
    StreamingAppTheme {
        ConfirmationDialog(true,"User Joao",{},{})
    }
}

