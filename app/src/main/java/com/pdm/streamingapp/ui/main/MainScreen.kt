package com.pdm.streamingapp.ui.main

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.pdm.streamingapp.R
import com.pdm.streamingapp.model.Movie
import com.pdm.streamingapp.ui.components.RequestVideoPermissions
import com.pdm.streamingapp.ui.theme.StreamingAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onPlayMovie: (Uri) -> Unit, onSignOut: () -> Unit, mainViewModel: MainViewModel = viewModel()) {
    val mainUiState by mainViewModel.mainUiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val context = LocalContext.current

    Log.d("MainActivity","MainScreen recompose triggered. Current screen: ${mainUiState.currentScreen}")

    RequestVideoPermissions { permissions ->
        val allGranted = permissions.values.all { it }
        if (!allGranted) {
            Toast.makeText(context, "Some permissions were denied", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = mainUiState.currentScreen.title, textAlign = TextAlign.Center) },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            MainBottomAppBar(
                mainUiState,
                onClickAppbarIcon = mainViewModel::setCurrentScreen,
            )
        },
    ) { innerPadding ->

        Box(modifier = Modifier.padding(innerPadding)){
            when (mainUiState.currentScreen) {
                MainScreens.Home -> {
                    if (mainUiState.movieList.isEmpty()) {
                        ShowError(onClick = mainViewModel::getMovieList)
                    } else {
                        LazyCardList(
                            movieList = mainUiState.movieList,
                            expandedCardId = mainUiState.expandedCardId,
                            onClickCard = mainViewModel::toggleCardExpansion,
                            downloadAndSaveMovie = mainViewModel::downloadAndSaveMovie,
                            fetchThumbnail = mainViewModel::fetchThumbnail,
                            getDownloadUri = mainViewModel::getDownloadUri,
                            deleteMovie = mainViewModel::deleteMovieCall,
                            onPlayMovie = onPlayMovie
                        )
                    }
                }

                MainScreens.Search -> {
                    val searchUiState by mainViewModel.searchUiState.collectAsState()
                    Column(modifier = Modifier.fillMaxSize()){
                        SearchBar(
                            searchUiState,
                            mainViewModel::updateSearchQuery,
                            mainViewModel::filterMovieList,
                            mainViewModel::toggleShowingList
                        )
                        if (searchUiState.showingList) {
                            LazyCardList(
                                movieList = searchUiState.movieList,
                                expandedCardId = searchUiState.expandedCardId,
                                onClickCard = mainViewModel::toggleSearchCardExpansion,
                                fetchThumbnail = mainViewModel::fetchThumbnail,
                                downloadAndSaveMovie = mainViewModel::downloadAndSaveMovie,
                                getDownloadUri = mainViewModel::getDownloadUri,
                                deleteMovie = mainViewModel::deleteMovieCall,
                                onPlayMovie = onPlayMovie,
                                modifier = Modifier.weight(1f) // Use weight to ensure LazyCardList does not overlap SearchBar
                            )
                            }
                        }
                    }

                MainScreens.Settings -> {
                    SettingsScreen(
                        signOut = mainViewModel::signOut,
                        onSignOut = onSignOut
                    )
                }
            }
        }
    }
}


@Composable
fun LazyCardList(
    movieList: List<Movie>,
    expandedCardId: Int,
    onClickCard: (Int) -> Unit,
    fetchThumbnail: (String, (String?) -> Unit) -> Unit,
    downloadAndSaveMovie: (Context, Int, (Boolean, String?) -> Unit) -> Unit,
    getDownloadUri: (Int, (Uri?) -> Unit) -> Unit,
    deleteMovie: (Context, Uri, Int) -> Unit,
    onPlayMovie: (Uri) -> Unit,
    modifier: Modifier = Modifier
){

    LazyColumn(modifier = modifier){
        items(
            items = movieList,
            key = {
                item -> item.id
            }
        ) { item ->
            MovieCard(
                movie = item,
                isExpanded = expandedCardId == item.id,
                onClickCard = {onClickCard(item.id)},
                fetchThumbnail = fetchThumbnail,
                downloadAndSaveMovie = downloadAndSaveMovie,
                getDownloadUri = getDownloadUri,
                deleteMovie = deleteMovie,
                onPlayMovie = onPlayMovie
            )
        }
    }
}

