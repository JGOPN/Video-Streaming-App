package com.pdm.streamingapp.ui.navigation

import android.net.Uri
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
import com.pdm.streamingapp.ui.main.MainScreen
import com.pdm.streamingapp.ui.movieplayer.MoviePlayerScreen

enum class StreamingAppDestinations(val title: String) {
    Login(title = "login"),
    Register(title = "register"),
    Main(title = "main"),
    MoviePlayer(title = "movieplayer")
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
                onLogin = {navController.navigate(StreamingAppDestinations.Main.name)},
                onSwitch = { navController.navigate(StreamingAppDestinations.Register.name) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }
        composable(route = StreamingAppDestinations.Register.name){
            RegisterScreen(
                onRegister = { navController.navigate(StreamingAppDestinations.Main.name)},
                onSwitch = { navController.navigate(StreamingAppDestinations.Login.name) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }
        composable(route = StreamingAppDestinations.Main.name) {
            MainScreen(
                onPlayMovie = { videoUri ->
                    val encodedUri = Uri.encode(videoUri.toString())
                    navController.navigate("movieplayer/$encodedUri")
                },
                onSignOut = {navController.navigate(StreamingAppDestinations.Login.name)}
            )
        }
        composable(route = "movieplayer/{videoUri}") { backStackEntry ->
            val videoUriString = backStackEntry.arguments?.getString("videoUri")
            val videoUri = videoUriString?.let { Uri.parse(Uri.decode(it)) }
            if (videoUri != null) {
                MoviePlayerScreen(videoUri = videoUri)
            }
        }

    }
}
