package com.pdm.streamingapp.ui.main

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pdm.streamingapp.model.Movie
import com.pdm.streamingapp.network.StreamingAppAPI
import com.pdm.streamingapp.network.TMDBApiService
import com.pdm.streamingapp.network.tmdbApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException


enum class MainScreens(val title : String){
    Home("Home"),
    Search("Search movies"),
    Settings("Settings")
}

data class MainUiState(
    val currentScreen : MainScreens = MainScreens.Home,
    val expandedCardId : Int = -1,  //id for the selected movie or user card
    val movieList : List<Movie> = listOf()
)

class MainViewModel : ViewModel() {

    init {
        getMovieList()
    }

    private val _mainUiState = MutableStateFlow(MainUiState())
    val mainUiState: StateFlow<MainUiState> = _mainUiState.asStateFlow()

    private val _dialogState = mutableStateOf(false)    //confirmation dialog
    val dialogState: State<Boolean> = _dialogState

    private val _selectedId = mutableStateOf<Int?>(null)        //selected id for delete
    val selectedMovieId: State<Int?> = _selectedId

    fun setCurrentScreen(screen: MainScreens){
        _mainUiState.update {
                currentState -> currentState.copy(currentScreen = screen)
        }
    }

    fun toggleCardExpansion(cardId: Int) {
        _mainUiState.update {
                currentState -> currentState.copy(expandedCardId = cardId)
        }
    }

    fun showDialog(id: Int) {
        _selectedId.value = id
        _dialogState.value = true
    }

    fun hideDialog() {
        _selectedId.value = null
        _dialogState.value = false
    }

    fun confirmDelete(onDelete: (Int) -> Unit) {
        _selectedId.value?.let { id ->
            onDelete(id)
        }
        hideDialog()
    }

    fun getMovieList() {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { StreamingAppAPI.retrofitService.getMovies() }
            }.onSuccess { movieList ->
                _mainUiState.update { currentState ->
                    currentState.copy(movieList = movieList)
                }
            }.onFailure { exception ->
                handleExceptions(exception)
            }
        }
    }

    fun fetchThumbnail(movieTitle: String, onSuccess: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = tmdbApi.searchMovies(movieTitle)
                val posterPath = response.results.firstOrNull()?.posterPath
                onSuccess(posterPath?.let { "https://image.tmdb.org/t/p/w500$it"})
            } catch (e: Exception) {
                handleExceptions(e)
            }
        }
    }

    fun signOut(){
        viewModelScope.launch {
            try {
                val auth = FirebaseAuth.getInstance()
                auth.signOut()
            } catch (e: Exception) {
                handleExceptions(e)
            }
        }
    }
}

fun handleExceptions(exception: Throwable){
    when (exception) {
        is IOException -> Log.e("Network", "Network error: ${exception.message}")
        is HttpException -> Log.e("Network", "HTTP error: ${exception.message}")
        else -> Log.e("Network", "Unexpected error: ${exception.message}")
    }
}
