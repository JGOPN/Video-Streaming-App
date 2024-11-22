package com.pdm.streamingapp.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pdm.streamingapp.ui.auth.LoginScreen
import com.pdm.streamingapp.ui.auth.RegisterScreen


//adicionar mais destinos aqui. Talvez um para visualizar a lista de filmes e um para a tela de exibicao do filme?
enum class StreamingAppDestinations(val title: String) {
    Login(title = "login"),
    Register(title = "register")
}

@Composable
fun StreamingAppNavGraph(navController: NavHostController,
                modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = StreamingAppDestinations.Login.name,
        modifier = modifier
    ){
        composable(route = StreamingAppDestinations.Login.name) {
            LoginScreen(
                onSubmit = {},
                onSwitch = { navController.navigate(StreamingAppDestinations.Register.name) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }
        composable(route = StreamingAppDestinations.Register.name){
            RegisterScreen(
                onSubmit = {},
                onSwitch = { navController.navigate(StreamingAppDestinations.Login.name) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }
    }
}