@Composable
fun MovieCard(
    movie: Movie,
    isExpanded: Boolean = false,
    onClickCard: (Int) -> Unit,
    fetchThumbnail: (String, (String?) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    downloadAndSaveMovie: (Context, Int, (Boolean, String?) -> Unit) -> Unit,
    getDownloadUri: (Int, (Uri?) -> Unit) -> Unit,
    deleteMovie: (Context, Uri, Int) -> Unit,
    onPlayMovie: (Uri) -> Unit
) {
    val context = LocalContext.current
    val movieDownloadUri = remember { mutableStateOf<Uri?>(null) }
    val isDownloaded = remember { mutableStateOf(false) }

    LaunchedEffect(movie.id) {
        getDownloadUri(movie.id) { uri ->
            movieDownloadUri.value = uri
            isDownloaded.value = uri != null
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.clickable { onClickCard(movie.id) }) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                MovieThumbnail(movie.title, fetchThumbnail, modifier = Modifier.size(80.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Release: ${movie.releaseYear}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expanded Section
            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Duration: ${movie.duration} minutes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Description: ${movie.description}",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Genres: ${movie.genres.joinToString(", ")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Action Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    if (!isDownloaded.value) {
                        IconButton(onClick = {
                            downloadAndSaveMovie(context, movie.id) { success, message ->
                                if (success) {
                                    Toast.makeText(
                                        context,
                                        "Movie downloaded successfully!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    isDownloaded.value = true
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Error: $message",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.download_24dp),
                                contentDescription = "Download",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(onClick = {
                            onPlayMovie(Uri.parse("http://35.204.253.240/hls/streams/movie_${movie.id}/playlist.m3u8"))
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.stream_24dp),
                                contentDescription = "Stream",
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    } else {
                        IconButton(onClick = { onPlayMovie(movieDownloadUri.value!!) }) {
                            Icon(
                                Icons.Rounded.PlayArrow,
                                contentDescription = "Play",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        IconButton(onClick = {
                            deleteMovie(context, movieDownloadUri.value!!, movie.id)
                            Toast.makeText(
                                context,
                                "Movie deleted successfully!",
                                Toast.LENGTH_LONG
                            ).show()
                            isDownloaded.value = false
                        }) {
                            Icon(
                                Icons.Rounded.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
//onClickAppbarIcon receives a MainScreens and updates viewModel to go to that screen (doesnt use navigation)
fun MainBottomAppBar(mainUiState: MainUiState, onClickAppbarIcon : (MainScreens) -> Unit){
    val currentScreen = mainUiState.currentScreen
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier.height(120.dp)
    ) {
        val iconModifier = Modifier
            .padding(0.dp)
            .clip(RoundedCornerShape(10.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ){
            IconButton(
                onClick = {onClickAppbarIcon(MainScreens.Home)},
                modifier = iconModifier
            ) {
                Icon(
                    Icons.Rounded.Home,
                    contentDescription = "Home",
                    tint = if (currentScreen==MainScreens.Home) Color.Black else Color.Unspecified
                )
            }

            IconButton(
                onClick = {onClickAppbarIcon(MainScreens.Search)},
                modifier = iconModifier
            ) {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = if (currentScreen==MainScreens.Search) Color.Black else Color.Unspecified
                )
            }

            FilledIconButton(
                onClick = {onClickAppbarIcon(MainScreens.Settings)},
                modifier = iconModifier
            ) {
                Icon(
                    Icons.Rounded.Settings,
                    contentDescription = "Settings",
                    tint = if (currentScreen==MainScreens.Settings) Color.Black else Color.Unspecified
                )
            }

        }
    }
}

@Composable
fun ShowError(onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center){
        Icon(
            Icons.Rounded.Warning,
            contentDescription = "warning",
            tint = Color.Red,
            modifier = Modifier.size(30.dp)
        )
        Text(text = "Failed to fetch movies. Server is offline or you're not connected to the internet", textAlign = TextAlign.Center)
        Button(
            onClick = onClick
        ) {
            Text(text = "Retry", textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun MovieThumbnail(
    movieTitle: String,
    fetchThumbnail: (String, (String?) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    //val thumbnailUrl by viewModel.thumbnailUrl.collectAsState()
    val thumbnailUrl = remember {  mutableStateOf<String?>(null) } //if true, shows user added confirmation.

    LaunchedEffect(movieTitle) {
        fetchThumbnail(movieTitle) { thumbnailUrl.value = it }
    }

    Icons.Rounded

    Image(
        painter = if(thumbnailUrl.value != null) rememberAsyncImagePainter(thumbnailUrl.value)
                  else painterResource(R.drawable.placeholder)
        ,
        contentDescription = "Movie Thumbnail",
        modifier = modifier
    )
}

@Composable
fun SearchBar(
    searchUiState: SearchUiState,
    updateSearchQuery: (String) -> Unit,
    filterMovieList: () -> Unit,
    toggleShowing: (Boolean) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        TextField(
            value = searchUiState.searchQuery,
            onValueChange = updateSearchQuery,
            placeholder = {
                Text(
                    text = "Search movies by title",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 56.dp)
        )

        FilledIconButton(
            onClick = {
                filterMovieList()
                toggleShowing(true)
                Log.d("MainActivity", "Search button pressed. ${searchUiState.showingList}")
            },
            modifier = Modifier.size(56.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Search",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    StreamingAppTheme{
        //MainScreen()
        /*MovieCard(
            movie = Movie(1, "Inception", "A mind-bending thriller", 2010, 1, 148, listOf("Sci-Fi", "Thriller")),
            isExpanded = true,
            onClickCard = {},
            onShowDialog = {},
            fetchThumbnail =
        )*/
    }
}