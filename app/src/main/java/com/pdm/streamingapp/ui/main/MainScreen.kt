package com.pdm.streamingapp.ui.main

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.pdm.streamingapp.model.Movie
import com.pdm.streamingapp.ui.components.ConfirmationDialog
import com.pdm.streamingapp.ui.theme.StreamingAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(mainViewModel: MainViewModel = viewModel()) {
    val mainUiState by mainViewModel.mainUiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Log.d("MainActivity","MainScreen recompose triggered. Current screen: ${mainUiState.currentScreen}")

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

        when (mainUiState.currentScreen) {
            MainScreens.Home -> {
                if (mainUiState.movieList.isEmpty()) {
                    ShowError(onClick = mainViewModel::getMovieList)
                } else {
                    LazyCardList(
                        innerPadding = innerPadding,
                        mainUiState = mainUiState,
                        dialogState = mainViewModel.dialogState.value,
                        selectedMovieId = mainViewModel.selectedMovieId.value,
                        onClickCard = mainViewModel::toggleCardExpansion,
                        hideDialog = mainViewModel::hideDialog,
                        showDialog = mainViewModel::showDialog,
                        fetchThumbnail = mainViewModel::fetchThumbnail
                    )
                }
            }
            MainScreens.Settings ->{
                SettingsScreen(
                    signOut = mainViewModel::signOut
                )
            }
            else -> {}
        }
    }
}


@Composable
fun LazyCardList(
    innerPadding: PaddingValues,
    mainUiState: MainUiState,
    dialogState: Boolean,
    selectedMovieId: Int?,
    onClickCard: (Int) -> Unit,
    hideDialog: () -> Unit,
    showDialog: (Int) -> Unit,
    fetchThumbnail: (String, (String?) -> Unit) -> Unit,
){

    ConfirmationDialog(//confirmation dialog for deleting users/movies. Opens when isVisible=true.
        selectedItem =
        mainUiState.movieList.find { it.id == selectedMovieId }?.title,
        isVisible = dialogState,
        onDismissRequest = hideDialog,
        onAcceptRequest = {  /*confirmDelete(deleteItem)*/ }
    )

    LazyColumn(modifier = Modifier
        .padding(innerPadding)
        .fillMaxSize()) {
        val movies = mainUiState.movieList
        items(
            items = movies,
            key = {
                item -> item.id
            }
        ) { item ->
            MovieCard(
                movie = item,
                isExpanded = mainUiState.expandedCardId == item.id,
                onClickCard = {onClickCard(item.id)},
                onShowDialog = showDialog,
                fetchThumbnail = fetchThumbnail
            )
        }
    }
}

@Composable
fun MovieCard(
    movie: Movie,
    isExpanded: Boolean = false,
    onClickCard: (Int) -> Unit,             // expand card
    onShowDialog: (Int) -> Unit,            //on delete icon, show confirmation dialog
    fetchThumbnail: (String, (String?) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth(), onClick = {onClickCard(movie.id)}) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
                .fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically){
                //use two rows to align the last icon to the right, suggested by chat.
                Column{
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.titleLarge,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2,
                        modifier = Modifier.padding(20.dp).width(275.dp)
                    )
                }
                Column {
                    MovieThumbnail(movie.title, fetchThumbnail)
                }
            }
            Column {
                IconButton(onClick = {onShowDialog(movie.id)}) {
                    Text(text = "Stream movie")
                    Icon(
                        Icons.Rounded.PlayArrow,
                        contentDescription = "delete movie"
                    )
                }
            }
        }
        if(isExpanded) Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
                .fillMaxWidth()
                .padding(6.dp, 0.dp, 6.dp, 6.dp)
        ){
            Column(Modifier.weight(2f)) {
                Text(
                    text = "Release year: ${movie.releaseYear}",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Duration: ${movie.duration} minutes",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Description: ${movie.description}",
                    style = MaterialTheme.typography.bodyMedium,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 3,
                )

            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Genre(s): ${movie.genres.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium,
                )
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
    fetchThumbnail: (String, (String?) -> Unit) -> Unit
) {
    //val thumbnailUrl by viewModel.thumbnailUrl.collectAsState()
    val thumbnailUrl = remember {  mutableStateOf<String?>(null) } //if true, shows user added confirmation.

    LaunchedEffect(movieTitle) {
        fetchThumbnail(movieTitle) { thumbnailUrl.value = it }
    }

    Image(
        painter = rememberAsyncImagePainter(thumbnailUrl.value),
        contentDescription = "Movie Thumbnail",
        modifier = Modifier.size(150.dp)
    )
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