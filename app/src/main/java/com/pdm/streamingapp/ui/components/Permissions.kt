package com.pdm.streamingapp.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun RequestVideoPermissions(onPermissionsResult: (Map<String, Boolean>) -> Unit) {
    // Determine the permissions based on Android version
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(Manifest.permission.READ_MEDIA_VIDEO)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // For Android 10 to 12, only read permission is needed as Scoped Storage is used
        listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    } else {
        // For Android 9 and below, both read and write permissions are required
        listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    // Register the launcher for multiple permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = onPermissionsResult
    )

    LaunchedEffect(Unit) {
        // Launch permission request when the composable enters composition
        permissionLauncher.launch(permissions.toTypedArray())
    }
}