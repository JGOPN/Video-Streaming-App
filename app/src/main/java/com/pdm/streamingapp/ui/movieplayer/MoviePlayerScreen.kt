package com.pdm.streamingapp.ui.movieplayer

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun MoviePlayerScreen(videoUri: Uri) {
    val context = LocalContext.current

    // Remember the ExoPlayer instance across recompositions
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(videoUri)
            setMediaItem(mediaItem)
            prepare()
        }
    }

    // Create the PlayerView and bind the ExoPlayer
    AndroidView(
        factory = {
            PlayerView(context).apply {
                this.player = player as ExoPlayer // Cast to ExoPlayer explicitly
                useController = true // Show playback controls
            }
        },
        modifier = Modifier.fillMaxSize()
    )

    // Manage ExoPlayer lifecycle
    DisposableEffect(player) {
        player.playWhenReady = true // Auto-start playback
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                // Handle errors here (e.g., show a message to the user)
                println("Player error: ${error.message}")
            }
        })

        onDispose {
            player.release()
        }
    }
}
