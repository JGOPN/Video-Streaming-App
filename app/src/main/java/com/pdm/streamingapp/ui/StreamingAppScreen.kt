package com.pdm.streamingapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.pdm.streamingapp.ui.navigation.StreamingAppNavGraph

@Composable
fun StreamingAppScreen(
    navController: NavHostController = rememberNavController()
) {
    StreamingAppNavGraph(navController)
}
